package com.example.sqlite.data

// Vehículo que el técnico confirma al inicio de la jornada (catálogo fijo de ejemplo).
data class Vehiculo(
    val patente: String,
    val modelo: String,
    val tipo: String,              // "Empresa" o "Particular"
    val combustible: String,
    val rendimientoKmL: Double
)

object CatalogoVehiculos {
    val lista = listOf(
        Vehiculo("KLPX45", "Toyota Hilux", "Empresa", "Diésel", 11.5),
        Vehiculo("RTVB72", "Nissan NP300", "Empresa", "Diésel", 10.8),
        Vehiculo("BCDF12", "Suzuki Swift (propio)", "Particular", "Gasolina 93", 15.2)
    )
}
