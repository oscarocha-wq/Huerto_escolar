package cl.duoc.huerto.u_i.panel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.Alerta
import cl.duoc.huerto.domain.SeveridadAlerta
import cl.duoc.huerto.domain.TipoAlerta
import cl.duoc.huerto.domain.TipoEvento
import cl.duoc.huerto.ui.theme.coloresTono
import cl.duoc.huerto.ui.theme.tono

fun iconoDe(tipo: TipoAlerta): ImageVector = when (tipo) {
    TipoAlerta.TAREA_ATRASADA -> Icons.Filled.EventBusy
    TipoAlerta.TAREA_RECHAZADA -> Icons.Filled.Replay
    TipoAlerta.RIEGO_ATRASADO, TipoAlerta.RIEGO_PROXIMO -> Icons.Filled.WaterDrop
    TipoAlerta.COSECHA_PROXIMA -> Icons.Filled.Agriculture
    TipoAlerta.MANTENIMIENTO -> Icons.Filled.Build
    TipoAlerta.POR_VALIDAR -> Icons.Filled.HourglassTop
    TipoAlerta.PROBLEMA_PENDIENTE -> Icons.Filled.ReportProblem
}

fun iconoDe(tipo: TipoEvento): ImageVector = when (tipo) {
    TipoEvento.TAREA_ASIGNADA -> Icons.Filled.Assignment
    TipoEvento.TAREA_ENVIADA -> Icons.Filled.Send
    TipoEvento.TAREA_VALIDADA -> Icons.Filled.CheckCircle
    TipoEvento.TAREA_RECHAZADA -> Icons.Filled.Replay
    TipoEvento.PROBLEMA_REPORTADO -> Icons.Filled.ReportProblem
    TipoEvento.PROBLEMA_ATENDIDO -> Icons.Filled.Build
}

/** Colores (fondo, contenido) desde el tema: urgente = rojo, aviso = ámbar, info = azul. */
@Composable
fun coloresDe(severidad: SeveridadAlerta): Pair<Color, Color> = coloresTono(severidad.tono())

@Composable
fun coloresDe(tipo: TipoEvento): Pair<Color, Color> = coloresTono(tipo.tono())

@Composable
fun IconoCircular(icono: ImageVector, fondo: Color, contenido: Color, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = fondo, modifier = modifier.size(40.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(22.dp))
        }
    }
}

/** Fila de un aviso: ícono de color según severidad, título y detalle. */
@Composable
fun AlertaContenido(alerta: Alerta, modifier: Modifier = Modifier) {
    val (fondo, contenido) = coloresDe(alerta.severidad)
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoCircular(iconoDe(alerta.tipo), fondo, contenido)
        Column(Modifier.weight(1f)) {
            Text(alerta.titulo, style = MaterialTheme.typography.titleSmall)
            Text(
                alerta.detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Estado vacío con ícono, reutilizable en alertas e historial. */
@Composable
fun EstadoVacio(icono: ImageVector, titulo: String, mensaje: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}