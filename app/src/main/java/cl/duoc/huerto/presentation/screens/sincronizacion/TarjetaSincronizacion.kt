package cl.duoc.huerto.presentation.screens.sincronizacion

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Tarjeta del Inicio para traer los datos del backend huertoapi.
 * Solo usa colores del tema (colorScheme).
 */
@Composable
fun TarjetaSincronizacion(modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as Application
    val vm: SincronizacionViewModel = viewModel(factory = SincronizacionViewModel.factory(app))
    val ui by vm.ui.collectAsStateWithLifecycle()

    OutlinedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        ui.mensaje == null -> Icons.Filled.CloudSync
                        ui.esError -> Icons.Filled.CloudOff
                        else -> Icons.Filled.CloudDone
                    },
                    contentDescription = null,
                    tint = if (ui.esError) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Servidor del huerto", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Trae sectores, cultivos, tareas y problemas desde huertoapi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ui.mensaje?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (ui.esError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                )
            }

            FilledTonalButton(
                onClick = vm::sincronizar,
                enabled = !ui.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.cargando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizando…")
                } else {
                    Icon(Icons.Filled.CloudSync, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizar con el servidor")
                }
            }
        }
    }
}
