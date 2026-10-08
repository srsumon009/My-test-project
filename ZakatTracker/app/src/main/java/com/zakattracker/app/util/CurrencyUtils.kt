package com.zakattracker.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Formats Taka amounts using South-Asian digit grouping (lakh/crore, e.g. 12,34,567) which
 * is the familiar convention in Bangladesh, prefixed with the Taka sign.
 */
private val bdGroupingFormat: DecimalFormat by lazy {
    // en_IN gives the 2-2-3 grouping used across South Asia (lakh/crore style).
    val symbols = DecimalFormatSymbols(Locale("en", "IN"))
    DecimalFormat("#,##,##0", symbols)
}

fun Double.toCurrencyString(): String {
    val rounded = this.roundToLong()
    return "৳ " + bdGroupingFormat.format(rounded)
}

fun Double.toPlainAmountString(): String {
    val rounded = (this * 100).roundToLong() / 100.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", rounded)
    }
}
