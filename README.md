<div align="center">
      <h1> 
            <img src="https://github.com/user-attachments/assets/452d9922-1f0d-4946-a403-a9b33074c068" width="180px">
            <br/>
            WeatherFlow
            <br/> 
      </h1>
</div>

   <br/> 


**WeatherFlow**, A modern Android weather application built in Kotlin with Material Design, utilizing a RESTful API (WeatherAPI) for accurate, real-time weather data. Structured with MVVM architecture for maintainability and scalability, it offers real-time current weather, 7-day forecasts with daily and hourly breakdowns, weather updates for popular cities, and a robust city search feature.

   <br/>
   
## 🌟 Features
- **Real-time Weather:** Get up-to-the-minute weather updates.
- **7-Day Forecasts:** View daily and hourly weather details for the next week.
- **Popular Cities:** Check weather for trending cities worldwide.
- **City Search:** Find weather data for any city with ease.

   <br/>
  
## 🛠️ Tech Stack
- **Language:** Kotlin, Android SDK
- **API:** RESTful WeatherAPI, Retrofit
- **Architecture:** MVVM
- **UI:** Material Design
- **Storage:** SharedPreferences


<br/>

## 📱 Screenshots 
| <img src="https://github.com/user-attachments/assets/1893fc2b-1123-49b0-aaaf-4f277e92c30e" width="250"/> | <img src="https://github.com/user-attachments/assets/afa7bf0f-bf61-491f-9085-6970d7c92419" width="250"/> | <img src="https://github.com/user-attachments/assets/bb8464b2-bdb0-4519-b15f-8c7cc9a6805d" width="250"/> | <img src="https://github.com/user-attachments/assets/8bab1352-f225-4f3a-a303-f1c2a4e9b077" width="250"/> | 
| :-------------:  | :-------------:  | :-------------:  | :-------------:  |
|     **Splash Screen**     |     **Primary Dashboard**     |     **Next 7 Days**     |     **Popular Cities Forecast**     |

| <img src="https://github.com/user-attachments/assets/00be94ba-eb52-4324-b2dc-b7a68c2fcc76" width="250"/> | <img src="https://github.com/user-attachments/assets/0a4118ce-3683-43c8-b417-9042a7177131" width="250"/> | <img src="https://github.com/user-attachments/assets/602c518d-3228-4855-add8-5d8f2bc49b05" width="250"/> | <img src="https://github.com/user-attachments/assets/99b76d47-673c-48c8-a4b5-f56eab8296dd" width="250"/> |
| :-------------:  | :-------------:  | :-------------:  | :-------------:  |
|     **Search Activity**     |     **Settings Activity**     |     **About App**     |     **Notification**     |

   <br/>
   
## 📂 Package Structure
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
├── util               # Utility classes (e.g., NetworkUtils)
├── viewModel          # ViewModel classes (WeatherViewModel) for MVVM logic
└── worker             # For sending daily weather notifications
