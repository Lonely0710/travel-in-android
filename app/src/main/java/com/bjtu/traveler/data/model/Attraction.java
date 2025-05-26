package com.bjtu.traveler.data.model;

import java.io.Serializable;

public class Attraction implements Serializable {
    public long id;
    public String name;
    public double lat;
    public double lon;
    public String category; // tourism 类型
    // 新增字段
    public String description;
    public String address;
    public String city;
    public String country;
    public String url; // 新增字段，图片或详情链接

    public Attraction(long id, String name, double lat, double lon, String category) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.category = category;
    }

    public Attraction(long id, String name, double lat, double lon, String category, String description, String address, String city, String country) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.category = category;
        this.description = description;
        this.address = address;
        this.city = city;
        this.country = country;
    }

    public Attraction(long id, String name, double lat, double lon, String category, String description, String address, String city, String country, String url) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.category = category;
        this.description = description;
        this.address = address;
        this.city = city;
        this.country = country;
        this.url = url;
    }
}
