package com.bjtu.traveler;

import android.app.Application;
import android.util.Log;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.bjtu.traveler.utils.BmobUtils;
import com.bjtu.traveler.utils.DeepSeekApiClient;

/**
 * 应用级别的Application类，用于初始化Bmob SDK
 */
public class TravelerApplication extends Application {
    private static final String TAG = "TravelerApplication";
    private static final String DEEPSEEK_API_KEY = "sk-f17f024b030445a9b74d5e987567f80d";
    public static LottieComposition chatLottieComposition;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化Bmob
        BmobUtils.initialize(this);

        // 在应用创建时初始化百度地图SDK
        SDKInitializer.setAgreePrivacy(this, true); // 必须设置隐私合规
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL); // 设置坐标类型

        // DeepSeek API 初始化
        if (!DEEPSEEK_API_KEY.startsWith("sk-")) {
            Log.e(TAG, "请在 TravelerApplication.java 中设置您的 DEEPSEEK_API_KEY！");
        } else {
            DeepSeekApiClient.init(DEEPSEEK_API_KEY);
            Log.d(TAG, "DeepSeek API Client initialized.");
        }

        // === Lottie 动画预加载 ===
        // 使用 LottieCompositionFactory 异步从 raw 资源加载 Lottie 动画
        LottieCompositionFactory.fromRawRes(this, R.raw.mapping_man)
                .addListener(composition -> {
                    // 加载成功后，将 composition 赋值给静态变量
                    chatLottieComposition = composition;
                    Log.d(TAG, "Lottie animation (mapping_man) pre-loaded successfully.");
                })
                .addFailureListener(throwable -> {
                    // 加载失败时，记录错误
                    Log.e(TAG, "Failed to pre-load Lottie animation (mapping_man): " + throwable.getMessage(), throwable);
                });
    }
} 