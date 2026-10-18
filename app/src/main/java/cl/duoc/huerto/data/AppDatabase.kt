package cl.duoc.huerto.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.huerto.data.dao.*
import cl.duoc.huerto.data.entities.*
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Usuario::class, Sector::class, Cultivo::class, Tarea::class, Problema::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun sectorDao(): SectorDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun tareaDao(): TareaDao
    abstract fun problemaDao(): ProblemaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "huerto.db"
            )
                .addMigrations(MIGRACION_1_2, MIGRACION_2_3)                .addCallback(SeedCallback)
                .build()
                .also { INSTANCE = it }
        }
    }

    private object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = INSTANCE ?: return@launch
                database.usuarioDao().insertarTodos(
                    listOf(
                        Usuario("ADM-01", "Docente Prueba", "1234", Perfil.ADMINISTRADOR, null),
                        Usuario("EST-01", "Estudiante Uno", "1234", Perfil.ESTUDIANTE, "Taller A"),
                        Usuario("EST-02", "Estudiante Dos", "1234", Perfil.ESTUDIANTE, "Taller A")
                    )
                )
                database.sectorDao().insertarTodos(
                    listOf(
                        Sector(codigo = "S-01", nombre = "Sector Norte",
                            descripcion = "Hortalizas de hoja", ubicacion = "Entrada del huerto",
                            estado = EstadoSector.EN_USO, responsableId = "ADM-01"),
                        Sector(codigo = "S-02", nombre = "Sector Sur",
                            descripcion = "Camas elevadas", ubicacion = "Junto al invernadero",
                            responsableId = "ADM-01")
                    )
                )
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