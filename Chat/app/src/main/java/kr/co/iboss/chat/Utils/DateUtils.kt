package kr.co.iboss.chat.Utils

import java.text.SimpleDateFormat
import java.util.*

/* 시간, 날짜를 Formatting 하는 Utils File */
object DateUtils {

    /* 밀리초 -> HH:mm 형태로 포맷 */
    fun formatTime(timeInMilliSec : Long) : String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return dateFormat.format(timeInMilliSec)
    }

    /* date month 형식으로 ('July 16') */
    fun formatDate(timeInMilliSec: Long) : String {
        val dateFormat = SimpleDateFormat("MMMM dd일", Locale.KOREAN)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return dateFormat.format(timeInMilliSec)
    }

    /* 디바이스 시간 기준으로 date가 오늘인지 아닌지 : Boolean */
    fun isToday(timeInMilliSec: Long) : Boolean {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val date = dateFormat.format(timeInMilliSec)
        return date == dateFormat.format(System.currentTimeMillis())
    }

    /* 시간 혹은 날짜를 return */
    fun formatDateTime(timeInMilliSec: Long) : String {
        return if (isToday(timeInMilliSec)) {
            formatTime(timeInMilliSec)
        }
        else {
            formatDate(timeInMilliSec)
        }
    }

    fun isSameDate(milliSecFirst: Long, milliSecLast : Long) : Boolean {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return dateFormat.format(milliSecFirst) == dateFormat.format(milliSecLast)
    }
}