package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.*
import cl.duoc.huerto.data.remote.FakeHuertoApi
import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.testutil.Datos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/*
 * CRUD contra el microservicio: cada cambio se guarda en Room Y se envía a huertoapi.
 * Si el servidor no responde, el cambio queda igual en Room (la app no se cae).
 */

private class SectorDaoMem(iniciales: List<Sector> = emptyList()) : SectorDao {
    val filas = iniciales.associateBy { it.id }.toMutableMap()
    override fun todos(): Flow<List<Sector>> = flowOf(filas.values.toList())
    override suspend fun porId(id: Int): Sector? = filas[id]
    override fun observar(id: Int): Flow<Sector?> = flowOf(filas[id])
    override suspend fun contarPorCodigo(codigo: String, idExcluido: Int) = 0
    override suspend fun contarCultivos(sectorId: Int) = 0
    override suspend fun contarTareas(sectorId: Int) = 0
    override suspend fun contarProblemas(sectorId: Int) = 0
    override suspend fun insertar(sector: Sector): Long {
        val id = if (sector.id == 0) (filas.keys.maxOrNull() ?: 0) + 1 else sector.id
        filas[id] = sector.copy(id = id); return id.toLong()
    }
    override suspend fun insertarTodos(sectores: List<Sector>) { sectores.forEach { insertar(it) } }
    override suspend fun actualizar(sector: Sector) { filas[sector.id] = sector }
    override suspend fun eliminar(sector: Sector) { filas.remove(sector.id) }
}

private class CultivoDaoMem : CultivoDao {
    val filas = mutableMapOf<Int, Cultivo>()
    override fun todos(): Flow<List<Cultivo>> = flowOf(filas.values.toList())
    override fun porSector(sectorId: Int): Flow<List<Cultivo>> = flowOf(filas.values.filter { it.sectorId == sectorId })
    override suspend fun porId(id: Int): Cultivo? = filas[id]
    override suspend fun contarTareas(cultivoId: Int) = 0
    override suspend fun insertar(cultivo: Cultivo): Long {
        val id = if (cultivo.id == 0) (filas.keys.maxOrNull() ?: 0) + 1 else cultivo.id
        filas[id] = cultivo.copy(id = id); return id.toLong()
    }
    override suspend fun insertarTodos(cultivos: List<Cultivo>) { cultivos.forEach { insertar(it) } }
    override suspend fun actualizar(cultivo: Cultivo) { filas[cultivo.id] = cultivo }
    override suspend fun eliminar(cultivo: Cultivo) { filas.remove(cultivo.id) }
}

private class TareaDaoMem : TareaDao {
    val filas = mutableMapOf<Int, Tarea>()
    override fun todas(): Flow<List<Tarea>> = flowOf(filas.values.toList())
    override fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>> = flowOf(emptyList())
    override fun observar(id: Int): Flow<Tarea?> = flowOf(filas[id])
    override suspend fun porId(id: Int): Tarea? = filas[id]
    override suspend fun insertar(tarea: Tarea): Long {
        val id = if (tarea.id == 0) (filas.keys.maxOrNull() ?: 0) + 1 else tarea.id
        filas[id] = tarea.copy(id = id); return id.toLong()
    }
    override suspend fun actualizar(tarea: Tarea) { filas[tarea.id] = tarea }
    override suspend fun eliminar(tarea: Tarea) { filas.remove(tarea.id) }
}

private class ProblemaDaoMem : ProblemaDao {
    val filas = mutableMapOf<Int, Problema>()
    override fun todos(): Flow<List<Problema>> = flowOf(filas.values.toList())
    override fun deUsuario(usuarioId: String): Flow<List<Problema>> = flowOf(emptyList())
    override fun observar(id: Int): Flow<Problema?> = flowOf(filas[id])
    override suspend fun porId(id: Int): Problema? = filas[id]
    override suspend fun insertar(problema: Problema): Long {
        val id = if (problema.id == 0) (filas.keys.maxOrNull() ?: 0) + 1 else problema.id
        filas[id] = problema.copy(id = id); return id.toLong()
    }
    override suspend fun actualizar(problema: Problema) { filas[problema.id] = problema }
}

private class SeguimientoDaoMem : SeguimientoDao() {
    override suspend fun insertarEvidencia(evidencia: Evidencia): Long = 1
    override suspend fun insertarValidacion(validacion: Validacion): Long = 1
    override suspend fun cambiarEstadoTarea(tareaId: Int, estado: String) {}
    override fun evidencias(): Flow<List<Evidencia>> = flowOf(emptyList())
    override fun validaciones(): Flow<List<Validacion>> = flowOf(emptyList())
    override fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>> = flowOf(emptyList())
    override fun validacionesDe(tareaId: Int): Flow<List<Validacion>> = flowOf(emptyList())
    override suspend fun ultimaEvidencia(tareaId: Int): Evidencia? = null
    override suspend fun contarEvidencias(tareaId: Int): Int = 0
}

class RepositoriosRemotosTest {

    // ---------- Sectores ----------

    @Test fun `crear sector lo envia al servidor y usa el id que devuelve`() = runTest {
        val dao = SectorDaoMem(); val api = FakeHuertoApi()
        SectorRepositoryImpl(dao, api).guardar(Datos.sector(0, codigo = "S-09"))

        assertEquals(listOf("POST sectores"), api.llamadas)
        assertEquals("S-09", dao.filas[100]?.codigo)
    }

