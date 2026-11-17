package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.DependenciasSector
import cl.duoc.huerto.domain.model.Sector
import kotlinx.coroutines.flow.Flow

/** Contrato de acceso a sectores. La implementación vive en la capa data. */
interface SectorRepository {
    fun sectores(): Flow<List<Sector>>
    fun observar(id: Int): Flow<Sector?>
    suspend fun porId(id: Int): Sector?
    suspend fun codigoDuplicado(codigo: String, idExcluido: Int): Boolean
    suspend fun guardar(sector: Sector)
    suspend fun dependencias(id: Int): DependenciasSector
    suspend fun eliminar(sector: Sector)
}
