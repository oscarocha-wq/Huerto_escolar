package cl.duoc.huerto.domain

private val ESTADOS_ABIERTOS = setOf(
    EstadoTarea.PENDIENTE, EstadoTarea.EN_DESARROLLO, EstadoTarea.RECHAZADA
)

/** "Atrasada" se calcula: no se guarda, así nunca queda desactualizada. */
fun estaAtrasada(estado: EstadoTarea, fechaProgramada: Long, hoyUtc: Long) =
    estado in ESTADOS_ABIERTOS && fechaProgramada < hoyUtc

fun puedeIniciar(estado: EstadoTarea) = estado == EstadoTarea.PENDIENTE

fun puedeMarcarRealizada(estado: EstadoTarea) = estado in ESTADOS_ABIERTOS

fun estaAsignadaA(sesion: Sesion, asignadoId: String?, grupo: String?) =
    asignadoId == sesion.usuarioId || (grupo != null && grupo == sesion.grupo)

fun puedeVerTarea(sesion: Sesion?, asignadoId: String?, grupo: String?) =
    sesion != null && (sesion.esAdministrador || estaAsignadaA(sesion, asignadoId, grupo))

/** Solo un administrador, solo si está pendiente de validación y nunca quien la realizó. */
fun puedeRevisar(sesion: Sesion?, estado: EstadoTarea, realizadaPorId: String?) =
    sesion != null &&
            sesion.esAdministrador &&
            estado == EstadoTarea.PENDIENTE_DE_VALIDACION &&
            sesion.usuarioId != realizadaPorId