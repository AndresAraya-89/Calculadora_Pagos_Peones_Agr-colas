package com.finca.pagopeones.util

/** Formatea montos en colones costarricenses: ₡12 000 (espacio como separador de miles). */
object MoneyFormatter {

    fun formatear(monto: Int): String {
        val signo = if (monto < 0) "-" else ""
        val texto = kotlin.math.abs(monto).toString()
        val sb = StringBuilder()
        for ((index, c) in texto.withIndex()) {
            val posicionDesdeDerecha = texto.length - index
            if (index != 0 && posicionDesdeDerecha % 3 == 0) {
                sb.append(' ')
            }
            sb.append(c)
        }
        return "₡$signo$sb"
    }

    fun formatearSinSimbolo(monto: Int): String = formatear(monto).removePrefix("₡")
}
