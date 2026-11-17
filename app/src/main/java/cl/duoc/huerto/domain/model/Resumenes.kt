package cl.duoc.huerto.domain.model

data class ResumenPanel(
    val pendientes: Int = 0,
    val atrasadas: Int = 0,
    val porValidar: Int = 0,
    val validadas: Int = 0,
    val problemasAbiertos: Int = 0,
    val alertas: Int = 0
)

data class ResumenAvisos(
    val atrasadas: Int = 0,
    val porValidar: Int = 0,
    val problemasNuevos: Int = 0
)
