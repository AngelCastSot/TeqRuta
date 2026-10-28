package com.example.sqlite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad Room: clínica u hospital de destino con su distancia oficial (km) desde la sede de Concepción.
@Entity(tableName = "destinos")
data class Destino(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val ciudad: String,
    val kmOficiales: Int
)

// Catálogo inicial que se pre-carga en Room la primera vez que se abre la app
object CatalogoDestinos {
    val ciudades = listOf("Concepción", "Chillán", "Los Ángeles", "Temuco")

    val iniciales = listOf(
        Destino(nombre = "Clínica Biobío Concepción", ciudad = "Concepción", kmOficiales = 12),
        Destino(nombre = "Hospital Regional de Concepción", ciudad = "Concepción", kmOficiales = 8),
        Destino(nombre = "Hospital Herminda Martín Chillán", ciudad = "Chillán", kmOficiales = 102),
        Destino(nombre = "Clínica Chillán", ciudad = "Chillán", kmOficiales = 104),
        Destino(nombre = "Hospital de Los Ángeles", ciudad = "Los Ángeles", kmOficiales = 125),
        Destino(nombre = "Clínica Los Ángeles", ciudad = "Los Ángeles", kmOficiales = 126),
        Destino(nombre = "Hospital Hernán Henríquez Temuco", ciudad = "Temuco", kmOficiales = 285),
        Destino(nombre = "Clínica Alemana de Temuco", ciudad = "Temuco", kmOficiales = 287)
    )
}
