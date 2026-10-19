package cl.duoc.huerto.u_i.tareas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huerto.domain.EstadoTarea
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.ui.theme.Tono
import cl.duoc.huerto.ui.theme.tono

/** El color sale del tema según el estado, y se anima al cambiar (ChipEstado). */
@Composable
fun ChipEstadoTarea(estado: EstadoTarea, modifier: Modifier = Modifier) =
    ChipEstado(estado.etiqueta(), estado.tono(), modifier)

@Composable
fun ChipAtrasada(modifier: Modifier = Modifier) =
    ChipEstado("Atrasada", Tono.ERROR, modifier)