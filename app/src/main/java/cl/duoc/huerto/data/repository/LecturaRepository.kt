package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.dao.LecturaDao
import cl.duoc.huerto.data.entities.LecturaSensor
import kotlinx.coroutines.flow.Flow

class LecturaRepository(private val dao: LecturaDao) {
    fun deSector(sectorId: Int): Flow<List<LecturaSensor>> = dao.deSector(sectorId)
    suspend fun guardar(lectura: LecturaSensor) { dao.insertar(lectura) }
    suspend fun borrarSimuladas(sectorId: Int) = dao.borrarSimuladas(sectorId)
}
