package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.dao.ProblemaDao
import cl.duoc.huerto.data.entities.Problema
import kotlinx.coroutines.flow.Flow

class ProblemaRepository(private val dao: ProblemaDao) {
    fun todos(): Flow<List<Problema>> = dao.todos()
    fun deUsuario(usuarioId: String): Flow<List<Problema>> = dao.deUsuario(usuarioId)
    fun observar(id: Int): Flow<Problema?> = dao.observar(id)
    suspend fun porId(id: Int): Problema? = dao.porId(id)
    suspend fun crear(problema: Problema) { dao.insertar(problema) }
    suspend fun actualizar(problema: Problema) = dao.actualizar(problema)
}