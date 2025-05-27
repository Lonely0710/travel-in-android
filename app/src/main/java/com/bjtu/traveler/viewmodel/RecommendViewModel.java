package com.bjtu.traveler.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.bjtu.traveler.data.model.Attraction;
import com.bjtu.traveler.api.OverpassApi;
import com.bjtu.traveler.data.model.CityList;
import com.bjtu.traveler.data.cache.AttractionCache;
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
    // 加载附近景点
    public void loadNearbyAttractions(double lat, double lon) {
        if (cachedNearbyAttractions != null && Math.abs(lat - cachedNearbyLat) < 0.01 && Math.abs(lon - cachedNearbyLon) < 0.01) {
            nearbyAttractions.setValue(new ArrayList<>(cachedNearbyAttractions));
            return;
        }
        int radius = 2000; // 2km
        int limit = 4;
        OverpassApi.fetchNearbyAttractions(lat, lon, radius, limit, new OverpassApi.Callback() {
            @Override
            public void onSuccess(List<Attraction> attractions) {
                cachedNearbyAttractions = new ArrayList<>(attractions);
                cachedNearbyLat = lat;
                cachedNearbyLon = lon;
                nearbyAttractions.setValue(new ArrayList<>(attractions));
            }
            @Override
            public void onFailure(Exception e) {
                nearbyAttractions.setValue(null);
            }
        });
    }
} 