package cl.duoc.huerto.presentation.screens.problemas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ProblemasViewModel(
    problemaRepo: ProblemaRepository,
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository,
    usuarioRepo: UsuarioRepository
) : ViewModel() {

    /** null = mostrar todos */
    private val _filtro = MutableStateFlow<EstadoProblema?>(null)
    val filtro: StateFlow<EstadoProblema?> = _filtro.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todos: Flow<List<ProblemaItem>> = SesionManager.sesion.flatMapLatest { sesion ->
        when {
            sesion == null -> flowOf(emptyList())
            sesion.esAdministrador -> combine(
                problemaRepo.todos(), sectorRepo.sectores(),
                cultivoRepo.cultivos(), usuarioRepo.estudiantes()
            ) { p, s, c, u -> construirItemsProblema(p, s, c, u) }
            else -> combine(
                problemaRepo.deUsuario(sesion.usuarioId),
                sectorRepo.sectores(), cultivoRepo.cultivos()
            ) { p, s, c -> construirItemsProblema(p, s, c, emptyList()) }
        }
    }

    /** null = todavía cargando. */
    val problemas: StateFlow<List<ProblemaItem>?> =
        combine(todos, _filtro) { items, f ->
            if (f == null) items else items.filter { it.problema.estado == f }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onFiltro(f: EstadoProblema?) { _filtro.value = f }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProblemasViewModel(
                    AppContainer.problemaRepository(app),
                    AppContainer.sectorRepository(app),
                    AppContainer.cultivoRepository(app),
                    AppContainer.usuarioRepository(app)
                ) as T
            }
        }
    }
}
