package cl.duoc.huerto.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.huerto.data.dao.*
import cl.duoc.huerto.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Usuario::class, Sector::class, Cultivo::class, Tarea::class,
        Evidencia::class, Validacion::class, Problema::class, LecturaSensor::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun tareaDao(): TareaDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun lecturaDao(): LecturaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "huerto.db"
            )
                .addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5)
                .addCallback(SeedCallback)
                .build()
                .also { INSTANCE = it }
        }
    }

    /** Solo la primera vez que se crea la base: carga los datos ficticios de demostración. */
    private object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = INSTANCE ?: return@launch
                cargarDatosDemo(database)
            }
        }
    }
}

val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tareas` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tipo` TEXT NOT NULL,
                `descripcion` TEXT NOT NULL,
                `sectorId` INTEGER NOT NULL,
                `cultivoId` INTEGER,
                `fechaCreacion` INTEGER NOT NULL,
                `fechaProgramada` INTEGER NOT NULL,
                `asignadoId` TEXT,
                `grupo` TEXT,
                `creadaPorId` TEXT NOT NULL,
                `estado` TEXT NOT NULL,
                `fechaRealizacion` INTEGER,
                `realizadaPorId` TEXT,
                `fotoRuta` TEXT,
                `comentarioEstudiante` TEXT,
                `comentarioDocente` TEXT,
                `validadorId` TEXT,
                `fechaValidacion` INTEGER
            )
            """.trimIndent()
        )
    }
}

val MIGRACION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `problemas` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tipo` TEXT NOT NULL,
                `descripcion` TEXT NOT NULL,
                `sectorId` INTEGER NOT NULL,
                `cultivoId` INTEGER,
                `reportadoPorId` TEXT NOT NULL,
                `fechaReporte` INTEGER NOT NULL,
                `fotoRuta` TEXT,
                `estado` TEXT NOT NULL,
                `comentarioDocente` TEXT,
                `atendidoPorId` TEXT,
                `fechaAtencion` INTEGER,
                FOREIGN KEY(`sectorId`) REFERENCES `sectores`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`cultivoId`) REFERENCES `cultivos`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`reportadoPorId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_problemas_sectorId` ON `problemas` (`sectorId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_problemas_cultivoId` ON `problemas` (`cultivoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_problemas_reportadoPorId` ON `problemas` (`reportadoPorId`)")
    }
}

/**
 * v3 → v4 (normalización):
 * - cultivos y tareas se recrean con claves foráneas (SQLite no permite agregarlas con ALTER).
 * - Los datos de entrega y revisión salen de tareas hacia las tablas evidencias y validaciones.
 * - Se agrega la cuenta EST-03.
 * No se pierde ningún dato existente.
 */
val MIGRACION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ---------- Cultivos con FK a sectores ----------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cultivos_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `descripcion` TEXT NOT NULL,
                `sectorId` INTEGER NOT NULL,
                `fechaSiembra` INTEGER,
                `fechaTrasplante` INTEGER,
                `fechaCosechaEstimada` INTEGER,
                `frecuenciaRiegoDias` INTEGER NOT NULL,
                `estado` TEXT NOT NULL,
                `observaciones` TEXT NOT NULL,
                `fotoUri` TEXT,
                FOREIGN KEY(`sectorId`) REFERENCES `sectores`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `cultivos_new` (`id`, `nombre`, `descripcion`, `sectorId`, `fechaSiembra`,
                `fechaTrasplante`, `fechaCosechaEstimada`, `frecuenciaRiegoDias`, `estado`,
                `observaciones`, `fotoUri`)
            SELECT `id`, `nombre`, `descripcion`, `sectorId`, `fechaSiembra`,
                `fechaTrasplante`, `fechaCosechaEstimada`, `frecuenciaRiegoDias`, `estado`,
                `observaciones`, `fotoUri`
            FROM `cultivos`
            """.trimIndent()
        )
        // Al borrar la tabla vieja, SQLite podría dejar en NULL el cultivo de los problemas:
        // respaldamos ese vínculo y lo restauramos después.
        db.execSQL("CREATE TABLE `_resp_problemas` AS SELECT `id`, `cultivoId` FROM `problemas` WHERE `cultivoId` IS NOT NULL")
        db.execSQL("DROP TABLE `cultivos`")
        db.execSQL("ALTER TABLE `cultivos_new` RENAME TO `cultivos`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cultivos_sectorId` ON `cultivos` (`sectorId`)")
        db.execSQL(
            """
            UPDATE `problemas` SET `cultivoId` =
                (SELECT r.`cultivoId` FROM `_resp_problemas` r WHERE r.`id` = `problemas`.`id`)
            WHERE `id` IN (SELECT `id` FROM `_resp_problemas`)
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `_resp_problemas`")

        // ---------- Cuenta EST-03 ----------
        db.execSQL(
            "INSERT OR IGNORE INTO `usuarios` (`id`, `nombre`, `clave`, `perfil`, `grupo`, `activo`) " +
                "VALUES ('EST-03', 'Estudiante Tres', '1234', 'ESTUDIANTE', 'Taller B', 1)"
        )

        // ---------- Limpieza: referencias que ya no existen ----------
        db.execSQL("UPDATE `tareas` SET `cultivoId` = NULL WHERE `cultivoId` IS NOT NULL AND `cultivoId` NOT IN (SELECT `id` FROM `cultivos`)")
        db.execSQL("UPDATE `tareas` SET `asignadoId` = NULL WHERE `asignadoId` IS NOT NULL AND `asignadoId` NOT IN (SELECT `id` FROM `usuarios`)")
        db.execSQL("DELETE FROM `tareas` WHERE `sectorId` NOT IN (SELECT `id` FROM `sectores`) OR `creadaPorId` NOT IN (SELECT `id` FROM `usuarios`)")

        // ---------- Respaldo de entregas y revisiones antes de recrear tareas ----------
        db.execSQL(
            """
            CREATE TABLE `_resp_tareas` AS SELECT `id`, `estado`, `fechaRealizacion`, `realizadaPorId`,
                `fotoRuta`, `comentarioEstudiante`, `comentarioDocente`, `validadorId`, `fechaValidacion`
            FROM `tareas`
            """.trimIndent()
        )

        // ---------- Tareas con FKs y sin datos de entrega/revisión ----------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tareas_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tipo` TEXT NOT NULL,
                `descripcion` TEXT NOT NULL,
                `sectorId` INTEGER NOT NULL,
                `cultivoId` INTEGER,
                `fechaCreacion` INTEGER NOT NULL,
                `fechaProgramada` INTEGER NOT NULL,
                `asignadoId` TEXT,
                `grupo` TEXT,
                `creadaPorId` TEXT NOT NULL,
                `estado` TEXT NOT NULL,
                FOREIGN KEY(`sectorId`) REFERENCES `sectores`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY(`cultivoId`) REFERENCES `cultivos`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`asignadoId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY(`creadaPorId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `tareas_new` (`id`, `tipo`, `descripcion`, `sectorId`, `cultivoId`, `fechaCreacion`,
                `fechaProgramada`, `asignadoId`, `grupo`, `creadaPorId`, `estado`)
            SELECT `id`, `tipo`, `descripcion`, `sectorId`, `cultivoId`, `fechaCreacion`,
                `fechaProgramada`, `asignadoId`, `grupo`, `creadaPorId`, `estado`
            FROM `tareas`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `tareas`")
        db.execSQL("ALTER TABLE `tareas_new` RENAME TO `tareas`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tareas_sectorId` ON `tareas` (`sectorId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tareas_cultivoId` ON `tareas` (`cultivoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tareas_asignadoId` ON `tareas` (`asignadoId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tareas_creadaPorId` ON `tareas` (`creadaPorId`)")

        // ---------- Tabla evidencias ----------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `evidencias` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tareaId` INTEGER NOT NULL,
                `autorId` TEXT NOT NULL,
                `fecha` INTEGER NOT NULL,
                `comentario` TEXT,
                `fotoRuta` TEXT,
                FOREIGN KEY(`tareaId`) REFERENCES `tareas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`autorId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_evidencias_tareaId` ON `evidencias` (`tareaId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_evidencias_autorId` ON `evidencias` (`autorId`)")

        // ---------- Tabla validaciones ----------
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `validaciones` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tareaId` INTEGER NOT NULL,
                `evidenciaId` INTEGER,
                `validadorId` TEXT NOT NULL,
                `resultado` TEXT NOT NULL,
                `comentario` TEXT,
                `fecha` INTEGER NOT NULL,
                FOREIGN KEY(`tareaId`) REFERENCES `tareas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`evidenciaId`) REFERENCES `evidencias`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`validadorId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_validaciones_tareaId` ON `validaciones` (`tareaId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_validaciones_evidenciaId` ON `validaciones` (`evidenciaId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_validaciones_validadorId` ON `validaciones` (`validadorId`)")

        // ---------- Mover los datos antiguos a las tablas nuevas ----------
        db.execSQL(
            """
            INSERT INTO `evidencias` (`tareaId`, `autorId`, `fecha`, `comentario`, `fotoRuta`)
            SELECT r.`id`, r.`realizadaPorId`, r.`fechaRealizacion`, r.`comentarioEstudiante`, r.`fotoRuta`
            FROM `_resp_tareas` r
            WHERE r.`fechaRealizacion` IS NOT NULL
              AND r.`realizadaPorId` IN (SELECT `id` FROM `usuarios`)
              AND r.`id` IN (SELECT `id` FROM `tareas`)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `validaciones` (`tareaId`, `evidenciaId`, `validadorId`, `resultado`, `comentario`, `fecha`)
            SELECT r.`id`,
                   (SELECT MAX(e.`id`) FROM `evidencias` e WHERE e.`tareaId` = r.`id`),
                   r.`validadorId`,
                   CASE r.`estado` WHEN 'VALIDADA' THEN 'APROBADA' ELSE 'RECHAZADA' END,
                   r.`comentarioDocente`,
                   r.`fechaValidacion`
            FROM `_resp_tareas` r
            WHERE r.`fechaValidacion` IS NOT NULL
              AND r.`estado` IN ('VALIDADA', 'RECHAZADA')
              AND r.`validadorId` IN (SELECT `id` FROM `usuarios`)
              AND r.`id` IN (SELECT `id` FROM `tareas`)
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `_resp_tareas`")
    }
}

/** v4 → v5: tabla de lecturas de sensores (simulación académica). */
val MIGRACION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `lecturas` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `sectorId` INTEGER NOT NULL,
                `fecha` INTEGER NOT NULL,
                `humedadSuelo` REAL NOT NULL,
                `temperatura` REAL NOT NULL,
                `humedadAmbiente` REAL NOT NULL,
                `origen` TEXT NOT NULL,
                FOREIGN KEY(`sectorId`) REFERENCES `sectores`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_lecturas_sectorId` ON `lecturas` (`sectorId`)")
    }
}
