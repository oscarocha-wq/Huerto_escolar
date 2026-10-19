package cl.duoc.huerto.u_i.problemas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huerto.domain.EstadoProblema
import cl.duoc.huerto.domain.etiqueta
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.ui.theme.tono

@Composable
fun ChipEstadoProblema(estado: EstadoProblema, modifier: Modifier = Modifier) =
    ChipEstado(estado.etiqueta(), estado.tono(), modifier)