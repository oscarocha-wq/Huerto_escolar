package cl.duoc.huerto.u_i.cultivos

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.EstadoCultivo
import cl.duoc.huerto.domain.LimitesCampo
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.BotonEliminar
import cl.duoc.huerto.u_i.SelectorFoto
import cl.duoc.huerto.u_i.CampoFecha
import cl.duoc.huerto.u_i.CampoValidado
import cl.duoc.huerto.u_i.MensajeError
import cl.duoc.huerto.u_i.SelectorDesplegable

@Composable
fun CultivoFormScreen(cultivoId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: CultivoFormViewModel = viewModel(
        factory = CultivoFormViewModel.factory(
            context.applicationContext as Application, cultivoId
        )
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val libre = !estado.cargando

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            Toast.makeText(context, "Cultivo guardado correctamente", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    LaunchedEffect(estado.eliminado) {
        if (estado.eliminado) {
            Toast.makeText(context, "Cultivo eliminado", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

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
            Text(
                if (estado.esEdicion) "Modificar cultivo" else "Nuevo cultivo",
                style = MaterialTheme.typography.titleLarge
            )
        }

        CampoValidado(
            value = estado.nombre,
            onValueChange = viewModel::onNombre,
            label = "Nombre común (ej: Lechuga)",
            icono = Icons.Filled.Eco,
            error = estado.errores.nombre,
            maxLength = LimitesCampo.CULTIVO_NOMBRE,
            enabled = libre
        )
        CampoValidado(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = "Descripción (opcional)",
            icono = Icons.Filled.Description,
            error = estado.errores.descripcion,
            maxLength = LimitesCampo.CULTIVO_DESCRIPCION,
            singleLine = false,
            minLines = 2,
            enabled = libre
        )

        SelectorDesplegable(
            etiqueta = "Sector",
            opciones = estado.sectores,
            seleccion = estado.sectores.firstOrNull { it.id == estado.sectorId },
            textoDe = { "${it.codigo} · ${it.nombre}" },
            onSeleccion = viewModel::onSector,
            error = estado.errores.sector
                ?: if (estado.sectores.isEmpty()) "Primero crea un sector" else null,
            enabled = libre
        )

        CampoFecha("Fecha de siembra", estado.fechaSiembra, viewModel::onFechaSiembra, enabled = libre)
        CampoFecha(
            "Fecha de trasplante (opcional)", estado.fechaTrasplante,
            viewModel::onFechaTrasplante, estado.errores.trasplante, libre
        )
        CampoFecha(
            "Fecha estimada de cosecha", estado.fechaCosecha,
            viewModel::onFechaCosecha, estado.errores.cosecha, libre
        )

        CampoValidado(
            value = estado.frecuencia,
            onValueChange = viewModel::onFrecuencia,
            label = "Riego cada (días)",
            icono = Icons.Filled.WaterDrop,
            error = estado.errores.frecuencia,
            ayuda = "Entre 1 y ${LimitesCampo.RIEGO_MAX_DIAS} días",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = libre
        )

        SelectorDesplegable(
            etiqueta = "Estado de desarrollo",
            opciones = EstadoCultivo.entries.toList(),
            seleccion = estado.estado,
            textoDe = { it.etiqueta() },
            onSeleccion = viewModel::onEstado,
            enabled = libre
        )

        CampoValidado(
            value = estado.observaciones,
            onValueChange = viewModel::onObservaciones,
            label = "Observaciones generales (opcional)",
            icono = Icons.Filled.EditNote,
            error = estado.errores.observaciones,
            maxLength = LimitesCampo.COMENTARIO,
            singleLine = false,
            minLines = 2,
            enabled = libre
        )

        Text("Fotografía del cultivo (opcional)", style = MaterialTheme.typography.titleSmall)
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
            texto = "Guardar",
            cargando = estado.cargando,
            onClick = viewModel::guardar,
            modifier = Modifier.fillMaxWidth()
        )
        if (estado.esEdicion) {
            BotonEliminar(
                texto = "Eliminar cultivo",
                titulo = "¿Eliminar cultivo?",
                mensaje = "Solo se puede eliminar si ninguna tarea lo usa. Esta acción no se puede deshacer.",
                onConfirmar = viewModel::eliminar,
                modifier = Modifier.fillMaxWidth(),
                enabled = libre
            )
        }
    }
}
