package cl.duoc.huerto.domain.rules

import cl.duoc.huerto.domain.model.Perfil
import cl.duoc.huerto.domain.model.Sesion

/** perfilRequerido = null significa que cualquier usuario con sesión puede entrar. */
fun Sesion?.puedeAcceder(perfilRequerido: Perfil?): Boolean =
    this != null && (perfilRequerido == null || perfil == perfilRequerido)
