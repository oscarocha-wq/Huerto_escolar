package cl.duoc.huerto.u_i.sectores

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.domain.formatearFechaUtc
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.ui.theme.Tono
import cl.duoc.huerto.ui.theme.coloresTono
import cl.duoc.huerto.ui.theme.tono

@Composable
fun SectorDetalleScreen(
    sectorId: Int,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit,
    onAbrirCultivo: (Int) -> Unit,
    onVerSensores: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: SectorDetalleViewModel = viewModel(
        factory = SectorDetalleViewModel.factory(context.applicationContext as Application, sectorId)
    )
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    // Si el sector se eliminó desde el formulario, volvemos a la lista
    LaunchedEffect(ui.cargando, ui.sector) {
        if (!ui.cargando && ui.sector == null) onVolver()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Detalle del sector", style = MaterialTheme.typography.titleLarge)
        }

        val sector = ui.sector
        if (ui.cargando || sector == null) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }

        // --- Datos del sector ---
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${sector.codigo} · ${sector.nombre}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    ChipEstado(sector.estado.etiqueta(), sector.estado.tono())
                }
                if (sector.ubicacion.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Place, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(sector.ubicacion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (sector.descripcion.isNotBlank()) {
                    Text(sector.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // --- Resumen ---
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Contador("Cultivos", ui.cultivos.size, Tono.EXITO, Modifier.weight(1f))
            Contador("Tareas abiertas", ui.tareasAbiertas, Tono.INFO, Modifier.weight(1f))
            Contador(
                "Problemas", ui.problemasAbiertos,
                if (ui.problemasAbiertos > 0) Tono.ERROR else Tono.NEUTRO,
                Modifier.weight(1f)
            )
        }

        OutlinedButton(onClick = { onEditar(sector.id) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Editar sector")
        }
        OutlinedButton(onClick = { onVerSensores(sector.id) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Sensors, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sensores (datos de prueba)")
        }

        // --- Cultivos del sector ---
        Text("Cultivos de este sector", style = MaterialTheme.typography.titleMedium)
        if (ui.cultivos.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Este sector aún no tiene cultivos. Agrégalos desde la pestaña Cultivos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            ui.cultivos.forEach { c ->
                CultivoFila(c, onClick = { onAbrirCultivo(c.id) })
            }
        }
    }
}

@Composable
private fun Contador(titulo: String, valor: Int, tono: Tono, modifier: Modifier = Modifier) {
    val (fondo, contenido) = coloresTono(tono)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = contenido)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("$valor", style = MaterialTheme.typography.headlineSmall)
            Text(titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CultivoFila(c: Cultivo, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(c.nombre, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                ChipEstado(c.estado.etiqueta(), c.estado.tono())
            }
            Text(
                "Siembra: ${formatearFechaUtc(c.fechaSiembra).ifBlank { "—" }}  ·  " +
                    "Cosecha: ${formatearFechaUtc(c.fechaCosechaEstimada).ifBlank { "—" }}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Riego cada ${c.frecuenciaRiegoDias} día(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
