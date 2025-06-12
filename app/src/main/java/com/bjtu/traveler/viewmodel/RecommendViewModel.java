package com.bjtu.traveler.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bjtu.traveler.R;
import com.bjtu.traveler.api.AmapApi;
import com.bjtu.traveler.data.model.Attraction;
import com.bjtu.traveler.api.OverpassApi;
import com.bjtu.traveler.data.model.CityList;
import com.bjtu.traveler.data.cache.AttractionCache;
import com.bumptech.glide.Glide;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 推荐区相关数据的 ViewModel
 */
public class RecommendViewModel extends ViewModel {
    // 推荐区分页显示相关变量
    private static final int RECOMMENDED_PAGE_SIZE = 20;
    private final MutableLiveData<List<Attraction>> recommendedDisplayList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingRecommended = new MutableLiveData<>(false);
    private int recommendedDisplayLoaded = 0;
    private List<Attraction> recommendedList = new ArrayList<>();
    private Map<String, List<Attraction>> categoryCache = new HashMap<>();

    // 热门区相关变量
    private int hotListLoaded = 0;
    private List<Attraction> hotAllList = new ArrayList<>();
    private final MutableLiveData<List<Attraction>> hotList = new MutableLiveData<>(new ArrayList<>());
    private static final int MAX_CACHE_SIZE = 20;
    private static final String[] HOT_CATEGORIES = {"attraction"};
    private static final String[] ADVENTURE_CATEGORIES = {"camp_site", "wilderness_hut", "picnic_site", "caravan_site", "chalet"};
    private static final String[] RELAX_CATEGORIES = {"viewpoint", "zoo", "theme_park", "hot_spring", "beach", "park", "garden", "spa"};
    private static final String[] CULTURE_CATEGORIES = {"museum", "gallery", "artwork"};

    // 附近景点相关
    private final MutableLiveData<List<Attraction>> nearbyAttractions = new MutableLiveData<>();
    private double cachedNearbyLat = 0;
    private double cachedNearbyLon = 0;
    private List<Attraction> cachedNearbyAttractions = null;

    // 当前定位
    private String currentCity = null;
    private String currentCountry = null;

