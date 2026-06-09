package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class CityWeatherInfo(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val region: String
)

val PREDEFINED_CITIES = listOf(
    CityWeatherInfo("LONDON", 51.5074, -0.1278, "UK"),
    CityWeatherInfo("TOKYO", 35.6762, 139.6503, "Japan"),
    CityWeatherInfo("NEW YORK", 40.7128, -74.0060, "USA"),
    CityWeatherInfo("NEW DELHI", 28.6139, 77.2090, "India"),
    CityWeatherInfo("REYKJAVIK", 64.1466, -21.9426, "Iceland"),
    CityWeatherInfo("SYDNEY", -33.8688, 151.2093, "Australia")
)

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather
)

data class CurrentWeather(
    val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "weather_code") val weatherCode: Int
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code"
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun create(): WeatherApi {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(WeatherApi::class.java)
        }
    }
}
