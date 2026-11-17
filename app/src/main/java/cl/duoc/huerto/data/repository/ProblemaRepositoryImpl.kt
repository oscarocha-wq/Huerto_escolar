package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.ProblemaDao
import cl.duoc.huerto.domain.model.Problema
import cl.duoc.huerto.domain.repository.ProblemaRepository
import kotlinx.coroutines.flow.Flow

class ProblemaRepositoryImpl(private val dao: ProblemaDao) : ProblemaRepository {
    override fun todos(): Flow<List<Problema>> = dao.todos()
    override fun deUsuario(usuarioId: String): Flow<List<Problema>> = dao.deUsuario(usuarioId)
    override fun observar(id: Int): Flow<Problema?> = dao.observar(id)
    override suspend fun porId(id: Int): Problema? = dao.porId(id)
    override suspend fun crear(problema: Problema) { dao.insertar(problema) }
    override suspend fun actualizar(problema: Problema) = dao.actualizar(problema)
}
