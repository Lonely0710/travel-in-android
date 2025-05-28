package com.bjtu.traveler.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.data.repository.UserRepository;
import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bjtu.traveler.data.repository.CityRepository;

import java.util.List;

/**
 * Home 页面 ViewModel，负责管理用户数据和城市轮播图数据
 */
public class HomeViewModel extends ViewModel {
    // 用户数据 LiveData
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    // CityRepository 实例
    private final CityRepository cityRepository;
    // 城市轮播图数据 LiveData，从 Repository 获取
    private final LiveData<List<CityCarouselItem>> cityCarouselLiveData;

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
     * 刷新用户信息
     */
    public void refreshUser() {
        User user = UserRepository.getInstance().getCurrentUser();
        userLiveData.setValue(user);
    }

    /**
     * 刷新城市轮播图数据 (委托给 Repository) 了
     */
    public void refreshCityCarousel() {
        cityRepository.refreshCityCarouselItems();
    }
} 