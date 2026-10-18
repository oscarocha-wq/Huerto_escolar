package cl.duoc.huerto.u_i

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.ui.theme.Tono
import cl.duoc.huerto.ui.theme.coloresTono

fun Tono.icono(): ImageVector = when (this) {
    Tono.NEUTRO -> Icons.Filled.Schedule
    Tono.INFO -> Icons.Filled.Info
    Tono.EXITO -> Icons.Filled.CheckCircle
    Tono.ADVERTENCIA -> Icons.Filled.Warning
    Tono.ERROR -> Icons.Filled.Error
}

/** Chip de estado. El color se anima cuando cambia el estado (ej: por validar → validada). */
@Composable
fun ChipEstado(texto: String, tono: Tono, modifier: Modifier = Modifier) {
    val (fondo, contenido) = coloresTono(tono)
    val fondoAnimado by animateColorAsState(fondo, label = "chipFondo")
    val contenidoAnimado by animateColorAsState(contenido, label = "chipContenido")

    Surface(
        color = fondoAnimado,
        contentColor = contenidoAnimado,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(tono.icono(), contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}