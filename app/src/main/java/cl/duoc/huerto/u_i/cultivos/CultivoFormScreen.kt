package cl.duoc.huerto.u_i.cultivos

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.EstadoCultivo
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.CampoFecha
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

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = viewModel::onNombre,
            label = { Text("Nombre común (ej: Lechuga)") },
            singleLine = true,
            isError = estado.errores.nombre != null,
            supportingText = { estado.errores.nombre?.let { Text(it) } },
            enabled = libre,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = { Text("Descripción") },
            enabled = libre,
            modifier = Modifier.fillMaxWidth()
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

        OutlinedTextField(
            value = estado.frecuencia,
            onValueChange = viewModel::onFrecuencia,
            label = { Text("Riego cada (días)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = estado.errores.frecuencia != null,
            supportingText = { estado.errores.frecuencia?.let { Text(it) } },
            enabled = libre,
            modifier = Modifier.fillMaxWidth()
        )

        SelectorDesplegable(
            etiqueta = "Estado de desarrollo",
            opciones = EstadoCultivo.entries.toList(),
            seleccion = estado.estado,
            textoDe = { it.etiqueta() },
            onSeleccion = viewModel::onEstado,
            enabled = libre
        )

        OutlinedTextField(
            value = estado.observaciones,
            onValueChange = viewModel::onObservaciones,
            label = { Text("Observaciones generales") },
            enabled = libre,
            modifier = Modifier.fillMaxWidth()
        )

        MensajeError(estado.errorGeneral)

        Spacer(Modifier.height(8.dp))
        BotonAccion(
            texto = "Guardar",
            cargando = estado.cargando,
            onClick = viewModel::guardar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}