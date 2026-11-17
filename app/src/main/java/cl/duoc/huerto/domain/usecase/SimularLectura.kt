package cl.duoc.huerto.domain.usecase

import cl.duoc.huerto.domain.model.LecturaSensor
import cl.duoc.huerto.domain.model.OrigenLectura
import kotlin.math.round
import kotlin.random.Random

private fun redondear(v: Double) = round(v * 10) / 10

/** Genera una lectura de prueba con valores realistas (a veces fuera de lo normal, para ver los estados). */
fun simularLectura(sectorId: Int, ahora: Long, random: Random = Random.Default) = LecturaSensor(
    sectorId = sectorId,
    fecha = ahora,
    humedadSuelo = redondear(random.nextDouble(15.0, 90.0)),
    temperatura = redondear(random.nextDouble(3.0, 36.0)),
    humedadAmbiente = redondear(random.nextDouble(25.0, 95.0)),
    origen = OrigenLectura.SIMULADA
)
