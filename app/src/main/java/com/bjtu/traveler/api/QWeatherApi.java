package com.bjtu.traveler.api;

import android.content.Context;
import android.util.Log;
import com.bjtu.traveler.data.model.WeatherData;
import com.bjtu.traveler.TravelerApplication;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

public class QWeatherApi {
    private static final String TAG = "QWeatherApi";
    private static final String BASE_URL = "https://devapi.qweather.com/v7/weather/now";
    // 你可以用Context.getAssets()或BuildConfig等方式获取APIKEY
    private static String getApiKey(Context context) {
        if (TravelerApplication.QWEATHER_API_KEY != null && !TravelerApplication.QWEATHER_API_KEY.isEmpty()) {
            return TravelerApplication.QWEATHER_API_KEY;
        }
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(context.getAssets().open("secrets.properties"));
            return props.getProperty("QWEATHER_API_KEY", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public interface WeatherCallback {
        void onSuccess(WeatherData data);
        void onError(String errorMsg);
    }

    public static void fetchWeatherByLocation(Context context, double latitude, double longitude, WeatherCallback callback) {
        String apiKey = getApiKey(context);
        String location = longitude + "," + latitude; // 注意顺序：lon,lat
        String url = BASE_URL + "?location=" + location + "&key=" + apiKey + "&lang=zh&unit=m";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("网络请求失败: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("API请求失败: " + response.code());
                    return;
                }
                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    JSONObject now = json.getJSONObject("now");
                    String cityName = "";
                    try { cityName = json.optString("refer", ""); } catch (Exception ignore) {}
                    String description = now.optString("text", "未知天气");
                    double temp = now.optDouble("temp", 0);
                    int humidity = now.optInt("humidity", 0);
                    String iconCode = now.optString("icon", "100");
                    double windSpeed = now.optDouble("windSpeed", 0); // 单位km/h
                    int pressure = now.optInt("pressure", 0); // 单位hPa
                    WeatherData data = new WeatherData(cityName, description, temp, humidity, iconCode, windSpeed, pressure);
                    callback.onSuccess(data);
                } catch (Exception e) {
                    callback.onError("JSON解析失败: " + e.getMessage());
                }
            }
        });
    }
} 