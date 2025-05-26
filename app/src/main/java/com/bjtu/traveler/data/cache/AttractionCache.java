package com.bjtu.traveler.data.cache;

import com.bjtu.traveler.data.model.Attraction;
import java.util.ArrayList;
import java.util.List;

/**
 * 景点缓存类，采用单例模式，缓存热门和推荐景点数据，避免重复请求。
 */
public class AttractionCache {
    // 单例实例
    private static final AttractionCache instance = new AttractionCache();

    // 热门景点缓存列表
    private final List<Attraction> hotAttractions = new ArrayList<>();
    // 热门景点是否已加载标志
    private boolean loaded = false;

    // 推荐景点缓存列表
    private final List<Attraction> recommendAttractions = new ArrayList<>();
    // 推荐景点是否已加载标志
    private boolean recommendLoaded = false;

    // 私有构造方法，防止外部实例化
    private AttractionCache() {}

    // 获取单例实例
    public static AttractionCache getInstance() {
        return instance;
    }

    // 设置热门景点缓存（线程安全）
    public synchronized void setHotAttractions(List<Attraction> list) {
        hotAttractions.clear();
        if (list != null) hotAttractions.addAll(list);
        loaded = true;
    }

    // 获取热门景点缓存（线程安全，返回副本）
    public synchronized List<Attraction> getHotAttractions() {
        return new ArrayList<>(hotAttractions);
    }

    // 判断热门景点是否已加载
    public synchronized boolean isLoaded() {
        return loaded;
    }

    // 设置推荐景点缓存（线程安全）
    public synchronized void setRecommendAttractions(List<Attraction> list) {
        recommendAttractions.clear();
        if (list != null) recommendAttractions.addAll(list);
        recommendLoaded = true;
    }

    // 获取推荐景点缓存（线程安全，返回副本）
    public synchronized List<Attraction> getRecommendAttractions() {
        return new ArrayList<>(recommendAttractions);
    }

    // 判断推荐景点是否已加载
    public synchronized boolean isRecommendLoaded() {
        return recommendLoaded;
    }

    // 清空所有缓存（线程安全）
    public synchronized void clear() {
        hotAttractions.clear();
        loaded = false;
        recommendAttractions.clear();
        recommendLoaded = false;
    }
}