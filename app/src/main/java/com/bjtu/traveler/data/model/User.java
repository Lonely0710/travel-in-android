package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {
    private String avatarUrl;

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
} 