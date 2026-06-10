package by.stepuk.weather.weather.data

import by.stepuk.weather.weather.model.WeatherInfo
import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Кэш погоды для офлайн-просмотра. Хранит последний успешный ответ по каждому
 * городу в key-value хранилище платформы (SharedPreferences / NSUserDefaults /
 * java.util.prefs / localStorage) через библиотеку multiplatform-settings.
 */
class WeatherCache(private val settings: Settings = Settings()) {

    private val json = Json { ignoreUnknownKeys = true }

    fun save(info: WeatherInfo) {
        settings.putString(keyFor(info.city), json.encodeToString(info))
        settings.putString(KEY_LAST_CITY, info.city)
    }

    fun load(city: String): WeatherInfo? =
        settings.getStringOrNull(keyFor(city))
            ?.let { runCatching { json.decodeFromString<WeatherInfo>(it) }.getOrNull() }

    fun lastCity(): String? = settings.getStringOrNull(KEY_LAST_CITY)

    fun clear() = settings.clear()

    private fun keyFor(city: String) = "weather_${city.trim().lowercase()}"

    companion object {
        private const val KEY_LAST_CITY = "last_city"
    }
}
