package com.jpm.codingchallenge.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WeatherData(
    @SerializedName("coord") val coordinates: Coordinates? = Coordinates(),
    @SerializedName("weather") val weather: ArrayList<Weather> = arrayListOf(),
    @SerializedName("base") val base: String? = null,
    @SerializedName("main") val main: Main? = Main(),
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("wind") val wind: Wind? = Wind(),
    @SerializedName("rain") val rain: Rain? = Rain(),
    @SerializedName("clouds") val clouds: Clouds? = Clouds(),
    @SerializedName("dt") val dt: Int? = null,
    @SerializedName("sys") val systemData: SystemData? = SystemData(),
    @SerializedName("timezone") val timezone: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("cod") val cod: Int? = null
): Serializable