package com.jpm.codingchallenge.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class City(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("name") val cityName: String,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?,
): Serializable
