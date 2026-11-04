package com.finca.pagopeones.calculo

import kotlin.math.round

/**
 * Lógica pura de cálculo de pago de jornales. No depende de clases de
 * Android para poder probarse con JUnit normal (no instrumentado).
 */
object PayCalculator {

    const val HORAS_MINIMAS = 0.0
    const val HORAS_MAXIMAS = 16.0

    /** Lanza [IllegalArgumentException] si las horas no son válidas. */
    fun validarHoras(horas: Double): String? {
        if (horas.isNaN()) return "Ingrese las horas trabajadas"
        if (horas <= HORAS_MINIMAS) return "Las horas deben ser mayores a 0"
        if (horas > HORAS_MAXIMAS) return "Las horas no pueden ser mayores a 16"
        return null
    }

    fun calcular(tipo: TipoJornada, horas: Double, config: ConfigPago): ResultadoPago {
        require(validarHoras(horas) == null) { "Horas inválidas: $horas" }

        val base = config.horasJornadaBase

        val resultado: ResultadoPago = when {
            horas < base -> {
                val monto = horas * config.precioHoraNormal
                val reconocimiento =
                    if (tipo == TipoJornada.DURA && config.incluirReconocimientoSiMenosDeJornada) {
                        config.reconocimientoJornadaDura
                    } else 0
                val subtotal = monto + reconocimiento
                ResultadoPago(
                    horasBase = horas,
                    horasExtra = 0.0,
                    montoBase = monto,
                    montoExtra = 0.0,
                    reconocimiento = reconocimiento,
                    subtotal = subtotal,
                    totalRedondeado = redondear(subtotal, config)
                )
            }

            horas == base -> {
                val monto = base * config.precioHoraNormal
                val reconocimiento = if (tipo == TipoJornada.DURA) config.reconocimientoJornadaDura else 0
                val subtotal = monto + reconocimiento
                ResultadoPago(
                    horasBase = base,
                    horasExtra = 0.0,
                    montoBase = monto,
                    montoExtra = 0.0,
                    reconocimiento = reconocimiento,
                    subtotal = subtotal,
                    totalRedondeado = redondear(subtotal, config)
                )
            }

            else -> {
                val extras = horas - base
                val montoBase = base * config.precioHoraNormal
                val tarifaExtra = if (tipo == TipoJornada.NORMAL) config.horaExtraNormal else config.horaExtraDura
                val montoExtra = extras * tarifaExtra
                val reconocimiento =
                    if (tipo == TipoJornada.DURA && config.sumarReconocimientoEnDuraConExtras) {
                        config.reconocimientoJornadaDura
                    } else 0
                val subtotal = montoBase + montoExtra + reconocimiento
                ResultadoPago(
                    horasBase = base,
                    horasExtra = extras,
                    montoBase = montoBase,
                    montoExtra = montoExtra,
                    reconocimiento = reconocimiento,
                    subtotal = subtotal,
                    totalRedondeado = redondear(subtotal, config)
                )
            }
        }
        return resultado
    }

    /** Redondea [subtotal] según la configuración de redondeo. */
    fun redondear(subtotal: Double, config: ConfigPago): Int {
        val monto = round(subtotal).toInt()
        if (!config.redondeoActivo) return monto

        val unidad = config.unidadRedondeo
        if (unidad <= 0) return monto

        val resto = monto % unidad
        if (resto == 0) return monto

        val pisoMultiplo = monto - resto
        val techoMultiplo = pisoMultiplo + unidad

        return when (config.modoRedondeo) {
            ModoRedondeo.SIEMPRE_ARRIBA -> techoMultiplo
            ModoRedondeo.SIEMPRE_ABAJO -> pisoMultiplo
            ModoRedondeo.MAS_CERCANO -> if (resto < unidad / 2) pisoMultiplo else techoMultiplo
        }
    }
}
