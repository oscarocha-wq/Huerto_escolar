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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.MAX_DESCRIPCION_PROBLEMA
import cl.duoc.huerto.domain.TipoProblema
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.MensajeError
import cl.duoc.huerto.u_i.SelectorDesplegable
import cl.duoc.huerto.u_i.SelectorFoto

@Composable
fun ProblemaFormScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ProblemaFormViewModel = viewModel(
        factory = ProblemaFormViewModel.factory(context.applicationContext as Application)
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val libre = !estado.cargando

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            Toast.makeText(context, "Problema reportado correctamente", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    val cultivosDelSector = estado.cultivos.filter { it.sectorId == estado.sectorId }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Reportar problema", style = MaterialTheme.typography.titleLarge)
        }

        SelectorDesplegable(
            etiqueta = "Tipo de problema",
            opciones = TipoProblema.entries.toList(),
            seleccion = estado.tipo,
            textoDe = { it.etiqueta() },
            onSeleccion = viewModel::onTipo,
            error = estado.errores.tipo,
            enabled = libre
        )

        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = { Text("¿Qué observaste?") },
            minLines = 3,
            isError = estado.errores.descripcion != null,
            supportingText = {
                Text(
                    estado.errores.descripcion
                        ?: "${estado.descripcion.length}/$MAX_DESCRIPCION_PROBLEMA"
                )
            },
            enabled = libre,
            modifier = Modifier.fillMaxWidth()
        )

        SelectorDesplegable(
            etiqueta = "Sector",
            opciones = estado.sectores,
            seleccion = estado.sectores.firstOrNull { it.id == estado.sectorId },
            textoDe = { "${it.codigo} · ${it.nombre}" },
            onSeleccion = viewModel::onSector,
            error = estado.errores.sector,
            enabled = libre
        )

        SelectorDesplegable(
            etiqueta = "Cultivo (opcional)",
            opciones = cultivosDelSector,
            seleccion = estado.cultivos.firstOrNull { it.id == estado.cultivoId },
            textoDe = { it.nombre },
            onSeleccion = viewModel::onCultivo,
            enabled = libre && estado.sectorId != null
        )
        if (estado.cultivoId != null) {
            TextButton(onClick = { viewModel.onCultivo(null) }) { Text("Quitar cultivo") }
        }

        // Galería + cámara + vista previa
        SelectorFoto(
            fotoRuta = estado.fotoRuta,
            onFoto = viewModel::onFoto,
            onError = viewModel::onErrorFoto,
            onQuitar = viewModel::quitarFoto,
            enabled = libre
        )

        MensajeError(estado.errorGeneral)

        Spacer(Modifier.height(8.dp))

        BotonAccion(
            texto = "Enviar reporte",
            cargando = estado.cargando,
            onClick = viewModel::guardar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}