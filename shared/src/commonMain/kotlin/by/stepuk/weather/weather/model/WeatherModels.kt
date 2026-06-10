package by.stepuk.weather.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Исключение доменного слоя погоды (например, пустой ответ API).
 */
class WeatherException(message: String) : Exception(message)

// ─── DTO для ответа сервиса wttr.in (?format=j1) ──────────────────────────────

@Serializable
data class WttrResponse(
    @SerialName("current_condition") val currentCondition: List<CurrentCondition> = emptyList(),
    @SerialName("nearest_area") val nearestArea: List<NearestArea> = emptyList(),
)

@Serializable
data class CurrentCondition(
    @SerialName("temp_C") val tempC: String = "",
    @SerialName("FeelsLikeC") val feelsLikeC: String = "",
    @SerialName("humidity") val humidity: String = "",
    @SerialName("windspeedKmph") val windspeedKmph: String = "",
    @SerialName("pressure") val pressure: String = "",
    @SerialName("weatherCode") val weatherCode: String = "",
    @SerialName("weatherDesc") val weatherDesc: List<ValueItem> = emptyList(),
)

@Serializable
data class NearestArea(
    @SerialName("areaName") val areaName: List<ValueItem> = emptyList(),
    @SerialName("country") val country: List<ValueItem> = emptyList(),
)

@Serializable
data class ValueItem(@SerialName("value") val value: String = "")

// ─── Доменная модель, удобная для UI и кэша ───────────────────────────────────

@Serializable
data class WeatherInfo(
    val city: String,
    val country: String = "",
    val temperatureC: Int,
    val feelsLikeC: Int,
    val description: String,
    val humidity: Int,
    val windKmph: Int,
    val pressure: Int,
    val weatherCode: Int,
) {
    /** Эмодзи-иконка по коду погоды WWO (используется wttr.in). */
    val icon: String get() = weatherEmoji(weatherCode)
}

/**
 * Преобразует ответ API в доменную модель.
 * @throws WeatherException если в ответе нет данных о погоде.
 */
fun WttrResponse.toWeatherInfo(queryCity: String): WeatherInfo {
    val current = currentCondition.firstOrNull()
        ?: throw WeatherException("Нет данных о погоде для «$queryCity»")
    val area = nearestArea.firstOrNull()
    val cityName = area?.areaName?.firstOrNull()?.value?.takeIf { it.isNotBlank() } ?: queryCity
    return WeatherInfo(
        city = cityName,
        country = area?.country?.firstOrNull()?.value.orEmpty(),
        temperatureC = current.tempC.toIntOrNull() ?: 0,
        feelsLikeC = current.feelsLikeC.toIntOrNull() ?: 0,
        description = current.weatherDesc.firstOrNull()?.value?.trim().orEmpty(),
        humidity = current.humidity.toIntOrNull() ?: 0,
        windKmph = current.windspeedKmph.toIntOrNull() ?: 0,
        pressure = current.pressure.toIntOrNull() ?: 0,
        weatherCode = current.weatherCode.toIntOrNull() ?: 0,
    )
}

/** Сопоставление кода погоды WWO с эмодзи-иконкой. */
fun weatherEmoji(code: Int): String = when (code) {
    113 -> "☀️"
    116 -> "🌤️"
    119, 122 -> "☁️"
    143, 248, 260 -> "🌫️"
    176, 263, 266, 293, 296, 353 -> "🌦️"
    179, 182, 185, 281, 284, 311, 314, 317, 350, 362, 365, 374, 377 -> "🌨️"
    200, 386, 389 -> "⛈️"
    227, 230, 320, 323, 326, 329, 332, 335, 338, 368, 371, 392, 395 -> "❄️"
    299, 302, 305, 308, 356, 359 -> "🌧️"
    else -> "🌡️"
}
