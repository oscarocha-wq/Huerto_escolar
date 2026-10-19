package cl.duoc.huerto.data

import cl.duoc.huerto.data.entities.*
import cl.duoc.huerto.domain.*

/**
 * Datos ficticios para demostrar todos los flujos de la app.
 * Se cargan solo la primera vez que se crea la base de datos.
 * Las fechas son relativas a "hoy" para que siempre haya tareas atrasadas,
 * próximos riegos y cosechas cercanas.
 */
suspend fun cargarDatosDemo(db: AppDatabase, hoy: Long = inicioDeHoyUtc()) {
    val dia = DIA_MS
    val hora = 3_600_000L
    fun d(n: Int) = hoy + n * dia                       // fecha programada (medianoche UTC)
    fun t(n: Int, h: Int) = hoy + n * dia + h * hora    // momento real (fecha y hora)

    // ---------- Usuarios (1 administrador + 3 estudiantes) ----------
    db.usuarioDao().insertarTodos(
        listOf(
            Usuario("ADM-01", "Docente Prueba", "1234", Perfil.ADMINISTRADOR, null),
            Usuario("EST-01", "Estudiante Uno", "1234", Perfil.ESTUDIANTE, "Taller A"),
            Usuario("EST-02", "Estudiante Dos", "1234", Perfil.ESTUDIANTE, "Taller A"),
            Usuario("EST-03", "Estudiante Tres", "1234", Perfil.ESTUDIANTE, "Taller B")
        )
    )

    // ---------- Sectores ----------
    db.sectorDao().insertarTodos(
        listOf(
            Sector(id = 1, codigo = "S-01", nombre = "Sector Norte",
                descripcion = "Hortalizas de hoja", ubicacion = "Entrada del huerto",
                estado = EstadoSector.EN_USO, responsableId = "ADM-01"),
            Sector(id = 2, codigo = "S-02", nombre = "Sector Sur",
                descripcion = "Camas elevadas", ubicacion = "Junto al invernadero",
                estado = EstadoSector.DISPONIBLE, responsableId = "ADM-01"),
            Sector(id = 3, codigo = "S-03", nombre = "Invernadero",
                descripcion = "Tomates y aromáticas", ubicacion = "Patio trasero",
                estado = EstadoSector.EN_USO, responsableId = "ADM-01"),
            Sector(id = 4, codigo = "S-04", nombre = "Compostera",
                descripcion = "Compost y lombricultura", ubicacion = "Al fondo, junto a la bodega",
                estado = EstadoSector.EN_MANTENIMIENTO, responsableId = "ADM-01")
        )
    )

    // ---------- Cultivos ----------
    db.cultivoDao().insertarTodos(
        listOf(
            Cultivo(id = 1, nombre = "Lechuga", descripcion = "Variedad escarola", sectorId = 1,
                fechaSiembra = d(-20), fechaCosechaEstimada = d(5), frecuenciaRiegoDias = 2,
                estado = EstadoCultivo.EN_CRECIMIENTO, observaciones = "Hojas firmes, buen color"),
            Cultivo(id = 2, nombre = "Rabanito", descripcion = "Rabanito rojo", sectorId = 1,
                fechaSiembra = d(-28), fechaCosechaEstimada = d(-1), frecuenciaRiegoDias = 2,
                estado = EstadoCultivo.LISTO_PARA_COSECHAR),
            Cultivo(id = 3, nombre = "Tomate cherry", descripcion = "Tutorado con cañas", sectorId = 3,
                fechaSiembra = d(-40), fechaTrasplante = d(-25), fechaCosechaEstimada = d(30),
                frecuenciaRiegoDias = 3, estado = EstadoCultivo.EN_CRECIMIENTO),
            Cultivo(id = 4, nombre = "Albahaca", descripcion = "Albahaca genovesa", sectorId = 3,
                fechaSiembra = d(-10), fechaCosechaEstimada = d(45), frecuenciaRiegoDias = 1,
                estado = EstadoCultivo.SEMBRADO),
            Cultivo(id = 5, nombre = "Zanahoria", descripcion = "Para el próximo ciclo", sectorId = 2,
                fechaSiembra = null, fechaCosechaEstimada = null, frecuenciaRiegoDias = 3,
                estado = EstadoCultivo.PLANIFICADO)
        )
    )

    // ---------- Tareas (una en cada estado) ----------
    val tareas = listOf(
        Tarea(id = 1, tipo = TipoTarea.RIEGO, descripcion = "Regar las lechugas con regadera, sin mojar las hojas.",
            sectorId = 1, cultivoId = 1, fechaCreacion = t(-3, 12), fechaProgramada = d(1),
            asignadoId = "EST-01", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.PENDIENTE),
        Tarea(id = 2, tipo = TipoTarea.OBSERVACION, descripcion = "Observar las hojas del tomate y anotar si hay manchas o insectos.",
            sectorId = 3, cultivoId = 3, fechaCreacion = t(-6, 12), fechaProgramada = d(-2),
            asignadoId = "EST-01", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.PENDIENTE),
        Tarea(id = 3, tipo = TipoTarea.LIMPIEZA, descripcion = "Retirar hojas secas y basura del pasillo del sector sur.",
            sectorId = 2, cultivoId = null, fechaCreacion = t(-4, 12), fechaProgramada = d(0),
            asignadoId = "EST-02", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.EN_DESARROLLO),
        Tarea(id = 4, tipo = TipoTarea.RIEGO, descripcion = "Regar la albahaca a primera hora.",
            sectorId = 3, cultivoId = 4, fechaCreacion = t(-3, 12), fechaProgramada = d(-1),
            asignadoId = "EST-01", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.PENDIENTE_DE_VALIDACION),
        Tarea(id = 5, tipo = TipoTarea.SIEMBRA, descripcion = "Sembrar albahaca en la cama 2, a 20 cm entre plantas.",
            sectorId = 3, cultivoId = 4, fechaCreacion = t(-12, 12), fechaProgramada = d(-10),
            asignadoId = "EST-02", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.VALIDADA),
        Tarea(id = 6, tipo = TipoTarea.REGISTRO_CRECIMIENTO, descripcion = "Medir la altura de 3 plantas de tomate y anotar cada medida.",
            sectorId = 3, cultivoId = 3, fechaCreacion = t(-6, 13), fechaProgramada = d(-3),
            asignadoId = "EST-03", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.RECHAZADA),
        Tarea(id = 7, tipo = TipoTarea.MANTENIMIENTO, descripcion = "Reparar el borde de madera de la compostera.",
            sectorId = 4, cultivoId = null, fechaCreacion = t(-2, 12), fechaProgramada = d(3),
            asignadoId = null, grupo = "Taller A", creadaPorId = "ADM-01", estado = EstadoTarea.PENDIENTE),
        Tarea(id = 8, tipo = TipoTarea.COSECHA, descripcion = "Cosechar los rabanitos que estén listos y llevarlos a la cocina.",
            sectorId = 1, cultivoId = 2, fechaCreacion = t(-1, 12), fechaProgramada = d(2),
            asignadoId = "EST-03", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.PENDIENTE),
        Tarea(id = 9, tipo = TipoTarea.RIEGO, descripcion = "Regar las lechugas del sector norte.",
            sectorId = 1, cultivoId = 1, fechaCreacion = t(-3, 13), fechaProgramada = d(-1),
            asignadoId = "EST-02", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.VALIDADA),
        Tarea(id = 10, tipo = TipoTarea.RIEGO, descripcion = "Regar el tomate cherry del invernadero.",
            sectorId = 3, cultivoId = 3, fechaCreacion = t(-4, 13), fechaProgramada = d(-2),
            asignadoId = "EST-03", grupo = null, creadaPorId = "ADM-01", estado = EstadoTarea.VALIDADA)
    )
    tareas.forEach { db.tareaDao().insertar(it) }

    // ---------- Entregas (evidencias) y revisiones (validaciones) ----------
    val seg = db.seguimientoDao()
    seg.insertarEvidencia(Evidencia(id = 1, tareaId = 4, autorId = "EST-01", fecha = t(-1, 13),
        comentario = "Regué con 2 litros, la tierra quedó húmeda.", fotoRuta = null))
    seg.insertarEvidencia(Evidencia(id = 2, tareaId = 5, autorId = "EST-02", fecha = t(-10, 14),
        comentario = "Sembradas 12 plantas en la cama 2.", fotoRuta = null))
    seg.insertarEvidencia(Evidencia(id = 3, tareaId = 6, autorId = "EST-03", fecha = t(-3, 15),
        comentario = "Miden más o menos 30 cm.", fotoRuta = null))
    seg.insertarEvidencia(Evidencia(id = 4, tareaId = 9, autorId = "EST-02", fecha = t(-1, 12),
        comentario = "Riego completo.", fotoRuta = null))
    seg.insertarEvidencia(Evidencia(id = 5, tareaId = 10, autorId = "EST-03", fecha = t(-2, 13),
        comentario = "Listo, regado con manguera suave.", fotoRuta = null))

    seg.insertarValidacion(Validacion(id = 1, tareaId = 5, evidenciaId = 2, validadorId = "ADM-01",
        resultado = ResultadoValidacion.APROBADA, comentario = "Buen trabajo, la distancia es correcta.", fecha = t(-9, 12)))
    seg.insertarValidacion(Validacion(id = 2, tareaId = 6, evidenciaId = 3, validadorId = "ADM-01",
        resultado = ResultadoValidacion.RECHAZADA, comentario = "Falta indicar la medida de cada planta por separado.", fecha = t(-2, 12)))
    seg.insertarValidacion(Validacion(id = 3, tareaId = 9, evidenciaId = 4, validadorId = "ADM-01",
        resultado = ResultadoValidacion.APROBADA, comentario = null, fecha = t(-1, 17)))
    seg.insertarValidacion(Validacion(id = 4, tareaId = 10, evidenciaId = 5, validadorId = "ADM-01",
        resultado = ResultadoValidacion.APROBADA, comentario = "Correcto.", fecha = t(-2, 18)))

    // ---------- Problemas ----------
    val prob = db.problemaDao()
    prob.insertar(Problema(id = 1, tipo = TipoProblema.FALTA_AGUA,
        descripcion = "Las lechugas del borde se ven marchitas y la tierra está muy seca.",
        sectorId = 1, cultivoId = 1, reportadoPorId = "EST-01", fechaReporte = t(-1, 19),
        fotoRuta = null, estado = EstadoProblema.INFORMADO))
    prob.insertar(Problema(id = 2, tipo = TipoProblema.PLAGA_SIMULADA,
        descripcion = "Se observan puntos blancos bajo las hojas del tomate (caso de práctica).",
        sectorId = 3, cultivoId = 3, reportadoPorId = "EST-03", fechaReporte = t(-3, 16),
        fotoRuta = null, estado = EstadoProblema.EN_REVISION,
        comentarioDocente = "Lo revisaremos el jueves en clase.", atendidoPorId = "ADM-01", fechaAtencion = t(-2, 13)))
    prob.insertar(Problema(id = 3, tipo = TipoProblema.DETERIORO_SECTOR,
        descripcion = "Una tabla del borde de la compostera está suelta.",
        sectorId = 4, cultivoId = null, reportadoPorId = "EST-02", fechaReporte = t(-5, 15),
        fotoRuta = null, estado = EstadoProblema.ATENDIDO,
        comentarioDocente = "Se creó una tarea de mantenimiento para repararla.",
        atendidoPorId = "ADM-01", fechaAtencion = t(-2, 12)))

    // ---------- Lecturas de sensores (simuladas y una manual) ----------
    val lect = db.lecturaDao()
    lect.insertar(LecturaSensor(sectorId = 1, fecha = t(-2, 12), humedadSuelo = 45.2, temperatura = 18.4,
        humedadAmbiente = 62.0, origen = OrigenLectura.SIMULADA))
    lect.insertar(LecturaSensor(sectorId = 1, fecha = t(-1, 12), humedadSuelo = 28.5, temperatura = 24.1,
        humedadAmbiente = 55.3, origen = OrigenLectura.SIMULADA))
    lect.insertar(LecturaSensor(sectorId = 3, fecha = t(-2, 18), humedadSuelo = 85.3, temperatura = 33.2,
        humedadAmbiente = 88.1, origen = OrigenLectura.SIMULADA))
    lect.insertar(LecturaSensor(sectorId = 3, fecha = t(-1, 18), humedadSuelo = 62.0, temperatura = 27.5,
        humedadAmbiente = 70.0, origen = OrigenLectura.MANUAL))
}
