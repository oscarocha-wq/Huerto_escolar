package cl.duoc.huerto.presentation.screens.tareas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.domain.model.etiqueta
import cl.duoc.huerto.presentation.components.ChipEstado
import cl.duoc.huerto.presentation.theme.Tono
import cl.duoc.huerto.presentation.theme.tono

/** El color sale del tema según el estado, y se anima al cambiar (ChipEstado). */
@Composable
fun ChipEstadoTarea(estado: EstadoTarea, modifier: Modifier = Modifier) =
    ChipEstado(estado.etiqueta(), estado.tono(), modifier)

@Composable
fun ChipAtrasada(modifier: Modifier = Modifier) =
    ChipEstado("Atrasada", Tono.ERROR, modifier)
