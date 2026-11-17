package cl.duoc.huerto.presentation.screens.sectores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.repository.SectorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SectoresViewModel(repo: SectorRepository) : ViewModel() {

    /** null = todavía cargando; lista vacía = no hay sectores. */
    val sectores: StateFlow<List<Sector>?> = repo.sectores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SectoresViewModel(AppContainer.sectorRepository(app)) as T
        }
    }
}
