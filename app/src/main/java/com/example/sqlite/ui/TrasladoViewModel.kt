package com.example.sqlite.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sqlite.data.CatalogoVehiculos
import com.example.sqlite.data.Destino
import com.example.sqlite.data.Traslado
import com.example.sqlite.data.TrasladoRepository
import com.example.sqlite.data.Vehiculo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

// Las 3 vistas de la app
enum class Pantalla { LOGIN, LISTADO, FORMULARIO }

// TODA la validación y la lógica de negocio está en este ViewModel.
// Las pantallas (@Composable) solo muestran el estado y avisan los eventos.
class TrasladoViewModel(
    app: Application,
    private val repo: TrasladoRepository
) : AndroidViewModel(app) {

    companion object {
        // Tarifas fijas de TEQMED SpA (pesos por km)
        const val TARIFA_EMPRESA = 120
        const val TARIFA_PARTICULAR = 180
        // Si la variación contra la distancia oficial supera 15%, hay desviación
        const val UMBRAL_DESVIACION = 0.15

        // Estados posibles de un traslado
        const val ESTADO_PENDIENTE = "PENDIENTE_SYNC"
        const val ESTADO_OBSERVADO = "OBSERVADO_DESVIACION"
        const val ESTADO_APROBADO = "APROBADO"

        private const val DOMINIO_CORREO = "@teqmed.cl"

        fun tarifaPorKm(tipoVehiculo: String): Int =
            if (tipoVehiculo == "Empresa") TARIFA_EMPRESA else TARIFA_PARTICULAR

        // costo = (kmFinal - kmInicial) * tarifa + peajes
        fun calcularCosto(kmRecorridos: Int, tipoVehiculo: String, peajes: Int): Int =
            kmRecorridos * tarifaPorKm(tipoVehiculo) + peajes

        // |D_decl - D_ofic| / D_ofic > 0.15
        fun hayDesviacion(kmRecorridos: Int, kmOficiales: Int): Boolean =
            kmOficiales > 0 && abs(kmRecorridos - kmOficiales).toDouble() / kmOficiales > UMBRAL_DESVIACION
    }

    init {
        // Pre-carga los destinos reales la primera vez que se abre la app
        viewModelScope.launch { repo.precargarDestinos() }
    }

    // ================= Navegación =================
    private val _pantalla = MutableStateFlow(Pantalla.LOGIN)
    val pantalla: StateFlow<Pantalla> = _pantalla.asStateFlow()

    fun volverAlListado() { _pantalla.value = Pantalla.LISTADO }

    // ================= Login =================
    private val _login = MutableStateFlow(LoginFormState())
    val login: StateFlow<LoginFormState> = _login.asStateFlow()

    private var tecnicoActual = ""

    private fun validarCorreo(v: String): Campo {
        val texto = v.trim()
        val error = if (!texto.endsWith(DOMINIO_CORREO) || texto.length <= DOMINIO_CORREO.length)
            "Usa tu correo corporativo (nombre$DOMINIO_CORREO)" else null
        return Campo(v, error, error == null)
    }

    private fun validarClave(v: String): Campo {
        val error = if (v.length < 6) "La contraseña debe tener al menos 6 caracteres" else null
        return Campo(v, error, error == null)
    }

    fun onCorreoChange(v: String) { _login.update { it.copy(correo = validarCorreo(v)) } }
    fun onClaveChange(v: String) { _login.update { it.copy(clave = validarClave(v)) } }

    fun iniciarSesion() {
        // Al presionar "Ingresar" se validan los dos campos (aunque estén vacíos)
        _login.update {
            it.copy(correo = validarCorreo(it.correo.valor), clave = validarClave(it.clave.valor))
        }
        val f = _login.value
        if (f.correo.valido && f.clave.valido) {
            tecnicoActual = f.correo.valor.trim().substringBefore("@")
            _pantalla.value = Pantalla.LISTADO
        }
    }

    fun cerrarSesion() {
        _login.value = LoginFormState()
        _vehiculoSeleccionado.value = null
        _vehiculoConfirmado.value = null
        _mensajeVehiculo.value = null
        tecnicoActual = ""
        _pantalla.value = Pantalla.LOGIN
    }

    // ================= Vehículo del día =================
    val vehiculos: List<Vehiculo> = CatalogoVehiculos.lista

    private val _vehiculoSeleccionado = MutableStateFlow<Vehiculo?>(null)
    val vehiculoSeleccionado: StateFlow<Vehiculo?> = _vehiculoSeleccionado.asStateFlow()

    private val _vehiculoConfirmado = MutableStateFlow<Vehiculo?>(null)
    val vehiculoConfirmado: StateFlow<Vehiculo?> = _vehiculoConfirmado.asStateFlow()

    private val _mensajeVehiculo = MutableStateFlow<String?>(null)
    val mensajeVehiculo: StateFlow<String?> = _mensajeVehiculo.asStateFlow()

    fun seleccionarVehiculo(v: Vehiculo) {
        _vehiculoSeleccionado.value = v
        _mensajeVehiculo.value = null
    }

    fun confirmarVehiculo() {
        val v = _vehiculoSeleccionado.value
        if (v == null) {
            _mensajeVehiculo.value = "Selecciona el vehículo que usarás hoy"
        } else {
            _vehiculoConfirmado.value = v
            _mensajeVehiculo.value = null
        }
    }

    fun cambiarVehiculo() { _vehiculoConfirmado.value = null }

    // ================= Listado y resumen =================
    val traslados: StateFlow<List<Traslado>> =
        repo.traslados.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Gasto total acumulado (suma de todos los costos)
    val gastoTotal: StateFlow<Int> =
        traslados.map { lista -> lista.sumOf { it.costoTotal } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // Cantidad de traslados guardados localmente
    val cantidadTraslados: StateFlow<Int> =
        traslados.map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun eliminar(traslado: Traslado) = viewModelScope.launch { repo.eliminar(traslado) }

    // El jefe de servicio aprueba un traslado pendiente
    fun aprobar(traslado: Traslado) = viewModelScope.launch {
        repo.actualizar(traslado.copy(estado = ESTADO_APROBADO))
    }

    // ================= Formulario de registro =================
    private val _form = MutableStateFlow(TrasladoFormState())
    val form: StateFlow<TrasladoFormState> = _form.asStateFlow()

    // Destinos del catálogo (Room) filtrados por la ciudad elegida en el formulario
    val destinosDeCiudad: StateFlow<List<Destino>> =
        combine(repo.destinos, _form.map { it.ciudad }.distinctUntilChanged()) { lista, ciudad ->
            lista.filter { it.ciudad == ciudad }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Botón "Registrar" de la barra inferior: exige tener el vehículo confirmado
    fun irARegistro() {
        if (_vehiculoConfirmado.value == null) {
            _mensajeVehiculo.value = "Confirma el vehículo del día antes de registrar un traslado"
        } else {
            _form.value = TrasladoFormState(tecnico = tecnicoActual)
            _pantalla.value = Pantalla.FORMULARIO
        }
    }

    // Aplica un cambio al formulario y recalcula costo/desviación
    private fun actualizar(cambio: (TrasladoFormState) -> TrasladoFormState) {
        _form.update { recalcular(cambio(it)) }
    }

    // ---- Validaciones de cada campo ----
    private fun validarOrigen(v: String): Campo {
        val error = if (v.isBlank()) "El origen es obligatorio" else null
        return Campo(v, error, error == null)
    }

    private fun validarMotivo(v: String): Campo {
        val error = if (v.isBlank()) "Ingresa el motivo u orden de llamado" else null
        return Campo(v, error, error == null)
    }

    private fun validarKmInicial(v: String): Campo {
        val n = v.toIntOrNull()
        val error = when {
            n == null -> "Ingresa un número entero"
            n <= 0 -> "El odómetro inicial debe ser mayor a 0"
            else -> null
        }
        return Campo(v, error, error == null)
    }

    private fun validarKmFinal(v: String, kmInicial: String): Campo {
        val fin = v.toIntOrNull()
        val ini = kmInicial.toIntOrNull()
        val error = when {
            fin == null -> "Ingresa un número entero"
            fin <= 0 -> "El odómetro final debe ser mayor a 0"
            ini != null && ini > 0 && fin <= ini -> "El odómetro final debe ser mayor al inicial"
            else -> null
        }
        return Campo(v, error, error == null)
    }

    private fun validarPeajeExtra(v: String): Campo {
        val n = v.toIntOrNull()
        val error = when {
            n == null -> "Ingresa un monto válido (0 si no hubo)"
            n < 0 -> "El monto no puede ser negativo"
            else -> null
        }
        return Campo(v, error, error == null)
    }

    // Total de peajes = peajes marcados + peaje extra (si es válido)
    private fun peajesTotal(f: TrasladoFormState): Int {
        val marcados = Peajes.lista.filter { it.nombre in f.peajesSeleccionados }.sumOf { it.monto }
        val extra = f.peajeExtra.valor.toIntOrNull()?.takeIf { it >= 0 } ?: 0
        return marcados + extra
    }

    // Calcula costo, variación y estado cuando ya hay datos suficientes
    private fun recalcular(f: TrasladoFormState): TrasladoFormState {
        val destino = f.destino
        val vehiculo = _vehiculoConfirmado.value
        val ini = f.kmInicial.valor.toIntOrNull()
        val fin = f.kmFinal.valor.toIntOrNull()
        if (destino == null || vehiculo == null || ini == null || fin == null ||
            !f.kmInicial.valido || !f.kmFinal.valido
        ) {
            return f.copy(resumen = null)
        }
        val km = fin - ini                                        // D_decl
        val peajes = peajesTotal(f)
        val variacion = abs(km - destino.kmOficiales).toDouble() / destino.kmOficiales * 100
        val desviacion = hayDesviacion(km, destino.kmOficiales)
        return f.copy(
            resumen = ResumenCalculo(
                kmRecorridos = km,
                variacionPorcentaje = variacion,
                peajesTotal = peajes,
                costoTotal = calcularCosto(km, vehiculo.tipo, peajes),
                hayDesviacion = desviacion,
                estado = if (desviacion) ESTADO_OBSERVADO else ESTADO_PENDIENTE
            )
        )
    }

    // ---- Eventos del formulario ----
    fun onOrigenChange(v: String) { actualizar { it.copy(origen = validarOrigen(v)) } }
    fun onMotivoChange(v: String) { actualizar { it.copy(motivo = validarMotivo(v)) } }

    fun onKmInicialChange(v: String) {
        actualizar {
            it.copy(
                kmInicial = validarKmInicial(v),
                // si el final ya tenía texto, se vuelve a validar contra el nuevo inicial
                kmFinal = if (it.kmFinal.valor.isEmpty()) it.kmFinal else validarKmFinal(it.kmFinal.valor, v)
            )
        }
    }

    fun onKmFinalChange(v: String) {
        actualizar { it.copy(kmFinal = validarKmFinal(v, it.kmInicial.valor)) }
    }

    fun onPeajeExtraChange(v: String) { actualizar { it.copy(peajeExtra = validarPeajeExtra(v)) } }

    // Al cambiar de ciudad se limpia el destino elegido
    fun onCiudadChange(ciudad: String) {
        actualizar { it.copy(ciudad = ciudad, destino = null) }
    }

    fun onDestinoChange(d: Destino) {
        actualizar { it.copy(destino = d, errorDestino = null) }
    }

    // Marca o desmarca un peaje (FilterChip)
    fun onPeajeToggle(nombre: String) {
        actualizar {
            val nuevos = if (nombre in it.peajesSeleccionados) it.peajesSeleccionados - nombre
            else it.peajesSeleccionados + nombre
            it.copy(peajesSeleccionados = nuevos)
        }
    }

    // ---- Recurso nativo 2: cámara ----
    // Si el usuario cancela la cámara llega null y se conserva la foto anterior
    fun onFotoCapturada(bitmap: Bitmap?) {
        if (bitmap != null) actualizar { it.copy(foto = bitmap, errorFoto = null) }
    }

    fun onPermisoCamaraDenegado() {
        actualizar { it.copy(errorFoto = "Permiso de cámara denegado. Actívalo en Ajustes para tomar la foto") }
    }

    // ---- Recurso nativo 1: GPS puntual ----
    // Si no hay permiso o GPS, obtenerCoordenada() devuelve coordenadas simuladas (nunca falla)
    fun capturarUbicacion(esSalida: Boolean, permisoConcedido: Boolean) {
        val coordenada = obtenerCoordenada(getApplication(), permisoConcedido, if (esSalida) 0.0 else 0.01)
        actualizar {
            if (esSalida) it.copy(salida = coordenada, errorGps = null)
            else it.copy(llegada = coordenada, errorGps = null)
        }
    }

    // ---- Guardar ----
    fun guardar() {
        val vehiculo = _vehiculoConfirmado.value ?: return

        // 1) Validar TODOS los campos y mostrar los errores en pantalla
        actualizar { f ->
            f.copy(
                origen = validarOrigen(f.origen.valor),
                motivo = validarMotivo(f.motivo.valor),
                kmInicial = validarKmInicial(f.kmInicial.valor),
                kmFinal = validarKmFinal(f.kmFinal.valor, f.kmInicial.valor),
                peajeExtra = validarPeajeExtra(f.peajeExtra.valor),
                errorDestino = if (f.destino == null) "Selecciona la clínica u hospital de destino" else null,
                // la foto del comprobante es obligatoria solo si hubo peajes
                errorFoto = if (peajesTotal(f) > 0 && f.foto == null)
                    "Toma la foto del comprobante de peaje" else null,
                errorGps = if (f.salida == null || f.llegada == null)
                    "Captura el GPS de salida y el de llegada" else null
            )
        }
        val f = _form.value
        val hayErrores = !f.origen.valido || !f.motivo.valido || !f.kmInicial.valido ||
                !f.kmFinal.valido || !f.peajeExtra.valido || f.errorDestino != null ||
                f.errorFoto != null || f.errorGps != null
        if (hayErrores) return

        // 2) Todo válido: se arma el traslado (aquí ya se puede usar !!)
        val resumen = f.resumen ?: return
        val destino = f.destino!!
        val salida = f.salida!!
        val llegada = f.llegada!!
        val nombresPeajes = Peajes.lista.filter { it.nombre in f.peajesSeleccionados }
            .joinToString(", ") { it.nombre }

        viewModelScope.launch {
            val rutaFoto = f.foto?.let { guardarFoto(it) } ?: ""
            repo.agregar(
                Traslado(
                    tecnico = f.tecnico,
                    patente = vehiculo.patente,
                    tipoVehiculo = vehiculo.tipo,
                    origen = f.origen.valor.trim(),
                    clinicaDestino = destino.nombre,
                    motivo = f.motivo.valor.trim(),
                    kmInicial = f.kmInicial.valor.toInt(),
                    kmFinal = f.kmFinal.valor.toInt(),
                    kmOficiales = destino.kmOficiales,
                    peajesRuta = nombresPeajes,
                    peajes = resumen.peajesTotal,
                    costoTotal = resumen.costoTotal,
                    estado = resumen.estado,
                    tieneDesviacion = resumen.hayDesviacion,
                    tieneFotoRespaldo = rutaFoto.isNotEmpty(),
                    fotoPath = rutaFoto,
                    latSalida = salida.latitud,
                    lonSalida = salida.longitud,
                    latLlegada = llegada.latitud,
                    lonLlegada = llegada.longitud,
                    gpsSimulado = salida.simulada || llegada.simulada
                )
            )
            _pantalla.value = Pantalla.LISTADO
        }
    }

    // Guarda la foto como JPG en la carpeta privada de la app y devuelve su ruta
    private suspend fun guardarFoto(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val archivo = File(getApplication<Application>().filesDir, "peaje_${System.currentTimeMillis()}.jpg")
            FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            archivo.absolutePath
        } catch (e: Exception) {
            ""   // si falla el guardado, el traslado igual se registra
        }
    }
}
