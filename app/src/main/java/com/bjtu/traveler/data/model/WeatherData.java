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
    private double windSpeed; // 新增：风速
    private int pressure;    // 新增：气压

    public WeatherData(String cityName, String description, double temperature, int humidity, String iconCode, double windSpeed, int pressure) {
        this.cityName = cityName;
        this.description = description;
        this.temperature = temperature;
        this.humidity = humidity;
        this.iconCode = iconCode;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
    }

    // 新增重载构造方法，兼容老代码
    public WeatherData(String cityName, String description, double temperature, int humidity, String iconCode) {
        this(cityName, description, temperature, humidity, iconCode, 0, 0);
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

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getPressure() {
        return pressure;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "cityName='" + cityName + '\'' +
                ", description='" + description + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", iconCode='" + iconCode + '\'' +
                ", windSpeed=" + windSpeed +
                ", pressure=" + pressure +
                '}';
    }
} 