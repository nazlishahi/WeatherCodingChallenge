package com.jpm.codingchallenge.di

import android.content.Context
import com.jpm.codingchallenge.api.GeocoderService
import com.jpm.codingchallenge.api.WeatherApiService
import com.jpm.codingchallenge.data.Repository
import com.jpm.codingchallenge.helper.DataStoreHelper
import com.jpm.codingchallenge.usecase.GetCitySuggestionsUseCase
import com.jpm.codingchallenge.usecase.GetCoordinatesByQueryUseCase
import com.jpm.codingchallenge.usecase.GetCurrentWeatherUseCase
import com.jpm.codingchallenge.viewmodel.MainViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WeatherModule {

    @Singleton
    @Provides
    fun provideRepository(
        weatherApiService: WeatherApiService,
        geocoderService: GeocoderService
    ): Repository {
        return Repository(weatherApiService, geocoderService)
    }

    @Singleton
    @Provides
    fun provideCurrentWeatherUseCase(repository: Repository): GetCurrentWeatherUseCase {
        return GetCurrentWeatherUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideCitySuggestionsUseCase(repository: Repository): GetCitySuggestionsUseCase {
        return GetCitySuggestionsUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideCoordinatesByQueryUseCase(repository: Repository): GetCoordinatesByQueryUseCase {
        return GetCoordinatesByQueryUseCase(repository)
    }

    @Singleton
    @Provides
    fun provideDataStoreHelper(@ApplicationContext context: Context): DataStoreHelper {
        return DataStoreHelper(context)
    }

    @Singleton
    @Provides
    fun provideViewModel(
        currentWeatherUseCase: GetCurrentWeatherUseCase,
        citySuggestionsUseCase: GetCitySuggestionsUseCase,
        coordinatesByQueryUseCase: GetCoordinatesByQueryUseCase,
        dataStoreHelper: DataStoreHelper
    ): MainViewModel {
        return MainViewModel(
            currentWeatherUseCase,
            citySuggestionsUseCase,
            coordinatesByQueryUseCase,
            dataStoreHelper
        )
    }
}