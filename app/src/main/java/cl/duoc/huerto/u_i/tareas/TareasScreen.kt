package cl.duoc.huerto.u_i.tareas

import android.app.Application
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.domain.formatearFechaUtc

@Composable
fun TareasScreen(
    onNueva: (() -> Unit)?,
    onAbrir: (Int) -> Unit,
    viewModel: TareasViewModel = viewModel(
        factory = TareasViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val tareas by viewModel.tareas.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroTarea.entries.forEach { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { viewModel.onFiltro(f) },
                        label = { Text(f.titulo) }
                    )
                }
            }

            if (tareas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay tareas para mostrar")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tareas, key = { it.tarea.id }) { item ->
                        // animateItem: las tarjetas entran, salen y se reordenan con movimiento
                        Box(Modifier.animateItem()) {
                            TareaCard(
                                item = item,
                                mostrarAsignado = onNueva != null,
                                onClick = { onAbrir(item.tarea.id) }
                            )
                        }
                    }
                }
            }
        }

        if (onNueva != null) {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva tarea") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun TareaCard(item: TareaItem, mostrarAsignado: Boolean, onClick: () -> Unit) {
    val t = item.tarea
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(t.tipo.etiqueta(), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipEstadoTarea(t.estado)
                if (item.atrasada) ChipAtrasada()
            }
            Text("Sector: ${item.sectorNombre}", style = MaterialTheme.typography.bodyMedium)
            item.cultivoNombre?.let {
                Text("Cultivo: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Programada: ${formatearFechaUtc(t.fechaProgramada)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (mostrarAsignado) {
                Text(
                    "Asignada a: ${item.asignadoTexto}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}