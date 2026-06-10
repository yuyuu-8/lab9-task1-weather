package by.stepuk.weather.weather.data

import by.stepuk.weather.weather.model.WeatherInfo
import by.stepuk.weather.weather.model.toWeatherInfo

/** Результат запроса погоды. */
sealed interface WeatherOutcome {
    data class Success(val info: WeatherInfo, val fromCache: Boolean) : WeatherOutcome
    data class Failure(val message: String) : WeatherOutcome
}

/**
 * Репозиторий объединяет сетевой слой и кэш. При успехе сохраняет данные в кэш;
 * при ошибке сети пытается вернуть последние сохранённые данные (офлайн-режим).
 */
class WeatherRepository(
    private val api: WeatherApi = WeatherApi.default(),
    private val cache: WeatherCache = WeatherCache(),
) {

    suspend fun getWeather(city: String): WeatherOutcome {
        val trimmed = city.trim()
        if (trimmed.isEmpty()) return WeatherOutcome.Failure("Введите название города")

        return try {
            val info = api.fetchRaw(trimmed).toWeatherInfo(trimmed)
            cache.save(info)
            WeatherOutcome.Success(info, fromCache = false)
        } catch (e: Exception) {
            // Требование: обработка исключений с выводом сообщения в консоль.
            println("WeatherRepository: ошибка запроса для «$trimmed» — ${e.message}")
            val cached = cache.load(trimmed)
            if (cached != null) {
                WeatherOutcome.Success(cached, fromCache = true)
            } else {
                WeatherOutcome.Failure(e.message ?: "Не удалось получить данные о погоде")
            }
        }
    }

    fun lastCity(): String? = cache.lastCity()
}
