package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobUser;
// import cn.bmob.v3.datatype.BmobFile; // BmobUser might handle avatarUrl internally, depending on SDK version and usage

public class User extends BmobUser {

    // Only include avatarUrl as specified by user schema
    private String avatarUrl;

    // Add getter and setter for avatarUrl
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}


