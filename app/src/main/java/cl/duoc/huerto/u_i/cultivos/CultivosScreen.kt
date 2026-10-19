package cl.duoc.huerto.u_i.cultivos

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.EstadoCultivo
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.domain.formatearFechaUtc
import cl.duoc.huerto.u_i.Cargando
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.u_i.ImagenLocal
import cl.duoc.huerto.u_i.panel.EstadoVacio
import androidx.compose.ui.draw.clip
import cl.duoc.huerto.ui.theme.tono

@Composable
fun CultivosScreen(
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit,
    viewModel: CultivosViewModel = viewModel(
        factory = CultivosViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val cultivos by viewModel.cultivos.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        val lista = cultivos
        when {
            lista == null -> Cargando()
            lista.isEmpty() -> EstadoVacio(
                Icons.Filled.Eco, "Sin cultivos", "Registra el primero con el botón \"Nuevo cultivo\"."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lista, key = { it.cultivo.id }) { item ->
                    Box(Modifier.animateItem()) {
                        CultivoCard(item, onClick = { onEditar(item.cultivo.id) })
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuevo cultivo") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
private fun CultivoCard(item: CultivoItem, onClick: () -> Unit) {
    val c = item.cultivo
    fun fecha(v: Long?) = if (v == null) "—" else formatearFechaUtc(v)

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
      Row(Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        c.fotoUri?.let {
            ImagenLocal(it, Modifier.size(64.dp).clip(MaterialTheme.shapes.small))
        }
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(c.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                EtiquetaEstadoCultivo(c.estado)
            }
            Text(
                "Sector: ${item.sectorNombre}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text("Siembra: ${fecha(c.fechaSiembra)}", style = MaterialTheme.typography.bodySmall)
            Text("Cosecha estimada: ${fecha(c.fechaCosechaEstimada)}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Riego cada ${c.frecuenciaRiegoDias} día(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
      }
    }
}

@Composable
private fun EtiquetaEstadoCultivo(estado: EstadoCultivo) = ChipEstado(estado.etiqueta(), estado.tono())