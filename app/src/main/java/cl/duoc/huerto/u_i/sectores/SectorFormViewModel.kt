package cl.duoc.huerto.u_i.sectores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.repository.SectorRepository
import cl.duoc.huerto.domain.ErroresSector
import cl.duoc.huerto.domain.EstadoSector
import cl.duoc.huerto.domain.Perfil
import cl.duoc.huerto.domain.puedeAcceder
import cl.duoc.huerto.domain.validarSector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val NUEVO_SECTOR = -1

data class SectorFormState(
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: EstadoSector = EstadoSector.DISPONIBLE,
    val errores: ErroresSector = ErroresSector(),
    val errorGeneral: String? = null,
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardado: Boolean = false
)

class SectorFormViewModel(
    private val repo: SectorRepository,
    private val sectorId: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(
        SectorFormState(
            esEdicion = sectorId != NUEVO_SECTOR,
            cargando = sectorId != NUEVO_SECTOR
        )
    )
    val estado: StateFlow<SectorFormState> = _estado.asStateFlow()

    private var original: Sector? = null

    init {
        if (sectorId != NUEVO_SECTOR) {
            viewModelScope.launch {
                val sector = repo.porId(sectorId)
                if (sector == null) {
                    _estado.update { it.copy(cargando = false, errorGeneral = "No se encontró el sector") }
                } else {
                    original = sector
                    _estado.update {
                        it.copy(
                            codigo = sector.codigo,
                            nombre = sector.nombre,
                            descripcion = sector.descripcion,
                            ubicacion = sector.ubicacion,
                            estado = sector.estado,
                            cargando = false
                        )
                    }
                }
            }
        }
    }

    fun onCodigo(v: String) = _estado.update { it.copy(codigo = v, errores = it.errores.copy(codigo = null)) }
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v, errores = it.errores.copy(nombre = null)) }
    fun onDescripcion(v: String) = _estado.update { it.copy(descripcion = v) }
    fun onUbicacion(v: String) = _estado.update { it.copy(ubicacion = v) }
    fun onEstado(v: EstadoSector) = _estado.update { it.copy(estado = v) }

    fun guardar() {
        // Control de acceso real: no basta con ocultar botones
        val sesion = SesionManager.sesion.value
        if (!sesion.puedeAcceder(Perfil.ADMINISTRADOR)) {
            _estado.update { it.copy(errorGeneral = "No tienes permiso para administrar sectores") }
            return
        }

        val actual = _estado.value
        val errores = validarSector(actual.codigo, actual.nombre)
        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val codigo = actual.codigo.trim().uppercase()
            val idExcluido = original?.id ?: 0

            if (repo.codigoDuplicado(codigo, idExcluido)) {
                _estado.update {
                    it.copy(cargando = false, errores = ErroresSector(codigo = "Ya existe un sector con ese código"))
                }
                return@launch
            }

            val ahora = System.currentTimeMillis()
            val base = original
            repo.guardar(
                Sector(
                    id = base?.id ?: 0,
                    codigo = codigo,
                    nombre = actual.nombre.trim(),
                    descripcion = actual.descripcion.trim(),
                    ubicacion = actual.ubicacion.trim(),
                    estado = actual.estado,
                    responsableId = base?.responsableId ?: sesion?.usuarioId,
                    fechaCreacion = base?.fechaCreacion ?: ahora,
                    fechaActualizacion = ahora
                )
            )
            _estado.update { it.copy(cargando = false, guardado = true) }
        }
    }

    companion object {
        fun factory(app: Application, sectorId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SectorFormViewModel(
                    SectorRepository(AppDatabase.get(app).sectorDao()),
                    sectorId
                ) as T
        }
    }
}