package com.github.lostfound.util

import java.text.SimpleDateFormat
import java.util.*

fun Date.toFormattedTime(): String {
    val currentTime = GregorianCalendar()
    val messageTime = GregorianCalendar().apply {
        time = this@toFormattedTime
    }
    if (currentTime.year != messageTime.year)
        return SimpleDateFormat("yyyy年${if (messageTime.month >= 10) "MM" else "M"}月${if (messageTime.dayOfMonth >= 10) "dd" else "d"}日", Locale.getDefault()).format(messageTime.time)
    if (currentTime.weekOfYear > messageTime.weekOfYear)
        return SimpleDateFormat("${if (messageTime.month >= 10) "MM" else "M"}月${if (messageTime.dayOfMonth >= 10) "dd" else "d"}日 HH:mm", Locale.getDefault()).format(messageTime.time)
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime.time)
    return when (currentTime.dayOfYear - messageTime.dayOfYear) {
        0 -> time
        1 -> "昨天 $time"
        else -> "${messageTime.dayOfWeek.toDayOfWeek()} $time"
    }
}

fun String.toFormattedTime(): String {
    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this@toFormattedTime) ?: return this@toFormattedTime
    return date.toFormattedTime()
}

fun Date.toSimpleString(): String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(this)

val Calendar.year: Int
    get() = this[Calendar.YEAR]

val Calendar.dayOfMonth: Int
    get() = this[Calendar.DAY_OF_MONTH]

val Calendar.dayOfYear: Int
    get() = this[Calendar.DAY_OF_YEAR]

val Calendar.month: Int
    get() = this[Calendar.MONTH]

val Calendar.dayOfWeek: Int
    get() = this[Calendar.DAY_OF_WEEK]

val Calendar.weekOfYear: Int
    get() = this[Calendar.WEEK_OF_YEAR] - if (firstDayOfWeek == Calendar.SUNDAY && dayOfWeek == 1) 1 else 0

fun Int.toDayOfWeek(): String {
    val dayChar = charArrayOf('日', '一', '二', '三', '四', '五', '六')
    return "周${if (this in 1..7) dayChar[this - 1] else "$this"}"
}