package cl.duoc.huerto.presentation.screens.tareas

import android.app.Application
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.model.etiqueta
import cl.duoc.huerto.domain.util.formatearFechaUtc
import cl.duoc.huerto.presentation.components.Cargando
import cl.duoc.huerto.presentation.components.DatoTarjeta
import cl.duoc.huerto.presentation.components.TarjetaElemento
import cl.duoc.huerto.presentation.components.icono
import cl.duoc.huerto.presentation.screens.panel.EstadoVacio
import cl.duoc.huerto.presentation.theme.Tono
import cl.duoc.huerto.presentation.theme.tono

@Composable
fun TareasScreen(
    onNueva: (() -> Unit)?,
    onAbrir: (Int) -> Unit,
    viewModel: TareasViewModel = viewModel(
        factory = TareasViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val tareas by viewModel.tareas.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroTarea.entries.forEach { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { viewModel.onFiltro(f) },
                        label = { Text(f.titulo) }
                    )
                }
            }

            val lista = tareas
            when {
                lista == null -> Cargando()
                lista.isEmpty() -> EstadoVacio(
                    Icons.AutoMirrored.Filled.Assignment,
                    "Sin tareas",
                    if (filtro == FiltroTarea.TODAS) "Todavía no hay tareas para mostrar."
                    else "No hay tareas en \"${filtro.titulo}\". Prueba con otro filtro."
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.tarea.id }) { item ->
                        // animateItem: las tarjetas entran, salen y se reordenan con movimiento
                        Box(Modifier.animateItem()) {
                            TareaCard(
                                item = item,
                                mostrarAsignado = onNueva != null,
                                onClick = { onAbrir(item.tarea.id) }
                            )
                        }
                    }
                }
            }
        }

        if (onNueva != null) {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva tarea") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun TareaCard(item: TareaItem, mostrarAsignado: Boolean, onClick: () -> Unit) {
    val t = item.tarea
    TarjetaElemento(
        titulo = t.tipo.etiqueta(),
        subtitulo = listOfNotNull(item.sectorNombre, item.cultivoNombre).joinToString(" · "),
        icono = t.tipo.icono(),
        // Una tarea atrasada se destaca en rojo aunque su estado sea "Pendiente"
        tono = if (item.atrasada) Tono.ERROR else t.estado.tono(),
        onClick = onClick,
        descripcion = t.descripcion,
        etiquetas = {
            ChipEstadoTarea(t.estado)
            if (item.atrasada) ChipAtrasada()
        },
        datos = buildList {
            add(DatoTarjeta(Icons.Filled.Event, formatearFechaUtc(t.fechaProgramada)))
            if (mostrarAsignado) add(DatoTarjeta(Icons.Filled.Person, item.asignadoTexto))
        }
    )
}
