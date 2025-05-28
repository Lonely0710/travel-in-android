package com.bjtu.traveler.api;

import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bjtu.traveler.TravelerApplication;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unsplash API 调用类
 * 负责与 Unsplash 开放平台进行交互，获取图片数据
 */
public class UnsplashApi {

    // Unsplash API 的基础 URL
    private static final String BASE_URL = "https://api.unsplash.com/";

    // OkHttpClient 实例，用于发送 HTTP 请求
    private final OkHttpClient client = new OkHttpClient();

    /**
     * 根据关键词（如城市名称）搜索 Unsplash 图片
     * @param query 搜索关键词
     * @return 包含匹配图片的 CityCarouselItem 列表
     * @throws IOException 如果发生网络或其他 I/O 错误
     */
    public List<CityCarouselItem> searchPhotos(String query) throws IOException {
        List<CityCarouselItem> cityItems = new ArrayList<>(); // 存储搜索结果的列表
        // 构建搜索图片的 API URL，per_page=1 表示每个关键词只获取一张图片
        String url = BASE_URL + "search/photos?query=" + query + "&per_page=1";

        // 获取从 secrets.properties 中读取的 Unsplash Access Key
        String accessKey = TravelerApplication.UNSPLASH_ACCESS_KEY;

        if (accessKey == null || accessKey.isEmpty()) {
            // 如果 Access Key 未设置，抛出异常或记录错误
            throw new IOException("Unsplash Access Key is not set in secrets.properties!");
        }

        // 构建 HTTP GET 请求
        Request request = new Request.Builder()
                .url(url) // 设置请求 URL
                // 添加认证头部，使用从 TravelerApplication 获取的 Access Key
                .header("Authorization", "Client-ID " + accessKey)
                .header("Accept-Version", "v1") // 指定 API 版本
                .build();

        // 执行 HTTP 请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            // 检查响应是否成功 (HTTP 状态码 2xx)
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 获取响应体字符串
            String responseBody = response.body().string();

            // 解析 JSON 响应
            JSONObject jsonResponse = new JSONObject(responseBody);
            // 获取搜索结果数组
            JSONArray results = jsonResponse.getJSONArray("results");

            // 如果结果数组不为空，处理第一个结果
            if (results.length() > 0) {
                JSONObject photo = results.getJSONObject(0); // 获取第一个图片对象
                // 获取图片的常规尺寸 URL
                String imageUrl = photo.getJSONObject("urls").getString("regular");
                // 使用搜索关键词作为城市名称，详情页 URL 暂时链接到 Unsplash 图片页面
                String detailUrl = photo.getJSONObject("links").getString("html");
                // Unsplash API 不直接返回城市名称，这里使用搜索关键词作为城市名
                String cityName = query;

                // 创建 CityCarouselItem 对象并添加到列表
                cityItems.add(new CityCarouselItem(imageUrl, cityName, detailUrl));
            }

        } catch (Exception e) {
            // 捕获解析 JSON 或其他异常
            e.printStackTrace(); // 打印错误堆栈
            // 重新抛出 IOException
            throw new IOException("Error fetching data from Unsplash API", e);
        }
        return cityItems; // 返回城市图片项列表
    }

    // 如果后续需要调用其他 Unsplash API 接口，可以在这里添加相应的方法
} 