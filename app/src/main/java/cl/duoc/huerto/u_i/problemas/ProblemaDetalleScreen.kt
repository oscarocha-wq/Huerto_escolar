package cl.duoc.huerto.u_i.problemas

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.domain.*
import cl.duoc.huerto.u_i.AccesoDenegado
import cl.duoc.huerto.u_i.ImagenLocal

@Composable
fun ProblemaDetalleScreen(problemaId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ProblemaDetalleViewModel = viewModel(
        factory = ProblemaDetalleViewModel.factory(context.applicationContext as Application, problemaId)
    )
    val item by viewModel.item.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val sesion by SesionManager.sesion.collectAsStateWithLifecycle()

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.mensajeMostrado()
        }
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
            Text("Detalle del problema", style = MaterialTheme.typography.titleLarge)
        }

        val actual = item
        if (actual == null) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }
        val p = actual.problema
        if (!puedeVerProblema(sesion, p.reportadoPorId)) {
            AccesoDenegado()
            return@Column
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ChipEstadoProblema(p.estado)
                Dato("Tipo", p.tipo.etiqueta())
                Dato("Descripción", p.descripcion)
                Dato("Sector", actual.sectorNombre)
                actual.cultivoNombre?.let { Dato("Cultivo", it) }
                Dato("Reportado el", formatearFechaHora(p.fechaReporte))
                if (sesion?.esAdministrador == true) Dato("Reportado por", actual.reportadoPorTexto)
                p.fotoRuta?.let {
                    ImagenLocal(
                        it,
                        Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        }

        if (p.comentarioDocente != null || p.fechaAtencion != null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Seguimiento", style = MaterialTheme.typography.titleMedium)
                    p.comentarioDocente?.let { Dato("Comentario del docente", it) }
                    p.fechaAtencion?.let { Dato("Atendido el", formatearFechaHora(it)) }
                }
            }
        }

        // Solo el administrador ve estas acciones
        val siguientes = siguientesEstados(p.estado)
        if (puedeGestionarProblema(sesion) && siguientes.isNotEmpty()) {
            OutlinedTextField(
                value = ui.comentario,
                onValueChange = viewModel::onComentario,
                label = { Text("Comentario de seguimiento") },
                modifier = Modifier.fillMaxWidth()
            )
            siguientes.forEach { nuevo ->
                Button(
                    onClick = { viewModel.cambiarEstado(nuevo) },
                    enabled = !ui.procesando,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text("Pasar a: ${nuevo.etiqueta()}") }
            }
        }

        ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun Dato(titulo: String, valor: String) {
    Column {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}