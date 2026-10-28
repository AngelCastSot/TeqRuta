package com.example.sqlite.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import java.util.Locale

// Formatea un entero como pesos chilenos: 12500 -> "$12.500"
fun formatoPesos(valor: Int): String =
    "$" + String.format(Locale.US, "%,d", valor).replace(",", ".")

// ---------- Peajes de la ruta (montos referenciales en CLP) ----------
data class Peaje(val nombre: String, val monto: Int)

object Peajes {
    val lista = listOf(
        Peaje("Agua Amarilla", 1900),
        Peaje("Queime", 2200),
        Peaje("Troncal Sur", 2600)
    )
}

// ---------- GPS puntual ----------
data class Coordenada(
    val latitud: Double,
    val longitud: Double,
    val simulada: Boolean       // true si no hubo GPS/permiso y se usó un valor de respaldo
) {
    val texto: String
        get() = String.format(Locale.US, "Lat %.5f  Lon %.5f", latitud, longitud) +
                if (simulada) " (simulada)" else " (GPS real)"
}

// Intenta leer la última ubicación conocida con LocationManager.
// Si no hay permiso, el GPS está apagado o el emulador no tiene ubicación,
// devuelve coordenadas simuladas de Concepción para que la app NUNCA falle.
@SuppressLint("MissingPermission")
fun obtenerCoordenada(context: Context, permisoConcedido: Boolean, desfase: Double): Coordenada {
    if (permisoConcedido) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val proveedores = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            for (proveedor in proveedores) {
                if (lm.isProviderEnabled(proveedor)) {
                    val ubicacion = lm.getLastKnownLocation(proveedor)
                    if (ubicacion != null) {
                        return Coordenada(ubicacion.latitude, ubicacion.longitude, false)
                    }
                }
            }
        } catch (e: Exception) {
            // Cualquier error (permiso, proveedor, etc.) cae al respaldo simulado
        }
    }
    return Coordenada(-36.8270 + desfase, -73.0498 + desfase, true)
}
