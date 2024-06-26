package com.jpm.codingchallenge.data

import com.google.gson.annotations.SerializedName

data class SystemData(
    @SerializedName("type") val type: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("sunrise") val sunrise: Int? = null,
    @SerializedName("sunset") val sunset: Int? = null
)