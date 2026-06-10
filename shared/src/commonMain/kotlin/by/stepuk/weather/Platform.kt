package by.stepuk.weather

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform