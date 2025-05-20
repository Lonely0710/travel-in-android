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
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param callback 注册结果回调
     */
    public void register(String username, String password, String email, 
                         MutableLiveData<BmobUserResult> callback) {
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);
        
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        
        user.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                if (e == null) {
                    // 注册成功
                    callback.postValue(new BmobUserResult(true, bmobUser, null));
                } else {
                    // 注册失败
                    callback.postValue(new BmobUserResult(false, null, e.getMessage()));
                }
            }
        });
    }

    /**
     * 用户登录
     * @param username 用户名/邮箱
     * @param password 密码
     * @param callback 登录结果回调
     */
    public void login(String username, String password, 
                      MutableLiveData<BmobUserResult> callback) {
        // 判断是否是邮箱登录
        if (username.contains("@")) {
            // 邮箱登录
            BmobUser.loginByAccount(username, password, new LogInListener<BmobUser>() {
                @Override
                public void done(BmobUser user, BmobException e) {
                    handleLoginResult(user, e, callback);
                }
            });
        } else {
            // 用户名登录
            BmobUser user = new BmobUser();
            user.setUsername(username);
            user.setPassword(password);
            user.login(new SaveListener<BmobUser>() {
                @Override
                public void done(BmobUser bmobUser, BmobException e) {
                    handleLoginResult(bmobUser, e, callback);
                }
            });
        }
    }

    /**
     * 处理登录结果
     */
    private void handleLoginResult(BmobUser user, BmobException e, 
                                 MutableLiveData<BmobUserResult> callback) {
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
     * 获取当前登录用户
     * @return 当前用户
     */
    public BmobUser getCurrentUser() {
        return BmobUser.getCurrentUser();
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
        private BmobUser user;
        private String errorMsg;

        public BmobUserResult(boolean success, BmobUser user, String errorMsg) {
            this.success = success;
            this.user = user;
            this.errorMsg = errorMsg;
        }

        public boolean isSuccess() {
            return success;
        }

        public BmobUser getUser() {
            return user;
        }

        public String getErrorMsg() {
            return errorMsg;
        }
    }

    /**
     * 使用手机号注册
     * @param email 邮箱
     * @param phone 手机号
     * @param username 用户名
     * @param password 密码
     * @param callback 注册结果回调
     */
    public void registerWithPhone(String email, String phone, String username, String password, 
                                 MutableLiveData<BmobUserResult> callback) {
        User user = new User();
        user.setEmail(email);
        user.setMobilePhoneNumber(phone);
        user.setUsername(username);
        user.setPassword(password);
        user.setAvatarUrl("https://api.multiavatar.com/" + username + ".png"); // 随机头像
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
} 