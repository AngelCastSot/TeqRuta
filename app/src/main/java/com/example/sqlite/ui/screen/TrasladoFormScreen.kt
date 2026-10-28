package com.example.sqlite.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sqlite.data.CatalogoDestinos
import com.example.sqlite.ui.Peajes
import com.example.sqlite.ui.TrasladoViewModel
import com.example.sqlite.ui.formatoPesos
import com.example.sqlite.ui.theme.FondoObservado
import com.example.sqlite.ui.theme.FondoPendiente
import com.example.sqlite.ui.theme.TextoSobreEstado
import java.util.Locale

// Texto de error animado (la pantalla solo muestra el mensaje que decidió el ViewModel)
@Composable
private fun MensajeError(mensaje: String?) {
    AnimatedVisibility(visible = mensaje != null) {
        Text(
            mensaje ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun Seccion(titulo: String) {
    Text(
        titulo,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrasladoFormScreen(vm: TrasladoViewModel) {
    val form by vm.form.collectAsState()
    val destinos by vm.destinosDeCiudad.collectAsState()
    val vehiculo by vm.vehiculoConfirmado.collectAsState()
    val context = LocalContext.current

    // ---------- Recurso nativo 1: GPS puntual con permiso en tiempo de ejecución ----------
    var gpsPendiente by remember { mutableStateOf<Boolean?>(null) }   // true = salida, false = llegada
    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        gpsPendiente?.let { vm.capturarUbicacion(it, concedido) }   // si se denegó, usa coordenadas simuladas
        gpsPendiente = null
    }
    fun pedirGps(esSalida: Boolean) {
        val yaTienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (yaTienePermiso) {
            vm.capturarUbicacion(esSalida, true)
        } else {
            gpsPendiente = esSalida
            permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // ---------- Recurso nativo 2: cámara (miniatura con TakePicturePreview) ----------
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        vm.onFotoCapturada(bitmap)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) camara.launch(null) else vm.onPermisoCamaraDenegado()
    }
    fun pedirCamara() {
        val yaTienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (yaTienePermiso) camara.launch(null) else permisoCamara.launch(Manifest.permission.CAMERA)
    }

    // El color del resumen cambia con animación según haya o no desviación
    val hayDesviacion = form.resumen?.hayDesviacion == true
    val colorResumen by animateColorAsState(
        targetValue = if (hayDesviacion) FondoObservado else FondoPendiente,
        label = "colorResumen"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registro de traslado", style = MaterialTheme.typography.titleMedium)
                        BadgeOffline()
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { vm.volverAlListado() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Técnico: ${form.tecnico}", fontWeight = FontWeight.Bold)
            vehiculo?.let { Text("Vehículo: ${it.patente} - ${it.modelo} (${it.tipo})") }

            // ---------- Origen y motivo ----------
            Seccion("Datos del llamado")
            CampoTexto(
                campo = form.origen,
                etiqueta = "Origen",
                icono = Icons.Default.Home,
                alCambiar = vm::onOrigenChange,
                modifier = Modifier.fillMaxWidth()
            )
            CampoTexto(
                campo = form.motivo,
                etiqueta = "Motivo / orden de llamado",
                icono = Icons.Default.Edit,
                alCambiar = vm::onMotivoChange,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Destino ----------
            Seccion("Cliente destino")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogoDestinos.ciudades.forEach { ciudad ->
                    FilterChip(
                        selected = form.ciudad == ciudad,
                        onClick = { vm.onCiudadChange(ciudad) },
                        label = { Text(ciudad) }
                    )
                }
            }
            destinos.forEach { d ->
                FilterChip(
                    selected = form.destino?.id == d.id,
                    onClick = { vm.onDestinoChange(d) },
                    label = { Text("${d.nombre} (${d.kmOficiales} km)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            MensajeError(form.errorDestino)

            // ---------- Odómetro ----------
            Seccion("Odómetro")
            CampoTexto(
                campo = form.kmInicial,
                etiqueta = "Odómetro inicial (km)",
                icono = Icons.Default.Speed,
                alCambiar = vm::onKmInicialChange,
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )
            CampoTexto(
                campo = form.kmFinal,
                etiqueta = "Odómetro final (km)",
                icono = Icons.Default.Speed,
                alCambiar = vm::onKmFinalChange,
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Peajes ----------
            Seccion("Peajes en ruta")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Peajes.lista.forEach { p ->
                    FilterChip(
                        selected = p.nombre in form.peajesSeleccionados,
                        onClick = { vm.onPeajeToggle(p.nombre) },
                        label = { Text("${p.nombre} ${formatoPesos(p.monto)}") }
                    )
                }
            }
            CampoTexto(
                campo = form.peajeExtra,
                etiqueta = "Peaje extra (CLP)",
                icono = Icons.Default.AttachMoney,
                alCambiar = vm::onPeajeExtraChange,
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Foto del comprobante ----------
            Seccion("Foto del comprobante de peaje")
            OutlinedButton(onClick = { pedirCamara() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Text(if (form.foto == null) "  Tomar foto" else "  Volver a tomar foto")
            }
            AnimatedVisibility(visible = form.foto != null) {
                form.foto?.let { foto ->
                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = "Foto del comprobante",
                        modifier = Modifier.padding(top = 8.dp).height(140.dp)
                    )
                }
            }
            MensajeError(form.errorFoto)

            // ---------- GPS ----------
            Seccion("GPS puntual")
            OutlinedButton(onClick = { pedirGps(true) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Text("  Capturar GPS de salida")
            }
            AnimatedVisibility(visible = form.salida != null) { Text(form.salida?.texto ?: "") }
            OutlinedButton(onClick = { pedirGps(false) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Text("  Capturar GPS de llegada")
            }
            AnimatedVisibility(visible = form.llegada != null) { Text(form.llegada?.texto ?: "") }
            MensajeError(form.errorGps)

            // ---------- Resumen de costo y desviación (aparece animado) ----------
            AnimatedVisibility(visible = form.resumen != null) {
                form.resumen?.let { r ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = colorResumen,
                            contentColor = TextoSobreEstado
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).animateContentSize()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Resumen del traslado", fontWeight = FontWeight.Bold)
                            Text("Distancia declarada: ${r.kmRecorridos} km")
                            Text(
                                String.format(Locale.US, "Variación vs distancia oficial: %.1f%%", r.variacionPorcentaje)
                            )
                            Text("Peajes: ${formatoPesos(r.peajesTotal)}")
                            Text(
                                "Costo estimado: ${formatoPesos(r.costoTotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Estado: ${r.estado}")
                            AnimatedVisibility(visible = r.hayDesviacion) {
                                Text(
                                    "ALERTA: la variación supera el 15% permitido",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.guardar() }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar traslado")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
