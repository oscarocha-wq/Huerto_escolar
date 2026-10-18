package cl.duoc.huerto.u_i.problemas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.entities.Problema
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.repository.*
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProblemaFormState(
    val tipo: TipoProblema? = null,
    val descripcion: String = "",
    val sectorId: Int? = null,
    val cultivoId: Int? = null,
    val fotoRuta: String? = null,
    val sectores: List<Sector> = emptyList(),
    val cultivos: List<Cultivo> = emptyList(),
    val errores: ErroresProblema = ErroresProblema(),
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val guardado: Boolean = false
)

class ProblemaFormViewModel(
    private val repo: ProblemaRepository,
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ProblemaFormState())
    val estado: StateFlow<ProblemaFormState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            sectorRepo.sectores().collect { l -> _estado.update { it.copy(sectores = l) } }
        }
        viewModelScope.launch {
            cultivoRepo.cultivos().collect { l -> _estado.update { it.copy(cultivos = l) } }
        }
    }

    fun onTipo(v: TipoProblema) = _estado.update { it.copy(tipo = v, errores = it.errores.copy(tipo = null)) }
    fun onDescripcion(v: String) = _estado.update {
        it.copy(
            descripcion = v.take(MAX_DESCRIPCION_PROBLEMA),
            errores = it.errores.copy(descripcion = null)
        )
    }
    fun onSector(s: Sector) = _estado.update {
        it.copy(sectorId = s.id, cultivoId = null, errores = it.errores.copy(sector = null))
    }
    fun onCultivo(c: Cultivo?) = _estado.update { it.copy(cultivoId = c?.id) }

    fun onFoto(ruta: String?) = _estado.update {
        if (ruta == null) it.copy(errorGeneral = "No se pudo cargar la imagen")
        else it.copy(fotoRuta = ruta, errorGeneral = null)
    }
    fun onErrorFoto(mensaje: String) = _estado.update { it.copy(errorGeneral = mensaje) }

    fun quitarFoto() = _estado.update { it.copy(fotoRuta = null) }

    fun guardar() {
        // Control de acceso real
        val sesion = SesionManager.sesion.value
        if (!puedeReportar(sesion)) {
            _estado.update { it.copy(errorGeneral = "Solo los estudiantes pueden reportar problemas") }
            return
        }

        val a = _estado.value
        val errores = validarProblema(a.tipo, a.descripcion, a.sectorId)
        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }
        val tipo = a.tipo ?: return
        val sectorId = a.sectorId ?: return
        val usuarioId = sesion?.usuarioId ?: return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            repo.crear(
                Problema(
                    tipo = tipo,
                    descripcion = a.descripcion.trim(),
                    sectorId = sectorId,
                    cultivoId = a.cultivoId,
                    reportadoPorId = usuarioId,
                    fechaReporte = System.currentTimeMillis(),
                    fotoRuta = a.fotoRuta
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
                return ProblemaFormViewModel(
                    ProblemaRepository(db.problemaDao()),
                    SectorRepository(db.sectorDao()),
                    CultivoRepository(db.cultivoDao())
                ) as T
            }
        }
    }
}