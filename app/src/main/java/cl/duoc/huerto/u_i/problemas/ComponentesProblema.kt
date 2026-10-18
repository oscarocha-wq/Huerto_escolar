package cl.duoc.huerto.u_i.problemas

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.EstadoProblema
import cl.duoc.huerto.domain.etiqueta

@Composable
fun ChipEstadoProblema(estado: EstadoProblema) {
    val objetivo = when (estado) {
        EstadoProblema.INFORMADO -> MaterialTheme.colorScheme.errorContainer
        EstadoProblema.EN_REVISION -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoProblema.ATENDIDO -> MaterialTheme.colorScheme.primaryContainer
        EstadoProblema.CERRADO -> MaterialTheme.colorScheme.secondaryContainer
    }
    val fondo by animateColorAsState(objetivo, tween(500), label = "color_chip_problema")
    Surface(
        color = fondo,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.animateContentSize()
    ) {
        Text(
            estado.etiqueta(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}