package com.zakattracker.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val displayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

private val displayDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)
fun LocalDate.toEpochDayLong(): Long = this.toEpochDay()

fun Long.toDisplayDate(): String = this.toLocalDate().format(displayFormatter)

/** Formats an epoch-millis timestamp (not an epoch-day) as a local date and time string. */
fun Long.toDisplayDateTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(displayDateTimeFormatter)

fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

fun daysBetween(startEpochDay: Long, endEpochDay: Long): Long = endEpochDay - startEpochDay
