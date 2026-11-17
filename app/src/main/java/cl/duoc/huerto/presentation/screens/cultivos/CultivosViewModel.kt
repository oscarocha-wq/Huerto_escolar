package cl.duoc.huerto.presentation.screens.cultivos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.domain.model.Cultivo
import cl.duoc.huerto.domain.repository.CultivoRepository
import cl.duoc.huerto.domain.repository.SectorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CultivoItem(val cultivo: Cultivo, val sectorNombre: String)

class CultivosViewModel(
    cultivoRepo: CultivoRepository,
    sectorRepo: SectorRepository
) : ViewModel() {

    /** null = cargando. */
    val cultivos: StateFlow<List<CultivoItem>?> =
        combine(cultivoRepo.cultivos(), sectorRepo.sectores()) { cultivos, sectores ->
            val nombres = sectores.associate { it.id to "${it.codigo} · ${it.nombre}" }
            cultivos.sortedBy { it.nombre }.map { CultivoItem(it, nombres[it.sectorId] ?: "Sector desconocido") }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CultivosViewModel(
                    AppContainer.cultivoRepository(app),
                    AppContainer.sectorRepository(app)
                ) as T
            }
        }
    }
}
