package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.*
import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.testutil.Datos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/*
 * Pruebas de la capa data: cada repositorio debe delegar correctamente en su DAO.
 * Los DAO son falsos (en memoria) y anotan qué método se llamó.
 */

private class FakeSectorDao : SectorDao {
    val llamadas = mutableListOf<String>()
    var porCodigo = 0
    override fun todos(): Flow<List<Sector>> = flowOf(listOf(Datos.sector(1)))
    override suspend fun porId(id: Int): Sector? = Datos.sector(id)
    override fun observar(id: Int): Flow<Sector?> = flowOf(Datos.sector(id))
    override suspend fun contarPorCodigo(codigo: String, idExcluido: Int): Int = porCodigo
    override suspend fun contarCultivos(sectorId: Int) = 1
    override suspend fun contarTareas(sectorId: Int) = 2
    override suspend fun contarProblemas(sectorId: Int) = 3
    override suspend fun insertar(sector: Sector): Long { llamadas += "insertar"; return 1 }
    override suspend fun insertarTodos(sectores: List<Sector>) { llamadas += "insertarTodos" }
    override suspend fun actualizar(sector: Sector) { llamadas += "actualizar" }
    override suspend fun eliminar(sector: Sector) { llamadas += "eliminar" }
}

private class FakeCultivoDao : CultivoDao {
    val llamadas = mutableListOf<String>()
    override fun todos(): Flow<List<Cultivo>> = flowOf(listOf(Datos.cultivo(1), Datos.cultivo(2)))
    override fun porSector(sectorId: Int): Flow<List<Cultivo>> = flowOf(listOf(Datos.cultivo(1, sectorId = sectorId)))
    override suspend fun porId(id: Int): Cultivo? = Datos.cultivo(id)
    override suspend fun contarTareas(cultivoId: Int) = 4
    override suspend fun insertar(cultivo: Cultivo): Long { llamadas += "insertar"; return 1 }
    override suspend fun insertarTodos(cultivos: List<Cultivo>) { llamadas += "insertarTodos" }
    override suspend fun actualizar(cultivo: Cultivo) { llamadas += "actualizar" }
    override suspend fun eliminar(cultivo: Cultivo) { llamadas += "eliminar" }
}

private class FakeTareaDao : TareaDao {
    val llamadas = mutableListOf<String>()
    override fun todas(): Flow<List<Tarea>> = flowOf(listOf(Datos.tarea(1)))
    override fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>> =
        flowOf(listOf(Datos.tarea(2, asignadoId = usuarioId)))
    override fun observar(id: Int): Flow<Tarea?> = flowOf(Datos.tarea(id))
    override suspend fun porId(id: Int): Tarea? = Datos.tarea(id)
    override suspend fun insertar(tarea: Tarea): Long { llamadas += "insertar"; return 1 }
    override suspend fun actualizar(tarea: Tarea) { llamadas += "actualizar" }
    override suspend fun eliminar(tarea: Tarea) { llamadas += "eliminar" }
}

private class FakeProblemaDao : ProblemaDao {
    val llamadas = mutableListOf<String>()
    override fun todos(): Flow<List<Problema>> = flowOf(listOf(Datos.problema(1)))
    override fun deUsuario(usuarioId: String): Flow<List<Problema>> =
        flowOf(listOf(Datos.problema(2, reportadoPorId = usuarioId)))
    override fun observar(id: Int): Flow<Problema?> = flowOf(Datos.problema(id))
    override suspend fun porId(id: Int): Problema? = Datos.problema(id)
    override suspend fun insertar(problema: Problema): Long { llamadas += "insertar"; return 1 }
    override suspend fun actualizar(problema: Problema) { llamadas += "actualizar" }
}

private class FakeLecturaDao : LecturaDao {
    val llamadas = mutableListOf<String>()
    override fun deSector(sectorId: Int): Flow<List<LecturaSensor>> = flowOf(emptyList())
    override suspend fun insertar(lectura: LecturaSensor): Long { llamadas += "insertar"; return 1 }
    override suspend fun borrarSimuladas(sectorId: Int): Int = 5
}

/** SeguimientoDao es una clase abstracta: se prueba su lógica de transacción real. */
private class FakeSeguimientoDao : SeguimientoDao() {
    val evidencias = mutableListOf<Evidencia>()
    val validaciones = mutableListOf<Validacion>()
    val estados = mutableListOf<Pair<Int, String>>()
    override suspend fun insertarEvidencia(evidencia: Evidencia): Long { evidencias += evidencia; return 1 }
    override suspend fun insertarValidacion(validacion: Validacion): Long { validaciones += validacion; return 1 }
    override suspend fun cambiarEstadoTarea(tareaId: Int, estado: String) { estados += tareaId to estado }
    override fun evidencias(): Flow<List<Evidencia>> = flowOf(evidencias.toList())
    override fun validaciones(): Flow<List<Validacion>> = flowOf(validaciones.toList())
    override fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>> = flowOf(evidencias.filter { it.tareaId == tareaId })
    override fun validacionesDe(tareaId: Int): Flow<List<Validacion>> = flowOf(validaciones.filter { it.tareaId == tareaId })
    override suspend fun ultimaEvidencia(tareaId: Int): Evidencia? = evidencias.lastOrNull { it.tareaId == tareaId }
    override suspend fun contarEvidencias(tareaId: Int): Int = evidencias.count { it.tareaId == tareaId }
}

