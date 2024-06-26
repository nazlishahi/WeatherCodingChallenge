package com.jpm.codingchallenge.api

import com.jpm.codingchallenge.data.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    companion object {
        private const val APP_ID_QUERY = "?appid=1e399783664ee007dc222e058a369a6c"
        private const val UNITS_QUERY = "&units=imperial"
    }

    @GET("data/2.5/weather${APP_ID_QUERY}${UNITS_QUERY}")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): WeatherData
}