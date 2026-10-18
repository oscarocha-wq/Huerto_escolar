package cl.duoc.huerto.u_i.sectores

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.domain.EstadoSector
import cl.duoc.huerto.domain.etiqueta
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
        if (sectores.isEmpty()) {
            Text(
                "Aún no hay sectores. Crea el primero.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sectores, key = { it.id }) { sector ->
                    SectorCard(sector, onClick = { onEditar(sector.id) })
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
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
        .format(Date(sector.fechaActualizacion))

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
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
                EtiquetaEstado(sector.estado)
            }
            if (sector.descripcion.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(sector.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
            if (sector.ubicacion.isNotBlank()) {
                Text(
                    "Ubicación: ${sector.ubicacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Actualizado: $fecha",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EtiquetaEstado(estado: EstadoSector) {
    val color = when (estado) {
        EstadoSector.DISPONIBLE -> MaterialTheme.colorScheme.secondaryContainer
        EstadoSector.EN_USO -> MaterialTheme.colorScheme.primaryContainer
        EstadoSector.EN_MANTENIMIENTO -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoSector.FUERA_DE_USO -> MaterialTheme.colorScheme.errorContainer
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            estado.etiqueta(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}