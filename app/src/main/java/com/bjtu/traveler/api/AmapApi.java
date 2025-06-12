package com.bjtu.traveler.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AmapApi {
    private static final String TAG = "AmapApi";
    private static final String BASE_URL = "https://restapi.amap.com/v3/place/text";
    private static final String PHOTO_URL = "https://restapi.amap.com/v3/place/photo";
    private static final String KEY = "d3b08b6851252fc297132c22161e2955";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface PlaceCallback {
        void onSuccess(JSONObject placeInfo);
        void onFailure(Exception e);
    }

    public interface PhotoCallback {
        void onSuccess(JSONArray photos);
        void onFailure(Exception e);
    }

    // 景点文本搜索
    public static void searchPlace(String keywords, @Nullable String city, PlaceCallback callback) {
        try {
            String encodedKeywords = URLEncoder.encode(keywords, "UTF-8");
            String url = BASE_URL + "?key=" + KEY +
                    "&keywords=" + encodedKeywords +
                    "&citylimit=true" +
                    "&extensions=all" +
                    "&output=json";
            if (city != null && !city.isEmpty()) {
                String encodedCity = URLEncoder.encode(city, "UTF-8");
                url += "&city=" + encodedCity;
            }
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        JSONArray pois = json.optJSONArray("pois");
                        if (pois != null && pois.length() > 0) {
                            JSONObject placeInfo = pois.getJSONObject(0);
                            // 直接解析 photos 字段
                            JSONArray photos = placeInfo.optJSONArray("photos");
                            if (photos != null && photos.length() > 0) {
                                JSONObject photoObj = photos.getJSONObject(0);
                                String photoUrl = photoObj.optString("url");
                                // 假设 placeInfo 代表景点对象，直接加上 url 字段
                                placeInfo.put("photoUrl", photoUrl);
                            }
                            callback.onSuccess(placeInfo);
                        } else {
                            callback.onFailure(new Exception("No result"));
                        }
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    // 查询景点图片
    public static void getPlacePhotos(String photoId, PhotoCallback callback) {
        String url = PHOTO_URL + "?key=" + KEY +
                "&photoid=" + photoId +
                "&extensions=all";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONArray photos = json.optJSONArray("photos");
                    if (photos != null) {
                        callback.onSuccess(photos);
                    } else {
                        callback.onFailure(new Exception("No photos"));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    // 景点文本搜索（基于高德Web API HTTP请求）
    public static void searchPlace(Context context, String keywords, @Nullable String city, PlaceCallback callback) {
        try {
            String encodedKeywords = URLEncoder.encode(keywords, "UTF-8");
            StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                    .append("?key=").append(KEY)
                    .append("&keywords=").append(encodedKeywords)
                    .append("&citylimit=true")
                    .append("&extensions=all")
                    .append("&output=json");
            if (city != null && !city.isEmpty()) {
                String encodedCity = URLEncoder.encode(city, "UTF-8");
                urlBuilder.append("&city=").append(encodedCity);
            }
            String url = urlBuilder.toString();
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String body = response.body().string();
                        JSONObject json = new JSONObject(body);
                        JSONArray pois = json.optJSONArray("pois");
                        if (pois != null && pois.length() > 0) {
                            JSONObject placeInfo = pois.getJSONObject(0);
                            JSONArray photos = placeInfo.optJSONArray("photos");
                            if (photos != null && photos.length() > 0) {
                                JSONObject photoObj = photos.getJSONObject(0);
                                String photoUrl = photoObj.optString("url");
                                placeInfo.put("photoUrl", photoUrl);
                            }
                            callback.onSuccess(placeInfo);
                        } else {
                            callback.onFailure(new Exception("No result"));
                        }
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
}
