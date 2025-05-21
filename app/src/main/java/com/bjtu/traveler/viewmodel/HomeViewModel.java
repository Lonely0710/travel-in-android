package com.bjtu.traveler.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.data.repository.UserRepository;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public HomeViewModel() {
        // 直接获取User类型
        User user = UserRepository.getInstance().getCurrentUser();
        userLiveData.setValue(user);
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    // 刷新用户信息
    public void refreshUser() {
        User user = UserRepository.getInstance().getCurrentUser();
        userLiveData.setValue(user);
    }
} 