package by.stepuk.weather

/** Образец ответа wttr.in (?format=j1), используемый в тестах. */
internal const val SAMPLE_J1 = """
{
  "current_condition": [
    {
      "temp_C": "12",
      "FeelsLikeC": "10",
      "humidity": "82",
      "windspeedKmph": "15",
      "pressure": "1013",
      "weatherCode": "116",
      "weatherDesc": [ { "value": "Partly cloudy" } ]
    }
  ],
  "nearest_area": [
    {
      "areaName": [ { "value": "Minsk" } ],
      "country": [ { "value": "Belarus" } ]
    }
  ]
}
"""
