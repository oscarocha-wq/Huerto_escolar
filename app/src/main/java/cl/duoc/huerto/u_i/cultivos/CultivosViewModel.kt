package cl.duoc.huerto.u_i.cultivos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.repository.CultivoRepository
import cl.duoc.huerto.data.repository.SectorRepository
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
                val db = AppDatabase.get(app)
                return CultivosViewModel(
                    CultivoRepository(db.cultivoDao()),
                    SectorRepository(db.sectorDao())
                ) as T
            }
        }
    }
}