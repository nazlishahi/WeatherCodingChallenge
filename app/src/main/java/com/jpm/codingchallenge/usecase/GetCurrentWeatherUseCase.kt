package com.jpm.codingchallenge.usecase

import com.jpm.codingchallenge.data.LocalWeatherDataModel
import com.jpm.codingchallenge.data.Repository
import com.jpm.codingchallenge.mapper.WeatherToLocalModelMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val repository: Repository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): LocalWeatherDataModel = withContext(ioDispatcher) {
        val weatherData = repository.getCurrentWeather(latitude, longitude)
        WeatherToLocalModelMapper().mapFrom(weatherData)
    }
}