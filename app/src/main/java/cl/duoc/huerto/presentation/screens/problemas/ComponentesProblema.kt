package cl.duoc.huerto.presentation.screens.problemas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.model.etiqueta
import cl.duoc.huerto.presentation.components.ChipEstado
import cl.duoc.huerto.presentation.theme.tono

@Composable
fun ChipEstadoProblema(estado: EstadoProblema, modifier: Modifier = Modifier) =
    ChipEstado(estado.etiqueta(), estado.tono(), modifier)
