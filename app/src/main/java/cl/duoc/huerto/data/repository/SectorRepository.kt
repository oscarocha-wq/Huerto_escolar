package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.dao.SectorDao
import cl.duoc.huerto.data.entities.Sector
import kotlinx.coroutines.flow.Flow

class SectorRepository(private val dao: SectorDao) {

    fun sectores(): Flow<List<Sector>> = dao.todos()

    suspend fun porId(id: Int): Sector? = dao.porId(id)

    suspend fun codigoDuplicado(codigo: String, idExcluido: Int): Boolean =
        dao.contarPorCodigo(codigo, idExcluido) > 0

    suspend fun guardar(sector: Sector) {
        if (sector.id == 0) dao.insertar(sector) else dao.actualizar(sector)
    }
}