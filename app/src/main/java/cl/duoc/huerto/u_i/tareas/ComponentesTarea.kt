package cl.duoc.huerto.u_i.tareas

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.EstadoTarea
import cl.duoc.huerto.domain.etiqueta

@Composable
private fun Etiqueta(texto: String, fondo: Color) {
    // El color cambia suavemente cuando la tarea cambia de estado
    val fondoAnimado by animateColorAsState(fondo, tween(500), label = "color_chip")
    Surface(
        color = fondoAnimado,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.animateContentSize()
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ChipEstadoTarea(estado: EstadoTarea) {
    val fondo = when (estado) {
        EstadoTarea.PENDIENTE, EstadoTarea.REALIZADA -> MaterialTheme.colorScheme.secondaryContainer
        EstadoTarea.EN_DESARROLLO, EstadoTarea.VALIDADA -> MaterialTheme.colorScheme.primaryContainer
        EstadoTarea.PENDIENTE_DE_VALIDACION -> MaterialTheme.colorScheme.tertiaryContainer
        EstadoTarea.RECHAZADA -> MaterialTheme.colorScheme.errorContainer
    }
    Etiqueta(estado.etiqueta(), fondo)
}

@Composable
fun ChipAtrasada() = Etiqueta("Atrasada", MaterialTheme.colorScheme.errorContainer)