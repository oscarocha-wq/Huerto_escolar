package cl.duoc.huerto.domain

/** perfilRequerido = null significa que cualquier usuario con sesión puede entrar. */
fun Sesion?.puedeAcceder(perfilRequerido: Perfil?): Boolean =
    this != null && (perfilRequerido == null || perfil == perfilRequerido)