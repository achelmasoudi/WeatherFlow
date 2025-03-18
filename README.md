<div align="center">
      <h1> 
            <img src="https://github.com/user-attachments/assets/452d9922-1f0d-4946-a403-a9b33074c068" width="165px">
            <br/>
            WeatherFlow
            <br/> 
      </h1>
</div>

   <br/> 


**WeatherFlow**, A modern Android weather application built in Kotlin with Material Design, utilizing a RESTful API (WeatherAPI) for accurate, real-time weather data. Structured with MVVM architecture for maintainability and scalability, it offers real-time current weather, 7-day forecasts with daily and hourly breakdowns, weather updates for popular cities, and a robust city search feature.


## Features
- **Real-time Weather**: Get up-to-the-minute weather updates.
- **7-Day Forecasts**: View daily and hourly weather details for the next week.
- **Popular Cities**: Check weather for trending cities worldwide.
- **City Search**: Find weather data for any city with ease.

## Tech Stack
- **Language**: Kotlin, Android SDK
- **API**: RESTful WeatherAPI, Retrofit
- **Architecture**: MVVM
- **UI**: Material Design
- **Storage**: SharedPreferences

## Package Structure
```plaintext
com.achelmas.weatherflow
├── data
│   ├── model          # Data classes for API responses (e.g., WeatherResponse, Forecast)
│   ├── repository     # WeatherRepository for API calls and data management
│   └── source
│       ├── local      # SharedPreferences for local storage (CityPreferences)
│       └── remote     # Retrofit setup and API service (ApiClient, WeatherApiService)
├── ui
│   ├── main           # MainActivity and core UI components
│   │   └── adapter    # RecyclerView adapters (CitySearchAdapter, HourlyWeatherAdapter)
│   ├── next7days      # UI for 7-day forecasts
│   │   └── adapter    # RecyclerView adapter (Next7DaysAdapter)
│   ├── popularcities  # UI for popular cities’ weather
│   │   └── adapter    # RecyclerView adapter (PopularCitiesWeatherAdapter)
│   ├── settings       # SettingsActivity for user preferences
│   └── splash         # Splash screen UI
├── util               # Utility classes (e.g., NetworkUtils, WeatherNotificationWorker)
└── viewModel          # ViewModel classes (WeatherViewModel) for MVVM logic
