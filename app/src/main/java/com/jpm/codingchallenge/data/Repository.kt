package com.jpm.codingchallenge.data

import com.jpm.codingchallenge.api.GeocoderService
import com.jpm.codingchallenge.api.WeatherApiService
import dagger.hilt.android.scopes.ActivityRetainedScoped

@ActivityRetainedScoped
class Repository(
    private val weatherApiService: WeatherApiService,
    private val geocoderService: GeocoderService
) {

    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData {
        return weatherApiService.getCurrentWeather(latitude, longitude)
    }

    suspend fun getCitySuggestions(query: String): List<City>? {
        return geocoderService.getCitySuggestions(query)
    }

    suspend fun getCoordinatesByQuery(query: String): List<City> {
        return geocoderService.getLatitudeAndLongitudeByQuery(query)
    }
}