package cl.duoc.huerto.domain

/** Solo un estudiante reporta problemas. */
fun puedeReportar(sesion: Sesion?) = sesion != null && sesion.perfil == Perfil.ESTUDIANTE

/** El administrador ve todos; el estudiante solo los que él reportó. */
fun puedeVerProblema(sesion: Sesion?, reportadoPorId: String) =
    sesion != null && (sesion.esAdministrador || sesion.usuarioId == reportadoPorId)

/** Solo el administrador cambia el estado de atención. */
fun puedeGestionarProblema(sesion: Sesion?) = sesion != null && sesion.esAdministrador

/** Transiciones permitidas desde cada estado. */
fun siguientesEstados(estado: EstadoProblema): List<EstadoProblema> = when (estado) {
    EstadoProblema.INFORMADO -> listOf(EstadoProblema.EN_REVISION)
    EstadoProblema.EN_REVISION -> listOf(EstadoProblema.ATENDIDO)
    EstadoProblema.ATENDIDO -> listOf(EstadoProblema.CERRADO, EstadoProblema.EN_REVISION)
    EstadoProblema.CERRADO -> emptyList()
}

/** Para marcar un problema como atendido hay que explicar qué se hizo. */
fun requiereComentario(nuevo: EstadoProblema) = nuevo == EstadoProblema.ATENDIDO