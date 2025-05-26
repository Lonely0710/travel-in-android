package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobObject;

public class DayPlan extends BmobObject {
    private String dayTitle;
    private String morningActivity;
    private String morningIconType;
    private String afternoonActivity;
    private String afternoonIconType;
    private String eveningActivity;
    private String eveningIconType;

    public DayPlan() {
    }

    public DayPlan(String dayTitle, String morningActivity, String morningIconType,
                   String afternoonActivity, String afternoonIconType,
                   String eveningActivity, String eveningIconType) {
        this.dayTitle = dayTitle;
        this.morningActivity = morningActivity;
        this.morningIconType = morningIconType;
        this.afternoonActivity = afternoonActivity;
        this.afternoonIconType = afternoonIconType;
        this.eveningActivity = eveningActivity;
        this.eveningIconType = eveningIconType;
    }

    // --- Getters/Setters ---
    public String getDayTitle() {
        return dayTitle;
    }

    public void setDayTitle(String dayTitle) {
        this.dayTitle = dayTitle;
    }

    public String getMorningActivity() {
        return morningActivity;
    }

    public void setMorningActivity(String morningActivity) {
        this.morningActivity = morningActivity;
    }

    public String getMorningIconType() {
        return morningIconType;
    }

    public void setMorningIconType(String morningIconType) {
        this.morningIconType = morningIconType;
    }

    public String getAfternoonActivity() {
        return afternoonActivity;
    }

    public void setAfternoonActivity(String afternoonActivity) {
        this.afternoonActivity = afternoonActivity;
    }

    public String getAfternoonIconType() {
        return afternoonIconType;
    }

    public void setAfternoonIconType(String afternoonIconType) {
        this.afternoonIconType = afternoonIconType;
    }

    public String getEveningActivity() {
        return eveningActivity;
    }

    public void setEveningActivity(String eveningActivity) {
        this.eveningActivity = eveningActivity;
    }

    public String getEveningIconType() {
        return eveningIconType;
    }

    public void setEveningIconType(String eveningIconType) {
        this.eveningIconType = eveningIconType;
    }
}