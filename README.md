# Задание 1. Погода в реальном времени (Compose Multiplatform)

Кроссплатформенное приложение для просмотра текущей погоды. Вариант **8**, развивает приложение
погоды из задания 9 лабораторной работы 5 (там — нативный Android/Java, здесь — единый код на
Kotlin Multiplatform + Compose Multiplatform).

## Возможности

- Ввод названия города и запрос погоды.
- Текущая температура, описание, иконка (эмодзи по коду погоды), влажность, скорость ветра, давление, «ощущается как».
- **Кэширование** последнего ответа по каждому городу для офлайн-просмотра (при ошибке сети показываются данные из кэша с пометкой).
- Восстановление последнего города при запуске.
- **Адаптивная вёрстка**: 1 / 2 / 3 колонки в зависимости от ширины экрана.
- **Платформенные стили**: Android — карточки Material 3 с тенями и скруглениями; iOS — плоские карточки + SegmentedControl для переключения городов + SearchBar; Desktop — минимализм с чёткими рамками; Web — отзывчивая сетка.
- Обработка исключений с выводом сообщения в консоль.

## Технологии

- Kotlin Multiplatform + Compose Multiplatform (общий UI и логика).
- **Ktor** — HTTP-клиент (движок по платформе: OkHttp для Android и Desktop, Darwin для iOS, JS для Web).
- **kotlinx.serialization** — разбор JSON.
- **Корутины** — асинхронные вызовы, состояние через `StateFlow` + `ViewModel`.
- **multiplatform-settings** — кэш (SharedPreferences / NSUserDefaults / java.prefs / localStorage).
- API погоды: `https://wttr.in/{city}?format=j1` — **без ключа API** (удобно для CI и публичного репозитория).

## Структура

| Модуль | Назначение |
|--------|-----------|
| `shared` | Общий код: модель, сеть, кэш, ViewModel, UI (`weather/…`), `App.kt` |
| `androidApp` | Точка входа Android (`MainActivity`) |
| `desktopApp` | Точка входа Desktop (JVM) |
| `webApp` | Точка входа Web (JS / WasmJS) |
| `iosApp` | Точка входа iOS (настроена, локально не собирается — нет macOS) |

Основной код приложения — в `shared/src/commonMain/kotlin/by/stepuk/weather/weather/`.

## Сборка и запуск

> Путь к проекту **не должен содержать кириллицу** (ограничение Android Gradle Plugin на Windows).

```bash
# Android — запустить конфигурацию androidApp в Android Studio, либо:
./gradlew :androidApp:assembleDebug

# Desktop (Windows/Linux/macOS)
./gradlew :desktopApp:run

# Web (WasmJS)
./gradlew :webApp:wasmJsBrowserDevelopmentRun
# Web (JS) — альтернатива
./gradlew :webApp:jsBrowserDevelopmentRun
```

## Тесты

```bash
# Логика (unit) + интеграция (Ktor MockEngine + кэш) + UI-тесты (виджеты) на JVM:
./gradlew :shared:jvmTest

# Android host-тесты:           ./gradlew :shared:testAndroidHostTest
# Web-тесты (нужен браузер):    ./gradlew :shared:wasmJsTest  |  :shared:jsTest
```

- **Модульные** (`commonTest/WeatherMappingTest.kt`) — разбор JSON, маппинг, иконки, граничные случаи.
- **Интеграционные** (`commonTest/WeatherRepositoryTest.kt`) — сеть + кэш + офлайн-fallback через `MockEngine`.
- **UI / виджеты** (`jvmTest/WeatherScreenUiTest.kt`) — отрисовка состояний и взаимодействие (`runComposeUiTest`).

## Примечание по версиям

Версии Ktor и multiplatform-settings заданы в `gradle/libs.versions.toml`
(`ktor = "3.1.3"`, `multiplatformSettings = "1.3.0"`). Если Gradle не сможет их разрешить
(`Could not find …`) или появится предупреждение о несовместимости klib — обновите версию на
актуальную 3.x / 1.x и пересинхронизируйте проект.
