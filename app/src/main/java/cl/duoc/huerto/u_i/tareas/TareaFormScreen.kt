package cl.duoc.huerto.u_i.tareas

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.LimitesCampo
import cl.duoc.huerto.domain.TipoTarea
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.CampoFecha
import cl.duoc.huerto.u_i.CampoValidado
import cl.duoc.huerto.u_i.MensajeError
import cl.duoc.huerto.u_i.SelectorDesplegable

@Composable
fun TareaFormScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: TareaFormViewModel = viewModel(
        factory = TareaFormViewModel.factory(context.applicationContext as Application)
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val libre = !estado.cargando

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            Toast.makeText(context, "Tarea creada y asignada", Toast.LENGTH_SHORT).show()
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
            Text("Nueva tarea", style = MaterialTheme.typography.titleLarge)
        }

        SelectorDesplegable(
            etiqueta = "Tipo de tarea",
            opciones = TipoTarea.entries.toList(),
            seleccion = estado.tipo,
            textoDe = { it.etiqueta() },
            onSeleccion = viewModel::onTipo,
            error = estado.errores.tipo,
            enabled = libre
        )

        CampoValidado(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = "Instrucciones para el estudiante",
            error = estado.errores.descripcion,
            ayuda = "Explica qué hacer (mín. ${LimitesCampo.TAREA_DESCRIPCION_MIN} caracteres)",
            maxLength = LimitesCampo.TAREA_DESCRIPCION,
            singleLine = false,
            minLines = 3,
            enabled = libre
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

        CampoFecha(
            etiqueta = "Fecha programada",
            valor = estado.fechaProgramada,
            onCambio = viewModel::onFecha,
            error = estado.errores.fecha,
            enabled = libre
        )

        Text("Asignar a", style = MaterialTheme.typography.titleSmall)
        ModoAsignacion.entries.forEach { modo ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = estado.modo == modo,
                        onClick = { viewModel.onModo(modo) },
                        enabled = libre,
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = estado.modo == modo, onClick = null, enabled = libre)
                Spacer(Modifier.width(12.dp))
                Text(if (modo == ModoAsignacion.ESTUDIANTE) "Un estudiante" else "Un grupo")
            }
        }

        if (estado.modo == ModoAsignacion.ESTUDIANTE) {
            SelectorDesplegable(
                etiqueta = "Estudiante",
                opciones = estado.estudiantes,
                seleccion = estado.estudiantes.firstOrNull { it.id == estado.estudianteId },
                textoDe = { "${it.nombre} (${it.id})" },
                onSeleccion = viewModel::onEstudiante,
                error = estado.errores.asignado,
                enabled = libre
            )
        } else {
            SelectorDesplegable(
                etiqueta = "Grupo",
                opciones = estado.grupos,
                seleccion = estado.grupo,
                textoDe = { it },
                onSeleccion = viewModel::onGrupo,
                error = estado.errores.asignado,
                enabled = libre
            )
        }

        MensajeError(estado.errorGeneral)

        Spacer(Modifier.height(8.dp))
        BotonAccion(
            texto = "Crear tarea",
            cargando = estado.cargando,
            onClick = viewModel::guardar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}