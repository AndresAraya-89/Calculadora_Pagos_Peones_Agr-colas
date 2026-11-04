package com.finca.pagopeones

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.finca.pagopeones.calculo.ConfigPago
import com.finca.pagopeones.calculo.ModoRedondeo
import com.finca.pagopeones.calculo.PayCalculator
import com.finca.pagopeones.calculo.TipoJornada
import com.finca.pagopeones.databinding.ActivityManualBinding
import com.finca.pagopeones.util.MoneyFormatter
import com.finca.pagopeones.util.Prefs

class ManualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManualBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val config = Prefs(this).cargarConfig()
        binding.textoManual.text = construirTexto(config)
    }

    private fun construirTexto(c: ConfigPago): String {
        val pNormal = c.precioHoraNormal
        val recon = c.reconocimientoJornadaDura
        val extraN = c.horaExtraNormal
        val extraD = c.horaExtraDura
        val base = c.horasJornadaBase

        val ejemploNormal = PayCalculator.calcular(TipoJornada.NORMAL, base, c)
        val ejemploDura = PayCalculator.calcular(TipoJornada.DURA, base, c)

        val horasConExtra = (base + 2.0).coerceAtMost(PayCalculator.HORAS_MAXIMAS)
        val hayEjemploConExtra = horasConExtra > base
        val ejemploNormalExtra = if (hayEjemploConExtra) PayCalculator.calcular(TipoJornada.NORMAL, horasConExtra, c) else null
        val ejemploDuraExtra = if (hayEjemploConExtra) PayCalculator.calcular(TipoJornada.DURA, horasConExtra, c) else null

        val unidad = MoneyFormatter.formatear(c.unidadRedondeo)
        val modo = when (c.modoRedondeo) {
            ModoRedondeo.MAS_CERCANO -> "al más cercano"
            ModoRedondeo.SIEMPRE_ARRIBA -> "siempre hacia arriba"
            ModoRedondeo.SIEMPRE_ABAJO -> "siempre hacia abajo"
        }

        return buildString {
            append("¿CÓMO USAR LA APP?\n\n")
            append("1. En la pantalla principal elija el tipo de jornada: Normal o Dura.\n")
            append("2. Escriba las horas trabajadas, o use los botones + y − para ajustar de media hora en media hora.\n")
            append("3. La app calcula sola si hay horas extra (cuando trabaja más de ${formatearHoras(base)} horas).\n")
            append("4. Revise el desglose y el total. Toque \"Compartir resumen\" para enviarlo por WhatsApp.\n\n")

            append("PRECIOS VIGENTES\n\n")
            append("• Jornada base: ${formatearHoras(base)} horas\n")
            append("• Precio por hora normal: ${MoneyFormatter.formatear(pNormal)}\n")
            append("• Reconocimiento por jornada dura: ${MoneyFormatter.formatear(recon)}\n")
            append("• Hora extra en jornada normal: ${MoneyFormatter.formatear(extraN)}\n")
            append("• Hora extra en jornada dura: ${MoneyFormatter.formatear(extraD)}\n\n")

            append("EJEMPLOS CON LOS PRECIOS ACTUALES\n\n")
            append("• Normal, ${formatearHoras(base)} h: ${MoneyFormatter.formatear(ejemploNormal.totalRedondeado)}\n")
            append("• Dura, ${formatearHoras(base)} h: ${MoneyFormatter.formatear(ejemploDura.totalRedondeado)}\n")
            if (ejemploNormalExtra != null && ejemploDuraExtra != null) {
                val horasExtra = horasConExtra - base
                append("• Normal, ${formatearHoras(horasConExtra)} h (${formatearHoras(horasExtra)} horas extra): ${MoneyFormatter.formatear(ejemploNormalExtra.totalRedondeado)}\n")
                append("• Dura, ${formatearHoras(horasConExtra)} h (${formatearHoras(horasExtra)} horas extra): ${MoneyFormatter.formatear(ejemploDuraExtra.totalRedondeado)}\n\n")
            } else {
                append("\n")
            }

            append("MENOS DE UNA JORNADA COMPLETA\n\n")
            append("Si trabaja menos de ${formatearHoras(base)} horas, se le paga horas × precio por hora normal")
            if (c.incluirReconocimientoSiMenosDeJornada) {
                append(", y si la jornada es dura también se suma el reconocimiento.\n\n")
            } else {
                append(", sin el reconocimiento de jornada dura.\n\n")
            }

            append("¿CÓMO FUNCIONA EL REDONDEO?\n\n")
            if (c.redondeoActivo) {
                append("El total se redondea en unidades de $unidad, $modo.\n")
                append("Ejemplo: si el subtotal termina en una cantidad menor a la mitad de la unidad, se redondea hacia abajo; si es la mitad o más, hacia arriba (cuando el modo es \"al más cercano\").\n")
            } else {
                append("El redondeo está desactivado: se paga el monto exacto calculado.\n")
            }
        }
    }

    private fun formatearHoras(horas: Double): String {
        return if (horas == horas.toLong().toDouble()) {
            horas.toLong().toString()
        } else {
            horas.toString()
        }
    }
}
