# Weather Application

## Overview

The Weather Application provides users a comprehensive and user-friendly interface to access real-time weather information. With features such as location selection, customizable temperature and wind speed units, and detailed forecasts, this application aims to inform users about weather conditions in their preferred locations.

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
- Displays forecast information for the past five days.

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

## API Usage

This application utilizes the following Weather APIs:
- [OpenWeatherMap 5 Day / 3 Hour Forecast API](https://api.openweathermap.org/data/2.5/forecast)
- [OpenWeatherMap Current Weather Data API](https://openweathermap.org/current)

## Architecture

The application follows the **Model-View-ViewModel (MVVM)** architectural pattern to ensure a clean separation of concerns, enhancing maintainability and testability.

## Technologies Used
- **Programming Language**: Kotlin
- **Platform**: Android
- **APIs**: OpenWeatherMap API
- **Architecture**: MVVM
- **Libraries**: [Retrofit, Room, Work, Flow, Coroutines, ..]

## Installation

To run this application locally, follow these steps:

1. Clone this repository:
   ```bash
   git clone https://github.com/MahmooudDarwish/weather.git
