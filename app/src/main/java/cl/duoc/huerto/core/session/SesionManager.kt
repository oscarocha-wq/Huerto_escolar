package cl.duoc.huerto.core.session

import cl.duoc.huerto.domain.model.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SesionManager {
    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    fun iniciar(sesion: Sesion) { _sesion.value = sesion }
    fun cerrar() { _sesion.value = null }
}
