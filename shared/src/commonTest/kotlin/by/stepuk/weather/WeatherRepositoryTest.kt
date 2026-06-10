package by.stepuk.weather

import by.stepuk.weather.weather.data.WeatherApi
import by.stepuk.weather.weather.data.WeatherCache
import by.stepuk.weather.weather.data.WeatherOutcome
import by.stepuk.weather.weather.data.WeatherRepository
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Интеграционные тесты репозитория: сеть (Ktor MockEngine) + кэш. */
class WeatherRepositoryTest {

    private fun apiReturning(body: String) = WeatherApi(
        HttpClient(MockEngine {
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    )

    private fun apiFailing() = WeatherApi(
        HttpClient(MockEngine { throw RuntimeException("network down") }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    )

    private fun emptyCache() = WeatherCache(MapSettings())

    @Test
    fun successReturnsFreshData() = runTest {
        val repo = WeatherRepository(apiReturning(SAMPLE_J1), emptyCache())
        val outcome = repo.getWeather("Minsk")
        assertTrue(outcome is WeatherOutcome.Success)
        assertFalse(outcome.fromCache)
        assertEquals(12, outcome.info.temperatureC)
        assertEquals("Minsk", outcome.info.city)
    }

    @Test
    fun blankCityFails() = runTest {
        val repo = WeatherRepository(apiReturning(SAMPLE_J1), emptyCache())
        val outcome = repo.getWeather("   ")
        assertTrue(outcome is WeatherOutcome.Failure)
    }

    @Test
    fun networkErrorFallsBackToCache() = runTest {
        val cache = emptyCache()
        // Сначала успешный запрос наполняет кэш.
        WeatherRepository(apiReturning(SAMPLE_J1), cache).getWeather("Minsk")
        // Затем сеть недоступна — должны получить данные из кэша.
        val outcome = WeatherRepository(apiFailing(), cache).getWeather("Minsk")
        assertTrue(outcome is WeatherOutcome.Success)
        assertTrue(outcome.fromCache)
        assertEquals(12, outcome.info.temperatureC)
    }

    @Test
    fun networkErrorWithoutCacheFails() = runTest {
        val outcome = WeatherRepository(apiFailing(), emptyCache()).getWeather("Tokyo")
        assertTrue(outcome is WeatherOutcome.Failure)
    }
}
