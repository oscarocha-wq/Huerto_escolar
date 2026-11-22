package cl.duoc.huerto.presentation.screens.clima

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.domain.model.Clima
import cl.duoc.huerto.domain.repository.ClimaRepository
import cl.duoc.huerto.domain.usecase.consejoRiego
import cl.duoc.huerto.domain.usecase.descripcionClima
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClimaUi(
    val cargando: Boolean = false,
    val clima: Clima? = null,
    val descripcion: String = "",
    val consejo: String = "",
    val error: String? = null
)

/** Carga el clima de la API externa al abrir el Inicio. */
class ClimaViewModel(private val repo: ClimaRepository) : ViewModel() {

    private val _ui = MutableStateFlow(ClimaUi())
    val ui: StateFlow<ClimaUi> = _ui.asStateFlow()

    init { cargar() }

    fun cargar() {
        if (_ui.value.cargando) return
        _ui.value = _ui.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            repo.climaActual()
                .onSuccess { c ->
                    _ui.value = ClimaUi(clima = c, descripcion = descripcionClima(c.codigo), consejo = consejoRiego(c))
                }
                .onFailure {
                    _ui.value = ClimaUi(error = "No se pudo obtener el clima. Revisa la conexión a internet.")
                }
        }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ClimaViewModel(AppContainer.climaRepository(app)) as T
        }
    }
}