class RepositoriosImplTest {

    @Test fun `sector repository`() = runTest {
        val dao = FakeSectorDao()
        val repo = SectorRepositoryImpl(dao)

        assertEquals(1, repo.sectores().first().size)
        assertEquals(3, repo.observar(3).first()?.id)
        assertEquals(2, repo.porId(2)?.id)
        assertFalse(repo.codigoDuplicado("S-01", 0))
        dao.porCodigo = 1
        assertTrue(repo.codigoDuplicado("S-01", 0))
        assertEquals(DependenciasSector(1, 2, 3), repo.dependencias(1))

        repo.guardar(Datos.sector(0))     // id 0 = nuevo
        repo.guardar(Datos.sector(5))     // id existente = actualizar
        repo.eliminar(Datos.sector(5))
        assertEquals(listOf("insertar", "actualizar", "eliminar"), dao.llamadas)
    }

    @Test fun `cultivo repository`() = runTest {
        val dao = FakeCultivoDao()
        val repo = CultivoRepositoryImpl(dao)

        assertEquals(2, repo.cultivos().first().size)
        assertEquals(3, repo.porSector(3).first().single().sectorId)
        assertEquals(7, repo.porId(7)?.id)
        assertEquals(4, repo.contarTareas(1))

        repo.guardar(Datos.cultivo(0))
        repo.guardar(Datos.cultivo(5))
        repo.eliminar(Datos.cultivo(5))
        assertEquals(listOf("insertar", "actualizar", "eliminar"), dao.llamadas)
    }

    @Test fun `tarea repository`() = runTest {
        val dao = FakeTareaDao()
        val repo = TareaRepositoryImpl(dao)

        assertEquals(1, repo.todas().first().size)
        assertEquals("EST-03", repo.paraEstudiante("EST-03", "Taller A").first().single().asignadoId)
        assertEquals(4, repo.observar(4).first()?.id)
        assertEquals(6, repo.porId(6)?.id)

        repo.crear(Datos.tarea(0))
        repo.actualizar(Datos.tarea(1))
        repo.eliminar(Datos.tarea(1))
        assertEquals(listOf("insertar", "actualizar", "eliminar"), dao.llamadas)
    }

    @Test fun `problema repository`() = runTest {
        val dao = FakeProblemaDao()
        val repo = ProblemaRepositoryImpl(dao)

        assertEquals(1, repo.todos().first().size)
        assertEquals("EST-02", repo.deUsuario("EST-02").first().single().reportadoPorId)
        assertEquals(3, repo.observar(3).first()?.id)
        assertEquals(4, repo.porId(4)?.id)

        repo.crear(Datos.problema(0))
        repo.actualizar(Datos.problema(1))
        assertEquals(listOf("insertar", "actualizar"), dao.llamadas)
    }

    @Test fun `lectura repository`() = runTest {
        val dao = FakeLecturaDao()
        val repo = LecturaRepositoryImpl(dao)

        assertTrue(repo.deSector(1).first().isEmpty())
        repo.guardar(LecturaSensor(sectorId = 1, fecha = 1L, humedadSuelo = 1.0, temperatura = 1.0, humedadAmbiente = 1.0, origen = OrigenLectura.MANUAL))
        assertEquals(listOf("insertar"), dao.llamadas)
        assertEquals(5, repo.borrarSimuladas(1))
    }

    @Test fun `entregar guarda la evidencia y deja la tarea pendiente de validacion`() = runTest {
        val dao = FakeSeguimientoDao()
        val repo = SeguimientoRepositoryImpl(dao)

        repo.entregar(Datos.evidencia(id = 1, tareaId = 8))

        assertEquals(1, repo.contarEvidencias(8))
        assertEquals(1, repo.ultimaEvidencia(8)?.id)
        assertEquals(1, repo.evidencias().first().size)
        assertEquals(1, repo.evidenciasDe(8).first().size)
        assertEquals(8 to EstadoTarea.PENDIENTE_DE_VALIDACION.name, dao.estados.single())
    }

    @Test fun `validar aprueba o rechaza la tarea`() = runTest {
        val dao = FakeSeguimientoDao()
        val repo = SeguimientoRepositoryImpl(dao)
        fun validacion(resultado: ResultadoValidacion) = Validacion(
            tareaId = 8, evidenciaId = 1, validadorId = "ADM-01", resultado = resultado, comentario = null, fecha = 1L
        )

        repo.registrarValidacion(validacion(ResultadoValidacion.APROBADA))
        repo.registrarValidacion(validacion(ResultadoValidacion.RECHAZADA))

        assertEquals(
            listOf(8 to EstadoTarea.VALIDADA.name, 8 to EstadoTarea.RECHAZADA.name),
            dao.estados
        )
        assertEquals(2, repo.validaciones().first().size)
        assertEquals(2, repo.validacionesDe(8).first().size)
    }
}
