package cl.duoc.huerto.u_i.sectores

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
import cl.duoc.huerto.domain.EstadoSector
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.MensajeError

@Composable
fun SectorFormScreen(sectorId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SectorFormViewModel = viewModel(
        factory = SectorFormViewModel.factory(
            context.applicationContext as Application, sectorId
        )
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            Toast.makeText(context, "Sector guardado correctamente", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                if (estado.esEdicion) "Modificar sector" else "Nuevo sector",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = estado.codigo,
            onValueChange = viewModel::onCodigo,
            label = { Text("Código (ej: S-03)") },
            singleLine = true,
            isError = estado.errores.codigo != null,
            supportingText = { estado.errores.codigo?.let { Text(it) } },
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = estado.nombre,
            onValueChange = viewModel::onNombre,
            label = { Text("Nombre") },
            singleLine = true,
            isError = estado.errores.nombre != null,
            supportingText = { estado.errores.nombre?.let { Text(it) } },
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = { Text("Descripción general") },
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = estado.ubicacion,
            onValueChange = viewModel::onUbicacion,
            label = { Text("Ubicación dentro del huerto") },
            singleLine = true,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text("Estado del sector", style = MaterialTheme.typography.titleSmall)

        EstadoSector.entries.forEach { opcion ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = estado.estado == opcion,
                        onClick = { viewModel.onEstado(opcion) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = estado.estado == opcion, onClick = null)
                Spacer(Modifier.width(12.dp))
                Text(opcion.etiqueta())
            }
        }

        Spacer(Modifier.height(8.dp))
        MensajeError(estado.errorGeneral)

        Spacer(Modifier.height(20.dp))
        BotonAccion(
            texto = "Guardar",
            cargando = estado.cargando,
            onClick = viewModel::guardar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}