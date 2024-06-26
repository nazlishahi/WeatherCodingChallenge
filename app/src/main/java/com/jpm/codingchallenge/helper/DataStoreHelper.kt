package com.jpm.codingchallenge.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.jpm.codingchallenge.LastSearchedLocation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreHelper @Inject constructor(private val context: Context) {

    companion object {
        private const val DATA_STORE_FILE_NAME = "user_prefs.pb"
    }

    private val Context.userPreferencesStore: DataStore<LastSearchedLocation> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = LastSearchedLocationSerializer
    )

    suspend fun saveSearchedLocation(latitude: Double, longitude: Double) {
        context.userPreferencesStore.updateData {
            it.toBuilder()
                .setLatitude(latitude)
                .setLongitude(longitude)
                .build()
        }
    }

    fun getLastSearchedLocation(): Flow<LastSearchedLocation> {
        return context.userPreferencesStore.data
    }
}