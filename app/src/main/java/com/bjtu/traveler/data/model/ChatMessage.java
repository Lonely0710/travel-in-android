package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobObject;

public class ChatMessage extends BmobObject {
    private String content;
    private boolean isSentByUser; // true for user, false for AI

    public ChatMessage() {
        // 必须有一个无参构造函数
    }

    public ChatMessage(String content, boolean isSentByUser) {
        this.content = content;
        this.isSentByUser = isSentByUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public void setSentByUser(boolean sentByUser) {
        isSentByUser = sentByUser;
    }
}