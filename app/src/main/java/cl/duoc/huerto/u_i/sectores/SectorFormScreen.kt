package cl.duoc.huerto.u_i.sectores

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tag
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
import cl.duoc.huerto.domain.LimitesCampo
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.BotonEliminar
import cl.duoc.huerto.u_i.CampoValidado
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.u_i.MensajeError
import cl.duoc.huerto.ui.theme.tono

@Composable
fun SectorFormScreen(sectorId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SectorFormViewModel = viewModel(
        factory = SectorFormViewModel.factory(
            context.applicationContext as Application, sectorId
        )
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val libre = !estado.cargando

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            Toast.makeText(context, "Sector guardado correctamente", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }
    LaunchedEffect(estado.eliminado) {
        if (estado.eliminado) {
            Toast.makeText(context, "Sector eliminado", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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

        Spacer(Modifier.height(8.dp))

        CampoValidado(
            value = estado.codigo,
            onValueChange = viewModel::onCodigo,
            label = "Código",
            icono = Icons.Filled.Tag,
            error = estado.errores.codigo,
            ayuda = "Formato: S-03",
            maxLength = LimitesCampo.SECTOR_CODIGO,
            enabled = libre
        )
        CampoValidado(
            value = estado.nombre,
            onValueChange = viewModel::onNombre,
            label = "Nombre",
            icono = Icons.Filled.Grass,
            error = estado.errores.nombre,
            maxLength = LimitesCampo.SECTOR_NOMBRE,
            enabled = libre
        )
        CampoValidado(
            value = estado.ubicacion,
            onValueChange = viewModel::onUbicacion,
            label = "Ubicación dentro del huerto",
            icono = Icons.Filled.Place,
            error = estado.errores.ubicacion,
            maxLength = LimitesCampo.SECTOR_UBICACION,
            enabled = libre
        )
        CampoValidado(
            value = estado.descripcion,
            onValueChange = viewModel::onDescripcion,
            label = "Descripción general (opcional)",
            icono = Icons.Filled.Description,
            error = estado.errores.descripcion,
            maxLength = LimitesCampo.SECTOR_DESCRIPCION,
            singleLine = false,
            minLines = 2,
            enabled = libre
        )

        Spacer(Modifier.height(8.dp))
        Text("Estado del sector", style = MaterialTheme.typography.titleSmall)

        EstadoSector.entries.forEach { opcion ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = estado.estado == opcion,
                        onClick = { viewModel.onEstado(opcion) },
                        enabled = libre,
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = estado.estado == opcion, onClick = null, enabled = libre)
                Spacer(Modifier.width(12.dp))
                ChipEstado(opcion.etiqueta(), opcion.tono())
            }
        }

        Spacer(Modifier.height(8.dp))
        MensajeError(estado.errorGeneral)

        Spacer(Modifier.height(12.dp))
        BotonAccion(
            texto = "Guardar",
            cargando = estado.cargando,
            onClick = viewModel::guardar,
            modifier = Modifier.fillMaxWidth()
        )

        if (estado.esEdicion) {
            Spacer(Modifier.height(4.dp))
            BotonEliminar(
                texto = "Eliminar sector",
                titulo = "¿Eliminar este sector?",
                mensaje = "Solo se puede eliminar si no tiene cultivos, tareas ni problemas. Esta acción no se puede deshacer.",
                onConfirmar = viewModel::eliminar,
                enabled = libre
            )
        }
    }
}