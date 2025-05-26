package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobUser;


public class User extends BmobUser {
    private String avatarUrl;

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public static long getCurrentUserId() {
        User user = (User) cn.bmob.v3.BmobUser.getCurrentUser(User.class);
        if (user != null && user.getObjectId() != null) {
            try {
                return user.getObjectId().hashCode();
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
}

