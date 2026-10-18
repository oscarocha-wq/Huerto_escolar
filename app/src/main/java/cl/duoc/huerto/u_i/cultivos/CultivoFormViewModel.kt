package cl.duoc.huerto.u_i.cultivos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.repository.CultivoRepository
import cl.duoc.huerto.data.repository.SectorRepository
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val NUEVO_CULTIVO = -1

data class CultivoFormState(
    val nombre: String = "",
    val descripcion: String = "",
    val sectorId: Int? = null,
    val fechaSiembra: Long? = null,
    val fechaTrasplante: Long? = null,
    val fechaCosecha: Long? = null,
    val frecuencia: String = "",
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,
    val observaciones: String = "",
    val sectores: List<Sector> = emptyList(),
    val errores: ErroresCultivo = ErroresCultivo(),
    val errorGeneral: String? = null,
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardado: Boolean = false
)

class CultivoFormViewModel(
    private val repo: CultivoRepository,
    sectorRepo: SectorRepository,
    private val cultivoId: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(
        CultivoFormState(
            esEdicion = cultivoId != NUEVO_CULTIVO,
            cargando = cultivoId != NUEVO_CULTIVO
        )
    )
    val estado: StateFlow<CultivoFormState> = _estado.asStateFlow()

    private var original: Cultivo? = null

    init {
        // Lista de sectores para el selector
        viewModelScope.launch {
            sectorRepo.sectores().collect { lista ->
                _estado.update { it.copy(sectores = lista) }
            }
        }
        if (cultivoId != NUEVO_CULTIVO) {
            viewModelScope.launch {
                val c = repo.porId(cultivoId)
                if (c == null) {
                    _estado.update { it.copy(cargando = false, errorGeneral = "No se encontró el cultivo") }
                } else {
                    original = c
                    _estado.update {
                        it.copy(
                            nombre = c.nombre,
                            descripcion = c.descripcion,
                            sectorId = c.sectorId,
                            fechaSiembra = c.fechaSiembra,
                            fechaTrasplante = c.fechaTrasplante,
                            fechaCosecha = c.fechaCosechaEstimada,
                            frecuencia = c.frecuenciaRiegoDias.toString(),
                            estado = c.estado,
                            observaciones = c.observaciones,
                            cargando = false
                        )
                    }
                }
            }
        }
    }

    fun onNombre(v: String) = _estado.update { it.copy(nombre = v, errores = it.errores.copy(nombre = null)) }
    fun onDescripcion(v: String) = _estado.update { it.copy(descripcion = v) }
    fun onSector(s: Sector) = _estado.update { it.copy(sectorId = s.id, errores = it.errores.copy(sector = null)) }
    fun onFechaSiembra(v: Long?) = _estado.update { it.copy(fechaSiembra = v, errores = it.errores.copy(trasplante = null, cosecha = null)) }
    fun onFechaTrasplante(v: Long?) = _estado.update { it.copy(fechaTrasplante = v, errores = it.errores.copy(trasplante = null)) }
    fun onFechaCosecha(v: Long?) = _estado.update { it.copy(fechaCosecha = v, errores = it.errores.copy(cosecha = null)) }
    fun onFrecuencia(v: String) = _estado.update {
        it.copy(frecuencia = v.filter(Char::isDigit).take(2), errores = it.errores.copy(frecuencia = null))
    }
    fun onEstado(v: EstadoCultivo) = _estado.update { it.copy(estado = v) }
    fun onObservaciones(v: String) = _estado.update { it.copy(observaciones = v) }

    fun guardar() {
        // Control de acceso real
        val sesion = SesionManager.sesion.value
        if (!sesion.puedeAcceder(Perfil.ADMINISTRADOR)) {
            _estado.update { it.copy(errorGeneral = "No tienes permiso para administrar cultivos") }
            return
        }

        val a = _estado.value
        val errores = validarCultivo(
            a.nombre, a.sectorId, a.frecuencia, a.fechaSiembra, a.fechaTrasplante, a.fechaCosecha
        )
        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }
        val sectorId = a.sectorId ?: return
        val frecuencia = a.frecuencia.toIntOrNull() ?: return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val base = original
            repo.guardar(
                Cultivo(
                    id = base?.id ?: 0,
                    nombre = a.nombre.trim(),
                    descripcion = a.descripcion.trim(),
                    sectorId = sectorId,
                    fechaSiembra = a.fechaSiembra,
                    fechaTrasplante = a.fechaTrasplante,
                    fechaCosechaEstimada = a.fechaCosecha,
                    frecuenciaRiegoDias = frecuencia,
                    estado = a.estado,
                    observaciones = a.observaciones.trim(),
                    fotoUri = base?.fotoUri
                )
            )
            _estado.update { it.copy(cargando = false, guardado = true) }
        }
    }

    companion object {
        fun factory(app: Application, cultivoId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(app)
                return CultivoFormViewModel(
                    CultivoRepository(db.cultivoDao()),
                    SectorRepository(db.sectorDao()),
                    cultivoId
                ) as T
            }
        }
    }
}