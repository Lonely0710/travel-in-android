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
import com.bjtu.traveler.api.QWeatherApi;
import com.bjtu.traveler.api.QWeatherGeoApi;
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
        QWeatherGeoApi.fetchCityNameByLocation(
            TravelerApplication.getAppContext(), latitude, longitude,
            new QWeatherGeoApi.CityNameCallback() {
                @Override
                public void onSuccess(String cityName) {
                    QWeatherApi.fetchWeatherByLocation(
                        TravelerApplication.getAppContext(), latitude, longitude,
                        new QWeatherApi.WeatherCallback() {
                            @Override
                            public void onSuccess(WeatherData data) {
                                WeatherData newData = new WeatherData(
                                    cityName, data.getDescription(), data.getTemperature(), data.getHumidity(), data.getIconCode(), data.getWindSpeed(), data.getPressure()
                                );
                                weatherLiveData.postValue(newData);
                            }
                            @Override
                            public void onError(String errorMsg) {
                                weatherLiveData.postValue(new WeatherData(cityName, "未知天气", 0, 0, "100", 0, 0));
                            }
                        }
                    );
                }
                @Override
                public void onError(String errorMsg) {
                    QWeatherApi.fetchWeatherByLocation(
                        TravelerApplication.getAppContext(), latitude, longitude,
                        new QWeatherApi.WeatherCallback() {
                            @Override
                            public void onSuccess(WeatherData data) {
                                weatherLiveData.postValue(data);
                            }
                            @Override
                            public void onError(String errorMsg) {
                                weatherLiveData.postValue(new WeatherData("", "未知天气", 0, 0, "100", 0, 0));
                            }
                        }
                    );
                }
            }
        );
    }
} 