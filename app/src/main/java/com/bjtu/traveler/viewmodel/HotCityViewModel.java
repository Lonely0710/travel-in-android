package com.bjtu.traveler.viewmodel;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bjtu.traveler.utils.PlaceScraper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HotCityViewModel extends AndroidViewModel {
    private final MutableLiveData<List<City>> hotCities = new MutableLiveData<>();
    private final MutableLiveData<City> selectedCity = new MutableLiveData<>();
    private final MutableLiveData<List<PlaceScraper.Destination>> attractions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public HotCityViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<List<City>> getHotCities() { return hotCities; }
    public LiveData<City> getSelectedCity() { return selectedCity; }
    public LiveData<List<PlaceScraper.Destination>> getAttractions() { return attractions; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void loadHotCities(String type) {
        final String fileName = "international".equals(type) ? "hot_cities_world.json" : "hot_cities_china.json";
        new Thread(() -> {
            List<City> cityList = new ArrayList<>();
            try (InputStream is = getApplication().getApplicationContext().getAssets().open(fileName);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                JSONArray arr = new JSONArray(sb.toString());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    cityList.add(new City(obj.getString("name"), obj.getString("pinyin")));
                }
                new Handler(Looper.getMainLooper()).post(() -> hotCities.setValue(cityList));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> error.setValue("城市列表加载失败: " + e.getMessage()));
            }
        }).start();
    }

    public void selectCity(City city) {
        selectedCity.setValue(city);
        loadAttractions(city.name);
    }

    public void loadAttractions(String cityName) {
        loading.setValue(true);
        error.setValue(null);
        PlaceScraper.scrapeCityDestinations(getApplication().getApplicationContext(), cityName)
                .thenAccept(list -> new Handler(Looper.getMainLooper()).post(() -> {
                    attractions.setValue(list);
                    loading.setValue(false);
                }))
                .exceptionally(e -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        error.setValue("景点加载失败: " + e.getMessage());
                        loading.setValue(false);
                    });
                    return null;
                });
    }

    public static class City {
        public final String name;
        public final String pinyin;
        public City(String name, String pinyin) {
            this.name = name;
            this.pinyin = pinyin;
        }
    }
} 