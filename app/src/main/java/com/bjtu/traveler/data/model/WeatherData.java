package com.bjtu.traveler.data.model;

/**
 * 和风天气数据模型类
 */
public class WeatherData {
    private String cityName;
    private String description;
    private double temperature;
    private int humidity;
    private String iconCode; // 和风天气icon代码

    public WeatherData(String cityName, String description, double temperature, int humidity, String iconCode) {
        this.cityName = cityName;
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
        this.iconCode = iconCode;
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

    public String getIconCode() {
        return iconCode;
    }
} 