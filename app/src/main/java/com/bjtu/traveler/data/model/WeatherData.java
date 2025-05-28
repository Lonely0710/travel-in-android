package com.bjtu.traveler.data.model;

/**
 * 天气数据模型类
 */
public class WeatherData {
    private String cityName;
    private String description;
    private double temperature;
    private int humidity;
    private int weatherId; // OpenWeather Weather condition ID

    public WeatherData(String cityName, String description, double temperature, int humidity, int weatherId) {
        this.cityName = cityName;
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
        this.weatherId = weatherId;
    }

    // Getters
    public String getCityName() {
        return cityName;
    }

    public String getDescription() {
        return description;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getWeatherId() {
        return weatherId;
    }
} 