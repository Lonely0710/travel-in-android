package com.bjtu.traveler.data.cache;

import android.content.Context;
import android.content.SharedPreferences;
import com.bjtu.traveler.data.model.CityCarouselItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

// Need to initialize this cache with a Context, perhaps in your Application class
public class CityCache {
    private static CityCache instance;
    private static final String PREF_NAME = "city_cache";
    private static final String KEY_CITY_LIST = "city_list";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours cache duration

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    private CityCache(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized CityCache getInstance() {
        if (instance == null) {
            // Note: This requires application context to be passed during initialization
            // You should initialize this singleton in your Application class and pass the context
            throw new IllegalStateException("CityCache must be initialized with context");
        }
        return instance;
    }

    // Method to initialize the cache with application context
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new CityCache(context.getApplicationContext());
        }
    }


    public void saveCityCarouselItems(List<CityCarouselItem> cityList) {
        String json = gson.toJson(cityList);
        sharedPreferences.edit()
                .putString(KEY_CITY_LIST, json)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public List<CityCarouselItem> getCityCarouselItems() {
        String json = sharedPreferences.getString(KEY_CITY_LIST, null);
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<List<CityCarouselItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public boolean isCacheExpired() {
        long lastSavedTimestamp = sharedPreferences.getLong(KEY_TIMESTAMP, 0);
        return (System.currentTimeMillis() - lastSavedTimestamp) > CACHE_DURATION;
    }

    public void clearCache() {
        sharedPreferences.edit().clear().apply();
    }
} 