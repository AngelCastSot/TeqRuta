package com.example.sqlite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad Room: cada fila es un traslado en terreno de un técnico de TEQMED SpA.
@Entity(tableName = "traslados")
data class Traslado(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tecnico: String,
    val patente: String,
    val tipoVehiculo: String,          // "Empresa" o "Particular"
    val origen: String,                // ej. "Sede Concepción"
    val clinicaDestino: String,
    val motivo: String,                // motivo u orden de llamado
    val kmInicial: Int,                // odómetro al salir
    val kmFinal: Int,                  // odómetro al llegar
    val kmOficiales: Int,              // distancia oficial del destino (D_ofic)
    val peajesRuta: String,            // nombres de los peajes marcados, ej. "Agua Amarilla, Queime"
    val peajes: Int,                   // monto total de peajes en CLP (marcados + extra)
    val costoTotal: Int,               // costo calculado en CLP
    val estado: String,                // PENDIENTE_SYNC, OBSERVADO_DESVIACION o APROBADO
    val tieneDesviacion: Boolean,      // true si la variación contra D_ofic supera el 15%
    val tieneFotoRespaldo: Boolean,    // true si se tomó la foto del comprobante
    val fotoPath: String,              // ruta del archivo JPG guardado en el celular ("" si no hay)
    val latSalida: Double,             // GPS puntual al iniciar el traslado
    val lonSalida: Double,
    val latLlegada: Double,            // GPS puntual al cerrar el traslado
    val lonLlegada: Double,
    val gpsSimulado: Boolean           // true si se usaron coordenadas simuladas (sin GPS/permiso)
)
