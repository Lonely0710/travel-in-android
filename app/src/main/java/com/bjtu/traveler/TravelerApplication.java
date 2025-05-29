package com.bjtu.traveler;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.bjtu.traveler.utils.BmobUtils;
import com.bjtu.traveler.utils.DeepSeekApiClient;
import com.bjtu.traveler.data.cache.CityCache;

/**
 * 应用级别的Application类，用于进行应用全局的初始化工作
 */
public class TravelerApplication extends Application {
    private static final String TAG = "TravelerApplication";
    public static LottieComposition chatLottieComposition;
    public static String BAIDU_LBS_API_KEY = null;
    public static String DEEPSEEK_API_KEY = null;
    public static String UNSPLASH_ACCESS_KEY = null; // 添加静态变量用于存储 Unsplash Access Key
    public static String QWEATHER_API_KEY = null; // 添加静态变量用于存储和风天气API Key
    private static TravelerApplication sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
        // 初始化Bmob
        BmobUtils.initialize(this);

        // === 初始化 CityCache ===
        CityCache.initialize(this);

        // === 读取secrets.properties中的API密钥 ===
        try {
            java.util.Properties properties = new java.util.Properties();
            java.io.InputStream inputStream = getAssets().open("secrets.properties");
            properties.load(inputStream);
            BAIDU_LBS_API_KEY = properties.getProperty("BAIDU_LBS_API_KEY");
            DEEPSEEK_API_KEY = properties.getProperty("DEEPSEEK_API_KEY");
            UNSPLASH_ACCESS_KEY = properties.getProperty("UNSPLASH_ACCESS_KEY"); // 读取 Unsplash Access Key
            QWEATHER_API_KEY = properties.getProperty("QWEATHER_API_KEY"); // 读取和风天气API Key
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "读取secrets.properties失败: " + e.getMessage(), e);
        }

        // 在应用创建时初始化百度地图SDK
        SDKInitializer.setAgreePrivacy(this, true); // 必须设置隐私合规
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL); // 设置坐标类型

        // DeepSeek API 初始化
        if (DEEPSEEK_API_KEY == null || !DEEPSEEK_API_KEY.startsWith("sk-")) {
            Log.e(TAG, "请在 assets/secrets.properties 中设置您的 DEEPSEEK_API_KEY！");
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

        // === 动态设置百度API Key到meta-data ===
        if (BAIDU_LBS_API_KEY != null && !BAIDU_LBS_API_KEY.isEmpty()) {
            try {
                android.content.pm.ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), android.content.pm.PackageManager.GET_META_DATA);
                if (appInfo.metaData != null) {
                    appInfo.metaData.putString("com.baidu.lbsapi.API_KEY", BAIDU_LBS_API_KEY);
                    Log.d(TAG, "百度API Key已动态注入meta-data: " + BAIDU_LBS_API_KEY);
                }
            } catch (Exception e) {
                Log.e(TAG, "动态注入百度API Key失败: " + e.getMessage(), e);
            }
        }

        // TODO: 检查 Unsplash Access Key 是否读取成功
        if (UNSPLASH_ACCESS_KEY == null || UNSPLASH_ACCESS_KEY.isEmpty()) {
             Log.e(TAG, "请在 assets/secrets.properties 中设置您的 UNSPLASH_ACCESS_KEY！");
        }

        // 检查 QWeather API Key 是否读取成功
        if (QWEATHER_API_KEY == null || QWEATHER_API_KEY.isEmpty()) {
            Log.e(TAG, "请在 assets/secrets.properties 中设置您的 QWEATHER_API_KEY！");
        }
    }

    public static Context getAppContext() {
        return sAppContext;
    }
} 