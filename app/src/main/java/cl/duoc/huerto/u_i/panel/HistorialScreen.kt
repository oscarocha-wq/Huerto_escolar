package cl.duoc.huerto.u_i.panel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.EventoHistorial
import cl.duoc.huerto.domain.formatearFechaHora

@Composable
fun HistorialScreen(
    eventos: List<EventoHistorial>,
    onAbrirEvento: (EventoHistorial) -> Unit,
    onVolver: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Historial de actividades", style = MaterialTheme.typography.titleLarge)
        }

        if (eventos.isEmpty()) {
            EstadoVacio(Icons.Filled.History, "Sin actividad", "Aquí aparecerán las tareas y problemas.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(eventos, key = { it.clave }) { evento ->
                    val (fondo, contenido) = coloresDe(evento.tipo)
                    Box(Modifier.animateItem()) {
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onAbrirEvento(evento) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconoCircular(iconoDe(evento.tipo), fondo, contenido)
                                Column(Modifier.weight(1f)) {
                                    Text(evento.titulo, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        evento.detalle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        formatearFechaHora(evento.fecha),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}