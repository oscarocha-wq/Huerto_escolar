package cl.duoc.huerto.presentation.screens.problemas

import android.app.Application
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.model.etiqueta
import cl.duoc.huerto.domain.util.formatearFechaHora
import cl.duoc.huerto.presentation.components.Cargando
import cl.duoc.huerto.presentation.components.DatoTarjeta
import cl.duoc.huerto.presentation.components.TarjetaElemento
import cl.duoc.huerto.presentation.components.icono
import cl.duoc.huerto.presentation.screens.panel.EstadoVacio
import cl.duoc.huerto.presentation.theme.tono

@Composable
fun ProblemasScreen(
    onNuevo: (() -> Unit)?,
    onAbrir: (Int) -> Unit,
    viewModel: ProblemasViewModel = viewModel(
        factory = ProblemasViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val problemas by viewModel.problemas.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val esAdmin = onNuevo == null

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtro == null,
                    onClick = { viewModel.onFiltro(null) },
                    label = { Text("Todos") }
                )
                EstadoProblema.entries.forEach { e ->
                    FilterChip(
                        selected = filtro == e,
                        onClick = { viewModel.onFiltro(e) },
                        label = { Text(e.etiqueta()) }
                    )
                }
            }

            val lista = problemas
            when {
                lista == null -> Cargando()
                lista.isEmpty() -> EstadoVacio(
                    Icons.Filled.ReportProblem,
                    "Sin problemas",
                    when {
                        filtro != null -> "No hay problemas en estado \"${filtro!!.etiqueta()}\"."
                        esAdmin -> "Nadie ha reportado problemas todavía."
                        else -> "Aún no has reportado problemas. Usa \"Reportar problema\"."
                    }
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.problema.id }) { item ->
                        Box(Modifier.animateItem()) {
                            ProblemaCard(item, mostrarAutor = esAdmin, onClick = { onAbrir(item.problema.id) })
                        }
                    }
                }
            }
        }

        if (onNuevo != null) {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Reportar problema") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun ProblemaCard(item: ProblemaItem, mostrarAutor: Boolean, onClick: () -> Unit) {
    val p = item.problema
    TarjetaElemento(
        titulo = p.tipo.etiqueta(),
        subtitulo = listOfNotNull(item.sectorNombre, item.cultivoNombre).joinToString(" · "),
        icono = p.tipo.icono(),
        tono = p.estado.tono(),
        onClick = onClick,
        descripcion = p.descripcion,
        etiquetas = { ChipEstadoProblema(p.estado) },
        datos = buildList {
            add(DatoTarjeta(Icons.Filled.Schedule, formatearFechaHora(p.fechaReporte)))
            if (mostrarAutor) add(DatoTarjeta(Icons.Filled.Person, item.reportadoPorTexto))
        }
    )
}
