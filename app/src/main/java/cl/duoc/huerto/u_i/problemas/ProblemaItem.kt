package cl.duoc.huerto.u_i.problemas

import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.entities.Problema
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.entities.Usuario

data class ProblemaItem(
    val problema: Problema,
    val sectorNombre: String,
    val cultivoNombre: String?,
    val reportadoPorTexto: String
)

fun construirItemsProblema(
    problemas: List<Problema>,
    sectores: List<Sector>,
    cultivos: List<Cultivo>,
    usuarios: List<Usuario>
): List<ProblemaItem> {
    val nombresSector = sectores.associate { it.id to "${it.codigo} · ${it.nombre}" }
    val nombresCultivo = cultivos.associate { it.id to it.nombre }
    val nombresUsuario = usuarios.associate { it.id to it.nombre }

    return problemas.map { p ->
        ProblemaItem(
            problema = p,
            sectorNombre = nombresSector[p.sectorId] ?: "Sector desconocido",
            cultivoNombre = p.cultivoId?.let { nombresCultivo[it] },
            reportadoPorTexto = nombresUsuario[p.reportadoPorId] ?: p.reportadoPorId
        )
    }
}