package com.jpm.codingchallenge.api

import com.jpm.codingchallenge.data.City
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocoderService {

    companion object {
        private const val APP_ID_QUERY = "&appid=1e399783664ee007dc222e058a369a6c"
    }

    @GET("geo/1.0/direct?limit=5${APP_ID_QUERY}")
    suspend fun getCitySuggestions(@Query("q") query: String): List<City>?

    @GET("geo/1.0/direct?limit=1${APP_ID_QUERY}")
    suspend fun getLatitudeAndLongitudeByQuery(@Query("q") query: String): List<City>
}