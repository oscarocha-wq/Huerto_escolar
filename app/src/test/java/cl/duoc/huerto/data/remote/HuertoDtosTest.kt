package cl.duoc.huerto.data.remote

import cl.duoc.huerto.data.remote.dto.*
import cl.duoc.huerto.domain.model.*
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/** Verifica que el JSON del backend se transforma bien en las entidades de Room. */
class HuertoDtosTest {

    private val gson = Gson()

    @Test fun `usuario del backend llega sin clave y se mapea`() {
        // Así responde huertoapi: la clave nunca viene (WRITE_ONLY)
        val json = """{"id":"EST-01","nombre":"Estudiante Uno","perfil":"ESTUDIANTE","grupo":"Taller A","activo":true}"""
        val usuario = gson.fromJson(json, UsuarioDto::class.java).toEntity()!!

        assertEquals("EST-01", usuario.id)
        assertEquals(Perfil.ESTUDIANTE, usuario.perfil)
        assertEquals("", usuario.clave)
        assertTrue(usuario.activo)
    }

    @Test fun `usuario sin id se descarta`() {
        assertNull(UsuarioDto(null, "X", Perfil.ESTUDIANTE, null, true).toEntity())
    }

    @Test fun `sector convierte id Long a Int y textos nulos a vacio`() {
        val json = """{"id":3,"codigo":"S-03","nombre":"Invernadero","descripcion":null,"estado":"EN_USO"}"""
        val sector = gson.fromJson(json, SectorDto::class.java).toEntity()!!

        assertEquals(3, sector.id)
        assertEquals("S-03", sector.codigo)
        assertEquals("", sector.descripcion)
        assertEquals("", sector.ubicacion)
        assertEquals(EstadoSector.EN_USO, sector.estado)
    }

    @Test fun `estado desconocido usa el valor por defecto`() {
        val json = """{"id":1,"codigo":"S-01","nombre":"Norte","estado":"NO_EXISTE"}"""
        val sector = gson.fromJson(json, SectorDto::class.java).toEntity()!!
        assertEquals(EstadoSector.DISPONIBLE, sector.estado)
    }

    @Test fun `cultivo sin sector se descarta`() {
        val dto = CultivoDto(1, "Lechuga", "", null, null, null, null, 2, null, null, null)
        assertNull(dto.toEntity())
    }

    @Test fun `cultivo completo se mapea`() {
        val dto = CultivoDto(5, "Tomate", "Cherry", 3, 100L, null, 200L, 2, EstadoCultivo.SEMBRADO, null, null)
        val c = dto.toEntity()!!
        assertEquals(5, c.id)
        assertEquals(3, c.sectorId)
        assertEquals(EstadoCultivo.SEMBRADO, c.estado)
        assertEquals("", c.observaciones)
    }

    @Test fun `tarea sin creador se descarta y con datos minimos usa valores por defecto`() {
        assertNull(TareaDto(1, null, "x", 1, null, null, null, null, null, null, null).toEntity())

        val t = TareaDto(2, null, null, 1, null, null, 500L, "EST-01", null, "ADM-01", null).toEntity()!!
        assertEquals(TipoTarea.OTRA, t.tipo)
        assertEquals(EstadoTarea.PENDIENTE, t.estado)
        assertEquals(500L, t.fechaProgramada)
        assertEquals("", t.descripcion)
    }

    @Test fun `problema se mapea y sin reportante se descarta`() {
        val p = ProblemaDto(7, TipoProblema.FALTA_AGUA, "Seco", 1, null, "EST-01", 10L, null, null, null, null, null).toEntity()!!
        assertEquals(7, p.id)
        assertEquals(EstadoProblema.INFORMADO, p.estado)

        assertNull(ProblemaDto(8, null, null, 1, null, null, null, null, null, null, null, null).toEntity())
    }
}
