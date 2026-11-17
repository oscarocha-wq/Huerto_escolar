package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.LecturaSensor
import kotlinx.coroutines.flow.Flow

/** Contrato de lecturas de sensores (simuladas o manuales). La implementación vive en la capa data. */
interface LecturaRepository {
    fun deSector(sectorId: Int): Flow<List<LecturaSensor>>
    suspend fun guardar(lectura: LecturaSensor)
    suspend fun borrarSimuladas(sectorId: Int): Int
}
