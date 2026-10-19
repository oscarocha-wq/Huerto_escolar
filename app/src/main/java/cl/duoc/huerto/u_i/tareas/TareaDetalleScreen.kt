package cl.duoc.huerto.u_i.tareas

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import cl.duoc.huerto.data.entities.Evidencia
import cl.duoc.huerto.data.entities.Validacion
import cl.duoc.huerto.u_i.AccesoDenegado
import cl.duoc.huerto.u_i.BotonEliminar
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.ui.theme.Tono
import cl.duoc.huerto.u_i.ImagenLocal
import cl.duoc.huerto.u_i.MensajeError
import cl.duoc.huerto.u_i.SelectorFoto
import cl.duoc.huerto.u_i.CampoValidado

@Composable
fun TareaDetalleScreen(tareaId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: TareaDetalleViewModel = viewModel(
        factory = TareaDetalleViewModel.factory(context.applicationContext as Application, tareaId)
    )
    val item by viewModel.item.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val seguimiento by viewModel.seguimiento.collectAsStateWithLifecycle()
    val sesion by SesionManager.sesion.collectAsStateWithLifecycle()
    var confirmar by remember { mutableStateOf(false) }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.mensajeMostrado()
        }
    }

    LaunchedEffect(ui.eliminada) {
        if (ui.eliminada) {
            Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show()
            onVolver()
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
            Text("Detalle de la tarea", style = MaterialTheme.typography.titleLarge)
        }

        val actual = item
        if (actual == null) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }
        val t = actual.tarea
        if (!puedeVerTarea(sesion, t.asignadoId, t.grupo)) {
            AccesoDenegado()
            return@Column
        }

        val esAdmin = sesion?.esAdministrador == true
        val esEstudianteAsignado = sesion != null && !esAdmin &&
                estaAsignadaA(sesion!!, t.asignadoId, t.grupo)

        // --- Datos de la tarea ---
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipEstadoTarea(t.estado)
                    if (actual.atrasada) ChipAtrasada()
                }
                Dato("Tipo", t.tipo.etiqueta())
                Dato("Instrucciones", t.descripcion)
                Dato("Sector", actual.sectorNombre)
                actual.cultivoNombre?.let { Dato("Cultivo", it) }
                Dato("Fecha programada", formatearFechaUtc(t.fechaProgramada))
                if (esAdmin) Dato("Asignada a", actual.asignadoTexto)
            }
        }

        // --- Seguimiento: entregas y revisiones (aparece con animación) ---
        val ultima = seguimiento.ultimaEvidencia
        AnimatedVisibility(
            visible = ultima != null,
            enter = fadeIn() + expandVertically()
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (seguimiento.intentos > 1) "Última entrega (intento ${seguimiento.intentos})"
                        else "Entrega",
                        style = MaterialTheme.typography.titleMedium
                    )
                    ultima?.let { DatosEvidencia(it) }
                    seguimiento.revisionActual?.let { DatosValidacion(it) }
                }
            }
        }

        // Revisiones anteriores (entregas que fueron rechazadas y corregidas)
        if (seguimiento.revisionesAnteriores.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Revisiones anteriores", style = MaterialTheme.typography.titleMedium)
                    seguimiento.revisionesAnteriores.forEach { v ->
                        DatosValidacion(v)
                        HorizontalDivider()
                    }
                }
            }
        }

        // --- Acciones del estudiante ---
        if (esEstudianteAsignado) {
            if (puedeMarcarRealizada(t.estado)) {
                if (t.estado == EstadoTarea.RECHAZADA) {
                    Text(
                        "El docente pidió repetir esta tarea. Lee su comentario y vuelve a enviarla.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (puedeIniciar(t.estado)) {
                    OutlinedButton(
                        onClick = viewModel::iniciar,
                        enabled = !ui.procesando,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Comenzar tarea") }
                }

                CampoValidado(
                    value = ui.comentario,
                    onValueChange = viewModel::onComentario,
                    label = "Comentario (opcional)",
                    singleLine = false,
                    minLines = 2,
                    maxLength = LimitesCampo.COMENTARIO
                )
                // Galería + cámara + vista previa
                SelectorFoto(
                    fotoRuta = ui.fotoRuta,
                    onFoto = viewModel::onFoto,
                    onError = viewModel::onErrorFoto,
                    onQuitar = viewModel::quitarFoto,
                    enabled = !ui.procesando
                )

                Button(
                    onClick = { confirmar = true },
                    enabled = !ui.procesando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) { Text("Marcar como realizada") }
            } else if (t.estado == EstadoTarea.PENDIENTE_DE_VALIDACION) {
                Text(
                    "Tu trabajo está esperando la revisión del docente.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // --- Acciones del administrador ---
        if (puedeRevisar(sesion, t.estado, ultima?.autorId)) {
            CampoValidado(
                value = ui.comentario,
                onValueChange = viewModel::onComentario,
                label = "Comentario (obligatorio para rechazar)",
                singleLine = false,
                minLines = 2,
                maxLength = LimitesCampo.COMENTARIO,
                ayuda = "Si rechazas, explica qué corregir (mín. ${LimitesCampo.MOTIVO_RECHAZO_MIN} caracteres)"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = viewModel::rechazar,
                    enabled = !ui.procesando,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) { Text("Rechazar") }
                Button(
                    onClick = viewModel::validar,
                    enabled = !ui.procesando,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) { Text("Validar") }
            }
        }

        // --- Eliminar (solo admin, solo si nadie la ha trabajado) ---
        if (puedeEliminarTarea(sesion, t.estado, seguimiento.intentos)) {
            BotonEliminar(
                texto = "Eliminar tarea",
                titulo = "¿Eliminar tarea?",
                mensaje = "La tarea aún no ha sido iniciada ni entregada. Esta acción no se puede deshacer.",
                onConfirmar = viewModel::eliminar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.procesando
            )
        }

        MensajeError(ui.error)
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Terminaste esta tarea?") },
            text = { Text("Quedará pendiente de validación hasta que un docente la revise.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.marcarRealizada()
                }) { Text("Sí, enviar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
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

@Composable
private fun DatosEvidencia(e: Evidencia) {
    Dato("Entregada el", formatearFechaHora(e.fecha))
    e.comentario?.let { Dato("Comentario del estudiante", it) }
    e.fotoRuta?.let {
        ImagenLocal(
            it,
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
        )
    }
}

@Composable
private fun DatosValidacion(v: Validacion) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChipEstado(
            v.resultado.etiqueta(),
            if (v.resultado == ResultadoValidacion.APROBADA) Tono.EXITO else Tono.ERROR
        )
        Text(
            formatearFechaHora(v.fecha),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    v.comentario?.let { Dato("Comentario del docente", it) }
}
