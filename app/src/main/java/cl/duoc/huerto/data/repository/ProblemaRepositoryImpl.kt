package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.ProblemaDao
import cl.duoc.huerto.data.remote.HuertoApi
import cl.duoc.huerto.data.remote.dto.AtencionProblemaDto
import cl.duoc.huerto.data.remote.dto.toDto
import cl.duoc.huerto.data.remote.intentarRemoto
import cl.duoc.huerto.domain.model.Problema
import cl.duoc.huerto.domain.repository.ProblemaRepository
import kotlinx.coroutines.flow.Flow

/** Problemas: se reportan (POST) y se atienden (PATCH estado) también en el microservicio. */
class ProblemaRepositoryImpl(
    private val dao: ProblemaDao,
    private val api: HuertoApi? = null
) : ProblemaRepository {

    override fun todos(): Flow<List<Problema>> = dao.todos()

    override fun deUsuario(usuarioId: String): Flow<List<Problema>> = dao.deUsuario(usuarioId)

    override fun observar(id: Int): Flow<Problema?> = dao.observar(id)

    override suspend fun porId(id: Int): Problema? = dao.porId(id)

    override suspend fun crear(problema: Problema) {
        val delServidor = api?.let { intentarRemoto { it.crearProblema(problema.toDto(conId = false)) } }
            ?.toEntity()?.copy(fotoRuta = problema.fotoRuta)
        val usarServidor = delServidor != null && dao.porId(delServidor.id) == null
        dao.insertar(if (usarServidor) delServidor!! else problema)
    }

    override suspend fun actualizar(problema: Problema) {
        dao.actualizar(problema)
        api?.let {
            intentarRemoto {
                it.atenderProblema(
                    problema.id.toLong(),
                    AtencionProblemaDto(problema.estado, problema.comentarioDocente, problema.atendidoPorId)
                )
            }
        }
    }
}
