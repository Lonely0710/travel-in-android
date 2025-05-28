package com.bjtu.traveler.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.data.repository.UserRepository;
import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bjtu.traveler.data.repository.CityRepository;
import com.bjtu.traveler.data.model.WeatherData;
import com.bjtu.traveler.api.OpenWeatherApi;
import com.bjtu.traveler.TravelerApplication;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Home 页面 ViewModel，负责管理用户数据、城市轮播图数据和天气数据
 */
public class HomeViewModel extends ViewModel {
    // 用户数据 LiveData
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    // CityRepository 实例
    private final CityRepository cityRepository;
    // 城市轮播图数据 LiveData，从 Repository 获取
    private final LiveData<List<CityCarouselItem>> cityCarouselLiveData;
    // 天气数据 LiveData
    private final MutableLiveData<WeatherData> weatherLiveData = new MutableLiveData<>();

    /**
     * ViewModel 构造方法
     */
    public HomeViewModel() {
        // 直接获取当前用户并设置到 LiveData
        User user = UserRepository.getInstance().getCurrentUser();
        userLiveData.setValue(user);

        // 初始化 CityRepository 并获取城市轮播图数据的 LiveData
        cityRepository = CityRepository.getInstance();
        // 从 Repository 获取 LiveData
        cityCarouselLiveData = cityRepository.cityCarouselLiveData;
    }

    /**
     * 获取用户数据 LiveData
     * @return 用户数据 LiveData
     */
    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    /**
     * 获取城市轮播图数据 LiveData
     * @return 城市轮播图数据 LiveData
     */
    public LiveData<List<CityCarouselItem>> getCityCarouselLiveData() {
        return cityCarouselLiveData;
    }

    /**
     * 获取天气数据 LiveData
     * @return 天气数据 LiveData
     */
    public LiveData<WeatherData> getWeatherLiveData() {
        return weatherLiveData;
    }

    /**
     * 刷新用户信息
     */
    public void refreshUser() {
        User user = UserRepository.getInstance().getCurrentUser();
        userLiveData.setValue(user);
    }

    /**
     * 刷新城市轮播图数据 (委托给 Repository)
     */
    public void refreshCityCarousel() {
        cityRepository.refreshCityCarouselItems();
    }

    /**
     * 根据位置获取天气数据
     * @param latitude 纬度
     * @param longitude 经度
     */
    public void fetchWeatherByLocation(double latitude, double longitude) {
        OpenWeatherApi.fetchWeatherByLocation(latitude, longitude, new OpenWeatherApi.WeatherCallback() {
            @Override
            public void onSuccess(String weatherJson) {
                // 在 ViewModel 中解析 JSON 并更新 LiveData
                try {
                    JSONObject weatherJsonObject = new JSONObject(weatherJson);
                    String cityName = "未知城市";
                    String description = "未知天气";
                    double temp = 0;
                    int humidity = 0;
                    int weatherId = 800;
                    try { cityName = weatherJsonObject.optString("name", "未知城市"); } catch (Exception ignore) {}
                    try {
                        JSONObject main = weatherJsonObject.optJSONObject("main");
                        if (main != null) {
                            temp = main.optDouble("temp", 0);
                            humidity = main.optInt("humidity", 0);
                        }
                    } catch (Exception ignore) {}
                    try {
                        JSONObject weatherArray = weatherJsonObject.getJSONArray("weather").getJSONObject(0);
                        description = weatherArray.optString("description", "未知天气");
                        weatherId = weatherArray.optInt("id", 800);
                    } catch (Exception ignore) {}
                    WeatherData weatherData = new WeatherData(cityName, description, temp, humidity, weatherId);
                    weatherLiveData.postValue(weatherData); // 使用 postValue 更新 LiveData
                } catch (Exception e) {
                    Log.e("HomeViewModel", "Error parsing weather JSON", e);
                    // 更新 LiveData 以反映错误状态，使用默认值
                    weatherLiveData.postValue(new WeatherData("未知城市", "未知天气", 0, 0, 800));
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("HomeViewModel", "Error fetching weather data: " + errorMessage);
                // 更新 LiveData 以反映错误状态，使用默认值
                weatherLiveData.postValue(new WeatherData("未知城市", "未知天气", 0, 0, 800));
            }
        });
    }
} 