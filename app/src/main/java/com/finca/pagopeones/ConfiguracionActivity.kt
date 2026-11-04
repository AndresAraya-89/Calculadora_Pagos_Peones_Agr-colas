package com.finca.pagopeones

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finca.pagopeones.calculo.ConfigPago
import com.finca.pagopeones.calculo.ModoRedondeo
import com.finca.pagopeones.calculo.PayCalculator
import com.finca.pagopeones.databinding.ActivityConfiguracionBinding
import com.finca.pagopeones.util.Prefs

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracionBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = Prefs(this)

        val pinGuardado = prefs.obtenerPin()
        if (!pinGuardado.isNullOrEmpty()) {
            pedirPin(pinGuardado)
        } else {
            inicializarPantalla()
        }
    }

    private fun pedirPin(pinCorrecto: String) {
        val input = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(android.text.InputFilter.LengthFilter(4))
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.dialogo_pin_titulo)
            .setMessage(R.string.dialogo_pin_mensaje)
            .setView(input)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_aceptar) { _, _ ->
                if (input.text.toString() == pinCorrecto) {
                    inicializarPantalla()
                } else {
                    Toast.makeText(this, R.string.pin_incorrecto, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton(R.string.btn_cancelar) { _, _ -> finish() }
            .show()
    }

    private fun inicializarPantalla() {
        cargarValoresEnPantalla()

        binding.switchUsarPin.setOnCheckedChangeListener { _, isChecked ->
            binding.editPin.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.btnGuardar.setOnClickListener { guardarCambios() }
        binding.btnRestaurar.setOnClickListener { confirmarRestaurar() }
    }

    private fun cargarValoresEnPantalla() {
        val config = prefs.cargarConfig()

        binding.editHorasJornadaBase.setText(config.horasJornadaBase.toString())
        binding.editPrecioHoraNormal.setText(config.precioHoraNormal.toString())
        binding.editReconocimientoDura.setText(config.reconocimientoJornadaDura.toString())
        binding.editExtraNormal.setText(config.horaExtraNormal.toString())
        binding.editExtraDura.setText(config.horaExtraDura.toString())

        binding.switchSumarReconExtras.isChecked = config.sumarReconocimientoEnDuraConExtras
        binding.switchReconMenosJornada.isChecked = config.incluirReconocimientoSiMenosDeJornada
        binding.switchRedondeoActivo.isChecked = config.redondeoActivo

        when (config.unidadRedondeo) {
            100 -> binding.radioUnidad100.isChecked = true
            500 -> binding.radioUnidad500.isChecked = true
            else -> binding.radioUnidad1000.isChecked = true
        }

        when (config.modoRedondeo) {
            ModoRedondeo.SIEMPRE_ARRIBA -> binding.radioSiempreArriba.isChecked = true
            ModoRedondeo.SIEMPRE_ABAJO -> binding.radioSiempreAbajo.isChecked = true
            ModoRedondeo.MAS_CERCANO -> binding.radioMasCercano.isChecked = true
        }

        val pinActual = prefs.obtenerPin()
        binding.switchUsarPin.isChecked = !pinActual.isNullOrEmpty()
        binding.editPin.visibility = if (binding.switchUsarPin.isChecked) android.view.View.VISIBLE else android.view.View.GONE

        actualizarTextoUltimaModificacion()
    }

    private fun actualizarTextoUltimaModificacion() {
        binding.textoUltimaModificacion.text =
            getString(R.string.ultima_modificacion, prefs.obtenerFechaUltimaModificacionTexto())
    }

    private fun guardarCambios() {
        val horasBase = binding.editHorasJornadaBase.text.toString().toDoubleOrNull()
        val precioHora = binding.editPrecioHoraNormal.text.toString().toIntOrNull()
        val reconocimiento = binding.editReconocimientoDura.text.toString().toIntOrNull()
        val extraNormal = binding.editExtraNormal.text.toString().toIntOrNull()
        val extraDura = binding.editExtraDura.text.toString().toIntOrNull()

        if (horasBase == null || horasBase <= 0.0 || horasBase > PayCalculator.HORAS_MAXIMAS || precioHora == null || precioHora < 0 ||
            reconocimiento == null || reconocimiento < 0 || extraNormal == null || extraNormal < 0 ||
            extraDura == null || extraDura < 0
        ) {
            Toast.makeText(this, R.string.valores_invalidos, Toast.LENGTH_SHORT).show()
            return
        }

        val unidadRedondeo = when (binding.grupoUnidadRedondeo.checkedRadioButtonId) {
            binding.radioUnidad100.id -> 100
            binding.radioUnidad500.id -> 500
            else -> 1000
        }

        val modoRedondeo = when (binding.grupoModoRedondeo.checkedRadioButtonId) {
            binding.radioSiempreArriba.id -> ModoRedondeo.SIEMPRE_ARRIBA
            binding.radioSiempreAbajo.id -> ModoRedondeo.SIEMPRE_ABAJO
            else -> ModoRedondeo.MAS_CERCANO
        }

        if (binding.switchUsarPin.isChecked) {
            val pin = binding.editPin.text.toString()
            if (pin.length != 4) {
                Toast.makeText(this, R.string.pin_debe_tener_4_digitos, Toast.LENGTH_SHORT).show()
                return
            }
            prefs.guardarPin(pin)
        } else {
            prefs.guardarPin(null)
        }

        val config = ConfigPago(
            horasJornadaBase = horasBase,
            precioHoraNormal = precioHora,
            reconocimientoJornadaDura = reconocimiento,
            horaExtraNormal = extraNormal,
            horaExtraDura = extraDura,
            sumarReconocimientoEnDuraConExtras = binding.switchSumarReconExtras.isChecked,
            incluirReconocimientoSiMenosDeJornada = binding.switchReconMenosJornada.isChecked,
            redondeoActivo = binding.switchRedondeoActivo.isChecked,
            unidadRedondeo = unidadRedondeo,
            modoRedondeo = modoRedondeo
        )

        prefs.guardarConfig(config)
        actualizarTextoUltimaModificacion()
        Toast.makeText(this, R.string.cambios_guardados, Toast.LENGTH_SHORT).show()
    }

    private fun confirmarRestaurar() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirmar_restaurar_titulo)
            .setMessage(R.string.confirmar_restaurar_mensaje)
            .setPositiveButton(R.string.btn_aceptar) { _, _ ->
                prefs.restaurarValoresPorDefecto()
                prefs.guardarPin(null)
                cargarValoresEnPantalla()
                Toast.makeText(this, R.string.valores_restaurados, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}
