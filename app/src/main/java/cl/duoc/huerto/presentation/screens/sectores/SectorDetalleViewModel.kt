package cl.duoc.huerto.presentation.screens.sectores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.domain.repository.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
                return SectorDetalleViewModel(
                    AppContainer.sectorRepository(app),
                    AppContainer.cultivoRepository(app),
                    AppContainer.tareaRepository(app),
                    AppContainer.problemaRepository(app),
                    sectorId
                ) as T
            }
        }
    }
}
