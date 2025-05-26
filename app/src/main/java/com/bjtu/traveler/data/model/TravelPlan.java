package com.bjtu.traveler.data.model;

import cn.bmob.v3.BmobObject;
import java.util.List;

public class TravelPlan extends BmobObject {
    private String destination;
    private int days;
    private int budget;
    private List<String> preferences;
    private String rawDestinationInput;
    private String rawDaysInput;
    private String rawBudgetInput;
    private String rawPreferencesInput;

    public TravelPlan() {
        // 必须有一个无参构造函数
    }

    // Getters and Setters
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    public String getRawDestinationInput() {
        return rawDestinationInput;
    }

    public void setRawDestinationInput(String rawDestinationInput) {
        this.rawDestinationInput = rawDestinationInput;
    }

    public String getRawDaysInput() {
        return rawDaysInput;
    }

    public void setRawDaysInput(String rawDaysInput) {
        this.rawDaysInput = rawDaysInput;
    }

    public String getRawBudgetInput() {
        return rawBudgetInput;
    }

    public void setRawBudgetInput(String rawBudgetInput) {
        this.rawBudgetInput = rawBudgetInput;
    }

    public String getRawPreferencesInput() {
        return rawPreferencesInput;
    }

    public void setRawPreferencesInput(String rawPreferencesInput) {
        this.rawPreferencesInput = rawPreferencesInput;
    }

    @Override
    public String toString() {
        return "TravelPlan{" +
                "destination='" + destination + '\'' +
                ", days=" + days +
                ", budget=" + budget +
                ", preferences=" + preferences +
                '}';
    }
}