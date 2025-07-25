# Weather Application

## Overview

The Weather Application provides users with a comprehensive and user-friendly interface to access real-time weather information. With features such as location selection, customizable temperature and wind speed units, and detailed forecasts, this application aims to inform users about weather conditions in their preferred locations.

## Features

### Settings Screen
- **Location Selection**: Users can choose their location via GPS or select a specific place from a map.
- **Temperature Units**: Users can select from Kelvin, Celsius, or Fahrenheit.
- **Wind Speed Units**: Options include meter/sec or miles/hour.
- **Language Preferences**: Choose between Arabic and English.

### Home Screen
- Displays current temperature, date, humidity, wind speed, pressure, cloud coverage, city name, and an appropriate weather icon.
- Provide a weather description (e.g., clear sky, light rain).
- Shows hourly weather data for the current date.
- Displays forecast information for the next five days.

### Weather Alerts Screen
- Users can set weather alerts with customizable options:
  - Duration for which the alarm is active.
  - Type of alarm (notification or default alarm sound).
  - Option to stop notifications or disable the alarm.

### Favorite Screen
- Lists favorite locations with an option to view detailed forecast information for each.
- Features a Floating Action Button (FAB) to add new favorite places.
- Users can either set a marker on a map or type the name of a city to save it to their favorites.
- Ability to remove saved locations.
  
## App Screenshots (Alarm & Notification Module)

<table>
  <tr>
    <td><b>Settings Screen</b></td>
    <td><b>Home Screen 1</b></td>
    <td><b>Home Screen 2</b></td>
    <td><b>Alarm Screen</b></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/047e2c9b-89b7-4911-8507-69dfa2fcb3f5" width="200"/></td>
    <td><img src="https://github.com/user-attachments/assets/0cd3f8a0-c48f-4e7d-8b4c-101d115c6ed9" width="200"/></td>
    <td><img src="https://github.com/user-attachments/assets/d8ede4f8-9733-4ec8-a498-8684884429d7" width="200"/></td>
    <td><img src="https://github.com/user-attachments/assets/ea1bb5a7-1997-4924-9338-73f621d6b59d" width="200"/></td>
  </tr>
  <tr>
    <td><b>Notification Creation</b></td>
    <td><b>Favorites Screen</b></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/11990410-08a3-4b7f-a771-aac4735d2ec9" width="200"/></td>
    <td><img src="https://github.com/user-attachments/assets/10b6c24e-a71a-49cc-86cf-01304c1c2530" width="200"/></td>
  </tr>
</table>

## API Usage

This application utilizes the following Weather APIs:
- [OpenWeatherMap Hourly Forecast 4 days](https://openweathermap.org/api/hourly-forecast)
- [OpenWeatherMap Current Weather Data API](https://openweathermap.org/current](https://openweathermap.org/current))
- [OpenWeatherMap Daily Forecast 16 days](https://openweathermap.org/forecast16)
- [OpenWeatherMap Climatic Forecast 30 days](https://openweathermap.org/api/forecast30)


## Architecture

The application follows the **Model-View-ViewModel (MVVM)** architectural pattern to ensure a clean separation of concerns, enhancing maintainability and testability.

## Technologies Used
- **Programming Language**: Kotlin
- **Platform**: Android
- **APIs**: OpenWeatherMap API
- **Architecture**: MVVM
- **Libraries**: [Retrofit, Room, Work, Flow, Coroutines, ..]

## Android versions
- **compileSdkVersion**: is set to 33 (Android 13).
- **minSdkVersion** is 21 (Android 5.0).
- **targetSdkVersion** is 33.

## Installation

To run this application locally, follow these steps:

1. Clone this repository:
   ```bash
   git clone https://github.com/MahmooudDarwish/weather.git
