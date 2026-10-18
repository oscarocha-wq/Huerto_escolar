package cl.duoc.huerto.u_i.sectores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.repository.SectorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SectoresViewModel(repo: SectorRepository) : ViewModel() {

    val sectores: StateFlow<List<Sector>> = repo.sectores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SectoresViewModel(SectorRepository(AppDatabase.get(app).sectorDao())) as T
        }
    }
}