package cl.duoc.huerto.u_i.sectores

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.Cargando
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.u_i.panel.EstadoVacio
import cl.duoc.huerto.ui.theme.tono
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SectoresScreen(
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit,
    viewModel: SectoresViewModel = viewModel(
        factory = SectoresViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val sectores by viewModel.sectores.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        val lista = sectores
        when {
            lista == null -> Cargando()
            lista.isEmpty() -> EstadoVacio(
                Icons.Filled.Grass, "Sin sectores", "Crea el primero con el botón \"Nuevo sector\"."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lista, key = { it.id }) { sector ->
                    Box(Modifier.animateItem()) {
                        SectorCard(sector, onClick = { onEditar(sector.id) })
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuevo sector") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
private fun SectorCard(sector: Sector, onClick: () -> Unit) {
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL"))
        .format(Date(sector.fechaActualizacion))

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${sector.codigo} · ${sector.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                ChipEstado(sector.estado.etiqueta(), sector.estado.tono())
            }
            if (sector.descripcion.isNotBlank()) {
                Text(sector.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
            if (sector.ubicacion.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Place, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        sector.ubicacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "Actualizado: $fecha",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
