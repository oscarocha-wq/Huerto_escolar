package cl.duoc.huerto.domain.usecase

import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.domain.model.EventoHistorial
import cl.duoc.huerto.domain.model.Evidencia
import cl.duoc.huerto.domain.model.Problema
import cl.duoc.huerto.domain.model.ResultadoValidacion
import cl.duoc.huerto.domain.model.ResumenPanel
import cl.duoc.huerto.domain.model.Sesion
import cl.duoc.huerto.domain.model.Tarea
import cl.duoc.huerto.domain.model.TipoEvento
import cl.duoc.huerto.domain.model.Validacion
import cl.duoc.huerto.domain.model.etiqueta
import cl.duoc.huerto.domain.rules.estaAtrasada
import cl.duoc.huerto.domain.rules.puedeVerProblema
import cl.duoc.huerto.domain.rules.puedeVerTarea

private val ABIERTAS_PANEL = setOf(
    EstadoTarea.PENDIENTE, EstadoTarea.EN_DESARROLLO, EstadoTarea.RECHAZADA
)

fun construirResumen(
    sesion: Sesion?,
    tareas: List<Tarea>,
    problemas: List<Problema>,
    totalAlertas: Int,
    hoy: Long
): ResumenPanel {
    if (sesion == null) return ResumenPanel()
    val visibles = tareas.filter { puedeVerTarea(sesion, it.asignadoId, it.grupo) }
    return ResumenPanel(
        pendientes = visibles.count { it.estado in ABIERTAS_PANEL },
        atrasadas = visibles.count { estaAtrasada(it.estado, it.fechaProgramada, hoy) },
        porValidar = visibles.count { it.estado == EstadoTarea.PENDIENTE_DE_VALIDACION },
        validadas = visibles.count { it.estado == EstadoTarea.VALIDADA },
        problemasAbiertos = problemas.count {
            puedeVerProblema(sesion, it.reportadoPorId) && it.estado != EstadoProblema.CERRADO
        },
        alertas = totalAlertas
    )
}

// ---------------- Historial ----------------

/** Historial más reciente primero. Cada persona ve solo lo que le corresponde. */
fun construirHistorial(
    sesion: Sesion?,
    tareas: List<Tarea>,
    problemas: List<Problema>,
    nombres: Map<String, String>,
    evidencias: List<Evidencia> = emptyList(),
    validaciones: List<Validacion> = emptyList()
): List<EventoHistorial> {
    if (sesion == null) return emptyList()
    fun quien(id: String?) = when {
        id == null -> "—"
        id == sesion.usuarioId -> "ti"
        else -> nombres[id] ?: id
    }
    fun conComentario(base: String, comentario: String?) =
        base + (comentario?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")

    val eventos = mutableListOf<EventoHistorial>()
    val visibles = tareas.filter { puedeVerTarea(sesion, it.asignadoId, it.grupo) }
    val tareaPorId = visibles.associateBy { it.id }

    // Asignación de cada tarea
    visibles.forEach { t ->
        val para = t.asignadoId?.let { quien(it) } ?: t.grupo?.let { "el grupo $it" } ?: "—"
        eventos += EventoHistorial(
            clave = "asignada-${t.id}",
            tipo = TipoEvento.TAREA_ASIGNADA,
            fecha = t.fechaCreacion,
            titulo = "Tarea asignada: ${t.tipo.etiqueta()}",
            detalle = "Para $para",
            tareaId = t.id
        )
    }

    // Cada entrega (puede haber varias si la tarea fue rechazada y reenviada)
    evidencias.forEach { e ->
        val t = tareaPorId[e.tareaId] ?: return@forEach
        eventos += EventoHistorial(
            clave = "entrega-${e.id}",
            tipo = TipoEvento.TAREA_ENVIADA,
            fecha = e.fecha,
            titulo = "Tarea enviada: ${t.tipo.etiqueta()}",
            detalle = conComentario("Enviada por ${quien(e.autorId)}", e.comentario),
            tareaId = t.id
        )
    }

    // Cada revisión del docente
    validaciones.forEach { v ->
        val t = tareaPorId[v.tareaId] ?: return@forEach
        val aprobada = v.resultado == ResultadoValidacion.APROBADA
        eventos += EventoHistorial(
            clave = "revision-${v.id}",
            tipo = if (aprobada) TipoEvento.TAREA_VALIDADA else TipoEvento.TAREA_RECHAZADA,
            fecha = v.fecha,
            titulo = (if (aprobada) "Tarea validada: " else "Tarea rechazada: ") + t.tipo.etiqueta(),
            detalle = conComentario("Revisada por ${quien(v.validadorId)}", v.comentario),
            tareaId = t.id
        )
    }

    problemas.filter { puedeVerProblema(sesion, it.reportadoPorId) }.forEach { p ->
        val tipo = p.tipo.etiqueta()
        eventos += EventoHistorial(
            clave = "problema-${p.id}",
            tipo = TipoEvento.PROBLEMA_REPORTADO,
            fecha = p.fechaReporte,
            titulo = "Problema reportado: $tipo",
            detalle = "Reportado por ${quien(p.reportadoPorId)}",
            problemaId = p.id
        )
        val fechaAtencion = p.fechaAtencion
        if (fechaAtencion != null &&
            (p.estado == EstadoProblema.ATENDIDO || p.estado == EstadoProblema.CERRADO)
        ) {
            eventos += EventoHistorial(
                clave = "atencion-${p.id}",
                tipo = TipoEvento.PROBLEMA_ATENDIDO,
                fecha = fechaAtencion,
                titulo = "Problema ${p.estado.etiqueta().lowercase()}: $tipo",
                detalle = p.comentarioDocente ?: "Sin comentario",
                problemaId = p.id
            )
        }
    }

    return eventos.sortedByDescending { it.fecha }
}
