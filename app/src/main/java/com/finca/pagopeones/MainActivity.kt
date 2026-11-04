package com.finca.pagopeones

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.finca.pagopeones.calculo.ConfigPago
import com.finca.pagopeones.calculo.PayCalculator
import com.finca.pagopeones.calculo.ResultadoPago
import com.finca.pagopeones.calculo.TipoJornada
import com.finca.pagopeones.databinding.ActivityMainBinding
import com.finca.pagopeones.util.MoneyFormatter
import com.finca.pagopeones.util.Prefs
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs
    private var config: ConfigPago = ConfigPago()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)

        binding.grupoTipoJornada.setOnCheckedChangeListener { _, _ -> recalcular() }

        binding.btnMasHoras.setOnClickListener { ajustarHoras(0.5) }
        binding.btnMenosHoras.setOnClickListener { ajustarHoras(-0.5) }

        binding.editHoras.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                recalcular()
            }
        })

        binding.btnCompartir.setOnClickListener { compartirResumen() }
        binding.btnConfiguracion.setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }
        binding.btnManual.setOnClickListener {
            startActivity(Intent(this, ManualActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        config = prefs.cargarConfig()
        recalcular()
    }

    private fun tipoSeleccionado(): TipoJornada =
        if (binding.radioDura.isChecked) TipoJornada.DURA else TipoJornada.NORMAL

    private fun horasActuales(): Double =
        binding.editHoras.text.toString().replace(",", ".").toDoubleOrNull() ?: Double.NaN

    private fun ajustarHoras(delta: Double) {
        val actual = horasActuales().let { if (it.isNaN()) 0.0 else it }
        var nuevo = actual + delta
        if (nuevo < 0.0) nuevo = 0.0
        if (nuevo > PayCalculator.HORAS_MAXIMAS) nuevo = PayCalculator.HORAS_MAXIMAS
        binding.editHoras.setText(formatearHoras(nuevo))
    }

    private fun formatearHoras(horas: Double): String = String.format(Locale.US, "%.1f", horas)

    private fun recalcular() {
        val horas = horasActuales()
        val error = PayCalculator.validarHoras(horas)
        if (error != null) {
            binding.textoError.text = error
            binding.textoError.visibility = android.view.View.VISIBLE
            limpiarResultado()
            return
        }
        binding.textoError.visibility = android.view.View.GONE

        val resultado = PayCalculator.calcular(tipoSeleccionado(), horas, config)
        mostrarResultado(resultado)
    }

    private fun limpiarResultado() {
        binding.valorHorasBase.text = "--"
        binding.valorHorasExtra.text = "--"
        binding.valorReconocimiento.text = "--"
        binding.valorSubtotal.text = "--"
        binding.valorTotal.text = "--"
    }

    private fun mostrarResultado(r: ResultadoPago) {
        binding.valorHorasBase.text = formatearHoras(r.horasBase) + " h"
        binding.valorHorasExtra.text = formatearHoras(r.horasExtra) + " h"
        binding.valorReconocimiento.text = MoneyFormatter.formatear(r.reconocimiento)
        binding.valorSubtotal.text = MoneyFormatter.formatear(r.subtotal.toInt())
        binding.valorTotal.text = MoneyFormatter.formatear(r.totalRedondeado)
    }

    private fun compartirResumen() {
        val horas = horasActuales()
        if (PayCalculator.validarHoras(horas) != null) return

        val tipo = tipoSeleccionado()
        val resultado = PayCalculator.calcular(tipo, horas, config)
        val nombreTipo = if (tipo == TipoJornada.DURA) getString(R.string.jornada_dura) else getString(R.string.jornada_normal)

        val resumen = buildString {
            append("Resumen de pago\n")
            append("Jornada: $nombreTipo\n")
            append("Horas trabajadas: ${formatearHoras(horas)} h\n")
            if (resultado.horasExtra > 0.0) {
                append("Horas extra: ${formatearHoras(resultado.horasExtra)} h\n")
            }
            if (resultado.reconocimiento > 0) {
                append("Reconocimiento jornada dura: ${MoneyFormatter.formatear(resultado.reconocimiento)}\n")
            }
            append("Subtotal: ${MoneyFormatter.formatear(resultado.subtotal.toInt())}\n")
            append("TOTAL A PAGAR: ${MoneyFormatter.formatear(resultado.totalRedondeado)}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, resumen)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.titulo_compartir)))
    }
}
