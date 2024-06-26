package com.jpm.codingchallenge.mapper

import com.jpm.codingchallenge.data.LocalWeatherDataModel
import com.jpm.codingchallenge.data.WeatherData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WeatherToLocalModelMapper: Mapper<WeatherData, LocalWeatherDataModel> {

    override fun mapFrom(from: WeatherData): LocalWeatherDataModel {
        val mainWeatherData = from.main
        val weather = from.weather.firstOrNull()
        val systemData = from.systemData

        val shiftInSecondsFromUTC = from.timezone
        val formattedSunriseTime = formatTime(systemData?.sunrise, shiftInSecondsFromUTC)
        val formattedSunsetTime = formatTime(systemData?.sunset, shiftInSecondsFromUTC)

        return LocalWeatherDataModel(
            from.name ?: "",
            systemData?.country,
            mainWeatherData?.temp?.toInt() ?: 0,
            mainWeatherData?.feelsLike?.toInt() ?: 0,
            weather?.description ?: "",
            mainWeatherData?.tempMin?.toInt() ?: 0,
            mainWeatherData?.tempMax?.toInt() ?: 0,
            weather?.icon?.let { "https://openweathermap.org/img/wn/$it@2x.png" } ?: "",
            from.wind?.speed?.toInt() ?: 0,
            mainWeatherData?.pressure ?: 0,
            mainWeatherData?.humidity ?: 0,
            formattedSunriseTime,
            formattedSunsetTime
        )
    }

    private fun formatTime(timestamp: Int?, shiftInSecondsFromUTC: Int?): String {
        val sdf = SimpleDateFormat("h:mm aa", Locale.getDefault())
        val time = timestamp?.toLong()?.times(1000) ?: 0L

        val shiftInHoursFromUTC = shiftInSecondsFromUTC?.div(3600)
        val zoneId = shiftInHoursFromUTC?.let {
            when {
                it == 0 -> "GMT"
                it > 0 -> "GMT+$it"
                else -> "GMT$it"
            }
        } ?: "GMT"

        sdf.timeZone = TimeZone.getTimeZone(zoneId)

        return sdf.format(Date(time))
    }
}

interface Mapper<F, T> {
    fun mapFrom(from: F): T
}