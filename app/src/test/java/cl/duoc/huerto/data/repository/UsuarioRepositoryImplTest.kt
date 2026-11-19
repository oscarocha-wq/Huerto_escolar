package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.UsuarioDao
import cl.duoc.huerto.data.remote.FakeHuertoApi
import cl.duoc.huerto.data.remote.dto.UsuarioDto
import cl.duoc.huerto.domain.model.Perfil
import cl.duoc.huerto.domain.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** Room falso: guarda los usuarios en memoria. */
private class UsuarioDaoEnMemoria(iniciales: List<Usuario> = emptyList()) : UsuarioDao {
    val usuarios = iniciales.associateBy { it.id }.toMutableMap()

    override suspend fun login(id: String, clave: String) =
        usuarios[id]?.takeIf { it.clave == clave && it.clave.isNotEmpty() && it.activo }
    override fun estudiantes(): Flow<List<Usuario>> = emptyFlow()
    override suspend fun insertarTodos(usuarios: List<Usuario>) { usuarios.forEach { this.usuarios[it.id] = it } }
    override suspend fun guardar(usuario: Usuario) { usuarios[usuario.id] = usuario }
}

class UsuarioRepositoryImplTest {

    private val docenteDto = UsuarioDto("ADM-01", "Docente Prueba", Perfil.ADMINISTRADOR, null, true)
    private val docenteLocal = Usuario("ADM-01", "Docente Prueba", "1234", Perfil.ADMINISTRADOR, null)

    @Test fun `login online correcto entra y guarda al usuario en Room`() = runTest {
        val dao = UsuarioDaoEnMemoria()
        val api = FakeHuertoApi(usuarios = listOf(docenteDto), claves = mapOf("ADM-01" to "1234"))
        val repo = UsuarioRepositoryImpl(dao, api)

        val sesion = repo.login(" adm-01 ", "1234")

        assertNotNull(sesion)
        assertEquals("ADM-01", sesion!!.usuarioId)
        assertEquals(Perfil.ADMINISTRADOR, sesion.perfil)
        // Queda guardado con la clave para entrar sin conexión después
        assertEquals("1234", dao.usuarios["ADM-01"]?.clave)
    }

    @Test fun `login online con clave incorrecta no entra aunque exista en Room`() = runTest {
        val dao = UsuarioDaoEnMemoria(listOf(docenteLocal))
        val api = FakeHuertoApi(usuarios = listOf(docenteDto), claves = mapOf("ADM-01" to "1234"))
        val repo = UsuarioRepositoryImpl(dao, api)

        assertNull(repo.login("ADM-01", "9999"))
    }

    @Test fun `sin conexion usa los datos de Room`() = runTest {
        val dao = UsuarioDaoEnMemoria(listOf(docenteLocal))
        val repo = UsuarioRepositoryImpl(dao, FakeHuertoApi(sinConexion = true))

        val sesion = repo.login("ADM-01", "1234")
        assertEquals("ADM-01", sesion?.usuarioId)
    }

    @Test fun `sin conexion y clave incorrecta no entra`() = runTest {
        val dao = UsuarioDaoEnMemoria(listOf(docenteLocal))
        val repo = UsuarioRepositoryImpl(dao, FakeHuertoApi(sinConexion = true))

        assertNull(repo.login("ADM-01", "0000"))
    }

    @Test fun `sin api funciona solo con Room`() = runTest {
        val repo = UsuarioRepositoryImpl(UsuarioDaoEnMemoria(listOf(docenteLocal)))
        assertNotNull(repo.login("adm-01", "1234"))
    }
}
