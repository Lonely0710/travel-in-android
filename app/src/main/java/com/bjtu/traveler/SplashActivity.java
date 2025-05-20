package com.bjtu.traveler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

public class SplashActivity extends BaseActivity {

    private static final long SPLASH_DISPLAY_LENGTH = 5000; // 闪屏页显示时间（毫秒）

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 延迟SPLASH_DISPLAY_LENGTH后跳转到AuthActivity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 检查用户登录状态（此处为模拟，实际应检查Bmob或其他登录状态）
                boolean isLoggedIn = false; // 假设用户未登录

                Intent mainIntent;
                if (isLoggedIn) {
                    // 如果已登录，跳转到MainActivity
                    mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // 如果未登录，跳转到AuthActivity
                    mainIntent = new Intent(SplashActivity.this, AuthActivity.class);
                }
                startActivity(mainIntent);
                finish(); // 关闭SplashActivity
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}