package com.bsobat.inventra.ui.ext

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.formatPricePerUom(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    return formatter.format(this)
}


fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(this * 1000))
}

fun String.toFormattedDateTime(): String {
    return this.toLongOrNull()?.toFormattedDateTime() ?: ""
}