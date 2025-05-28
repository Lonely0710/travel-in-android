package com.bjtu.traveler.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bjtu.traveler.data.cache.CityCache;
import com.bjtu.traveler.api.UnsplashApi; // 导入 Unsplash API 类

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 城市数据仓库类 - 负责获取城市轮播图数据，并管理缓存和 API 调用
 */
public class CityRepository {
    private static CityRepository instance;
    private final CityCache cityCache; // 城市缓存实例
    private final UnsplashApi unsplashApi; // Unsplash API 实例
    // 使用单线程执行器处理后台任务，如网络请求和缓存操作
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Repository 内部维护的 MutableLiveData，用于存储城市轮播图数据并通知观察者
    private final MutableLiveData<List<CityCarouselItem>> _cityCarouselLiveData = new MutableLiveData<>();
    // 暴露给 ViewModel 的不可变 LiveData，ViewModel 只能观察它的变化
    public final LiveData<List<CityCarouselItem>> cityCarouselLiveData = _cityCarouselLiveData; 

    /**
     * 私有构造方法，初始化缓存和 API 实例，并加载数据
     */
    private CityRepository() {
        cityCache = CityCache.getInstance(); // 获取城市缓存单例
        unsplashApi = new UnsplashApi(); // 初始化 Unsplash API
        // 在 Repository 初始化时尝试加载数据
        loadCityCarouselItems();
    }

    /**
     * 获取 CityRepository 单例
     * @return CityRepository 实例
     */
    public static synchronized CityRepository getInstance() {
        if (instance == null) {
            instance = new CityRepository();
        }
        return instance;
    }

    /**
     * 加载城市轮播图数据：先尝试从缓存读取，如果缓存不存在或过期，则从 API 获取
     * 数据会通过 cityCarouselLiveData 暴露
     */
    private void loadCityCarouselItems() {
        executorService.execute(() -> {
            // 首先，尝试从缓存加载数据
            List<CityCarouselItem> cachedData = cityCache.getCityCarouselItems();
            if (cachedData != null && !cachedData.isEmpty()) {
                _cityCarouselLiveData.postValue(cachedData); // 将缓存数据发送给内部 MutableLiveData
                // 检查缓存是否过期
                if (cityCache.isCacheExpired()) {
                    // 如果缓存过期，在后台线程从 API 获取最新数据
                    fetchCitiesFromApi();
                }
            } else {
                // 如果缓存为空或不存在，则从 API 获取数据
                fetchCitiesFromApi();
            }
        });
    }

    /**
     * 从 Unsplash API 获取城市图片数据，并在获取成功后更新缓存和内部 LiveData
     */
    private void fetchCitiesFromApi() {
        executorService.execute(() -> {
            List<CityCarouselItem> apiData = new ArrayList<>();
            // 定义一个城市列表作为搜索关键词
            String[] cities = {"Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Chengdu", "Tokyo", "Paris", "New York"}; // 示例城市

            for (String city : cities) {
                try {
                    // 调用 Unsplash API 搜索图片，每个城市获取一张
                    List<CityCarouselItem> items = unsplashApi.searchPhotos(city);
                    if (items != null && !items.isEmpty()) {
                        // 将搜索到的城市图片项添加到结果列表
                        apiData.add(items.get(0)); // 取第一个结果
                    }
                } catch (IOException e) {
                    // 处理 API 调用错误
                    e.printStackTrace(); // 打印错误堆栈
                    // 如果 API 调用失败，可以考虑记录日志或返回部分已获取的数据
                    // 当前实现是打印错误堆栈并继续处理下一个城市
                }
            }

            // 如果从 API 获取到了数据
            if (!apiData.isEmpty()) {
                 // 更新缓存
                cityCache.saveCityCarouselItems(apiData);
                // 将新数据发送给内部 MutableLiveData
                _cityCarouselLiveData.postValue(apiData);
            } else {
                 // 如果 API 没有返回任何数据，可以根据需求处理
                 // 例如：如果缓存也不存在，可以发送一个空列表；如果缓存存在，保持旧数据不变
            }
        });
    }

    /**
     * 刷新城市轮播图数据 (例如，下拉刷新)
     * 调用此方法会强制从 API 获取最新数据并更新缓存和 LiveData
     */
    public void refreshCityCarouselItems() {
        fetchCitiesFromApi(); // 从 API 获取数据
    }
} 