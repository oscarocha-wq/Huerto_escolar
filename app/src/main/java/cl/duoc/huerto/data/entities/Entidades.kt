package cl.duoc.huerto.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.huerto.domain.*

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String,          // ej: "EST-01" (ficticio)
    val nombre: String,                  // ficticio
    val clave: String,                   // solo para el MVP académico
    val perfil: Perfil,
    val grupo: String?,                  // taller o grupo asignado
    val activo: Boolean = true
)

@Entity(tableName = "sectores")
data class Sector(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val estado: EstadoSector = EstadoSector.DISPONIBLE,
    val responsableId: String?,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)

/** Un cultivo pertenece a un sector. No se puede borrar un sector que tenga cultivos (RESTRICT). */
@Entity(
    tableName = "cultivos",
    foreignKeys = [
        ForeignKey(
            entity = Sector::class, parentColumns = ["id"], childColumns = ["sectorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("sectorId")]
)
data class Cultivo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val sectorId: Int,
    val fechaSiembra: Long?,
    val fechaTrasplante: Long? = null,
    val fechaCosechaEstimada: Long?,
    val frecuenciaRiegoDias: Int,
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,
    val observaciones: String = "",
    val fotoUri: String? = null
)

/**
 * La tarea guarda solo lo que la define y su estado actual.
 * Las entregas van en Evidencia y las revisiones en Validacion (3FN).
 */
@Entity(
    tableName = "tareas",
    foreignKeys = [
        ForeignKey(
            entity = Sector::class, parentColumns = ["id"], childColumns = ["sectorId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Cultivo::class, parentColumns = ["id"], childColumns = ["cultivoId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Usuario::class, parentColumns = ["id"], childColumns = ["asignadoId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Usuario::class, parentColumns = ["id"], childColumns = ["creadaPorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("sectorId"), Index("cultivoId"), Index("asignadoId"), Index("creadaPorId")]
)
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: TipoTarea,
    val descripcion: String,              // instrucciones
    val sectorId: Int,
    val cultivoId: Int?,
    val fechaCreacion: Long,
    val fechaProgramada: Long,            // medianoche UTC (DatePicker)
    val asignadoId: String?,              // estudiante individual
    val grupo: String?,                   // o grupo completo
    val creadaPorId: String,
    val estado: EstadoTarea = EstadoTarea.PENDIENTE
)

/** Cada vez que el estudiante entrega una tarea se guarda una evidencia nueva. */
@Entity(
    tableName = "evidencias",
    foreignKeys = [
        ForeignKey(
            entity = Tarea::class, parentColumns = ["id"], childColumns = ["tareaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class, parentColumns = ["id"], childColumns = ["autorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("tareaId"), Index("autorId")]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tareaId: Int,
    val autorId: String,                  // quién la entregó
    val fecha: Long,
    val comentario: String?,
    val fotoRuta: String?                 // copia interna de la foto
)

/** Cada revisión del docente: quién, cuándo, resultado y comentario. */
@Entity(
    tableName = "validaciones",
    foreignKeys = [
        ForeignKey(
            entity = Tarea::class, parentColumns = ["id"], childColumns = ["tareaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Evidencia::class, parentColumns = ["id"], childColumns = ["evidenciaId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Usuario::class, parentColumns = ["id"], childColumns = ["validadorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("tareaId"), Index("evidenciaId"), Index("validadorId")]
)
data class Validacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tareaId: Int,
    val evidenciaId: Int?,                // la entrega que se revisó
    val validadorId: String,
    val resultado: ResultadoValidacion,
    val comentario: String?,
    val fecha: Long
)

@Entity(
    tableName = "problemas",
    foreignKeys = [
        ForeignKey(
            entity = Sector::class, parentColumns = ["id"], childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Cultivo::class, parentColumns = ["id"], childColumns = ["cultivoId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Usuario::class, parentColumns = ["id"], childColumns = ["reportadoPorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sectorId"), Index("cultivoId"), Index("reportadoPorId")]
)
data class Problema(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: TipoProblema,
    val descripcion: String,
    val sectorId: Int,
    val cultivoId: Int?,
    val reportadoPorId: String,
    val fechaReporte: Long,
    val fotoRuta: String?,
    val estado: EstadoProblema = EstadoProblema.INFORMADO,
    val comentarioDocente: String? = null,
    val atendidoPorId: String? = null,
    val fechaAtencion: Long? = null
)

/** Lectura de sensor por sector. Origen SIMULADA = dato de prueba (simulación académica). */
@Entity(
    tableName = "lecturas",
    foreignKeys = [
        ForeignKey(
            entity = Sector::class, parentColumns = ["id"], childColumns = ["sectorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sectorId")]
)
data class LecturaSensor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sectorId: Int,
    val fecha: Long,
    val humedadSuelo: Double,      // %
    val temperatura: Double,       // °C
    val humedadAmbiente: Double,   // %
    val origen: OrigenLectura
)
