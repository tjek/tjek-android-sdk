package com.tjek.sdk.api

import com.tjek.sdk.TjekLogCat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*

// V2 date format is in the form yyyy-MM-dd'T'HH:mm:ssZZZZ.
// I want to be able to parse both types of offset that this format can give:
// 2000-05-06T22:00:00+0000
// 2000-05-06T22:00:00+00:00


private val offset1: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendOffset("+HHMM", "0000")
    .toFormatter(Locale.ENGLISH)
private val offset2: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendOffset("+HH:MM", "+00:00")
    .toFormatter(Locale.ENGLISH)
private var parser: DateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendOptional(offset1)
    .appendOptional(offset2)
    .parseCaseInsensitive()
    .toFormatter(Locale.ENGLISH)

fun ValidityDateStr.toValidityDate(): ValidityDate? {
    return try {
        OffsetDateTime.parse(this, parser)
    }catch (e: Exception) {
        TjekLogCat.e(e.message ?: "date parsing fail for $this")
        null
    }
}

fun minOf(d1: ValidityDate, d2: ValidityDate): ValidityDate {
    return if (d1 < d2) d1 else d2
}

fun maxOf(d1: ValidityDate, d2: ValidityDate): ValidityDate {
    return if (d1 > d2) d1 else d2
}

fun distantPast(): ValidityDate {
    return OffsetDateTime.MIN
}

fun distantFuture(): ValidityDate {
    return OffsetDateTime.MAX
}

fun TimeOfDayStr.toTimeOfDay(): TimeOfDay {
    var hours = 0
    var minutes = 0
    var seconds = 0
    try {
        // seconds could fail in case of weird patterns like 08:00:00-0100. In that case, just ignore it
        val components = split(':').map { it.toIntOrNull() ?: 0 }
        hours = if (components.isNotEmpty()) components[0] else 0
        minutes = if (components.size > 1) components[1] else 0
        seconds = if (components.size > 2) components[2] else 0
    } catch (e: Exception) {
        TjekLogCat.printStackTrace(e)
    }
    return try {
        TimeOfDay.of(hours, minutes, seconds)
    } catch (e: java.lang.Exception) {
        // if the hours were bogus, like 24:00:12
        TjekLogCat.printStackTrace(e)
        TimeOfDay.MIDNIGHT
    }
}

fun DayOfWeekStr.toDayOfWeek(): DayOfWeek {
    return DayOfWeek.valueOf(uppercase(Locale.ENGLISH))
}