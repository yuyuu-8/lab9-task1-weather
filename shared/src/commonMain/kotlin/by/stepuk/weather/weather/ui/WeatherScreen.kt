package by.stepuk.weather.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.stepuk.weather.PlatformType
import by.stepuk.weather.platformType
import by.stepuk.weather.weather.model.WeatherInfo
import by.stepuk.weather.weather.presentation.WeatherUiState
import by.stepuk.weather.weather.presentation.WeatherViewModel

/**
 * Главный экран погоды. Верстка адаптируется под размер экрана (1/2/3 колонки)
 * и стилизуется под платформу (Android — карточки с тенями, iOS — плоские,
 * Desktop — чёткие рамки, Web — отзывчивая сетка).
 */
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel { WeatherViewModel() }) {
    val state by viewModel.state.collectAsState()
    val recent by viewModel.recentCities.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    WeatherScreenContent(
        state = state,
        recent = recent,
        query = query,
        onQueryChange = { query = it },
        onSearch = { viewModel.loadWeather(query) },
        onSelectRecent = { city ->
            query = city
            viewModel.loadWeather(city)
        },
    )
}

/**
 * Stateless-версия экрана: всё состояние и колбэки приходят параметрами.
 * Благодаря этому экран детерминированно тестируется UI-тестами без корутин.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun WeatherScreenContent(
    state: WeatherUiState,
    recent: List<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelectRecent: (String) -> Unit,
    platform: PlatformType = platformType(),
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            Modifier.fillMaxSize().safeContentPadding().padding(16.dp)
        ) {
            val columns = when {
                maxWidth < 500.dp -> 1
                maxWidth < 840.dp -> 2
                else -> 3
            }
            Column(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Погода",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                SearchField(
                    value = query,
                    onValueChange = onQueryChange,
                    onSearch = onSearch,
                    platform = platform,
                )
                if (recent.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    RecentCities(recent, platform, onSelect = onSelectRecent)
                }
                Spacer(Modifier.height(20.dp))
                when (val s = state) {
                    WeatherUiState.Idle -> Hint("Введите название города, чтобы увидеть погоду")
                    WeatherUiState.Loading -> LoadingView()
                    is WeatherUiState.Error -> ErrorView(s.message, onRetry = onSearch)
                    is WeatherUiState.Success -> WeatherContent(s.info, s.fromCache, columns, platform)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    platform: PlatformType,
) {
    val shape = when (platform) {
        PlatformType.ANDROID -> RoundedCornerShape(24.dp) // Android: скруглённые углы
        PlatformType.IOS -> RoundedCornerShape(12.dp)     // iOS: SearchBar-стиль
        PlatformType.DESKTOP -> RoundedCornerShape(6.dp)  // Desktop: чёткие рамки
        PlatformType.WEB -> RoundedCornerShape(14.dp)
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = shape,
        leadingIcon = { Text("🔍") },
        trailingIcon = { TextButton(onClick = onSearch) { Text("OK") } },
        placeholder = {
            Text(if (platform == PlatformType.IOS) "Поиск города" else "Введите город")
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RecentCities(cities: List<String>, platform: PlatformType, onSelect: (String) -> Unit) {
    if (platform == PlatformType.IOS) {
        // iOS: SegmentedControl для переключения между городами
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            cities.forEachIndexed { index, city ->
                SegmentedButton(
                    selected = index == 0,
                    onClick = { onSelect(city) },
                    shape = SegmentedButtonDefaults.itemShape(index, cities.size),
                ) { Text(city, maxLines = 1) }
            }
        }
    } else {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cities.forEach { city ->
                FilterChip(selected = false, onClick = { onSelect(city) }, label = { Text(city) })
            }
        }
    }
}

@Composable
private fun WeatherContent(info: WeatherInfo, fromCache: Boolean, columns: Int, platform: PlatformType) {
    Column(Modifier.fillMaxWidth()) {
        HeroCard(info, platform)
        if (fromCache) {
            Spacer(Modifier.height(8.dp))
            Text(
                "⚠ Данные из кэша (офлайн-режим)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(16.dp))
        val tiles = listOf(
            "Ощущается" to "${info.feelsLikeC}°C",
            "Влажность" to "${info.humidity}%",
            "Ветер" to "${info.windKmph} км/ч",
            "Давление" to "${info.pressure} гПа",
        )
        tiles.chunked(columns).forEach { rowTiles ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowTiles.forEach { (label, valueText) ->
                    InfoTile(label, valueText, Modifier.weight(1f), platform)
                }
                repeat(columns - rowTiles.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HeroCard(info: WeatherInfo, platform: PlatformType) {
    val shape = RoundedCornerShape(if (platform == PlatformType.DESKTOP) 8.dp else 22.dp)
    val styled = when (platform) {
        PlatformType.ANDROID -> Modifier.shadow(8.dp, shape)
        PlatformType.WEB -> Modifier.shadow(3.dp, shape)
        PlatformType.IOS -> Modifier // плоско, без тени
        PlatformType.DESKTOP -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
    }
    Box(
        Modifier.fillMaxWidth().then(styled).clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            val title = if (info.country.isNotBlank()) "${info.city}, ${info.country}" else info.city
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(info.icon, fontSize = 64.sp)
            Text("${info.temperatureC}°C", style = MaterialTheme.typography.displaySmall)
            if (info.description.isNotBlank()) {
                Text(info.description, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier, platform: PlatformType) {
    val shape = RoundedCornerShape(if (platform == PlatformType.DESKTOP) 6.dp else 16.dp)
    val styled = when (platform) {
        PlatformType.ANDROID -> Modifier.shadow(2.dp, shape)
        PlatformType.DESKTOP -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
        else -> Modifier
    }
    Box(
        modifier.then(styled).clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚠", fontSize = 40.sp)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
private fun Hint(text: String) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
