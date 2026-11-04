package com.finca.pagopeones.util

import android.content.Context
import android.content.SharedPreferences
import com.finca.pagopeones.calculo.ConfigPago
import com.finca.pagopeones.calculo.ModoRedondeo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Envuelve SharedPreferences para guardar la configuración de cálculo. */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    fun cargarConfig(): ConfigPago {
        val defecto = ConfigPago()
        return ConfigPago(
            horasJornadaBase = sp.getFloat(K_HORAS_BASE, defecto.horasJornadaBase.toFloat()).toDouble(),
            precioHoraNormal = sp.getInt(K_PRECIO_HORA, defecto.precioHoraNormal),
            reconocimientoJornadaDura = sp.getInt(K_RECONOCIMIENTO, defecto.reconocimientoJornadaDura),
            horaExtraNormal = sp.getInt(K_EXTRA_NORMAL, defecto.horaExtraNormal),
            horaExtraDura = sp.getInt(K_EXTRA_DURA, defecto.horaExtraDura),
            sumarReconocimientoEnDuraConExtras = sp.getBoolean(K_SUMAR_RECON_EXTRAS, defecto.sumarReconocimientoEnDuraConExtras),
            incluirReconocimientoSiMenosDeJornada = sp.getBoolean(K_RECON_MENOS_JORNADA, defecto.incluirReconocimientoSiMenosDeJornada),
            redondeoActivo = sp.getBoolean(K_REDONDEO_ACTIVO, defecto.redondeoActivo),
            unidadRedondeo = sp.getInt(K_UNIDAD_REDONDEO, defecto.unidadRedondeo),
            modoRedondeo = ModoRedondeo.valueOf(
                sp.getString(K_MODO_REDONDEO, defecto.modoRedondeo.name) ?: defecto.modoRedondeo.name
            )
        )
    }

    fun guardarConfig(config: ConfigPago) {
        sp.edit()
            .putFloat(K_HORAS_BASE, config.horasJornadaBase.toFloat())
            .putInt(K_PRECIO_HORA, config.precioHoraNormal)
            .putInt(K_RECONOCIMIENTO, config.reconocimientoJornadaDura)
            .putInt(K_EXTRA_NORMAL, config.horaExtraNormal)
            .putInt(K_EXTRA_DURA, config.horaExtraDura)
            .putBoolean(K_SUMAR_RECON_EXTRAS, config.sumarReconocimientoEnDuraConExtras)
            .putBoolean(K_RECON_MENOS_JORNADA, config.incluirReconocimientoSiMenosDeJornada)
            .putBoolean(K_REDONDEO_ACTIVO, config.redondeoActivo)
            .putInt(K_UNIDAD_REDONDEO, config.unidadRedondeo)
            .putString(K_MODO_REDONDEO, config.modoRedondeo.name)
            .putLong(K_ULTIMA_MODIFICACION, System.currentTimeMillis())
            .apply()
    }

    fun restaurarValoresPorDefecto() {
        guardarConfig(ConfigPago())
    }

    fun obtenerFechaUltimaModificacionTexto(): String {
        val millis = sp.getLong(K_ULTIMA_MODIFICACION, 0L)
        if (millis == 0L) return "Sin cambios registrados"
        val formato = SimpleDateFormat("d 'de' MMMM 'de' yyyy, h:mm a", Locale("es", "CR"))
        return formato.format(Date(millis))
    }

    fun obtenerPin(): String? = sp.getString(K_PIN, null)

    fun guardarPin(pin: String?) {
        sp.edit().putString(K_PIN, pin).apply()
    }

    companion object {
        private const val NOMBRE_ARCHIVO = "pago_peones_prefs"

        private const val K_HORAS_BASE = "horas_jornada_base"
        private const val K_PRECIO_HORA = "precio_hora_normal"
        private const val K_RECONOCIMIENTO = "reconocimiento_jornada_dura"
        private const val K_EXTRA_NORMAL = "hora_extra_normal"
        private const val K_EXTRA_DURA = "hora_extra_dura"
        private const val K_SUMAR_RECON_EXTRAS = "sumar_reconocimiento_dura_extras"
        private const val K_RECON_MENOS_JORNADA = "reconocimiento_menos_jornada"
        private const val K_REDONDEO_ACTIVO = "redondeo_activo"
        private const val K_UNIDAD_REDONDEO = "unidad_redondeo"
        private const val K_MODO_REDONDEO = "modo_redondeo"
        private const val K_ULTIMA_MODIFICACION = "ultima_modificacion"
        private const val K_PIN = "pin_seguridad"
    }
}
