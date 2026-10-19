package cl.duoc.huerto.u_i.tareas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class TareasViewModel(
    tareaRepo: TareaRepository,
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository,
    usuarioRepo: UsuarioRepository
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroTarea.TODAS)
    val filtro: StateFlow<FiltroTarea> = _filtro.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todas: Flow<List<TareaItem>> = SesionManager.sesion.flatMapLatest { sesion ->
        when {
            sesion == null -> flowOf(emptyList())
            sesion.esAdministrador -> combine(
                tareaRepo.todas(), sectorRepo.sectores(),
                cultivoRepo.cultivos(), usuarioRepo.estudiantes()
            ) { t, s, c, e -> construirItems(t, s, c, e) }
            else -> combine(
                tareaRepo.paraEstudiante(sesion.usuarioId, sesion.grupo),
                sectorRepo.sectores(), cultivoRepo.cultivos()
            ) { t, s, c -> construirItems(t, s, c, emptyList()) }
        }
    }

    /** null = todavía cargando. */
    val tareas: StateFlow<List<TareaItem>?> =
        combine(todas, _filtro) { items, f -> items.filter { f.acepta(it) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onFiltro(f: FiltroTarea) { _filtro.value = f }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(app)
                return TareasViewModel(
                    TareaRepository(db.tareaDao()),
                    SectorRepository(db.sectorDao()),
                    CultivoRepository(db.cultivoDao()),
                    UsuarioRepository(db.usuarioDao())
                ) as T
            }
        }
    }
}