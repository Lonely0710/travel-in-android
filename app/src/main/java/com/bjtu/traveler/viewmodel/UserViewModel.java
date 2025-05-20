package com.bjtu.traveler.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bjtu.traveler.data.repository.UserRepository;
import com.bjtu.traveler.utils.BmobUtils;
import com.bjtu.traveler.data.model.User;

import cn.bmob.v3.BmobUser;

/**
 * 用户ViewModel - 连接UI和数据仓库
 */
public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private MutableLiveData<UserRepository.BmobUserResult> loginResult = new MutableLiveData<>();
    private MutableLiveData<UserRepository.BmobUserResult> registerResult = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        // 初始化Bmob
        BmobUtils.initialize(application);
        // 获取用户仓库
        userRepository = UserRepository.getInstance();
    }

    /**
     * 用户登录
     * @param username 用户名/邮箱
     * @param password 密码
     */
    public void login(String username, String password) {
        userRepository.login(username, password, loginResult);
    }

    /**
     * 获取登录结果LiveData
     */
    public LiveData<UserRepository.BmobUserResult> getLoginResult() {
        return loginResult;
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     */
    public void register(String username, String password, String email) {
        userRepository.register(username, password, email, registerResult);
    }

    /**
     * 获取注册结果LiveData
     */
    public LiveData<UserRepository.BmobUserResult> getRegisterResult() {
        return registerResult;
    }

    /**
     * 检查用户是否已登录
     */
    public boolean isUserLoggedIn() {
        return userRepository.isUserLoggedIn();
    }

    /**
     * 获取当前登录用户
     */
    public BmobUser getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    /**
     * 退出登录
     */
    public void logout() {
        userRepository.logout();
    }

    /**
     * 使用手机号注册
     * @param email 邮箱
     * @param phone 手机号
     * @param username 用户名
     * @param password 密码
     */
    public void registerWithPhone(String email, String phone, String username, String password) {
        userRepository.registerWithPhone(email, phone, username, password, registerResult);
    }

    /**
     * 获取当前登录用户（LiveData）
     */
    public LiveData<User> getCurrentUserLiveData() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        BmobUser bmobUser = userRepository.getCurrentUser();
        if (bmobUser instanceof User) {
            userLiveData.setValue((User) bmobUser);
        } else {
            userLiveData.setValue(null);
        }
        return userLiveData;
    }
} 