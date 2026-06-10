package by.stepuk.weather

import by.stepuk.weather.weather.model.WeatherException
import by.stepuk.weather.weather.model.WttrResponse
import by.stepuk.weather.weather.model.toWeatherInfo
import by.stepuk.weather.weather.model.weatherEmoji
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Модульные тесты разбора JSON и преобразования в доменную модель. */
class WeatherMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesAndMapsResponse() {
        val dto = json.decodeFromString<WttrResponse>(SAMPLE_J1)
        val info = dto.toWeatherInfo("minsk")

        assertEquals("Minsk", info.city)
        assertEquals("Belarus", info.country)
        assertEquals(12, info.temperatureC)
        assertEquals(10, info.feelsLikeC)
        assertEquals(82, info.humidity)
        assertEquals(15, info.windKmph)
        assertEquals(1013, info.pressure)
        assertEquals(116, info.weatherCode)
        assertEquals("Partly cloudy", info.description)
    }

    @Test
    fun emptyConditionThrows() {
        val dto = json.decodeFromString<WttrResponse>("""{"current_condition":[]}""")
        assertFailsWith<WeatherException> { dto.toWeatherInfo("nowhere") }
    }

    @Test
    fun fallsBackToQueryCityWhenAreaMissing() {
        val dto = json.decodeFromString<WttrResponse>(
            """{"current_condition":[{"temp_C":"5","weatherDesc":[{"value":"Clear"}]}]}"""
        )
        val info = dto.toWeatherInfo("Praha")
        assertEquals("Praha", info.city)
        assertEquals(5, info.temperatureC)
        assertEquals("Clear", info.description)
    }

    @Test
    fun mapsWeatherCodeToEmoji() {
        assertEquals("☀️", weatherEmoji(113))
        assertEquals("⛈️", weatherEmoji(200))
        assertEquals("🌡️", weatherEmoji(999)) // неизвестный код -> запасная иконка
    }
}
