package com.jpm.codingchallenge.viewmodel

import android.app.SearchManager
import android.database.MatrixCursor
import android.location.Location
import android.provider.BaseColumns
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jpm.codingchallenge.data.LocalWeatherDataModel
import com.jpm.codingchallenge.helper.DataStoreHelper
import com.jpm.codingchallenge.usecase.GetCitySuggestionsUseCase
import com.jpm.codingchallenge.usecase.GetCoordinatesByQueryUseCase
import com.jpm.codingchallenge.usecase.GetCurrentWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val citySuggestionsUseCase: GetCitySuggestionsUseCase,
    private val coordinatesByQueryUseCase: GetCoordinatesByQueryUseCase,
    private val dataStoreHelper: DataStoreHelper
): ViewModel() {

    private val _uiState =  MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val _progressFlag = MutableLiveData<Boolean>()
    val progressFlag: LiveData<Boolean> = _progressFlag


    fun onLastKnownLocationRetrieved(lastKnownLocation: Location) {
        viewModelScope.launch {
            dataStoreHelper.getLastSearchedLocation().firstOrNull()?.let { lastSearchedLocation ->
                if (lastSearchedLocation.latitude == 0.0 && lastSearchedLocation.longitude == 0.0) {
                    getWeatherInfo(lastKnownLocation.latitude, lastKnownLocation.longitude)
                } else {
                    getWeatherInfo(lastSearchedLocation.latitude, lastSearchedLocation.longitude)
                }
            } ?: run {
                getWeatherInfo(lastKnownLocation.latitude, lastKnownLocation.longitude)
            }
        }
    }

    fun getCitySuggestions(query: String?) {
        if (query.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            _progressFlag.postValue(true)
            runCatching {
                val suggestionList = citySuggestionsUseCase.invoke(query)
                val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))

                suggestionList?.forEachIndexed { index, city ->
                    cursor.addRow(arrayOf(index, "${city.cityName}, ${city.country}"))
                }
                _uiState.postValue(UiState.PopulateCitySuggestions(cursor))
            }.onFailure {
                _uiState.postValue(UiState.Error(it.localizedMessage))
            }
            _progressFlag.postValue(false)
        }
    }

    fun onCitySuggestionClicked(selectedSuggestion: String) {
        viewModelScope.launch {
            _progressFlag.postValue(true)
            runCatching {
                val city = coordinatesByQueryUseCase.invoke(selectedSuggestion).firstOrNull()
                city?.let {
                    getWeatherInfo(it.latitude, it.longitude)
                }
            }.onFailure {
                _uiState.postValue(UiState.Error(it.localizedMessage))
            }
            _progressFlag.postValue(false)
        }
    }

    @VisibleForTesting
    suspend fun getWeatherInfo(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _progressFlag.postValue(true)
            runCatching {
                val currentWeatherInfo = getCurrentWeatherUseCase.invoke(latitude, longitude)
                _uiState.postValue(UiState.Success(currentWeatherInfo))
                dataStoreHelper.saveSearchedLocation(latitude, longitude)
            }.onFailure {
                _uiState.postValue(UiState.Error(it.localizedMessage))
            }
            _progressFlag.postValue(false)
        }
    }

    sealed class UiState {
        data class Success(val weatherData: LocalWeatherDataModel): UiState()
        data class PopulateCitySuggestions(val cursor: MatrixCursor): UiState()
        data class Error(val errorMessage: String?): UiState()
    }
}
