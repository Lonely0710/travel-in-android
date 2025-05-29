package com.bjtu.traveler.api;

import android.content.Context;
import com.bjtu.traveler.TravelerApplication;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class QWeatherGeoApi {
    private static final String BASE_URL = "https://geoapi.qweather.com/v2/city/lookup";

    public interface CityNameCallback {
        void onSuccess(String cityName);
        void onError(String errorMsg);
    }

    public static void fetchCityNameByLocation(Context context, double latitude, double longitude, CityNameCallback callback) {
        String apiKey = TravelerApplication.QWEATHER_API_KEY;
        String location = longitude + "," + latitude;
        String url = BASE_URL + "?location=" + location + "&key=" + apiKey + "&lang=zh";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("GeoAPI请求失败: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("GeoAPI响应失败: " + response.code());
                    return;
                }
                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    JSONArray locations = json.optJSONArray("location");
                    if (locations != null && locations.length() > 0) {
                        JSONObject loc = locations.getJSONObject(0);
                        String name = loc.optString("name", "");
                        callback.onSuccess(name);
                    } else {
                        callback.onError("未找到城市名");
                    }
                } catch (Exception e) {
                    callback.onError("GeoAPI解析失败: " + e.getMessage());
                }
            }
        });
    }
} 