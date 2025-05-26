package com.bjtu.traveler.api;

import android.os.Handler;
import android.os.Looper;
import com.bjtu.traveler.data.model.Attraction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class WikipediaApi {
    private static final String SUMMARY_URL = "https://zh.wikipedia.org/api/rest_v1/page/summary/";
    // private static final OkHttpClient client = new OkHttpClient.Builder()
    //         .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("api.wlai.vip", 8080)))
    //         .connectTimeout(10, TimeUnit.SECONDS)
    //         .readTimeout(15, TimeUnit.SECONDS)
    //         .build();
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    // 错误码定义
    public static final int ERROR_NETWORK = 1;
    public static final int ERROR_NOT_FOUND = 2;
    public static final int ERROR_PARSE = 3;
    public static final int ERROR_UNKNOWN = 99;

    public interface Callback {
        void onSuccess(String description, String imageUrl);
        void onFailure(int errorCode, String errorMsg);
    }

    // 通过景点名称获取简介和图片
    public static void fetchAttractionInfo(String name, Callback callback) {
        fetchAttractionInfo(name, callback, false);
    }

    // 支持动态选择是否使用代理
    public static void fetchAttractionInfo(String name, Callback callback, boolean useProxy) {
        try {
            String encodedName = name.replace(" ", "_"); // 先替换空格
            encodedName = URLEncoder.encode(encodedName, StandardCharsets.UTF_8.toString());
            String url = SUMMARY_URL + encodedName;
            OkHttpClient realClient;
            if (useProxy) {
                realClient = new OkHttpClient.Builder()
                        .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("api.wlai.vip", 8080)))
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();
            } else {
                realClient = client;
            }
            Request request = new Request.Builder().url(url).build();
            realClient.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(ERROR_NETWORK, "网络错误: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        int code = response.code();
                        if (code == 404) {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(ERROR_NOT_FOUND, "未找到相关条目 (404)"));
                        } else {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(ERROR_UNKNOWN, "HTTP错误: " + code));
                        }
                        return;
                    }
                    String json = response.body().string();
                    try {
                        JsonObject obj = new Gson().fromJson(json, JsonObject.class);
                        // 重定向处理
                        if (obj.has("redirects") && !obj.get("redirects").isJsonNull()) {
                            String redirectedName = obj.get("redirects").getAsString();
                            fetchAttractionInfo(redirectedName, callback, useProxy);
                            return;
                        }
                        String desc = obj.has("extract") && !obj.get("extract").isJsonNull()
                                ? obj.get("extract").getAsString()
                                : "暂无简介";
                        String imgUrl = null;
                        if (obj.has("originalimage") && !obj.get("originalimage").isJsonNull()) {
                            JsonObject imgObj = obj.getAsJsonObject("originalimage");
                            if (imgObj.has("source") && !imgObj.get("source").isJsonNull()) {
                                imgUrl = imgObj.get("source").getAsString();
                            }
                        } else if (obj.has("thumbnail") && !obj.get("thumbnail").isJsonNull()) {
                            JsonObject imgObj = obj.getAsJsonObject("thumbnail");
                            if (imgObj.has("source") && !imgObj.get("source").isJsonNull()) {
                                imgUrl = imgObj.get("source").getAsString();
                            }
                        } else if (obj.has("content_urls") && !obj.get("content_urls").isJsonNull()) {
                            JsonObject contentUrls = obj.getAsJsonObject("content_urls");
                            if (contentUrls.has("desktop") && !contentUrls.get("desktop").isJsonNull()) {
                                JsonObject desktop = contentUrls.getAsJsonObject("desktop");
                                if (desktop.has("page") && !desktop.get("page").isJsonNull()) {
                                    imgUrl = desktop.get("page").getAsString(); // 备用页面链接
                                }
                            }
                        }
                        final String descFinal = desc;
                        final String imgUrlFinal = imgUrl;
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(descFinal, imgUrlFinal));
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(ERROR_PARSE, "解析失败: " + e.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(ERROR_UNKNOWN, "未知错误: " + e.getMessage()));
        }
    }
}
