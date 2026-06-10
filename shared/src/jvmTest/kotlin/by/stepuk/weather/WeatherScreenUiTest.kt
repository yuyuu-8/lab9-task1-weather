package by.stepuk.weather

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import by.stepuk.weather.weather.model.WeatherInfo
import by.stepuk.weather.weather.presentation.WeatherUiState
import by.stepuk.weather.weather.ui.WeatherScreenContent
import kotlin.test.Test
import kotlin.test.assertTrue

/** UI-тесты (виджеты) экрана погоды. Выполняются на desktop (JVM). */
@OptIn(ExperimentalTestApi::class)
class WeatherScreenUiTest {

    private val sample = WeatherInfo(
        city = "Minsk",
        country = "Belarus",
        temperatureC = 12,
        feelsLikeC = 10,
        description = "Partly cloudy",
        humidity = 82,
        windKmph = 15,
        pressure = 1013,
        weatherCode = 116,
    )

    @Test
    fun showsTitleAndPlaceholderInIdle() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WeatherScreenContent(
                    state = WeatherUiState.Idle,
                    recent = emptyList(),
                    query = "",
                    onQueryChange = {},
                    onSearch = {},
                    onSelectRecent = {},
                    platform = PlatformType.ANDROID,
                )
            }
        }
        onNodeWithText("Погода").assertIsDisplayed()
        onNodeWithText("Введите город").assertIsDisplayed()
    }

    @Test
    fun showsTemperatureOnSuccess() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WeatherScreenContent(
                    state = WeatherUiState.Success(sample, fromCache = false),
                    recent = emptyList(),
                    query = "Minsk",
                    onQueryChange = {},
                    onSearch = {},
                    onSelectRecent = {},
                    platform = PlatformType.DESKTOP,
                )
            }
        }
        onNodeWithText("12°C").assertIsDisplayed()
        onNodeWithText("Minsk, Belarus").assertIsDisplayed()
    }

    @Test
    fun showsRetryOnError() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WeatherScreenContent(
                    state = WeatherUiState.Error("Сетевая ошибка"),
                    recent = emptyList(),
                    query = "x",
                    onQueryChange = {},
                    onSearch = {},
                    onSelectRecent = {},
                    platform = PlatformType.WEB,
                )
            }
        }
        onNodeWithText("Сетевая ошибка").assertIsDisplayed()
        onNodeWithText("Повторить").assertIsDisplayed()
    }

    @Test
    fun okButtonTriggersSearch() = runComposeUiTest {
        var searched = false
        setContent {
            MaterialTheme {
                WeatherScreenContent(
                    state = WeatherUiState.Idle,
                    recent = emptyList(),
                    query = "Minsk",
                    onQueryChange = {},
                    onSearch = { searched = true },
                    onSelectRecent = {},
                    platform = PlatformType.ANDROID,
                )
            }
        }
        onNodeWithText("OK").performClick()
        assertTrue(searched)
    }
}
