package com.example.smartfoodassistant.api

import com.example.smartfoodassistant.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {
    @GET("data/2.5/weather")
    fun getWeatherData(
        @Query("q") city: String,   // City name
        @Query("appid") apiKey: String,  // Your API key
        @Query("units") units: String = "metric" // Metric system (Celsius)
    ): Call<WeatherResponse>
}