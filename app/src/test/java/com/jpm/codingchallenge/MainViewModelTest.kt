package com.jpm.codingchallenge

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.jpm.codingchallenge.data.City
import com.jpm.codingchallenge.data.LocalWeatherDataModel
import com.jpm.codingchallenge.helper.DataStoreHelper
import com.jpm.codingchallenge.usecase.GetCitySuggestionsUseCase
import com.jpm.codingchallenge.usecase.GetCoordinatesByQueryUseCase
import com.jpm.codingchallenge.usecase.GetCurrentWeatherUseCase
import com.jpm.codingchallenge.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @Mock
    lateinit var currentWeatherUseCase: GetCurrentWeatherUseCase

    @Mock
    lateinit var citySuggestionsUseCase: GetCitySuggestionsUseCase

    @Mock
    lateinit var coordinatesByQueryUseCase: GetCoordinatesByQueryUseCase

    @Mock
    lateinit var dataStoreHelper: DataStoreHelper

    private lateinit var viewModel: MainViewModel

    @Mock
    lateinit var progressFlagObserver: Observer<Boolean>

    @Mock
    lateinit var uiStateObserver: Observer<MainViewModel.UiState>

    @Mock
    lateinit var lastSearchedLocationFlow: Flow<LastSearchedLocation>

    private lateinit var uiStateCaptor: KArgumentCaptor<MainViewModel.UiState>

    private lateinit var progressFlagCaptor: KArgumentCaptor<Boolean>

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var localWeatherData: LocalWeatherDataModel

    @Mock
    private lateinit var mockLocation: Location

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(dispatcher)

        uiStateCaptor = argumentCaptor<MainViewModel.UiState>()
        progressFlagCaptor = argumentCaptor<Boolean>()

        viewModel = MainViewModel(currentWeatherUseCase, citySuggestionsUseCase, coordinatesByQueryUseCase, dataStoreHelper)

        viewModel.progressFlag.observeForever(progressFlagObserver)
        viewModel.uiState.observeForever(uiStateObserver)
    }

    @After
    fun tearDown() {
        viewModel.progressFlag.removeObserver(progressFlagObserver)
        viewModel.uiState.removeObserver(uiStateObserver)
        Dispatchers.resetMain()
    }

    @Test
    fun testOnLastKnownLocationRetrieved() = runTest {
        Mockito.`when`(dataStoreHelper.getLastSearchedLocation()).thenReturn(lastSearchedLocationFlow)
        whenever(currentWeatherUseCase.invoke(mockLocation.latitude, mockLocation.longitude)).thenReturn(localWeatherData)

        viewModel.onLastKnownLocationRetrieved(mockLocation)

        Mockito.verify(progressFlagObserver, times(2))
            .onChanged(progressFlagCaptor.capture())

        Mockito.verify(currentWeatherUseCase, times(1))
            .invoke(mockLocation.latitude, mockLocation.longitude)

        Mockito.verify(uiStateObserver, times(1))
            .onChanged(uiStateCaptor.capture())

        Mockito.verify(dataStoreHelper, times(1))
            .getLastSearchedLocation()

        Mockito.verify(dataStoreHelper, times(1))
            .saveSearchedLocation(mockLocation.latitude, mockLocation.longitude)

        Assert.assertEquals(progressFlagCaptor.allValues.size, 2)
        Assert.assertEquals(progressFlagCaptor.allValues[0], true)
        Assert.assertEquals(progressFlagCaptor.allValues[1], false)

        Assert.assertEquals(uiStateCaptor.allValues.size, 1)
        Assert.assertEquals(uiStateCaptor.allValues[0], MainViewModel.UiState.Success(localWeatherData))
    }

    @Test
    fun testGetCitySuggestions() = runTest {
        val mockQuery = "San Francisco"
        val mockCityList = mock<List<City>>()

        viewModel.getCitySuggestions(mockQuery)

        Mockito.verify(progressFlagObserver, times(2))
            .onChanged(progressFlagCaptor.capture())

        Mockito.verify(citySuggestionsUseCase, times(1)).invoke(mockQuery)

        Mockito.verify(uiStateObserver, times(1))
            .onChanged(uiStateCaptor.capture())

        Assert.assertEquals(progressFlagCaptor.allValues.size, 2)
        Assert.assertEquals(progressFlagCaptor.allValues[0], true)
        Assert.assertEquals(progressFlagCaptor.allValues[1], false)

        Assert.assertEquals(uiStateCaptor.allValues.size, 1)
        Assert.assertTrue(uiStateCaptor.allValues[0] is MainViewModel.UiState.PopulateCitySuggestions)
    }

    @Test
    fun testOnCitySuggestionClicked() = runTest {
        val mockSuggestion = "San Francisco"
        val mockCityList = listOf(City(mockLocation.latitude, mockLocation.longitude, mockSuggestion, "US", "CA"))

        whenever(currentWeatherUseCase.invoke(mockLocation.latitude, mockLocation.longitude)).thenReturn(localWeatherData)
        whenever(coordinatesByQueryUseCase.invoke(mockSuggestion)).thenReturn(mockCityList)

        viewModel.onCitySuggestionClicked(mockSuggestion)

        Mockito.verify(progressFlagObserver, times(4))
            .onChanged(progressFlagCaptor.capture())

        Mockito.verify(coordinatesByQueryUseCase, times(1))
            .invoke(mockSuggestion)

        Mockito.verify(currentWeatherUseCase, times(1))
            .invoke(mockLocation.latitude, mockLocation.longitude)

        Mockito.verify(uiStateObserver, times(1))
            .onChanged(uiStateCaptor.capture())

        Mockito.verify(dataStoreHelper, never())
            .getLastSearchedLocation()

        Mockito.verify(dataStoreHelper, times(1))
            .saveSearchedLocation(mockLocation.latitude, mockLocation.longitude)

        Assert.assertEquals(progressFlagCaptor.allValues.size, 4)
        Assert.assertEquals(progressFlagCaptor.allValues[0], true)
        Assert.assertEquals(progressFlagCaptor.allValues[1], false)
        Assert.assertEquals(progressFlagCaptor.allValues[2], true)
        Assert.assertEquals(progressFlagCaptor.allValues[3], false)

        Assert.assertEquals(uiStateCaptor.allValues.size, 1)
        Assert.assertTrue(uiStateCaptor.allValues[0] is MainViewModel.UiState.Success)
    }
}