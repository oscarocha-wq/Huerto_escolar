package cl.duoc.huerto.presentation.screens.panel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.domain.repository.*
import cl.duoc.huerto.domain.usecase.*
import cl.duoc.huerto.domain.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/** Agrupa los datos base (combine acepta hasta 5 flujos a la vez). */
private data class DatosPanel(
    val tareas: List<Tarea>,
    val cultivos: List<Cultivo>,
    val sectores: List<Sector>,
    val problemas: List<Problema>,
    val usuarios: List<Usuario>
)

class PanelViewModel(
    tareaRepo: TareaRepository,
    cultivoRepo: CultivoRepository,
    sectorRepo: SectorRepository,
    problemaRepo: ProblemaRepository,
    usuarioRepo: UsuarioRepository,
    seguimientoRepo: SeguimientoRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val ui: StateFlow<PanelUi> = SesionManager.sesion.flatMapLatest { sesion ->
        if (sesion == null) {
            flowOf(PanelUi())
        } else {
            val problemas = if (sesion.esAdministrador) problemaRepo.todos()
            else problemaRepo.deUsuario(sesion.usuarioId)
            val usuarios = if (sesion.esAdministrador) usuarioRepo.estudiantes()
            else flowOf(emptyList())

            val datos = combine(
                tareaRepo.todas(), cultivoRepo.cultivos(), sectorRepo.sectores(), problemas, usuarios
            ) { tareas, cultivos, sectores, probs, users ->
                DatosPanel(tareas, cultivos, sectores, probs, users)
            }

            combine(datos, seguimientoRepo.evidencias(), seguimientoRepo.validaciones()) { d, evid, valid ->
                val hoy = inicioDeHoyUtc()
                // Todo se filtra por sesión dentro de las funciones del dominio
                val alertas = generarAlertas(sesion, d.tareas, d.cultivos, d.sectores, d.problemas, hoy)
                PanelUi(
                    sesion = sesion,
                    nombre = sesion.nombre,
                    esAdmin = sesion.esAdministrador,
                    resumen = construirResumen(sesion, d.tareas, d.problemas, alertas.size, hoy),
                    alertas = alertas,
                    historial = construirHistorial(
                        sesion, d.tareas, d.problemas,
                        d.usuarios.associate { it.id to it.nombre },
                        evid, valid
                    )
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PanelUi())

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PanelViewModel(
                    AppContainer.tareaRepository(app),
                    AppContainer.cultivoRepository(app),
                    AppContainer.sectorRepository(app),
                    AppContainer.problemaRepository(app),
                    AppContainer.usuarioRepository(app),
                    AppContainer.seguimientoRepository(app)
                ) as T
            }
        }
    }
}
