package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.LecturaDao
import cl.duoc.huerto.domain.model.LecturaSensor
import cl.duoc.huerto.domain.repository.LecturaRepository
import kotlinx.coroutines.flow.Flow

class LecturaRepositoryImpl(private val dao: LecturaDao) : LecturaRepository {
    override fun deSector(sectorId: Int): Flow<List<LecturaSensor>> = dao.deSector(sectorId)
    override suspend fun guardar(lectura: LecturaSensor) { dao.insertar(lectura) }
    override suspend fun borrarSimuladas(sectorId: Int): Int = dao.borrarSimuladas(sectorId)
}
