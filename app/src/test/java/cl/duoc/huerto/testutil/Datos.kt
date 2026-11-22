package cl.duoc.huerto.testutil

import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.domain.util.inicioDeHoyUtc

/** Datos de prueba reutilizables. */
object Datos {
    const val DIA = 86_400_000L

    val ADMIN = Sesion("ADM-01", "Docente Prueba", Perfil.ADMINISTRADOR, null)
    val EST1 = Sesion("EST-01", "Estudiante Uno", Perfil.ESTUDIANTE, "Taller A")
    val EST2 = Sesion("EST-02", "Estudiante Dos", Perfil.ESTUDIANTE, "Taller B")

    fun hoy() = inicioDeHoyUtc()

    fun sector(
        id: Int = 1,
        codigo: String = "S-0$id",
        nombre: String = "Sector $id",
        estado: EstadoSector = EstadoSector.EN_USO
    ) = Sector(
        id = id, codigo = codigo, nombre = nombre, descripcion = "Descripción",
        ubicacion = "Patio central", estado = estado, responsableId = "ADM-01",
        fechaCreacion = 100L, fechaActualizacion = 100L
    )

    fun cultivo(id: Int = 1, sectorId: Int = 1, nombre: String = "Lechuga") = Cultivo(
        id = id, nombre = nombre, descripcion = "Descripción", sectorId = sectorId,
        fechaSiembra = 1_000L, fechaTrasplante = null, fechaCosechaEstimada = 5_000L,
        frecuenciaRiegoDias = 2, estado = EstadoCultivo.SEMBRADO, observaciones = "Bien",
        fotoUri = "foto.jpg"
    )

    fun tarea(
        id: Int = 1,
        sectorId: Int = 1,
        estado: EstadoTarea = EstadoTarea.PENDIENTE,
        asignadoId: String? = "EST-01",
        grupo: String? = null,
        fechaProgramada: Long = hoy() + DIA,
        cultivoId: Int? = null
    ) = Tarea(
        id = id, tipo = TipoTarea.RIEGO, descripcion = "Regar las plantas", sectorId = sectorId,
        cultivoId = cultivoId, fechaCreacion = 100L, fechaProgramada = fechaProgramada,
        asignadoId = asignadoId, grupo = grupo, creadaPorId = "ADM-01", estado = estado
    )

    fun problema(
        id: Int = 1,
        sectorId: Int = 1,
        estado: EstadoProblema = EstadoProblema.INFORMADO,
        reportadoPorId: String = "EST-01",
        cultivoId: Int? = null
    ) = Problema(
        id = id, tipo = TipoProblema.FALTA_AGUA, descripcion = "La tierra está muy seca",
        sectorId = sectorId, cultivoId = cultivoId, reportadoPorId = reportadoPorId,
        fechaReporte = 100L, fotoRuta = null, estado = estado
    )

    fun estudiante(id: String = "EST-01", grupo: String? = "Taller A", nombre: String = "Estudiante Uno") =
        Usuario(id, nombre, "1234", Perfil.ESTUDIANTE, grupo)

    fun evidencia(id: Int = 1, tareaId: Int = 1, autorId: String = "EST-01", fecha: Long = 200L) =
        Evidencia(id = id, tareaId = tareaId, autorId = autorId, fecha = fecha, comentario = "Listo", fotoRuta = null)
}
