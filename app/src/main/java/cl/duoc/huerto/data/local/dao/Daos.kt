package cl.duoc.huerto.data.local.dao

import androidx.room.*
import cl.duoc.huerto.domain.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    // clave != '' : un usuario bajado del servidor sin clave no puede entrar sin conexión
    @Query("SELECT * FROM usuarios WHERE id = :id AND clave = :clave AND clave != '' AND activo = 1")
    suspend fun login(id: String, clave: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE perfil = 'ESTUDIANTE'")
    fun estudiantes(): Flow<List<Usuario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(usuarios: List<Usuario>)

    /** Inserta o actualiza (sin borrar) al usuario que entró con el servidor. */
    @Upsert
    suspend fun guardar(usuario: Usuario)
}

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectores ORDER BY nombre")
    fun todos(): Flow<List<Sector>>

    @Query("SELECT * FROM sectores WHERE id = :id")
    suspend fun porId(id: Int): Sector?

    @Query("SELECT * FROM sectores WHERE id = :id")
    fun observar(id: Int): Flow<Sector?>

    @Query("SELECT COUNT(*) FROM sectores WHERE codigo = :codigo AND id != :idExcluido")
    suspend fun contarPorCodigo(codigo: String, idExcluido: Int): Int

    @Query("SELECT COUNT(*) FROM cultivos WHERE sectorId = :sectorId")
    suspend fun contarCultivos(sectorId: Int): Int

    @Query("SELECT COUNT(*) FROM tareas WHERE sectorId = :sectorId")
    suspend fun contarTareas(sectorId: Int): Int

    @Query("SELECT COUNT(*) FROM problemas WHERE sectorId = :sectorId")
    suspend fun contarProblemas(sectorId: Int): Int

    @Insert suspend fun insertar(sector: Sector): Long
    @Insert suspend fun insertarTodos(sectores: List<Sector>)
    @Update suspend fun actualizar(sector: Sector)
    @Delete suspend fun eliminar(sector: Sector)
}

@Dao
interface CultivoDao {
    @Query("SELECT * FROM cultivos ORDER BY nombre")
    fun todos(): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos WHERE sectorId = :sectorId")
    fun porSector(sectorId: Int): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos WHERE id = :id")
    suspend fun porId(id: Int): Cultivo?

    @Query("SELECT COUNT(*) FROM tareas WHERE cultivoId = :cultivoId")
    suspend fun contarTareas(cultivoId: Int): Int

    @Insert suspend fun insertar(cultivo: Cultivo): Long
    @Insert suspend fun insertarTodos(cultivos: List<Cultivo>)
    @Update suspend fun actualizar(cultivo: Cultivo)
    @Delete suspend fun eliminar(cultivo: Cultivo)
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
    @Delete suspend fun eliminar(tarea: Tarea)
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

/**
 * Entregas y revisiones de tareas. Cada operación guarda el registro Y cambia el
 * estado de la tarea dentro de una misma transacción: o se hacen las dos, o ninguna.
 */
@Dao
abstract class SeguimientoDao {
    @Insert abstract suspend fun insertarEvidencia(evidencia: Evidencia): Long
    @Insert abstract suspend fun insertarValidacion(validacion: Validacion): Long

    @Query("UPDATE tareas SET estado = :estado WHERE id = :tareaId")
    abstract suspend fun cambiarEstadoTarea(tareaId: Int, estado: String)

    @Query("SELECT * FROM evidencias ORDER BY fecha DESC")
    abstract fun evidencias(): Flow<List<Evidencia>>

    @Query("SELECT * FROM validaciones ORDER BY fecha DESC")
    abstract fun validaciones(): Flow<List<Validacion>>

    @Query("SELECT * FROM evidencias WHERE tareaId = :tareaId ORDER BY fecha DESC, id DESC")
    abstract fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>>

    @Query("SELECT * FROM validaciones WHERE tareaId = :tareaId ORDER BY fecha DESC, id DESC")
    abstract fun validacionesDe(tareaId: Int): Flow<List<Validacion>>

    @Query("SELECT * FROM evidencias WHERE tareaId = :tareaId ORDER BY fecha DESC, id DESC LIMIT 1")
    abstract suspend fun ultimaEvidencia(tareaId: Int): Evidencia?

    @Query("SELECT COUNT(*) FROM evidencias WHERE tareaId = :tareaId")
    abstract suspend fun contarEvidencias(tareaId: Int): Int

    /** El estudiante entrega: nueva evidencia y la tarea pasa a "Pendiente de validación". */
    @Transaction
    open suspend fun entregar(evidencia: Evidencia) {
        insertarEvidencia(evidencia)
        cambiarEstadoTarea(evidencia.tareaId, EstadoTarea.PENDIENTE_DE_VALIDACION.name)
    }

    /** El docente revisa: nueva validación y la tarea pasa a "Validada" o "Rechazada". */
    @Transaction
    open suspend fun registrarValidacion(validacion: Validacion) {
        insertarValidacion(validacion)
        val nuevo = if (validacion.resultado == ResultadoValidacion.APROBADA) EstadoTarea.VALIDADA
                    else EstadoTarea.RECHAZADA
        cambiarEstadoTarea(validacion.tareaId, nuevo.name)
    }
}

/**
 * Guarda en Room lo que llega del backend (huertoapi).
 * Usa @Upsert (insertar o actualizar) y NO REPLACE: REPLACE borra la fila y la vuelve
 * a crear, y eso activaría el ON DELETE CASCADE, borrando evidencias y validaciones.
 */
@Dao
abstract class SincronizacionDao {
    @Upsert abstract suspend fun upsertUsuarios(usuarios: List<Usuario>)
    @Upsert abstract suspend fun upsertSectores(sectores: List<Sector>)
    @Upsert abstract suspend fun upsertCultivos(cultivos: List<Cultivo>)
    @Upsert abstract suspend fun upsertTareas(tareas: List<Tarea>)
    @Upsert abstract suspend fun upsertProblemas(problemas: List<Problema>)

    /** El servidor nunca envía la clave: se conserva la que ya estaba en el teléfono. */
    @Query("SELECT * FROM usuarios")
    abstract suspend fun usuariosLocales(): List<Usuario>

    /** Todo o nada, y en el orden de las claves foráneas (padres antes que hijos). */
    @Transaction
    open suspend fun guardarTodo(
        usuarios: List<Usuario>,
        sectores: List<Sector>,
        cultivos: List<Cultivo>,
        tareas: List<Tarea>,
        problemas: List<Problema>
    ) {
        val claves = usuariosLocales().associate { it.id to it.clave }
        upsertUsuarios(usuarios.map { it.copy(clave = claves[it.id] ?: it.clave) })
        upsertSectores(sectores)
        upsertCultivos(cultivos)
        upsertTareas(tareas)
        upsertProblemas(problemas)
    }
}

@Dao
interface LecturaDao {
    @Query("SELECT * FROM lecturas WHERE sectorId = :sectorId ORDER BY fecha DESC, id DESC")
    fun deSector(sectorId: Int): Flow<List<LecturaSensor>>

    @Insert suspend fun insertar(lectura: LecturaSensor): Long

    /** Borra solo los datos de prueba; los registros manuales se conservan. */
    @Query("DELETE FROM lecturas WHERE sectorId = :sectorId AND origen = 'SIMULADA'")
    suspend fun borrarSimuladas(sectorId: Int): Int
}
