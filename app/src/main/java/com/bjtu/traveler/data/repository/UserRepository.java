package com.bjtu.traveler.data.repository;

import androidx.lifecycle.MutableLiveData;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;

import com.bjtu.traveler.data.model.User;

/**
 * 用户数据仓库类 - 处理用户注册、登录等操作
 */
public class UserRepository {
    private static UserRepository instance;

    private UserRepository() {
        // 私有构造方法，防止外部实例化
    }

    /**
     * 获取UserRepository单例
     * @return UserRepository实例
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * 注册方法，支持手机号、邮箱、用户名、密码
     * @param email 邮箱
     * @param phone 手机号
     * @param username 用户名
     * @param password 密码
     * @param callback 注册结果回调
     */
    public void register(String email, String phone, String username, String password,
                         MutableLiveData<BmobUserResult> callback) {
        User user = new User();
        user.setEmail(email);
        user.setMobilePhoneNumber(phone);
        user.setUsername(username);
        user.setPassword(password);
        user.setAvatarUrl("https://api.multiavatar.com/" + username + ".png");
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User bmobUser, BmobException e) {
                if (e == null) {
                    callback.postValue(new BmobUserResult(true, bmobUser, null));
                } else {
                    callback.postValue(new BmobUserResult(false, null, e.getMessage()));
                }
            }
        });
    }

    /**
     * 统一登录用User
     * @param username 用户名/邮箱
     * @param password 密码
     * @param callback 登录结果回调
     */
    public void login(String username, String password,
                      MutableLiveData<BmobUserResult> callback) {
        User.loginByAccount(username, password, new LogInListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                handleLoginResult(user, e, callback);
            }
        });
    }

    /**
     * 处理登录结果
     */
    private void handleLoginResult(User user, BmobException e,
                                 MutableLiveData<BmobUserResult> callback) {
        if (callback == null) return; // 防止NPE
        if (e == null) {
            // 登录成功
            callback.postValue(new BmobUserResult(true, user, null));
        } else {
            // 登录失败
            callback.postValue(new BmobUserResult(false, null, e.getMessage()));
        }
    }

    /**
     * 检查用户是否已登录
     * @return 是否已登录
     */
    public boolean isUserLoggedIn() {
        return BmobUser.isLogin();
    }

    /**
     * 统一返回User
     * @return 当前用户
     */
    public User getCurrentUser() {
        BmobUser bmobUser = BmobUser.getCurrentUser(User.class);
        if (bmobUser instanceof User) {
            return (User) bmobUser;
        } else {
            return null;
        }
    }

    /**
     * 退出登录
     */
    public void logout() {
        BmobUser.logOut();
    }

    /**
     * Bmob用户操作结果包装类
     */
    public static class BmobUserResult {
        private boolean success;
        private User user;
        private String errorMsg;

        public BmobUserResult(boolean success, User user, String errorMsg) {
            this.success = success;
            this.user = user;
            this.errorMsg = errorMsg;
        }

        public boolean isSuccess() {
            return success;
        }

        public User getUser() {
            return user;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }
} 