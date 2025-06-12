package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;
import com.bjtu.traveler.data.model.User;

public class Post extends BmobObject {
    private String tittle;
    private String article;
    private String imgUrl;

    private String category;
    private String location;

    private User userId;

    public Post() {
        // 默认无参构造函数
    }

    public String getTittle() { return tittle; }
    public void setTittle(String tittle) { this.tittle = tittle; }

    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getUserId() { return userId; }
    public void setUserId(User userId) { this.userId = userId; }
}

