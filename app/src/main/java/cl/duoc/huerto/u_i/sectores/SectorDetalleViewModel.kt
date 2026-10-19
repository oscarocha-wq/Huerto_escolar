package cl.duoc.huerto.u_i.sectores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.repository.*
import cl.duoc.huerto.domain.EstadoProblema
import cl.duoc.huerto.domain.EstadoTarea
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SectorDetalleUi(
    val cargando: Boolean = true,
    val sector: Sector? = null,
    val cultivos: List<Cultivo> = emptyList(),
    val tareasAbiertas: Int = 0,
    val problemasAbiertos: Int = 0
)

class SectorDetalleViewModel(
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository,
    tareaRepo: TareaRepository,
    problemaRepo: ProblemaRepository,
    private val sectorId: Int
) : ViewModel() {

    val ui: StateFlow<SectorDetalleUi> = combine(
        sectorRepo.observar(sectorId),
        cultivoRepo.porSector(sectorId),
        tareaRepo.todas(),
        problemaRepo.todos()
    ) { sector, cultivos, tareas, problemas ->
        SectorDetalleUi(
            cargando = false,
            sector = sector,
            cultivos = cultivos.sortedBy { it.nombre },
            tareasAbiertas = tareas.count { it.sectorId == sectorId && it.estado != EstadoTarea.VALIDADA },
            problemasAbiertos = problemas.count { it.sectorId == sectorId && it.estado != EstadoProblema.CERRADO }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SectorDetalleUi())

    companion object {
        fun factory(app: Application, sectorId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(app)
                return SectorDetalleViewModel(
                    SectorRepository(db.sectorDao()),
                    CultivoRepository(db.cultivoDao()),
                    TareaRepository(db.tareaDao()),
                    ProblemaRepository(db.problemaDao()),
                    sectorId
                ) as T
            }
        }
    }
}
