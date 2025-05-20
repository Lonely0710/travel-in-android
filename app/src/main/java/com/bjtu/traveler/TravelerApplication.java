package com.bjtu.traveler;

import android.app.Application;
import com.bjtu.traveler.utils.BmobUtils;

/**
 * 应用级别的Application类，用于初始化Bmob SDK
 */
public class TravelerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化Bmob
        BmobUtils.initialize(this);
    }
} 