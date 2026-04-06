package com.rohitkhandelwal.tickr.core.time

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TaskDateFormatter {
    private const val PATTERN = "yyyy-MM-dd"

    fun format(dateMillis: Long?): String {
        if (dateMillis == null) return ""
        return dateFormat().format(Date(dateMillis))
    }

    fun parse(text: String): Long? {
        if (text.isBlank()) return null

        return try {
            dateFormat().parse(text.trim())?.time
        } catch (_: ParseException) {
            null
        }
    }

    private fun dateFormat(): SimpleDateFormat {
        return SimpleDateFormat(PATTERN, Locale.US).apply {
            isLenient = false
        }
    }
}
