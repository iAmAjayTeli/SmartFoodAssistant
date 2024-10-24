package com.example.smartfoodassistant.retrofit

import com.example.smartfoodassistant.api.OpenWeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/") // OpenWeather base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: OpenWeatherApi = retrofit.create(OpenWeatherApi::class.java)
}