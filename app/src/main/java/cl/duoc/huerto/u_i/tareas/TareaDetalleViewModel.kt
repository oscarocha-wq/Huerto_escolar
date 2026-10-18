package cl.duoc.huerto.u_i.tareas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.Tarea
import cl.duoc.huerto.data.entities.Usuario
import cl.duoc.huerto.data.repository.*
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetalleUi(
    val comentario: String = "",
    val fotoRuta: String? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false
)

class TareaDetalleViewModel(
    private val repo: TareaRepository,
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository,
    usuarioRepo: UsuarioRepository,
    private val tareaId: Int
) : ViewModel() {

    private val _ui = MutableStateFlow(DetalleUi())
    val ui: StateFlow<DetalleUi> = _ui.asStateFlow()

    // Solo el administrador necesita los nombres de los estudiantes
    private val estudiantes: Flow<List<Usuario>> =
        if (SesionManager.sesion.value?.esAdministrador == true) usuarioRepo.estudiantes()
        else flowOf(emptyList<Usuario>())

    val item: StateFlow<TareaItem?> = combine(
        repo.observar(tareaId), sectorRepo.sectores(), cultivoRepo.cultivos(), estudiantes
    ) { tarea, sectores, cultivos, est ->
        tarea?.let { construirItems(listOf(it), sectores, cultivos, est).firstOrNull() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onComentario(v: String) = _ui.update { it.copy(comentario = v, error = null) }

    fun onFoto(ruta: String?) = _ui.update {
        if (ruta == null) it.copy(error = "No se pudo cargar la imagen")
        else it.copy(fotoRuta = ruta, error = null)
    }
    fun onErrorFoto(mensaje: String) = _ui.update { it.copy(error = mensaje) }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }

    private suspend fun tareaDeEstudiante(): Pair<Sesion, Tarea>? {
        val sesion = SesionManager.sesion.value
        val tarea = repo.porId(tareaId)
        if (sesion == null || tarea == null || sesion.perfil != Perfil.ESTUDIANTE ||
            !estaAsignadaA(sesion, tarea.asignadoId, tarea.grupo)
        ) {
            _ui.update { it.copy(error = "No tienes permiso para trabajar en esta tarea") }
            return null
        }
        return sesion to tarea
    }

    fun iniciar() {
        viewModelScope.launch {
            val (_, tarea) = tareaDeEstudiante() ?: return@launch
            if (!puedeIniciar(tarea.estado)) {
                _ui.update { it.copy(error = "Esta tarea ya fue iniciada") }
                return@launch
            }
            repo.actualizar(tarea.copy(estado = EstadoTarea.EN_DESARROLLO))
            _ui.update { it.copy(mensaje = "Tarea iniciada") }
        }
    }

    fun marcarRealizada() {
        viewModelScope.launch {
            val (sesion, tarea) = tareaDeEstudiante() ?: return@launch
            if (!puedeMarcarRealizada(tarea.estado)) {
                _ui.update { it.copy(error = "Esta tarea no se puede marcar como realizada") }
                return@launch
            }
            val u = _ui.value
            _ui.update { it.copy(procesando = true) }
            // El estudiante nunca deja la tarea "validada": queda pendiente de validación
            repo.actualizar(
                tarea.copy(
                    estado = EstadoTarea.PENDIENTE_DE_VALIDACION,
                    fechaRealizacion = System.currentTimeMillis(),
                    realizadaPorId = sesion.usuarioId,
                    fotoRuta = u.fotoRuta ?: tarea.fotoRuta,
                    comentarioEstudiante = u.comentario.trim().ifBlank { null },
                    validadorId = null,
                    fechaValidacion = null
                )
            )
            _ui.value = DetalleUi(mensaje = "Tarea enviada para validación")
        }
    }

    fun validar() = revisar(aprobar = true)
    fun rechazar() = revisar(aprobar = false)

    private fun revisar(aprobar: Boolean) {
        viewModelScope.launch {
            val sesion = SesionManager.sesion.value
            val tarea = repo.porId(tareaId) ?: return@launch
            if (!puedeRevisar(sesion, tarea.estado, tarea.realizadaPorId)) {
                _ui.update { it.copy(error = "No tienes permiso para revisar esta tarea") }
                return@launch
            }
            val comentario = _ui.value.comentario.trim()
            if (!aprobar && comentario.isBlank()) {
                _ui.update { it.copy(error = "Escribe un comentario para indicar qué debe corregirse") }
                return@launch
            }
            _ui.update { it.copy(procesando = true) }
            repo.actualizar(
                tarea.copy(
                    estado = if (aprobar) EstadoTarea.VALIDADA else EstadoTarea.RECHAZADA,
                    comentarioDocente = comentario.ifBlank { null },
                    validadorId = sesion?.usuarioId,
                    fechaValidacion = System.currentTimeMillis()
                )
            )
            _ui.value = DetalleUi(
                mensaje = if (aprobar) "Tarea validada" else "Tarea rechazada: el estudiante deberá repetirla"
            )
        }
    }

    companion object {
        fun factory(app: Application, tareaId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(app)
                return TareaDetalleViewModel(
                    TareaRepository(db.tareaDao()),
                    SectorRepository(db.sectorDao()),
                    CultivoRepository(db.cultivoDao()),
                    UsuarioRepository(db.usuarioDao()),
                    tareaId
                ) as T
            }
        }
    }
}