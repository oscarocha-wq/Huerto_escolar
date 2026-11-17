package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.Cultivo
import kotlinx.coroutines.flow.Flow

/** Contrato de acceso a cultivos. La implementación vive en la capa data. */
interface CultivoRepository {
    fun cultivos(): Flow<List<Cultivo>>
    fun porSector(sectorId: Int): Flow<List<Cultivo>>
    suspend fun porId(id: Int): Cultivo?
    suspend fun guardar(cultivo: Cultivo)
    suspend fun contarTareas(id: Int): Int
    suspend fun eliminar(cultivo: Cultivo)
}
