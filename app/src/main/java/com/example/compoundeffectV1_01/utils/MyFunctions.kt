package com.example.compoundeffectV1_01.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density
import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.roundToInt

@Composable
fun LoadingScreen(modifier:Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "LoadingScreen")
    }
}


// region کار با localDataTime
@SuppressLint("DefaultLocale")
fun timeInstanceToLocalDate(timeInstance: Calendar): LocalDateTime {
    val formattedDateTime = String.format(
        "%04d-%02d-%02dT%02d:%02d:%02d",
        timeInstance.get(Calendar.YEAR),
        timeInstance.get(Calendar.MONTH) + 1, // اضافه کردن 1 به ماه برای تصحیح مقدار
        timeInstance.get(Calendar.DAY_OF_MONTH),
        timeInstance.get(Calendar.HOUR_OF_DAY), // استفاده از HOUR_OF_DAY به‌جای HOUR
        timeInstance.get(Calendar.MINUTE),
        timeInstance.get(Calendar.SECOND)
    )

    return LocalDateTime.parse(formattedDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

fun createTimeForSampleEvents(
    timeInstance: Calendar,
    hour: Int,
    minute: Int,
    second: Int
): LocalDateTime {
    timeInstance.set(Calendar.HOUR_OF_DAY, hour)
    timeInstance.set(Calendar.HOUR_OF_DAY, hour)
    return LocalDateTime.of(
        timeInstance.get(Calendar.YEAR),
        timeInstance.get(Calendar.MONTH) + 1, // چون ماه‌ها از 0 شروع می‌شوند
        timeInstance.get(Calendar.DAY_OF_MONTH),
        timeInstance.time.hours, // استفاده از HOUR_OF_DAY برای فرمت ۲۴ ساعته
        minute,
        second
    )


}

val EventTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
val DayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E MM dd")
val HourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:00")

// endregion

// region تبدیل میلادی به شمسی

fun convertToPersianDate(localDate: LocalDate): String {

    val year = localDate.year
    val month = localDate.monthValue
    val day = localDate.dayOfMonth

    // آرایه تعداد روزهای هر ماه میلادی (سال کبیسه بررسی شده)
    val monthDays = arrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    if (isGregorianLeapYear(year)) monthDays[1] = 29

    // محاسبه تعداد روزهای سپری‌شده از ابتدای سال میلادی
    var dayOfYear = day
    for (i in 0 until (month - 1)) {
        dayOfYear += monthDays[i]
    }

    // تبدیل سال میلادی به شمسی
    val persianYear = if (dayOfYear <= 79) year - 622 else year - 621

    // محاسبه روزهای سپری‌شده از ابتدای سال شمسی
    val persianDayOfYear = if (dayOfYear > 79) {
        dayOfYear - 79
    } else {
        if (isGregorianLeapYear(year - 1)) dayOfYear + 287 else dayOfYear + 286
    }

    // محاسبه ماه و روز شمسی
    var persianMonth = 1
    var persianDay = persianDayOfYear
    while (persianDay > getPersianMonthDays(persianYear, persianMonth)) {
        persianDay -= getPersianMonthDays(persianYear, persianMonth)
        persianMonth++
    }

    return "$persianYear/$persianMonth/$persianDay"
}

// بررسی سال کبیسه میلادی
fun isGregorianLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

// تعداد روزهای هر ماه شمسی
fun getPersianMonthDays(year: Int, month: Int): Int {
    return when {
        month <= 6 -> 31
        month <= 11 -> 30
        else -> if (isPersianLeapYear(year)) 30 else 29
    }
}

// بررسی سال کبیسه شمسی
fun isPersianLeapYear(year: Int): Boolean {

    return (((year + 2346) * 683) % 2820) < 683
}

fun convertToPersianDatePretty(localDate: LocalDate): String {
    val raw = convertToPersianDate(localDate) // "yyyy/m/d"
    val parts = raw.split("/")
    val y = parts.getOrNull(0) ?: return raw
    val m = (parts.getOrNull(1)?.toIntOrNull() ?: 0).coerceAtLeast(1)
    val d = (parts.getOrNull(2)?.toIntOrNull() ?: 0).coerceAtLeast(1)
    return "%s/%02d/%02d".format(y, m, d)
}



// endregion

// region extended functions
fun Color.colorToString(): String {
    return "${red},${green},${blue},${alpha}"
}
fun String.stringToColor(): Color {
    val components = this.split(",").map { it.toFloat() }
//    require(components.size == 4) { "String representation of color should have four components: red, green, blue, alpha" }
//    val (red, green, blue, alpha) = components
    return Color(components[0], components[1], components[2], components[3])
}







// endregion



fun currentTimeHeightPx(timeInstance: Calendar, hourHeight: Float): Int {
    val currentTimeToMinute =
        (timeInstance.get(Calendar.HOUR_OF_DAY) * 60) + (timeInstance.get(Calendar.MINUTE))
    val currentTimeHeight = ((hourHeight / 60) * (currentTimeToMinute.toFloat())).roundToInt()
    return currentTimeHeight
}


fun durationMinutesSameDay(start: LocalTime, end: LocalTime): Int {
    val s = start.hour * 60 + start.minute
    val e = end.hour * 60 + end.minute
    return e - s
}

fun LocalTime.plusMinutesClamped(minutes: Int): LocalTime {
    val total = (hour * 60 + minute) + minutes
    val clamped = total.coerceIn(0, 23 * 60 + 59)
    return LocalTime.of(clamped / 60, clamped % 60)
}

fun LocalTime.ensureAfter(start: LocalTime): LocalTime {
    val s = start.hour * 60 + start.minute
    val e = hour * 60 + minute
    if (e > s) return this
    val fixed = (s + 1).coerceAtMost(23 * 60 + 59)
    return LocalTime.of(fixed / 60, fixed % 60)
}

fun LocalTime.ceilToNextQuarter(): LocalTime {
    val total = hour * 60 + minute
    val ceil = ((total + 14) / 15) * 15   // ceil to 15
    val wrapped = ceil % (24 * 60)
    return LocalTime.of(wrapped / 60, wrapped % 60)
}


fun LocalDate.toJalali(): JalaliCalendar {
    val g = GregorianCalendar(this.year, this.monthValue - 1, this.dayOfMonth)
    return JalaliCalendar(g)
}

fun JalaliCalendar.toLocalDate(): LocalDate {
    val g: Calendar = this.toGregorian()
    return LocalDate.of(
        g.get(Calendar.YEAR),
        g.get(Calendar.MONTH) + 1,
        g.get(Calendar.DAY_OF_MONTH)
    )
}

fun JalaliCalendar.toFaText(): String {
    // نمونه خروجی: 1404/02/03  (یا اگر دوست داری monthString هم می‌تونی استفاده کنی)
    val m = this.month.toString().padStart(2, '0')
    val d = this.day.toString().padStart(2, '0')
    return "${this.year}/$m/$d"
}







