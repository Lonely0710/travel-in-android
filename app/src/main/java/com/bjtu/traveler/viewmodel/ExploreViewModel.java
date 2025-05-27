package com.bjtu.traveler.viewmodel;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import java.util.Locale;

/**
 * 探索区定位与地理信息的 ViewModel
 */
public class ExploreViewModel extends ViewModel {
    // 当前城市和国家
    private final MutableLiveData<String> currentCity = new MutableLiveData<>("定位中...");
    private final MutableLiveData<String> currentCountry = new MutableLiveData<>("");
    private final MutableLiveData<Location> lastKnownLocation = new MutableLiveData<>();
    private LocationManager locationManager;
    private LocationListener locationListener;

    // 获取当前城市
    public LiveData<String> getCurrentCity() {
        return currentCity;
    }
    // 获取当前国家
    public LiveData<String> getCurrentCountry() {
        return currentCountry;
    }
    // 获取最后已知位置
    public LiveData<Location> getLastKnownLocation() {
        return lastKnownLocation;
    }

    // 启动定位监听
    public void startLocationUpdates(Context context) {
        if (locationManager != null) return;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastKnownLocation.setValue(location);
                updateCityAndCountry(context, location);
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) {}
        };
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
            Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location best = null;
            if (lastGpsLocation != null && lastNetworkLocation != null) {
                best = (lastGpsLocation.getTime() > lastNetworkLocation.getTime()) ? lastGpsLocation : lastNetworkLocation;
            } else if (lastGpsLocation != null) {
                best = lastGpsLocation;
            } else if (lastNetworkLocation != null) {
                best = lastNetworkLocation;
            }
            if (best != null) {
                lastKnownLocation.setValue(best);
                updateCityAndCountry(context, best);
            }
        } catch (SecurityException e) {
            // 权限不足，外部应先请求权限
        }
    }
    // 停止定位监听
    public void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        locationManager = null;
        locationListener = null;
    }
    // 更新城市和国家
    private void updateCityAndCountry(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentCity.setValue(address.getLocality());
                currentCountry.setValue(address.getCountryName());
            }
        } catch (Exception e) {
            currentCity.setValue("未知城市");
            currentCountry.setValue("未知国家");
        }
    }
}