    @Test fun `sin servidor el sector se guarda igual en Room`() = runTest {
        val dao = SectorDaoMem(); val api = FakeHuertoApi(sinConexion = true)
        SectorRepositoryImpl(dao, api).guardar(Datos.sector(0, codigo = "S-09"))

        assertEquals(1, dao.filas.size)
        assertEquals("S-09", dao.filas[1]?.codigo)
    }

    @Test fun `si el id del servidor ya existe en el telefono se usa el id local`() = runTest {
        val dao = SectorDaoMem(listOf(Datos.sector(100, codigo = "S-77")))
        SectorRepositoryImpl(dao, FakeHuertoApi()).guardar(Datos.sector(0, codigo = "S-09"))

        assertEquals("S-77", dao.filas[100]?.codigo)
        assertEquals("S-09", dao.filas[101]?.codigo)
    }

    @Test fun `editar y eliminar sector llaman a PUT y DELETE`() = runTest {
        val dao = SectorDaoMem(listOf(Datos.sector(3))); val api = FakeHuertoApi()
        val repo = SectorRepositoryImpl(dao, api)

        repo.guardar(Datos.sector(3, nombre = "Editado"))
        repo.eliminar(Datos.sector(3))

        assertEquals(listOf("PUT sectores/3", "DELETE sectores/3"), api.llamadas)
        assertTrue(dao.filas.isEmpty())
    }

    @Test fun `sin api el repositorio solo usa Room`() = runTest {
        val dao = SectorDaoMem()
        SectorRepositoryImpl(dao).guardar(Datos.sector(0))
        assertEquals(1, dao.filas.size)
    }

    // ---------- Cultivos ----------

    @Test fun `CRUD de cultivos contra el servidor conserva la foto local`() = runTest {
        val dao = CultivoDaoMem(); val api = FakeHuertoApi()
        val repo = CultivoRepositoryImpl(dao, api)

        repo.guardar(Datos.cultivo(0))
        assertEquals("foto.jpg", dao.filas[100]?.fotoUri)

        repo.guardar(dao.filas[100]!!.copy(nombre = "Lechuga crespa"))
        repo.eliminar(dao.filas[100]!!)

        assertEquals(listOf("POST cultivos", "PUT cultivos/100", "DELETE cultivos/100"), api.llamadas)
    }

    @Test fun `cultivo sin servidor queda en Room`() = runTest {
        val dao = CultivoDaoMem()
        CultivoRepositoryImpl(dao, FakeHuertoApi(sinConexion = true)).guardar(Datos.cultivo(0))
        assertEquals(1, dao.filas.size)
    }

    // ---------- Tareas ----------

    @Test fun `tareas se crean editan cambian de estado y eliminan en el servidor`() = runTest {
        val dao = TareaDaoMem(); val api = FakeHuertoApi()
        val repo = TareaRepositoryImpl(dao, api)

        repo.crear(Datos.tarea(0))
        val creada = dao.filas[100]!!
        repo.actualizar(creada.copy(estado = EstadoTarea.EN_DESARROLLO))
        repo.eliminar(creada)

        assertEquals(
            listOf("POST tareas", "PUT tareas/100", "PATCH tareas/100/estado EN_DESARROLLO", "DELETE tareas/100"),
            api.llamadas
        )
    }

    @Test fun `tarea sin servidor queda en Room`() = runTest {
        val dao = TareaDaoMem()
        TareaRepositoryImpl(dao, FakeHuertoApi(sinConexion = true)).crear(Datos.tarea(0))
        assertEquals(1, dao.filas.size)
    }

    // ---------- Problemas ----------

    @Test fun `problemas se reportan y atienden en el servidor`() = runTest {
        val dao = ProblemaDaoMem(); val api = FakeHuertoApi()
        val repo = ProblemaRepositoryImpl(dao, api)

        repo.crear(Datos.problema(0).copy(fotoRuta = "evidencia.jpg"))
        assertEquals("evidencia.jpg", dao.filas[100]?.fotoRuta)

        repo.actualizar(dao.filas[100]!!.copy(estado = EstadoProblema.EN_REVISION, atendidoPorId = "ADM-01"))

        assertEquals(listOf("POST problemas", "PATCH problemas/100/estado EN_REVISION"), api.llamadas)
        assertEquals(EstadoProblema.EN_REVISION, dao.filas[100]?.estado)
    }

    @Test fun `problema sin servidor queda en Room`() = runTest {
        val dao = ProblemaDaoMem()
        ProblemaRepositoryImpl(dao, FakeHuertoApi(sinConexion = true)).crear(Datos.problema(0))
        assertEquals(1, dao.filas.size)
    }

    // ---------- Seguimiento (entregar / validar) ----------

    @Test fun `entregar y validar avisan el nuevo estado de la tarea al servidor`() = runTest {
        val api = FakeHuertoApi()
        val repo = SeguimientoRepositoryImpl(SeguimientoDaoMem(), api)
        fun validacion(r: ResultadoValidacion) =
            Validacion(tareaId = 5, evidenciaId = 1, validadorId = "ADM-01", resultado = r, comentario = null, fecha = 1L)

        repo.entregar(Datos.evidencia(tareaId = 5))
        repo.registrarValidacion(validacion(ResultadoValidacion.APROBADA))
        repo.registrarValidacion(validacion(ResultadoValidacion.RECHAZADA))

        assertEquals(
            listOf(
                "PATCH tareas/5/estado PENDIENTE_DE_VALIDACION",
                "PATCH tareas/5/estado VALIDADA",
                "PATCH tareas/5/estado RECHAZADA"
            ),
            api.llamadas
        )
    }
}
