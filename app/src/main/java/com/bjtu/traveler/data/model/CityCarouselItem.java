package com.bjtu.traveler.data.model;

public class CityCarouselItem {
    private String imageUrl;
    private String cityName;
    private String detailUrl; // 虽然WebView暂时不做，但数据模型中包含这个字段

    public CityCarouselItem(String imageUrl, String cityName, String detailUrl) {
        this.imageUrl = imageUrl;
        this.cityName = cityName;
        this.detailUrl = detailUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCityName() {
        return cityName;
    }

    public String getDetailUrl() {
        return detailUrl;
    }
} 