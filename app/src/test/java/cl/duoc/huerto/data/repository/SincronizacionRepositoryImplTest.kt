package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.SincronizacionDao
import cl.duoc.huerto.data.remote.FakeHuertoApi
import cl.duoc.huerto.data.remote.dto.*
import cl.duoc.huerto.domain.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** DAO falso: registra lo que se guarda, en el orden en que se guarda. */
private class SincronizacionDaoEnMemoria(
    private val locales: List<Usuario> = emptyList()
) : SincronizacionDao() {
    val orden = mutableListOf<String>()
    var usuarios: List<Usuario> = emptyList()
    var sectores: List<Sector> = emptyList()
    var cultivos: List<Cultivo> = emptyList()
    var tareas: List<Tarea> = emptyList()
    var problemas: List<Problema> = emptyList()

    override suspend fun upsertUsuarios(usuarios: List<Usuario>) { orden += "usuarios"; this.usuarios = usuarios }
    override suspend fun upsertSectores(sectores: List<Sector>) { orden += "sectores"; this.sectores = sectores }
    override suspend fun upsertCultivos(cultivos: List<Cultivo>) { orden += "cultivos"; this.cultivos = cultivos }
    override suspend fun upsertTareas(tareas: List<Tarea>) { orden += "tareas"; this.tareas = tareas }
    override suspend fun upsertProblemas(problemas: List<Problema>) { orden += "problemas"; this.problemas = problemas }
    override suspend fun usuariosLocales(): List<Usuario> = locales
}

class SincronizacionRepositoryImplTest {

    private fun apiConDatos() = FakeHuertoApi(
        usuarios = listOf(
            UsuarioDto("ADM-01", "Docente Prueba", Perfil.ADMINISTRADOR, null, true),
            UsuarioDto("EST-09", "Estudiante Nuevo", Perfil.ESTUDIANTE, "Taller C", true)
        ),
        sectores = listOf(SectorDto(1, "S-01", "Sector Norte", null, null, EstadoSector.EN_USO, "ADM-01", 1L, 1L)),
        cultivos = listOf(
            CultivoDto(1, "Lechuga", "", 1, null, null, null, 2, null, null, null),
            CultivoDto(2, "Sin sector", "", null, null, null, null, 2, null, null, null) // se descarta
        ),
        tareas = listOf(TareaDto(1, TipoTarea.RIEGO, "Regar", 1, 1, 1L, 2L, "EST-09", null, "ADM-01", null)),
        problemas = listOf(ProblemaDto(1, TipoProblema.FALTA_AGUA, "Seco", 1, null, "EST-09", 1L, null, null, null, null, null))
    )

    @Test fun `sincronizar guarda todo en orden de claves foraneas`() = runTest {
        val dao = SincronizacionDaoEnMemoria()
        val resultado = SincronizacionRepositoryImpl(apiConDatos(), dao).sincronizar()

        assertTrue(resultado.isSuccess)
        assertEquals(listOf("usuarios", "sectores", "cultivos", "tareas", "problemas"), dao.orden)

        val r = resultado.getOrThrow()
        assertEquals(2, r.usuarios)
        assertEquals(1, r.sectores)
        assertEquals(1, r.cultivos) // el cultivo sin sector no se guarda
        assertEquals(1, r.tareas)
        assertEquals(1, r.problemas)
        assertEquals(6, r.total)
    }

    @Test fun `conserva la clave local de los usuarios que ya existian`() = runTest {
        val local = Usuario("ADM-01", "Docente Prueba", "1234", Perfil.ADMINISTRADOR, null)
        val dao = SincronizacionDaoEnMemoria(locales = listOf(local))

        SincronizacionRepositoryImpl(apiConDatos(), dao).sincronizar()

        assertEquals("1234", dao.usuarios.first { it.id == "ADM-01" }.clave)
        assertEquals("", dao.usuarios.first { it.id == "EST-09" }.clave)
    }

    @Test fun `sin conexion falla y no guarda nada`() = runTest {
        val dao = SincronizacionDaoEnMemoria()
        val resultado = SincronizacionRepositoryImpl(FakeHuertoApi(sinConexion = true), dao).sincronizar()

        assertTrue(resultado.isFailure)
        assertTrue(dao.orden.isEmpty())
    }
}
