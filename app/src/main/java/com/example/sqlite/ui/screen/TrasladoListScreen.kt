package com.example.sqlite.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sqlite.data.Traslado
import com.example.sqlite.data.Vehiculo
import com.example.sqlite.ui.TrasladoViewModel
import com.example.sqlite.ui.formatoPesos
import com.example.sqlite.ui.theme.FondoAprobado
import com.example.sqlite.ui.theme.FondoObservado
import com.example.sqlite.ui.theme.FondoPendiente
import com.example.sqlite.ui.theme.TextoSobreEstado
import java.util.Locale

// Color de fondo de la tarjeta según el estado del traslado
private fun colorDeEstado(estado: String): Color = when (estado) {
    TrasladoViewModel.ESTADO_APROBADO -> FondoAprobado
    TrasladoViewModel.ESTADO_OBSERVADO -> FondoObservado
    else -> FondoPendiente
}

private fun textoDeEstado(estado: String): String = when (estado) {
    TrasladoViewModel.ESTADO_APROBADO -> "APROBADO"
    TrasladoViewModel.ESTADO_OBSERVADO -> "OBSERVADO POR DESVIACIÓN"
    else -> "PENDIENTE DE SINCRONIZAR"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrasladoListScreen(vm: TrasladoViewModel) {
    val traslados by vm.traslados.collectAsState()
    val gastoTotal by vm.gastoTotal.collectAsState()
    val cantidad by vm.cantidadTraslados.collectAsState()
    val seleccionado by vm.vehiculoSeleccionado.collectAsState()
    val confirmado by vm.vehiculoConfirmado.collectAsState()
    val mensajeVehiculo by vm.mensajeVehiculo.collectAsState()

    var paraEliminar by remember { mutableStateOf<Traslado?>(null) }

    // Diálogo de confirmación antes de borrar
    paraEliminar?.let { t ->
        AlertDialog(
            onDismissRequest = { paraEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Eliminar el traslado a '${t.clinicaDestino}'?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminar(t)
                    paraEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { paraEliminar = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TeqRuta - Traslados", style = MaterialTheme.typography.titleMedium)
                        BadgeOffline()
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { vm.cerrarSesion() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        },
        // Navegación inferior: Traslados (esta pantalla) y Registrar (formulario)
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Traslados") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { vm.irARegistro() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Registrar") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ---- Resumen superior ----
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gasto total acumulado", style = MaterialTheme.typography.labelMedium)
                            Text(
                                formatoPesos(gastoTotal) + " CLP",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Traslados guardados", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "$cantidad",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ---- Vehículo del día ----
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).animateContentSize()
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Vehículo del día", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        // Caso 1: ya confirmado
                        AnimatedVisibility(visible = confirmado != null) {
                            confirmado?.let { v ->
                                Column {
                                    Spacer(Modifier.height(4.dp))
                                    DetalleVehiculo(v)
                                    TextButton(onClick = { vm.cambiarVehiculo() }) { Text("Cambiar vehículo") }
                                }
                            }
                        }

                        // Caso 2: falta confirmar
                        AnimatedVisibility(visible = confirmado == null) {
                            Column {
                                Spacer(Modifier.height(4.dp))
                                Text("Confirma el vehículo antes de registrar traslados", style = MaterialTheme.typography.bodySmall)
                                Row(
                                    Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    vm.vehiculos.forEach { v ->
                                        FilterChip(
                                            selected = seleccionado?.patente == v.patente,
                                            onClick = { vm.seleccionarVehiculo(v) },
                                            label = { Text("${v.patente} (${v.tipo})") }
                                        )
                                    }
                                }
                                AnimatedVisibility(visible = seleccionado != null) {
                                    seleccionado?.let { DetalleVehiculo(it) }
                                }
                                Button(onClick = { vm.confirmarVehiculo() }) { Text("Confirmar vehículo") }
                            }
                        }

                        // Mensaje de error (animado)
                        AnimatedVisibility(visible = mensajeVehiculo != null) {
                            Text(mensajeVehiculo ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // ---- Listado de traslados ----
            if (traslados.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No hay traslados. Usa \"Registrar\" para crear uno.")
                    }
                }
            } else {
                items(traslados, key = { it.id }) { t ->
                    TrasladoItem(
                        traslado = t,
                        onAprobar = { vm.aprobar(t) },
                        onEliminar = { paraEliminar = t }
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun DetalleVehiculo(v: Vehiculo) {
    Column {
        Text("${v.patente} - ${v.modelo}", fontWeight = FontWeight.Bold)
        Text("Tipo: ${v.tipo} | Combustible: ${v.combustible}")
        Text("Rendimiento: ${v.rendimientoKmL} km/L | Tarifa: " +
                formatoPesos(TrasladoViewModel.tarifaPorKm(v.tipo)) + "/km")
    }
}

@Composable
private fun TrasladoItem(traslado: Traslado, onAprobar: () -> Unit, onEliminar: () -> Unit) {
    var expandido by remember(traslado.id) { mutableStateOf(false) }
    val kmRecorridos = traslado.kmFinal - traslado.kmInicial

    // El color de fondo cambia con animación cuando cambia el estado (ej. al aprobar)
    val fondo by animateColorAsState(targetValue = colorDeEstado(traslado.estado), label = "colorEstado")

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = fondo, contentColor = TextoSobreEstado),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .animateContentSize()                      // la tarjeta crece suave al expandir
            .clickable { expandido = !expandido }
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(traslado.clinicaDestino, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(textoDeEstado(traslado.estado), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text("Técnico: ${traslado.tecnico} | ${traslado.tipoVehiculo} ${traslado.patente}")
            Text("Recorrido: $kmRecorridos km (oficial: ${traslado.kmOficiales} km)")

            // Alerta de desviación (aparece animada)
            AnimatedVisibility(visible = traslado.tieneDesviacion) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        "ALERTA: desviación de kilometraje sobre 15%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Detalles que se muestran al tocar la tarjeta
            AnimatedVisibility(visible = expandido) {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Origen: ${traslado.origen}")
                    Text("Motivo: ${traslado.motivo}")
                    Text("Odómetro: ${traslado.kmInicial} -> ${traslado.kmFinal}")
                    Text("Peajes: ${formatoPesos(traslado.peajes)}" +
                            if (traslado.peajesRuta.isNotEmpty()) " (${traslado.peajesRuta})" else "")
                    Text(
                        String.format(Locale.US, "GPS salida: %.5f, %.5f", traslado.latSalida, traslado.lonSalida)
                    )
                    Text(
                        String.format(Locale.US, "GPS llegada: %.5f, %.5f", traslado.latLlegada, traslado.lonLlegada) +
                                if (traslado.gpsSimulado) " (simulado)" else ""
                    )
                    Text(if (traslado.tieneFotoRespaldo) "Foto del comprobante: guardada" else "Foto del comprobante: no")
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Costo: ${formatoPesos(traslado.costoTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    if (traslado.estado == TrasladoViewModel.ESTADO_PENDIENTE) {
                        TextButton(onClick = onAprobar) { Text("Aprobar") }
                    }
                    TextButton(onClick = onEliminar) { Text("Eliminar") }
                }
            }
        }
    }
}
