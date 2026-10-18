package cl.duoc.huerto.data.dao

import androidx.room.*
import cl.duoc.huerto.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE id = :id AND clave = :clave AND activo = 1")
    suspend fun login(id: String, clave: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE perfil = 'ESTUDIANTE'")
    fun estudiantes(): Flow<List<Usuario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<Usuario>)
}

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectores ORDER BY nombre")
    fun todos(): Flow<List<Sector>>

    @Query("SELECT * FROM sectores WHERE id = :id")
    suspend fun porId(id: Int): Sector?

    @Query("SELECT COUNT(*) FROM sectores WHERE codigo = :codigo AND id != :idExcluido")
    suspend fun contarPorCodigo(codigo: String, idExcluido: Int): Int

    @Insert suspend fun insertar(sector: Sector): Long
    @Insert suspend fun insertarTodos(sectores: List<Sector>)
    @Update suspend fun actualizar(sector: Sector)
}

@Dao
interface CultivoDao {
    @Query("SELECT * FROM cultivos ORDER BY nombre")
    fun todos(): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos WHERE sectorId = :sectorId")
    fun porSector(sectorId: Int): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos WHERE id = :id")
    suspend fun porId(id: Int): Cultivo?

    @Insert suspend fun insertar(cultivo: Cultivo): Long
    @Insert suspend fun insertarTodos(cultivos: List<Cultivo>)
    @Update suspend fun actualizar(cultivo: Cultivo)
}

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas ORDER BY fechaProgramada")
    fun todas(): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE asignadoId = :usuarioId OR grupo = :grupo ORDER BY fechaProgramada")
    fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    fun observar(id: Int): Flow<Tarea?>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun porId(id: Int): Tarea?

    @Insert suspend fun insertar(tarea: Tarea): Long
    @Update suspend fun actualizar(tarea: Tarea)
}

@Dao
interface ProblemaDao {
    @Query("SELECT * FROM problemas ORDER BY fechaReporte DESC")
    fun todos(): Flow<List<Problema>>

    @Query("SELECT * FROM problemas WHERE reportadoPorId = :usuarioId ORDER BY fechaReporte DESC")
    fun deUsuario(usuarioId: String): Flow<List<Problema>>

    @Query("SELECT * FROM problemas WHERE id = :id")
    fun observar(id: Int): Flow<Problema?>

    @Query("SELECT * FROM problemas WHERE id = :id")
    suspend fun porId(id: Int): Problema?

    @Insert suspend fun insertar(problema: Problema): Long
    @Update suspend fun actualizar(problema: Problema)
}