package cl.duoc.huerto.core.di

import android.content.Context
import cl.duoc.huerto.data.local.AppDatabase
import cl.duoc.huerto.data.repository.CultivoRepositoryImpl
import cl.duoc.huerto.data.repository.LecturaRepositoryImpl
import cl.duoc.huerto.data.repository.ProblemaRepositoryImpl
import cl.duoc.huerto.data.repository.SectorRepositoryImpl
import cl.duoc.huerto.data.repository.SeguimientoRepositoryImpl
import cl.duoc.huerto.data.repository.TareaRepositoryImpl
import cl.duoc.huerto.data.repository.UsuarioRepositoryImpl
import cl.duoc.huerto.domain.repository.CultivoRepository
import cl.duoc.huerto.domain.repository.LecturaRepository
import cl.duoc.huerto.domain.repository.ProblemaRepository
import cl.duoc.huerto.domain.repository.SectorRepository
import cl.duoc.huerto.domain.repository.SeguimientoRepository
import cl.duoc.huerto.domain.repository.TareaRepository
import cl.duoc.huerto.domain.repository.UsuarioRepository

/**
 * Inyección de dependencias manual (composition root).
 * Es el único lugar que conoce las implementaciones de la capa data:
 * los ViewModels piden los contratos de domain/repository y no saben que existe Room.
 */
object AppContainer {
    private fun db(context: Context) = AppDatabase.get(context)

    fun usuarioRepository(context: Context): UsuarioRepository =
        UsuarioRepositoryImpl(db(context).usuarioDao())

    fun sectorRepository(context: Context): SectorRepository =
        SectorRepositoryImpl(db(context).sectorDao())

    fun cultivoRepository(context: Context): CultivoRepository =
        CultivoRepositoryImpl(db(context).cultivoDao())

    fun tareaRepository(context: Context): TareaRepository =
        TareaRepositoryImpl(db(context).tareaDao())

    fun seguimientoRepository(context: Context): SeguimientoRepository =
        SeguimientoRepositoryImpl(db(context).seguimientoDao())

    fun problemaRepository(context: Context): ProblemaRepository =
        ProblemaRepositoryImpl(db(context).problemaDao())

    fun lecturaRepository(context: Context): LecturaRepository =
        LecturaRepositoryImpl(db(context).lecturaDao())
}
