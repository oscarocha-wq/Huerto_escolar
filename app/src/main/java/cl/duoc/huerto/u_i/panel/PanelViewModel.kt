package cl.duoc.huerto.u_i.panel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.*
import cl.duoc.huerto.data.repository.*
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class PanelUi(
    val nombre: String = "",
    val esAdmin: Boolean = false,
    val resumen: ResumenPanel = ResumenPanel(),
    val alertas: List<Alerta> = emptyList(),
    val historial: List<EventoHistorial> = emptyList()
)

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
                val db = AppDatabase.get(app)
                return PanelViewModel(
                    TareaRepository(db.tareaDao()),
                    CultivoRepository(db.cultivoDao()),
                    SectorRepository(db.sectorDao()),
                    ProblemaRepository(db.problemaDao()),
                    UsuarioRepository(db.usuarioDao()),
                    SeguimientoRepository(db.seguimientoDao())
                ) as T
            }
        }
    }
}
