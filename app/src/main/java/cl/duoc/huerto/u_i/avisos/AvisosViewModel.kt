package cl.duoc.huerto.u_i.avisos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.Tarea
import cl.duoc.huerto.data.repository.ProblemaRepository
import cl.duoc.huerto.data.repository.TareaRepository
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

private fun resumenDe(tareas: List<Tarea>, problemasNuevos: Int): ResumenAvisos {
    val hoy = inicioDeHoyUtc()
    return ResumenAvisos(
        atrasadas = tareas.count { estaAtrasada(it.estado, it.fechaProgramada, hoy) },
        porValidar = tareas.count { it.estado == EstadoTarea.PENDIENTE_DE_VALIDACION },
        problemasNuevos = problemasNuevos
    )
}

class AvisosViewModel(
    tareaRepo: TareaRepository,
    problemaRepo: ProblemaRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val resumen: StateFlow<ResumenAvisos> = SesionManager.sesion.flatMapLatest { sesion ->
        when {
            sesion == null -> flowOf(ResumenAvisos())
            sesion.esAdministrador -> combine(
                tareaRepo.todas(), problemaRepo.todos()
            ) { tareas, problemas ->
                resumenDe(tareas, problemas.count { it.estado == EstadoProblema.INFORMADO })
            }
            else -> tareaRepo.paraEstudiante(sesion.usuarioId, sesion.grupo)
                .map { resumenDe(it, 0) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenAvisos())

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(app)
                return AvisosViewModel(
                    TareaRepository(db.tareaDao()),
                    ProblemaRepository(db.problemaDao())
                ) as T
            }
        }
    }
}