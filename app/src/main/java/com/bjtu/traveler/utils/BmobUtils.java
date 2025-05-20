package com.bjtu.traveler.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import cn.bmob.v3.Bmob;

/**
 * Bmob工具类 - 用于初始化和获取Bmob AppID
 */
public class BmobUtils {
    private static final String SECRETS_FILE = "secrets.properties";
    private static final String APP_ID_KEY = "BMOB_APP_ID";
    private static String appId = null;
    private static boolean isInitialized = false;

    /**
     * 从assets/secrets.properties中读取Bmob应用ID
     * @param context 应用上下文
     * @return Bmob应用ID
     */
    public static String getAppId(Context context) {
        if (appId == null) {
            Properties properties = new Properties();
            AssetManager assetManager = context.getAssets();
            
            try (InputStream inputStream = assetManager.open(SECRETS_FILE)) {
                properties.load(inputStream);
                appId = properties.getProperty(APP_ID_KEY);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return appId;
    }

    /**
     * 初始化Bmob SDK
     * @param context 应用上下文
     */
    public static void initialize(Context context) {
        if (!isInitialized) {
            String appId = getAppId(context);
            if (appId != null && !appId.isEmpty()) {
                Bmob.initialize(context, appId);
                isInitialized = true;
            }
        }
    }

    /**
     * 检查Bmob是否已初始化
     * @return 是否已初始化
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
} 