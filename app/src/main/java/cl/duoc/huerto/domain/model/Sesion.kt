package cl.duoc.huerto.domain.model

data class Sesion(
    val usuarioId: String,
    val nombre: String,
    val perfil: Perfil,
    val grupo: String?
) {
    val esAdministrador: Boolean get() = perfil == Perfil.ADMINISTRADOR
}
