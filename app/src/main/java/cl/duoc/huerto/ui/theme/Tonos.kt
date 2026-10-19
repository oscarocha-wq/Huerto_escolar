package cl.duoc.huerto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cl.duoc.huerto.domain.*

/** Severidad visual común a toda la app. */
enum class Tono { NEUTRO, INFO, EXITO, ADVERTENCIA, ERROR }

/** (fondo, contenido) de cada tono. Las pantallas nunca eligen colores a mano. */
@Composable
fun coloresTono(tono: Tono): Pair<Color, Color> {
    val s = MaterialTheme.semanticColors
    val c = MaterialTheme.colorScheme
    return when (tono) {
        Tono.NEUTRO -> c.surfaceVariant to c.onSurfaceVariant
        Tono.INFO -> s.infoContainer to s.onInfoContainer
        Tono.EXITO -> s.successContainer to s.onSuccessContainer
        Tono.ADVERTENCIA -> s.warningContainer to s.onWarningContainer
        Tono.ERROR -> c.errorContainer to c.onErrorContainer
    }
}

// ---- Mapeos puros (sin Compose, se pueden probar) ----

fun EstadoTarea.tono(): Tono = when (this) {
    EstadoTarea.PENDIENTE -> Tono.NEUTRO
    EstadoTarea.EN_DESARROLLO -> Tono.INFO
    EstadoTarea.REALIZADA -> Tono.INFO
    EstadoTarea.PENDIENTE_DE_VALIDACION -> Tono.ADVERTENCIA
    EstadoTarea.VALIDADA -> Tono.EXITO
    EstadoTarea.RECHAZADA -> Tono.ERROR
}

fun EstadoCultivo.tono(): Tono = when (this) {
    EstadoCultivo.PLANIFICADO -> Tono.NEUTRO
    EstadoCultivo.SEMBRADO -> Tono.INFO
    EstadoCultivo.EN_CRECIMIENTO -> Tono.INFO
    EstadoCultivo.LISTO_PARA_COSECHAR -> Tono.EXITO
    EstadoCultivo.COSECHADO -> Tono.EXITO
    EstadoCultivo.CON_PROBLEMAS -> Tono.ERROR
    EstadoCultivo.FINALIZADO -> Tono.NEUTRO
}

fun EstadoProblema.tono(): Tono = when (this) {
    EstadoProblema.INFORMADO -> Tono.ERROR
    EstadoProblema.EN_REVISION -> Tono.ADVERTENCIA
    EstadoProblema.ATENDIDO -> Tono.INFO
    EstadoProblema.CERRADO -> Tono.EXITO
}

fun EstadoSector.tono(): Tono = when (this) {
    EstadoSector.DISPONIBLE -> Tono.EXITO
    EstadoSector.EN_USO -> Tono.INFO
    EstadoSector.EN_MANTENIMIENTO -> Tono.ADVERTENCIA
    EstadoSector.FUERA_DE_USO -> Tono.NEUTRO
}

fun SeveridadAlerta.tono(): Tono = when (this) {
    SeveridadAlerta.URGENTE -> Tono.ERROR
    SeveridadAlerta.AVISO -> Tono.ADVERTENCIA
    SeveridadAlerta.INFO -> Tono.INFO
}

fun TipoEvento.tono(): Tono = when (this) {
    TipoEvento.TAREA_ASIGNADA -> Tono.INFO
    TipoEvento.TAREA_ENVIADA -> Tono.ADVERTENCIA
    TipoEvento.TAREA_VALIDADA, TipoEvento.PROBLEMA_ATENDIDO -> Tono.EXITO
    TipoEvento.TAREA_RECHAZADA, TipoEvento.PROBLEMA_REPORTADO -> Tono.ERROR
}

fun EstadoLectura.tono(): Tono = when (this) {
    EstadoLectura.NORMAL -> Tono.EXITO
    EstadoLectura.BAJO, EstadoLectura.ALTO -> Tono.ADVERTENCIA
}
