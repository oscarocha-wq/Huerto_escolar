package cl.duoc.huerto.core

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.Perfil
import cl.duoc.huerto.domain.model.Sesion
import org.junit.Assert.*
import org.junit.Test

class SesionManagerTest {

    @Test fun `iniciar y cerrar sesion`() {
        val sesion = Sesion("EST-01", "Ana", Perfil.ESTUDIANTE, "Taller A")
        SesionManager.iniciar(sesion)
        assertEquals(sesion, SesionManager.sesion.value)
        SesionManager.cerrar()
        assertNull(SesionManager.sesion.value)
    }
}
