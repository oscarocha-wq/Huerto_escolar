package cl.duoc.huerto.presentation.screens.sectores

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.model.etiqueta
import cl.duoc.huerto.presentation.components.Cargando
import cl.duoc.huerto.presentation.components.ChipEstado
import cl.duoc.huerto.presentation.components.DatoTarjeta
import cl.duoc.huerto.presentation.components.TarjetaElemento
import cl.duoc.huerto.presentation.screens.panel.EstadoVacio
import cl.duoc.huerto.presentation.theme.tono
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SectoresScreen(
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit,
    viewModel: SectoresViewModel = viewModel(
        factory = SectoresViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val sectores by viewModel.sectores.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        val lista = sectores
        when {
            lista == null -> Cargando()
            lista.isEmpty() -> EstadoVacio(
                Icons.Filled.Grass, "Sin sectores", "Crea el primero con el botón \"Nuevo sector\"."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lista, key = { it.id }) { sector ->
                    Box(Modifier.animateItem()) {
                        SectorCard(sector, onClick = { onEditar(sector.id) })
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onNuevo,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nuevo sector") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
private fun SectorCard(sector: Sector, onClick: () -> Unit) {
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL"))
        .format(Date(sector.fechaActualizacion))

    TarjetaElemento(
        titulo = sector.nombre,
        subtitulo = sector.codigo,
        icono = Icons.Filled.Grass,
        tono = sector.estado.tono(),
        onClick = onClick,
        descripcion = sector.descripcion,
        etiquetas = { ChipEstado(sector.estado.etiqueta(), sector.estado.tono()) },
        datos = buildList {
            if (sector.ubicacion.isNotBlank()) add(DatoTarjeta(Icons.Filled.Place, sector.ubicacion))
            add(DatoTarjeta(Icons.Filled.Update, "Actualizado $fecha"))
        }
    )
}
