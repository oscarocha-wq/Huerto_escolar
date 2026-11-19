package cl.duoc.huerto.presentation.screens.sincronizacion

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.domain.repository.SincronizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SincronizacionUi(
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false
)

/** Botón "Sincronizar": baja los datos del backend y los deja en Room. */
class SincronizacionViewModel(
    private val repo: SincronizacionRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SincronizacionUi())
    val ui: StateFlow<SincronizacionUi> = _ui.asStateFlow()

    fun sincronizar() {
        if (_ui.value.cargando) return
        _ui.update { it.copy(cargando = true, mensaje = null) }
        viewModelScope.launch {
            repo.sincronizar()
                .onSuccess { r ->
                    _ui.value = SincronizacionUi(
                        mensaje = "Listo: ${r.sectores} sectores, ${r.cultivos} cultivos, " +
                            "${r.tareas} tareas y ${r.problemas} problemas."
                    )
                }
                .onFailure {
                    _ui.value = SincronizacionUi(
                        mensaje = "No se pudo conectar con el servidor. " +
                            "Revisa que huertoapi esté encendido. Se siguen usando los datos del teléfono.",
                        esError = true
                    )
                }
        }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SincronizacionViewModel(AppContainer.sincronizacionRepository(app)) as T
        }
    }
}
