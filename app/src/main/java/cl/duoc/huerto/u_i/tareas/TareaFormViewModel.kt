package cl.duoc.huerto.u_i.tareas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.entities.Tarea
import cl.duoc.huerto.data.entities.Usuario
import cl.duoc.huerto.data.repository.*
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ModoAsignacion { ESTUDIANTE, GRUPO }

data class TareaFormState(
    val tipo: TipoTarea? = null,
    val descripcion: String = "",
    val sectorId: Int? = null,
    val cultivoId: Int? = null,
    val fechaProgramada: Long? = null,
    val modo: ModoAsignacion = ModoAsignacion.ESTUDIANTE,
    val estudianteId: String? = null,
    val grupo: String? = null,
    val sectores: List<Sector> = emptyList(),
    val cultivos: List<Cultivo> = emptyList(),
    val estudiantes: List<Usuario> = emptyList(),
    val errores: ErroresTarea = ErroresTarea(),
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val guardado: Boolean = false
) {
    val grupos: List<String> get() = estudiantes.mapNotNull { it.grupo }.distinct().sorted()
}

class TareaFormViewModel(
    private val repo: TareaRepository,
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository,
    usuarioRepo: UsuarioRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(TareaFormState())
    val estado: StateFlow<TareaFormState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            sectorRepo.sectores().collect { l -> _estado.update { it.copy(sectores = l) } }
        }
        viewModelScope.launch {
            cultivoRepo.cultivos().collect { l -> _estado.update { it.copy(cultivos = l) } }
        }
        viewModelScope.launch {
            usuarioRepo.estudiantes().collect { l -> _estado.update { it.copy(estudiantes = l) } }
        }
    }

    fun onTipo(v: TipoTarea) = _estado.update { it.copy(tipo = v, errores = it.errores.copy(tipo = null)) }
    fun onDescripcion(v: String) = _estado.update { it.copy(descripcion = v, errores = it.errores.copy(descripcion = null)) }
    fun onSector(s: Sector) = _estado.update {
        it.copy(sectorId = s.id, cultivoId = null, errores = it.errores.copy(sector = null))
    }
    fun onCultivo(c: Cultivo?) = _estado.update { it.copy(cultivoId = c?.id) }
    fun onFecha(v: Long?) = _estado.update { it.copy(fechaProgramada = v, errores = it.errores.copy(fecha = null)) }
    fun onModo(m: ModoAsignacion) = _estado.update { it.copy(modo = m, errores = it.errores.copy(asignado = null)) }
    fun onEstudiante(u: Usuario) = _estado.update { it.copy(estudianteId = u.id, errores = it.errores.copy(asignado = null)) }
    fun onGrupo(g: String) = _estado.update { it.copy(grupo = g, errores = it.errores.copy(asignado = null)) }

    fun guardar() {
        // Control de acceso real
        val sesion = SesionManager.sesion.value
        if (!sesion.puedeAcceder(Perfil.ADMINISTRADOR)) {
            _estado.update { it.copy(errorGeneral = "No tienes permiso para crear tareas") }
            return
        }

        val a = _estado.value
        val asignadoId = if (a.modo == ModoAsignacion.ESTUDIANTE) a.estudianteId else null
        val grupo = if (a.modo == ModoAsignacion.GRUPO) a.grupo else null

        val errores = validarTarea(
            a.tipo, a.descripcion, a.sectorId, a.fechaProgramada,
            tieneResponsable = asignadoId != null || grupo != null
        )
        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }
        val tipo = a.tipo ?: return
        val sectorId = a.sectorId ?: return
        val fecha = a.fechaProgramada ?: return
        val creador = sesion?.usuarioId ?: return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            repo.crear(
                Tarea(
                    tipo = tipo,
                    descripcion = a.descripcion.trim(),
                    sectorId = sectorId,
                    cultivoId = a.cultivoId,
                    fechaCreacion = System.currentTimeMillis(),
                    fechaProgramada = fecha,
                    asignadoId = asignadoId,
                    grupo = grupo,
                    creadaPorId = creador
                )
            )
            _estado.update { it.copy(cargando = false, guardado = true) }
        }
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(app)
                return TareaFormViewModel(
                    TareaRepository(db.tareaDao()),
                    SectorRepository(db.sectorDao()),
                    CultivoRepository(db.cultivoDao()),
                    UsuarioRepository(db.usuarioDao())
                ) as T
            }
        }
    }
}