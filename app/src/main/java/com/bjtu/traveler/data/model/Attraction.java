package com.bjtu.traveler.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Attraction implements Serializable {
    // 兼容原有字段
    private long id;
    private String name;
    private double lat;
    private double lon;
    private String category; // tourism 类型
    // 新增字段
    private String description;
    private String address;
    private String city;
    private String country;
    private String url; // 新增字段，图片或详情链接

    // 兼容RoutesFragment.DayPlan解析所需字段
    @SerializedName("detailPageUrl")
    private String detailPageUrl;
    @SerializedName("businessId")
    private String businessId;
    @SerializedName("commentScore")
    private String commentScore;
    @SerializedName("sightLevel")
    private String sightLevel;
    @SerializedName("coverImageUrl")
    private String coverImageUrl;
    @SerializedName("tagNames")
    private List<String> tagNames;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("heatScore")
    private double heatScore;
    @SerializedName("price")
    private int price;
    @SerializedName("addressDistance")
    private String addressDistance;

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

    // === Getter & Setter ===
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDetailPageUrl() { return detailPageUrl; }
    public void setDetailPageUrl(String detailPageUrl) { this.detailPageUrl = detailPageUrl; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getCommentScore() { return commentScore; }
    public void setCommentScore(String commentScore) { this.commentScore = commentScore; }
    public String getSightLevel() { return sightLevel; }
    public void setSightLevel(String sightLevel) { this.sightLevel = sightLevel; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getHeatScore() { return heatScore; }
    public void setHeatScore(double heatScore) { this.heatScore = heatScore; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getAddressDistance() { return addressDistance; }
    public void setAddressDistance(String addressDistance) { this.addressDistance = addressDistance; }
}
