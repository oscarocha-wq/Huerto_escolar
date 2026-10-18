package cl.duoc.huerto.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.huerto.domain.*
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String,          // ej: "USR-001" (ficticio)
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

@Entity(tableName = "cultivos")
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

@Entity(tableName = "tareas")
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
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val fechaRealizacion: Long? = null,
    val realizadaPorId: String? = null,
    val fotoRuta: String? = null,
    val comentarioEstudiante: String? = null,
    val comentarioDocente: String? = null,
    val validadorId: String? = null,
    val fechaValidacion: Long? = null
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