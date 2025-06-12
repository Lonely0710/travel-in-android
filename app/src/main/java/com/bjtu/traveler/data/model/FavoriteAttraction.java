package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobObject;

/**
 * 收藏景点表（Bmob）
 */
public class FavoriteAttraction extends BmobObject {
    private String name;
    private String category;
    private String description;
    private String city;
    private String country;
    private String province;
    private String imgUrl;
    private String userId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
