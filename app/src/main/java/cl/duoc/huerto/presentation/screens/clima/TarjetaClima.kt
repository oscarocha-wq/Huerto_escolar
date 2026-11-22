package cl.duoc.huerto.presentation.screens.clima

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

/** Tarjeta del Inicio con el clima de la API externa Open-Meteo. Solo colores del tema. */
@Composable
fun TarjetaClima(modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as Application
    val vm: ClimaViewModel = viewModel(factory = ClimaViewModel.factory(app))
    val ui by vm.ui.collectAsStateWithLifecycle()

    OutlinedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (ui.error != null) Icons.Filled.CloudOff else Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = if (ui.error != null) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Clima en el huerto", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Fuente: API externa Open-Meteo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = vm::cargar, enabled = !ui.cargando) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar clima")
                }
            }

            val clima = ui.clima
            when {
                ui.cargando -> LinearProgressIndicator(Modifier.fillMaxWidth())
                ui.error != null -> Text(
                    ui.error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                clima != null -> {
                    Text(
                        "${clima.temperatura.roundToInt()} °C · ${ui.descripcion}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        buildString {
                            append("Humedad ${clima.humedad} % · Lluvia hoy ${clima.lluviaHoyMm} mm")
                            if (clima.minima != null && clima.maxima != null) {
                                append(" · Mín ${clima.minima.roundToInt()} / Máx ${clima.maxima.roundToInt()} °C")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(ui.consejo, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
