package cl.duoc.huerto.testutil

import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/*
 * Repositorios falsos en memoria para probar los ViewModels sin Room.
 * Guardan lo que se les pide en listas públicas para poder revisarlo en las pruebas.
 */

class FakeSectorRepository(iniciales: List<Sector> = emptyList()) : SectorRepository {
    val lista = MutableStateFlow(iniciales)
    val guardados = mutableListOf<Sector>()
    val eliminados = mutableListOf<Sector>()
    var duplicado = false
    var dependencias = DependenciasSector()
    var fallaAlEliminar = false

    override fun sectores(): Flow<List<Sector>> = lista
    override fun observar(id: Int): Flow<Sector?> = lista.map { l -> l.firstOrNull { it.id == id } }
    override suspend fun porId(id: Int): Sector? = lista.value.firstOrNull { it.id == id }
    override suspend fun codigoDuplicado(codigo: String, idExcluido: Int): Boolean = duplicado
    override suspend fun guardar(sector: Sector) { guardados += sector }
    override suspend fun dependencias(id: Int): DependenciasSector = dependencias
    override suspend fun eliminar(sector: Sector) {
        if (fallaAlEliminar) throw IllegalStateException("FOREIGN KEY")
        eliminados += sector
    }
}

class FakeCultivoRepository(iniciales: List<Cultivo> = emptyList()) : CultivoRepository {
    val lista = MutableStateFlow(iniciales)
    val guardados = mutableListOf<Cultivo>()
    val eliminados = mutableListOf<Cultivo>()
    var tareas = 0
    var fallaAlEliminar = false

    override fun cultivos(): Flow<List<Cultivo>> = lista
    override fun porSector(sectorId: Int): Flow<List<Cultivo>> = lista.map { l -> l.filter { it.sectorId == sectorId } }
    override suspend fun porId(id: Int): Cultivo? = lista.value.firstOrNull { it.id == id }
    override suspend fun guardar(cultivo: Cultivo) { guardados += cultivo }
    override suspend fun contarTareas(id: Int): Int = tareas
    override suspend fun eliminar(cultivo: Cultivo) {
        if (fallaAlEliminar) throw IllegalStateException("FOREIGN KEY")
        eliminados += cultivo
    }
}

class FakeTareaRepository(iniciales: List<Tarea> = emptyList()) : TareaRepository {
    val lista = MutableStateFlow(iniciales)
    val creadas = mutableListOf<Tarea>()
    val actualizadas = mutableListOf<Tarea>()
    val eliminadas = mutableListOf<Tarea>()
    var fallaAlEliminar = false

    override fun todas(): Flow<List<Tarea>> = lista
    override fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>> =
        lista.map { l -> l.filter { it.asignadoId == usuarioId || (grupo != null && it.grupo == grupo) } }
    override fun observar(id: Int): Flow<Tarea?> = lista.map { l -> l.firstOrNull { it.id == id } }
    override suspend fun porId(id: Int): Tarea? = lista.value.firstOrNull { it.id == id }
    override suspend fun crear(tarea: Tarea) { creadas += tarea }
    override suspend fun actualizar(tarea: Tarea) {
        actualizadas += tarea
        lista.value = lista.value.map { if (it.id == tarea.id) tarea else it }
    }
    override suspend fun eliminar(tarea: Tarea) {
        if (fallaAlEliminar) throw IllegalStateException("FOREIGN KEY")
        eliminadas += tarea
    }
}

class FakeProblemaRepository(iniciales: List<Problema> = emptyList()) : ProblemaRepository {
    val lista = MutableStateFlow(iniciales)
    val creados = mutableListOf<Problema>()
    val actualizados = mutableListOf<Problema>()

    override fun todos(): Flow<List<Problema>> = lista
    override fun deUsuario(usuarioId: String): Flow<List<Problema>> =
        lista.map { l -> l.filter { it.reportadoPorId == usuarioId } }
    override fun observar(id: Int): Flow<Problema?> = lista.map { l -> l.firstOrNull { it.id == id } }
    override suspend fun porId(id: Int): Problema? = lista.value.firstOrNull { it.id == id }
    override suspend fun crear(problema: Problema) { creados += problema }
    override suspend fun actualizar(problema: Problema) {
        actualizados += problema
        lista.value = lista.value.map { if (it.id == problema.id) problema else it }
    }
}

class FakeUsuarioRepository(iniciales: List<Usuario> = emptyList()) : UsuarioRepository {
    val lista = MutableStateFlow(iniciales)
    override suspend fun login(id: String, clave: String): Sesion? = null
    override fun estudiantes(): Flow<List<Usuario>> = lista
}

class FakeSeguimientoRepository(
    evidenciasIniciales: List<Evidencia> = emptyList(),
    validacionesIniciales: List<Validacion> = emptyList()
) : SeguimientoRepository {
    val evidencias = MutableStateFlow(evidenciasIniciales)
    val validaciones = MutableStateFlow(validacionesIniciales)
    val entregadas = mutableListOf<Evidencia>()
    val registradas = mutableListOf<Validacion>()

    override fun evidencias(): Flow<List<Evidencia>> = evidencias
    override fun validaciones(): Flow<List<Validacion>> = validaciones
    override fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>> =
        evidencias.map { l -> l.filter { it.tareaId == tareaId } }
    override fun validacionesDe(tareaId: Int): Flow<List<Validacion>> =
        validaciones.map { l -> l.filter { it.tareaId == tareaId } }
    override suspend fun ultimaEvidencia(tareaId: Int): Evidencia? =
        evidencias.value.filter { it.tareaId == tareaId }.maxByOrNull { it.fecha }
    override suspend fun contarEvidencias(tareaId: Int): Int = evidencias.value.count { it.tareaId == tareaId }
    override suspend fun entregar(evidencia: Evidencia) { entregadas += evidencia }
    override suspend fun registrarValidacion(validacion: Validacion) { registradas += validacion }
}

class FakeLecturaRepository(iniciales: List<LecturaSensor> = emptyList()) : LecturaRepository {
    val lista = MutableStateFlow(iniciales)
    val guardadas = mutableListOf<LecturaSensor>()

    override fun deSector(sectorId: Int): Flow<List<LecturaSensor>> =
        lista.map { l -> l.filter { it.sectorId == sectorId } }
    override suspend fun guardar(lectura: LecturaSensor) { guardadas += lectura }
    override suspend fun borrarSimuladas(sectorId: Int): Int {
        val borrar = lista.value.filter { it.sectorId == sectorId && it.origen == OrigenLectura.SIMULADA }
        lista.value = lista.value - borrar.toSet()
        return borrar.size
    }
}
