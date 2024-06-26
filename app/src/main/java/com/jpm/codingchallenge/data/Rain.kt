package com.jpm.codingchallenge.data

import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("1h") val oneHourTemperature: Double? = null
)