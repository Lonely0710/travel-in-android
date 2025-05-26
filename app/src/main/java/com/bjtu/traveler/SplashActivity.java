package com.bjtu.traveler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

import com.bjtu.traveler.viewmodel.UserViewModel;
import com.bjtu.traveler.data.repository.UserRepository;
import cn.bmob.v3.Bmob;
import com.bjtu.traveler.data.cache.AttractionCache;
import com.bjtu.traveler.data.model.CityList;
import com.bjtu.traveler.api.OverpassApi;
import com.bjtu.traveler.data.model.Attraction;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SplashActivity extends BaseActivity {

    private static final long SPLASH_DISPLAY_LENGTH = 1500; // 闪屏页显示时间（毫秒）

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 跳过登录流程，直接用测试账号登录
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 测试账号
            String testEmail = "aaa@123.com";
            String testPassword = "12345678";
            UserRepository.getInstance().login(testEmail, testPassword, null);
            // 直接跳转到MainActivity
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);


        // 原始登录逻辑：
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // 检查用户登录状态（此处为模拟，实际应检查Bmob或其他登录状态）
//                boolean isLoggedIn = false; // 假设用户未登录
//
//                Intent mainIntent;
//                if (isLoggedIn) {
//                    // 如果已登录，跳转到MainActivity
//                    mainIntent = new Intent(SplashActivity.this, MainActivity.class);
//                } else {
//                    // 如果未登录，跳转到AuthActivity
//                    mainIntent = new Intent(SplashActivity.this, AuthActivity.class);
//                }
//                startActivity(mainIntent);
//                finish(); // 关闭SplashActivity
//            }
//        }, SPLASH_DISPLAY_LENGTH);
    }
}

