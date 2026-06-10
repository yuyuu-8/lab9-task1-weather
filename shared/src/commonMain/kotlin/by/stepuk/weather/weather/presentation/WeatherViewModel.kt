package by.stepuk.weather.weather.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.stepuk.weather.weather.data.WeatherOutcome
import by.stepuk.weather.weather.data.WeatherRepository
import by.stepuk.weather.weather.model.WeatherInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Состояние экрана погоды. */
sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val info: WeatherInfo, val fromCache: Boolean) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

/**
 * ViewModel экрана погоды. Асинхронные вызовы выполняются в [viewModelScope]
 * через корутины; состояние публикуется через [StateFlow].
 */
class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    private val _recentCities = MutableStateFlow<List<String>>(emptyList())
    val recentCities: StateFlow<List<String>> = _recentCities.asStateFlow()

    init {
        // При запуске показываем погоду последнего просмотренного города (из кэша).
        repository.lastCity()?.let { loadWeather(it) }
    }

    fun loadWeather(city: String) {
        val trimmed = city.trim()
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            when (val outcome = repository.getWeather(trimmed)) {
                is WeatherOutcome.Success -> {
                    _state.value = WeatherUiState.Success(outcome.info, outcome.fromCache)
                    addRecent(outcome.info.city)
                }
                is WeatherOutcome.Failure ->
                    _state.value = WeatherUiState.Error(outcome.message)
            }
        }
    }

    private fun addRecent(city: String) {
        _recentCities.update { current -> (listOf(city) + current).distinct().take(4) }
    }
}
