package com.bjtu.traveler.utils;

import java.util.HashMap;
import java.util.Map;

public class InternationalCityMapper {

    private static final Map<String, String> INTERNATIONAL_CITY_MAP = new HashMap<>();

    static {
        INTERNATIONAL_CITY_MAP.put("巴黎", "paris");
        INTERNATIONAL_CITY_MAP.put("伦敦", "london");
        INTERNATIONAL_CITY_MAP.put("纽约", "newyork");
        INTERNATIONAL_CITY_MAP.put("迪拜", "dubai");
        INTERNATIONAL_CITY_MAP.put("洛杉矶", "losangeles");
        INTERNATIONAL_CITY_MAP.put("罗马", "rome");
        INTERNATIONAL_CITY_MAP.put("首尔", "seoul");
        INTERNATIONAL_CITY_MAP.put("悉尼", "sydney");
        INTERNATIONAL_CITY_MAP.put("东京", "tokyo");
        INTERNATIONAL_CITY_MAP.put("新加坡", "singapore");
    }

    /**
     * Converts a Chinese international city name to its English equivalent.
     * If the city is not found in the map, the original Chinese name is returned.
     *
     * @param chineseCityName The Chinese name of the international city.
     * @return The English name of the city, or the original Chinese name if not found.
     */
    public static String getEnglishCityName(String chineseCityName) {
        // Return the English name if found, otherwise return the original Chinese name.
        return INTERNATIONAL_CITY_MAP.getOrDefault(chineseCityName, chineseCityName);
    }
} 