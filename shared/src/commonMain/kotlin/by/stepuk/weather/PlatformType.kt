package by.stepuk.weather

/** Тип целевой платформы — для адаптации стиля интерфейса (требование задания). */
enum class PlatformType { ANDROID, IOS, DESKTOP, WEB }

/** Возвращает платформу выполнения. Реализуется отдельно для каждой цели (expect/actual). */
expect fun platformType(): PlatformType
