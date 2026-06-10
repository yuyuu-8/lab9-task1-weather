package by.stepuk.weather

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import by.stepuk.weather.weather.ui.WeatherScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        WeatherScreen()
    }
}
