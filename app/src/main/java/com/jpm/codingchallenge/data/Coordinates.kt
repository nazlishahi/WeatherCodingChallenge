package com.jpm.codingchallenge.data

import com.google.gson.annotations.SerializedName

data class Coordinates(
    @SerializedName("lon") val lon: Double? = null,
    @SerializedName("lat") val lat: Double? = null
)