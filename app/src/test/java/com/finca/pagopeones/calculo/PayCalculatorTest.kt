package com.finca.pagopeones.calculo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PayCalculatorTest {

    private val configDefault = ConfigPago()

    @Test
    fun `normal 8 horas da 10000`() {
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 8.0, configDefault)
        assertEquals(9600.0, r.subtotal, 0.0)
        assertEquals(10000, r.totalRedondeado)
    }

    @Test
    fun `dura 8 horas da 12000`() {
        val r = PayCalculator.calcular(TipoJornada.DURA, 8.0, configDefault)
        assertEquals(11600.0, r.subtotal, 0.0)
        assertEquals(12000, r.totalRedondeado)
    }

    @Test
    fun `normal 10 horas da 12000`() {
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 10.0, configDefault)
        assertEquals(12200.0, r.subtotal, 0.0)
        assertEquals(12000, r.totalRedondeado)
    }

    @Test
    fun `dura 10 horas da 14000`() {
        val r = PayCalculator.calcular(TipoJornada.DURA, 10.0, configDefault)
        assertEquals(13600.0, r.subtotal, 0.0)
        assertEquals(14000, r.totalRedondeado)
    }

    @Test
    fun `subtotal 12500 redondea a 13000`() {
        // Config artificial para forzar un subtotal exacto de 12500.
        val config = configDefault.copy(precioHoraNormal = 12500, horasJornadaBase = 1.0)
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 1.0, config)
        assertEquals(12500.0, r.subtotal, 0.0)
        assertEquals(13000, r.totalRedondeado)
    }

    @Test
    fun `normal 6 horas da 7000`() {
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 6.0, configDefault)
        assertEquals(7200.0, r.subtotal, 0.0)
        assertEquals(7000, r.totalRedondeado)
    }

    @Test
    fun `dura con extras no suma reconocimiento por defecto`() {
        val r = PayCalculator.calcular(TipoJornada.DURA, 10.0, configDefault)
        assertEquals(0, r.reconocimiento)
    }

    @Test
    fun `dura con extras suma reconocimiento si el interruptor esta activo`() {
        val config = configDefault.copy(sumarReconocimientoEnDuraConExtras = true)
        val r = PayCalculator.calcular(TipoJornada.DURA, 10.0, config)
        assertEquals(2000, r.reconocimiento)
        assertEquals(15600.0, r.subtotal, 0.0)
    }

    @Test
    fun `acepta medias horas`() {
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 9.5, configDefault)
        // base 9600 + 1.5 extra * 1300 = 11550
        assertEquals(11550.0, r.subtotal, 0.0)
    }

    @Test
    fun `redondeo siempre arriba`() {
        val config = configDefault.copy(modoRedondeo = ModoRedondeo.SIEMPRE_ARRIBA)
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 10.0, config) // 12200
        assertEquals(13000, r.totalRedondeado)
    }

    @Test
    fun `redondeo siempre abajo`() {
        val config = configDefault.copy(modoRedondeo = ModoRedondeo.SIEMPRE_ABAJO)
        val r = PayCalculator.calcular(TipoJornada.DURA, 8.0, config) // 11600
        assertEquals(11000, r.totalRedondeado)
    }

    @Test
    fun `redondeo desactivado deja el monto exacto`() {
        val config = configDefault.copy(redondeoActivo = false)
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 8.0, config)
        assertEquals(9600, r.totalRedondeado)
    }

    @Test
    fun `unidad de redondeo 100`() {
        val config = configDefault.copy(unidadRedondeo = 100)
        val r = PayCalculator.calcular(TipoJornada.NORMAL, 10.0, config) // 12200
        assertEquals(12200, r.totalRedondeado)
    }

    @Test
    fun `horas negativas o cero lanzan excepcion`() {
        assertThrows(IllegalArgumentException::class.java) {
            PayCalculator.calcular(TipoJornada.NORMAL, 0.0, configDefault)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PayCalculator.calcular(TipoJornada.NORMAL, -5.0, configDefault)
        }
    }

    @Test
    fun `horas mayores a 16 lanzan excepcion`() {
        assertThrows(IllegalArgumentException::class.java) {
            PayCalculator.calcular(TipoJornada.NORMAL, 16.5, configDefault)
        }
    }

    @Test
    fun `menos de 8 horas no suma reconocimiento por defecto`() {
        val r = PayCalculator.calcular(TipoJornada.DURA, 5.0, configDefault)
        assertEquals(6000.0, r.subtotal, 0.0)
        assertEquals(0, r.reconocimiento)
    }
}
