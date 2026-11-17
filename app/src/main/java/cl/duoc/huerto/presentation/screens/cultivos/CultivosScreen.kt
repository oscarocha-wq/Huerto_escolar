package cl.duoc.huerto.presentation.screens.cultivos

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.model.EstadoCultivo
import cl.duoc.huerto.domain.model.etiqueta
import cl.duoc.huerto.domain.util.formatearFechaUtc
import cl.duoc.huerto.presentation.components.Cargando
import cl.duoc.huerto.presentation.components.ChipEstado
import cl.duoc.huerto.presentation.components.DatoTarjeta
import cl.duoc.huerto.presentation.components.TarjetaElemento
import cl.duoc.huerto.presentation.screens.panel.EstadoVacio
import cl.duoc.huerto.presentation.theme.tono

@Composable
fun CultivosScreen(
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit,
    viewModel: CultivosViewModel = viewModel(
        factory = CultivosViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val cultivos by viewModel.cultivos.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        val lista = cultivos
        when {
            lista == null -> Cargando()
            lista.isEmpty() -> EstadoVacio(
                Icons.Filled.Eco, "Sin cultivos", "Registra el primero con el botón \"Nuevo cultivo\"."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lista, key = { it.cultivo.id }) { item ->
                    Box(Modifier.animateItem()) {
                        CultivoCard(item, onClick = { onEditar(item.cultivo.id) })
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuevo cultivo") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
private fun CultivoCard(item: CultivoItem, onClick: () -> Unit) {
    val c = item.cultivo
    fun fecha(v: Long?) = if (v == null) "Sin fecha" else formatearFechaUtc(v)

    TarjetaElemento(
        titulo = c.nombre,
        subtitulo = item.sectorNombre,
        icono = Icons.Filled.Eco,
        tono = c.estado.tono(),
        onClick = onClick,
        fotoRuta = c.fotoUri,
        etiquetas = { EtiquetaEstadoCultivo(c.estado) },
        datos = listOf(
            DatoTarjeta(Icons.Filled.Spa, "Siembra ${fecha(c.fechaSiembra)}"),
            DatoTarjeta(Icons.Filled.Agriculture, "Cosecha ${fecha(c.fechaCosechaEstimada)}"),
            DatoTarjeta(Icons.Filled.WaterDrop, "Riego cada ${c.frecuenciaRiegoDias} día(s)")
        )
    )
}

@Composable
private fun EtiquetaEstadoCultivo(estado: EstadoCultivo) = ChipEstado(estado.etiqueta(), estado.tono())
