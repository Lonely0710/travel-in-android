package com.bjtu.traveler.api;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bjtu.traveler.TravelerApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OpenWeather API 调用类
 * 负责构建请求，发送网络请求获取天气数据，并处理基本响应
 */
public class OpenWeatherApi {

    private static final String TAG = "OpenWeatherApi";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final OkHttpClient client = new OkHttpClient(); // 单例 OkHttpClient

    /**
     * 获取当前位置的天气数据
     * @param latitude 纬度
     * @param longitude 经度
     * @param callback 天气数据获取结果的回调
     */
    public static void fetchWeatherByLocation(double latitude, double longitude, @NonNull WeatherCallback callback) {
        String apiKey = TravelerApplication.OPENWEATHER_API_KEY; // 从Application类获取API Key
        if (apiKey == null || apiKey.isEmpty()) {
            Log.e(TAG, "OpenWeather API Key 未设置！");
            callback.onError("API密钥未设置");
            return;
        }

        // 构建 OpenWeather API URL
        String lang = "zh_cn"; // 设置语言为中文
        String units = "metric"; // 设置单位为摄氏度 (摄氏度，米/秒)
        String weatherUrl = Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter("lat", String.valueOf(latitude))
                .appendQueryParameter("lon", String.valueOf(longitude))
                .appendQueryParameter("appid", apiKey)
                .appendQueryParameter("lang", lang)
                .appendQueryParameter("units", units)
                .build().toString();

        Log.d(TAG, "Fetching weather data from: " + weatherUrl);

        // 构建 OkHttp 请求
        Request request = new Request.Builder()
                .url(weatherUrl)
                .build();

        // 执行异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Error fetching weather data", e);
                callback.onError("网络请求失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 请求成功
                    String weatherJsonString = Objects.requireNonNull(response.body()).string();
                    Log.d(TAG, "Weather Raw Response: " + weatherJsonString);
                    try {
                        JSONObject weatherJson = new JSONObject(weatherJsonString);
                        // 在这里可以初步解析 JSON 并传递结构化的数据，或者直接传递 JSON 字符串让调用者解析
                        // 为了简单，这里直接传递 JSON 字符串，让 Fragment 或 ViewModel 去解析具体字段
                        callback.onSuccess(weatherJsonString);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing weather JSON string in API class", e);
                        callback.onError("解析数据失败");
                    } finally {
                        // 关闭响应体
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                } else {
                    // 请求失败 (非 2xx 状态码)
                    Log.e(TAG, "OpenWeather API request failed: " + response.code() + " " + response.message());
                    callback.onError("获取天气失败: " + response.code());
                     // 关闭响应体
                    if (response.body() != null) {
                       response.body().close();
                    }
                }
            }
        });
    }

    /**
     * 天气数据获取结果回调接口
     */
    public interface WeatherCallback {
        void onSuccess(String weatherJson);
        void onError(String errorMessage);
    }
} 