package cl.duoc.huerto.domain

import cl.duoc.huerto.data.entities.Evidencia
import cl.duoc.huerto.data.entities.Validacion

/** Todo lo que pasó con una tarea: sus entregas (evidencias) y sus revisiones (validaciones). */
data class Seguimiento(
    val evidencias: List<Evidencia> = emptyList(),
    val validaciones: List<Validacion> = emptyList()
) {
    val ultimaEvidencia: Evidencia?
        get() = evidencias.maxWithOrNull(compareBy<Evidencia>({ it.fecha }, { it.id }))

    /** Cuántas veces se ha entregado la tarea. */
    val intentos: Int get() = evidencias.size

    /** La revisión de la última entrega, o null si todavía no se revisa. */
    val revisionActual: Validacion?
        get() = ultimaEvidencia?.let { e ->
            validaciones.filter { it.evidenciaId == e.id }
                .maxWithOrNull(compareBy<Validacion>({ it.fecha }, { it.id }))
        }

    /** Revisiones de entregas anteriores (ya corregidas), de la más reciente a la más antigua. */
    val revisionesAnteriores: List<Validacion>
        get() = validaciones.filter { it.id != revisionActual?.id }
            .sortedWith(compareByDescending<Validacion> { it.fecha }.thenByDescending { it.id })
}

/** Una entrega necesita al menos una foto o un comentario como evidencia. */
fun validarEntrega(comentario: String, fotoRuta: String?): String? = when {
    comentario.isBlank() && fotoRuta == null -> "Agrega una foto o un comentario como evidencia"
    comentario.length > LimitesCampo.COMENTARIO -> "Máximo ${LimitesCampo.COMENTARIO} caracteres"
    else -> null
}

fun ResultadoValidacion.etiqueta() = when (this) {
    ResultadoValidacion.APROBADA -> "Aprobada"
    ResultadoValidacion.RECHAZADA -> "Rechazada"
}
