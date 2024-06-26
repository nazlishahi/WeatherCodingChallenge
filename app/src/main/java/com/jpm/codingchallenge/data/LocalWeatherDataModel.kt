package com.jpm.codingchallenge.data

data class LocalWeatherDataModel(
    val cityName: String = "",
    val countryName: String? = null,
    val currentTemperature: Int = 0,
    val feelsLikeTemperature: Int = 0,
    val description: String = "",
    val minTemperature: Int = 0,
    val maxTemperature: Int = 0,
    val weatherIconUrl: String = "",
    val windSpeed: Int = 0,
    val pressure: Int = 0,
    val humidity: Int = 0,
    val sunriseTime: String = "",
    val sunsetTime: String = ""
)