    // 获取推荐区数据
    public LiveData<List<Attraction>> getRecommendedDisplayList() {
        return recommendedDisplayList;
    }
    public LiveData<Boolean> getIsLoadingRecommended() {
        return isLoadingRecommended;
    }
    // 获取热门区数据
    public LiveData<List<Attraction>> getHotList() {
        return hotList;
    }
    // 获取附近景点数据
    public LiveData<List<Attraction>> getNearbyAttractions() {
        return nearbyAttractions;
    }
    // 设置当前定位
    public void setCurrentLocation(String city, String country) {
        this.currentCity = city;
        this.currentCountry = country;
    }
    // 新增：设置当前位置经纬度
    public void setCurrentLocationLatLon(double lat, double lon) {
        this.cachedNearbyLat = lat;
        this.cachedNearbyLon = lon;
    }
    // 加载推荐区数据（按类型）
    public void loadRecommendedAttractions(String type) {
        if (isLoadingRecommended.getValue() != null && isLoadingRecommended.getValue()) return;
        isLoadingRecommended.setValue(true);
        if (categoryCache.containsKey(type)) {
            recommendedDisplayList.setValue(new ArrayList<>(categoryCache.get(type)));
            isLoadingRecommended.setValue(false);
            return;
        }
        String[] typesArr;
        if (type.equals("hot")) {
            typesArr = HOT_CATEGORIES;
        } else if (type.equals("adventure")) {
            typesArr = ADVENTURE_CATEGORIES;
        } else if (type.equals("relax")) {
            typesArr = RELAX_CATEGORIES;
        } else {
            typesArr = CULTURE_CATEGORIES;
        }
        List<String> cities = new ArrayList<>(CityList.RECOMMEND_CITIES);
        Collections.shuffle(cities);
        List<String> selectedCities = cities.subList(0, Math.min(8, cities.size()));
        fetchRecommendedAttractionsBatch(type, typesArr, selectedCities, 2);
    }
    // 推荐区批量API请求（带超时保护）
    private void fetchRecommendedAttractionsBatch(String type, String[] typesArr, List<String> selectedCities, int limit) {
        List<Attraction> all = new ArrayList<>();
        int totalTasks = typesArr.length * selectedCities.size();
        AtomicInteger finished = new AtomicInteger(0);
        for (String cat : typesArr) {
            for (String city : selectedCities) {
                OverpassApi.fetchAttractionsByCategory(city, cat, limit, new OverpassApi.Callback() {
                    @Override
                    public void onSuccess(List<Attraction> attractions) {
                        if (attractions != null && !attractions.isEmpty()) {
                            synchronized (all) {
                                for (Attraction a : attractions) {
                                    if (a.getCategory() == null || !a.getCategory().equalsIgnoreCase(cat)) continue;
                                    a.setCity(city);
                                    all.add(a);
                                }
                            }
                        }
                        if (finished.incrementAndGet() == totalTasks) {
                            categoryCache.put(type, new ArrayList<>(all));
                            recommendedDisplayList.setValue(new ArrayList<>(all));
                            isLoadingRecommended.setValue(false);
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        if (finished.incrementAndGet() == totalTasks) {
                            categoryCache.put(type, new ArrayList<>(all));
                            recommendedDisplayList.setValue(new ArrayList<>(all));
                            isLoadingRecommended.setValue(false);
                        }
                    }
                });
            }
        }
    }
    // 加载热门景点
    public void loadHotAttractions() {
        hotAllList.clear();
        hotListLoaded = 0;
        List<String> cities = new ArrayList<>(CityList.HOT_CITIES);
        Collections.shuffle(cities);
        List<String> selectedCities = cities.subList(0, Math.min(5, cities.size()));
        List<Attraction> allAttractions = new ArrayList<>();
        AtomicInteger finished = new AtomicInteger(0);
        for (String city : selectedCities) {
            if (allAttractions.size() >= MAX_CACHE_SIZE) break;
            OverpassApi.fetchAttractions(city, 10, new OverpassApi.Callback() {
                @Override
                public void onSuccess(List<Attraction> attractions) {
                    if (attractions != null && !attractions.isEmpty()) {
                        synchronized (allAttractions) {
                            int canAdd = Math.min(MAX_CACHE_SIZE - allAttractions.size(), attractions.size());
                            for (int i = 0; i < canAdd; i++) {
                                Attraction a = attractions.get(i);
                                a.setCity(city);
                                if (a.getCategory() == null) continue;
                                String cat = a.getCategory().trim().toLowerCase();
                                if (cat.equals("hotel") || cat.equals("hostel") || cat.equals("information")) continue;
                                allAttractions.add(a);
                            }
                        }
                    }
                    if (finished.incrementAndGet() == selectedCities.size() || allAttractions.size() >= MAX_CACHE_SIZE) {
                        AttractionCache.getInstance().setHotAttractions(new ArrayList<>(allAttractions));
                        hotAllList.clear();
                        hotAllList.addAll(allAttractions);
                        fillHotListPage();
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    if (finished.incrementAndGet() == selectedCities.size() || allAttractions.size() >= MAX_CACHE_SIZE) {
                        AttractionCache.getInstance().setHotAttractions(new ArrayList<>(allAttractions));
                        hotAllList.clear();
                        hotAllList.addAll(allAttractions);
                        fillHotListPage();
                    }
                }
            });
        }
    }

    // 填充一页热门景点
    private void fillHotListPage() {
        List<Attraction> filteredHotAllList = new ArrayList<>();
        for (Attraction a : hotAllList) {
            if (a.getCategory() == null) continue;
            String cat = a.getCategory().trim().toLowerCase();
            if (cat.equals("hotel") || cat.equals("hostel") || cat.equals("information")) continue;
            filteredHotAllList.add(a);
        }
        int remain = filteredHotAllList.size() - hotListLoaded;
        int count = Math.min(20, remain);
        if (count > 0) {
            List<Attraction> current = new ArrayList<>(hotList.getValue());
            current.addAll(filteredHotAllList.subList(hotListLoaded, hotListLoaded + count));
            hotListLoaded += count;
            hotList.setValue(current);
        }
    }

    // 加载附近景点（无参，直接用当前缓存的定位值）
    public void loadNearbyAttractions() {
        double lat = cachedNearbyLat;
        double lon = cachedNearbyLon;
        // 如果没有定位信息，直接返回空
        if (lat == 0 && lon == 0) {
            nearbyAttractions.setValue(null);
            return;
        }
        int radius = 3000; // 2km
        int limit = 5;
        OverpassApi.fetchNearbyAttractions(lat, lon, radius, limit, new OverpassApi.Callback() {
            @Override
            public void onSuccess(List<Attraction> attractions) {
                cachedNearbyAttractions = new ArrayList<>(attractions);
                nearbyAttractions.setValue(new ArrayList<>(attractions));
            }
            @Override
            public void onFailure(Exception e) {
                nearbyAttractions.setValue(null);
            }
        });
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径，单位：米
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 计算当前位置与目标点的距离（单位：米），当前位置用缓存的经纬度
    public double calculateDistanceToCurrent(double lat2, double lon2) {
        double lat1 = cachedNearbyLat;
        double lon1 = cachedNearbyLon;
        return calculateDistance(lat1, lon1, lat2, lon2);
    }

    // 获取距离字符串（如“距离 0.5 km”），无经纬度返回空串
    public String getDistanceString(Attraction attraction) {
        if (attraction == null || attraction.getLat() == 0 || attraction.getLon() == 0) return "";
        double distance = calculateDistanceToCurrent(attraction.getLat(), attraction.getLon());
        double km = distance / 1000.0;
        return String.format("距离 %.1f km", km);
    }

    // 填充附近景点卡片内容（ViewModel版本，便于Fragment直接调用）
    public void fillNearbyPlaceCard(View card, Attraction attraction, Fragment fragment) {
        TextView tvName = card.findViewById(R.id.text_place_name);
        TextView tvDistance = card.findViewById(R.id.text_distance);
        ImageView ivImg = card.findViewById(R.id.image_place);
        // 只有内容变化时才 setText/setImage，避免重复刷新导致抖动
        if (tvName != null && !attraction.getName().equals(tvName.getText().toString())) {
            tvName.setText(attraction.getName());
        }
        if (tvDistance != null) {
            String newDistance = getDistanceString(attraction);
            if (!newDistance.equals(tvDistance.getText().toString())) {
                tvDistance.setText(newDistance);
            }
        }
        if (ivImg != null && attraction.getUrl() != null) {
            Object tag = ivImg.getTag();
            if (tag == null || !attraction.getUrl().equals(tag.toString())) {
                Glide.with(card.getContext()).load(attraction.getUrl()).into(ivImg);
                ivImg.setTag(attraction.getUrl());
            }
        }
        card.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("name", attraction.getName());
            args.putString("category", attraction.getCategory());
            args.putString("description", attraction.getDescription());
            args.putString("city", attraction.getCity());
            args.putString("country", attraction.getCountry());
            args.putString("imgUrl", attraction.getUrl());
            com.bjtu.traveler.ui.explore.AttractionDetailFragment detailFragment = new com.bjtu.traveler.ui.explore.AttractionDetailFragment();
            detailFragment.setArguments(args);
            fragment.requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    // 高德API补全景点信息（不指定类型）
    public interface AmapDetailCallback {
        void onSuccess(Attraction attraction);
        void onFailure(Exception e);
    }
    public void fetchAmapDetailForAttraction(Context context, Attraction attraction, AmapDetailCallback callback) {
        if (attraction == null || attraction.getName() == null) {
            Log.e("AmapDebug", "Attraction or name is null");
            callback.onFailure(new Exception("Attraction or name is null"));
            return;
        }
        String city = attraction.getCity();
        Log.d("AmapDebug", "Start search: name=" + attraction.getName() + ", city=" + city);
        // 用高德SDK查
        searchAmapWithFallback(context, attraction, city, callback, false);
    }

    // 新增：先用city查，无结果再全局查
    private void searchAmapWithFallback(Context context, Attraction attraction, String city, AmapDetailCallback callback, boolean isFallback) {
        String keywords = attraction.getName();
        Log.d("AmapDebug", "AmapApi request: keywords=" + keywords + ", city=" + (city == null ? "" : city) + ", isFallback=" + isFallback);
        AmapApi.searchPlace(context, keywords, isFallback ? null : city, new AmapApi.PlaceCallback() {
            @Override
            public void onSuccess(JSONObject placeInfo) {
                Log.d("AmapDebug", "AmapApi success: " + placeInfo.toString());
                attraction.setAddress(placeInfo.optString("address"));
                String location = placeInfo.optString("location");
                if (location != null && location.contains(",")) {
                    String[] locArr = location.split(",");
                    try {
                        attraction.setLon(Double.parseDouble(locArr[0]));
                        attraction.setLat(Double.parseDouble(locArr[1]));
                    } catch (Exception ignore) {
                        Log.e("AmapDebug", "Location parse error: " + location);
                    }
                }
                String url = placeInfo.optString("photoUrl");
                if (url != null && !url.isEmpty()) {
                    attraction.setUrl(url);
                    Log.d("AmapDebug", "Set url: " + url);
                }
                callback.onSuccess(attraction);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("AmapDebug", "AmapApi error: " + e.getMessage() + ", isFallback=" + isFallback);
                // 如果不是降级查，且有city，降级为不带city再查一次
                if (!isFallback && city != null && !city.isEmpty()) {
                    searchAmapWithFallback(context, attraction, null, callback, true);
                } else {
                    callback.onFailure(e);
                }
            }
        });
    }

    // 新增：外部可调用，强制刷新推荐区列表
    public void updateRecommendedDisplayList(List<Attraction> list) {
        recommendedDisplayList.postValue(list); // 用postValue替换setValue，兼容子线程
    }

    // 新增：按城市名和类型搜索景点
    public interface CitySearchCallback {
        void onSuccess(List<Attraction> attractions);
        void onFailure(Exception e);
    }
    public void searchAttractionsByCity(String city, String[] categories, int limit, CitySearchCallback callback) {
        if (city == null || city.trim().isEmpty()) {
            callback.onFailure(new Exception("城市名为空"));
            return;
        }
        List<Attraction> allResults = new ArrayList<>();
        AtomicInteger finished = new AtomicInteger(0);
        for (String cat : categories) {
            OverpassApi.fetchAttractionsByCategory(city, cat, limit, new OverpassApi.Callback() {
                @Override
                public void onSuccess(List<Attraction> attractions) {
                    if (attractions != null && !attractions.isEmpty()) {
                        synchronized (allResults) {
                            for (Attraction a : attractions) {
                                a.setCity(city);
                            }
                            allResults.addAll(attractions);
                        }
                    }
                    if (finished.incrementAndGet() == categories.length) {
                        callback.onSuccess(new ArrayList<>(allResults));
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    if (finished.incrementAndGet() == categories.length) {
                        callback.onSuccess(new ArrayList<>(allResults));
                    }
                }
            });
        }
    }

    // 新增：获取分类缓存
    public Map<String, List<Attraction>> getCategoryCache() {
        return categoryCache;
    }
}
