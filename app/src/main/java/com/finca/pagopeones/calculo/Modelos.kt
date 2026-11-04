package com.finca.pagopeones.calculo

/** Tipo de jornada elegido por el usuario. */
enum class TipoJornada {
    NORMAL,
    DURA
}

/** Modo de redondeo del total final. */
enum class ModoRedondeo {
    MAS_CERCANO,
    SIEMPRE_ARRIBA,
    SIEMPRE_ABAJO
}

/**
 * Parámetros de cálculo, todos editables desde Configuración.
 * Los montos se manejan en colones (enteros).
 */
data class ConfigPago(
    val horasJornadaBase: Double = 8.0,
    val precioHoraNormal: Int = 1200,
    val reconocimientoJornadaDura: Int = 2000,
    val horaExtraNormal: Int = 1300,
    val horaExtraDura: Int = 2000,
    // Regla 4: por defecto NO se suma el reconocimiento cuando la jornada
    // dura tiene horas extra. El usuario puede activarlo.
    val sumarReconocimientoEnDuraConExtras: Boolean = false,
    // Caso especial de menos de 8 h en jornada dura: por defecto no se
    // reconoce el plus de jornada dura, solo horas x precio normal.
    val incluirReconocimientoSiMenosDeJornada: Boolean = false,
    val redondeoActivo: Boolean = true,
    val unidadRedondeo: Int = 1000,
    val modoRedondeo: ModoRedondeo = ModoRedondeo.MAS_CERCANO
)

/** Desglose completo de un cálculo de pago. */
data class ResultadoPago(
    val horasBase: Double,
    val horasExtra: Double,
    val montoBase: Double,
    val montoExtra: Double,
    val reconocimiento: Int,
    val subtotal: Double,
    val totalRedondeado: Int
)
