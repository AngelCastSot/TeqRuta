package com.example.sqlite.ui

import android.graphics.Bitmap
import com.example.sqlite.data.Destino

// Un campo de texto con su validación ya resuelta por el ViewModel.
// La pantalla solo lee estos valores: no valida nada.
data class Campo(
    val valor: String = "",
    val error: String? = null,     // mensaje de error (null = sin error)
    val valido: Boolean = false    // true cuando el valor ya pasó la validación
)

// Estado del formulario de Login
data class LoginFormState(
    val correo: Campo = Campo(),
    val clave: Campo = Campo()
)

// Resultado del cálculo automático que se muestra en el formulario
data class ResumenCalculo(
    val kmRecorridos: Int,             // D_decl = kmFinal - kmInicial
    val variacionPorcentaje: Double,   // |D_decl - D_ofic| / D_ofic * 100
    val peajesTotal: Int,
    val costoTotal: Int,
    val hayDesviacion: Boolean,
    val estado: String
)

// Estado del formulario de Registro de Traslado
data class TrasladoFormState(
    val tecnico: String = "",
    val origen: Campo = Campo("Sede Concepción", null, true),
    val motivo: Campo = Campo(),
    val ciudad: String = "Concepción",
    val destino: Destino? = null,
    val errorDestino: String? = null,
    val kmInicial: Campo = Campo(),
    val kmFinal: Campo = Campo(),
    val peajesSeleccionados: Set<String> = emptySet(),   // peajes marcados con FilterChip
    val peajeExtra: Campo = Campo("0", null, true),
    val foto: Bitmap? = null,                            // miniatura del comprobante de peaje
    val errorFoto: String? = null,
    val salida: Coordenada? = null,                      // GPS al iniciar
    val llegada: Coordenada? = null,                     // GPS al cerrar
    val errorGps: String? = null,
    val resumen: ResumenCalculo? = null                  // null mientras faltan datos
)
