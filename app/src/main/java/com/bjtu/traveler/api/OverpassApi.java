package com.bjtu.traveler.api;

import com.bjtu.traveler.data.model.Attraction;
import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class OverpassApi {
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final OkHttpClient client = new OkHttpClient();

    public interface Callback {
        void onSuccess(List<Attraction> attractions);
        void onFailure(Exception e);
    }

    // 获取景区数据
    public static void fetchAttractions(String city, Callback callback) {
        String query = "[out:json][timeout:25];area[\"name\"=\"" + city + "\"][\"boundary\"=\"administrative\"]->.searchArea;"
                + "(node[\"tourism\"](area.searchArea);way[\"tourism\"](area.searchArea);relation[\"tourism\"](area.searchArea););out center tags;";
        RequestBody body = RequestBody.create(query, MediaType.parse("text/plain"));
        Request request = new Request.Builder().url(OVERPASS_URL).post(body).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(new IOException("Unexpected code " + response)));
                    return;
                }
                String json = response.body().string();
                OverpassResult result = new Gson().fromJson(json, OverpassResult.class);
                List<Attraction> list = new ArrayList<>();
                if (result != null && result.elements != null) {
                    for (OverpassResult.Element e : result.elements) {
                        String name = e.tags != null ? e.tags.name : null;
                        String tourism = e.tags != null ? e.tags.tourism : null;
                        double lat = e.lat != 0 ? e.lat : (e.center != null ? e.center.lat : 0);
                        double lon = e.lon != 0 ? e.lon : (e.center != null ? e.center.lon : 0);
                        String desc = e.tags != null && e.tags.description != null ? e.tags.description : null;
                        String address = e.tags != null && e.tags.address != null ? e.tags.address : null;
                        String cityTag = e.tags != null && e.tags.city != null ? e.tags.city : null;
                        String country = e.tags != null && e.tags.country != null ? e.tags.country : null;
                        if (name != null && tourism != null) {
                            list.add(new Attraction(e.id, name, lat, lon, tourism, desc, address, cityTag, country));
                        }
                    }
                }
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(list));
            }
        });
    }

    // 支持限制每次请求返回的景点数量
    public static void fetchAttractions(String city, int limit, Callback callback) {
        String query = "[out:json][timeout:25];area[\"name\"=\"" + city + "\"][\"boundary\"=\"administrative\"]->.searchArea;"
                + "(node[\"tourism\"](area.searchArea);way[\"tourism\"](area.searchArea);relation[\"tourism\"](area.searchArea););out center tags " + limit + ";";
        RequestBody body = RequestBody.create(query, MediaType.parse("text/plain"));
        Request request = new Request.Builder().url(OVERPASS_URL).post(body).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(new IOException("Unexpected code " + response)));
                    return;
                }
                String json = response.body().string();
                OverpassResult result = new Gson().fromJson(json, OverpassResult.class);
                List<Attraction> list = new ArrayList<>();
                if (result != null && result.elements != null) {
                    for (OverpassResult.Element e : result.elements) {
                        String name = e.tags != null ? e.tags.name : null;
                        String tourism = e.tags != null ? e.tags.tourism : null;
                        double lat = e.lat != 0 ? e.lat : (e.center != null ? e.center.lat : 0);
                        double lon = e.lon != 0 ? e.lon : (e.center != null ? e.center.lon : 0);
                        if (name != null && tourism != null) {
                            list.add(new Attraction(e.id, name, lat, lon, tourism));
                        }
                    }
                }
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(list));
            }
        });
    }

    // 按分类查找景点（如探险、放松、文化等）
    public static void fetchAttractionsByCategory(String city, String tourismType, int limit, Callback callback) {
        String query = "[out:json][timeout:25];area[\"name\"=\"" + city + "\"][\"boundary\"=\"administrative\"]->.searchArea;"
                + "(node[\"tourism\"=\"" + tourismType + "\"](area.searchArea);way[\"tourism\"=\"" + tourismType + "\"](area.searchArea);relation[\"tourism\"=\"" + tourismType + "\"](area.searchArea););out center tags " + limit + ";";
        RequestBody body = RequestBody.create(query, MediaType.parse("text/plain"));
        Request request = new Request.Builder().url(OVERPASS_URL).post(body).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(new IOException("Unexpected code " + response)));
                    return;
                }
                String json = response.body().string();
                OverpassResult result = new Gson().fromJson(json, OverpassResult.class);
                List<Attraction> list = new ArrayList<>();
                if (result != null && result.elements != null) {
                    for (OverpassResult.Element e : result.elements) {
                        String name = e.tags != null ? e.tags.name : null;
                        String tourism = e.tags != null ? e.tags.tourism : null;
                        double lat = e.lat != 0 ? e.lat : (e.center != null ? e.center.lat : 0);
                        double lon = e.lon != 0 ? e.lon : (e.center != null ? e.center.lon : 0);
                        String desc = e.tags != null && e.tags.description != null ? e.tags.description : null;
                        String address = e.tags != null && e.tags.address != null ? e.tags.address : null;
                        String cityTag = e.tags != null && e.tags.city != null ? e.tags.city : null;
                        String country = e.tags != null && e.tags.country != null ? e.tags.country : null;
                        if (name != null && tourism != null) {
                            list.add(new Attraction(e.id, name, lat, lon, tourism, desc, address, cityTag, country));
                        }
                    }
                }
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(list));
            }
        });
    }

    // 查询当前位置附近最近的两个景点（通过经纬度和半径）
    public static void fetchNearbyAttractions(double lat, double lon, int radius, int limit, Callback callback) {
        // 查询tourism类的景点，按距离排序，取最近limit个
        String query = String.format(
                "[out:json][timeout:25];(node[\"tourism\"](around:%d,%.6f,%.6f);way[\"tourism\"](around:%d,%.6f,%.6f);relation[\"tourism\"](around:%d,%.6f,%.6f););out center tags;",
                radius, lat, lon, radius, lat, lon, radius, lat, lon
        );
        RequestBody body = RequestBody.create(query, MediaType.parse("text/plain"));
        Request request = new Request.Builder().url(OVERPASS_URL).post(body).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(new IOException("Unexpected code " + response)));
                    return;
                }
                String json = response.body().string();
                OverpassResult result = new Gson().fromJson(json, OverpassResult.class);
                List<Attraction> list = new ArrayList<>();
                if (result != null && result.elements != null) {
                    for (OverpassResult.Element e : result.elements) {
                        String name = e.tags != null ? e.tags.name : null;
                        String tourism = e.tags != null ? e.tags.tourism : null;
                        double alat = e.lat != 0 ? e.lat : (e.center != null ? e.center.lat : 0);
                        double alon = e.lon != 0 ? e.lon : (e.center != null ? e.center.lon : 0);
                        String desc = e.tags != null && e.tags.description != null ? e.tags.description : null;
                        String address = e.tags != null && e.tags.address != null ? e.tags.address : null;
                        String cityTag = e.tags != null && e.tags.city != null ? e.tags.city : null;
                        String country = e.tags != null && e.tags.country != null ? e.tags.country : null;
                        if (name != null && tourism != null) {
                            list.add(new Attraction(e.id, name, alat, alon, tourism, desc, address, cityTag, country));
                        }
                    }
                }
                // 按距离排序
                list.sort((a, b) -> {
                    double da = Math.pow(a.getLat() - lat, 2) + Math.pow(a.getLon() - lon, 2);
                    double db = Math.pow(b.getLat() - lat, 2) + Math.pow(b.getLon() - lon, 2);
                    return Double.compare(da, db);
                });
                // 只取最近limit个
                List<Attraction> resultList = list.size() > limit ? list.subList(0, limit) : list;
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(resultList));
            }
        });
    }

    // Overpass API 返回结构
    public static class OverpassResult {
        public List<Element> elements;
        public static class Element {
            public long id;
            public String type;
            public double lat;
            public double lon;
            public Center center;
            public Tags tags;
        }
        public static class Center {
            public double lat;
            public double lon;
        }
        public static class Tags {
            public String name;
            public String tourism;
            public String description;
            public String address;
            public String city;
            public String country;
        }
    }
}
