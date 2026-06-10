package by.stepuk.weather.weather.data

import by.stepuk.weather.weather.model.WttrResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Фабрика HTTP-клиента Ktor. Движок (engine) выбирается автоматически в зависимости
 * от платформы: OkHttp (Android и Desktop), Darwin (iOS), JS (Web).
 */
object HttpClientFactory {
    fun create(): HttpClient = HttpClient()
}

/**
 * Клиент публичного API погоды wttr.in. Запрос вида
 * `https://wttr.in/{город}?format=j1` возвращает JSON с текущей погодой.
 * Ключ API не требуется, что упрощает кроссплатформенную сборку и CI.
 *
 * Тело ответа читается как текст и разбирается вручную через kotlinx.serialization:
 * wttr.in отдаёт JSON с заголовком `Content-Type: text/plain`, поэтому полагаться
 * на автоматическую десериализацию по Content-Type нельзя.
 */
class WeatherApi(private val client: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun fetchRaw(city: String): WttrResponse {
        val body = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "wttr.in"
                appendPathSegments(city.trim())
                parameters.append("format", "j1")
            }
        }.bodyAsText()
        return json.decodeFromString(body)
    }

    companion object {
        fun default(): WeatherApi = WeatherApi(HttpClientFactory.create())
    }
}
