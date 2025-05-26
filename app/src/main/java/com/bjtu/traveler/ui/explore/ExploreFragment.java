package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bjtu.traveler.adapter.AttractionAdapter;
import com.bjtu.traveler.api.OverpassApi;
import com.bjtu.traveler.data.model.Attraction;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.CityList;
import com.bjtu.traveler.data.cache.AttractionCache;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import android.os.Handler;
import android.os.Looper;
import java.util.HashMap;
import java.util.Map;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Geocoder;
import android.location.Address;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import com.bjtu.traveler.ui.explore.DiscoverFragment;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bjtu.traveler.api.WikipediaApi;
import com.bumptech.glide.Glide;
import android.text.TextUtils;
import androidx.fragment.app.FragmentTransaction;
import com.bjtu.traveler.ui.profile.PostHistoryFragment;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class ExploreFragment extends Fragment {
    private RecyclerView rvHotSpots, rvRecommended;
    private AttractionAdapter hotAdapter, recommendedAdapter;
    private List<Attraction> hotList = new ArrayList<>();
    private List<Attraction> recommendedList = new ArrayList<>();
    private ProgressBar progressRecommended;

    // 推荐区分页显示相关变量
    private static final int RECOMMENDED_PAGE_SIZE = 20;
    private int recommendedDisplayLoaded = 0;
    private List<Attraction> recommendedDisplayList = new ArrayList<>();

    // 热门区分页相关变量
    private int hotListLoaded = 0;
    private List<Attraction> hotAllList = new ArrayList<>();

    // 分类映射（根据OSM官网tourism类型整理）
    private static final String[] HOT_CATEGORIES = {"attraction"};
    private static final String[] ADVENTURE_CATEGORIES = {"camp_site", "wilderness_hut", "picnic_site", "caravan_site", "chalet"};
    private static final String[] RELAX_CATEGORIES = {"viewpoint", "zoo", "theme_park", "hot_spring", "beach", "park", "garden", "spa"};
    private static final String[] CULTURE_CATEGORIES = {"museum", "gallery", "artwork"};

    private Chip chipHot, chipAdventure, chipRelax, chipCulture;

    // 最大缓存条数
    private static final int MAX_CACHE_SIZE = 20;

    // 分类数据缓存，key为类型（adventure/relax/culture），value为景点列表
    private Map<String, List<Attraction>> categoryCache = new HashMap<>();

    // 当前定位
    private String currentCity = null;
    private String currentCountry = null;

    // 附近景点数据缓存
    private List<Attraction> cachedNearbyAttractions = null;
    private double cachedNearbyLat = 0;
    private double cachedNearbyLon = 0;

    // 防止推荐区重复加载
    private boolean isLoadingRecommended = false;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private ActivityResultLauncher<String[]> permissionLauncher;

    // 判断category是否属于指定类型
    private boolean isCategoryIn(String category, String[] types) {
        if (category == null) return false;
        for (String t : types) {
            if (category.equalsIgnoreCase(t)) return true;
        }
        return false;
    }

    // 获取推荐区分区数据，全部从categoryCache取，首次加载时自动查API
    private List<Attraction> getDataForCategory(String type) {
        if (categoryCache.containsKey(type)) {
            return categoryCache.get(type);
        }
        return new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        // 添加“发现”按钮，右上角
        TextView tvDiscover = view.findViewById(R.id.tv_discover);
        tvDiscover.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new DiscoverFragment())
                    .hide(this)
                    .addToBackStack(null)
                    .commit();
        });

        // 初始化权限请求Launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    View v = getView();
                    if (v != null) {
                        TextView tvCurrentLocation = v.findViewById(R.id.tv_current_location);
                        if (Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) ||
                                Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false))) {
                            getLocationAndUpdate(v.getContext(), tvCurrentLocation);
                        } else {
                            tvCurrentLocation.setText("定位失败: 无权限");
                        }
                    }
                }
        );

        // 定位相关
        TextView tvCurrentLocation = view.findViewById(R.id.tv_current_location);
        tvCurrentLocation.setText("定位中...");
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 使用新版API请求权限
            permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            getLocationAndUpdate(view.getContext(), tvCurrentLocation);
        }

        currentCity = null;
        currentCountry = null;

        chipHot = view.findViewById(R.id.chip_hot);
        chipAdventure = view.findViewById(R.id.chip_adventure);
        chipRelax = view.findViewById(R.id.chip_relax);
        chipCulture = view.findViewById(R.id.chip_culture);

        RecyclerView rvRecommended = view.findViewById(R.id.rv_recommended);
        recommendedAdapter = new AttractionAdapter(new ArrayList<>(), R.layout.item_vertical_attraction, currentCity, currentCountry);
        rvRecommended.setAdapter(recommendedAdapter);
        // 设置点击事件，跳转到详情Fragment
        recommendedAdapter.setOnAttractionClickListener((attraction, imgUrl, city, country) -> {
            Bundle args = new Bundle();
            args.putString("name", attraction.name);
            args.putString("category", attraction.category);
            args.putString("description", attraction.description);
            // 传递景点自身的city和country字段
            args.putString("city", attraction.city);
            args.putString("country", attraction.country);
            args.putString("imgUrl", imgUrl);
            // 记录当前chip类型
            args.putString("chipType", getCurrentChipType());
            AttractionDetailFragment fragment = new AttractionDetailFragment();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .hide(this)
                    .addToBackStack(null)
                    .commit();
        });

        rvHotSpots = view.findViewById(R.id.rv_hot_spots);
        hotAdapter = new AttractionAdapter(hotList, R.layout.item_horizontal_attraction, currentCity, currentCountry);
        rvHotSpots.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvHotSpots.setAdapter(hotAdapter);
        // 热门区点击事件，跳转到详情Fragment
        hotAdapter.setOnAttractionClickListener((attraction, imgUrl, city, country) -> {
            Bundle args = new Bundle();
            args.putString("name", attraction.name);
            args.putString("category", attraction.category);
            args.putString("description", attraction.description);
            // 传递查询用城市city
            args.putString("city", city);
            args.putString("country", country);
            args.putString("imgUrl", imgUrl);
            // 记录当前chip类型
            args.putString("chipType", getCurrentChipType());
            AttractionDetailFragment fragment = new AttractionDetailFragment();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .hide(this)
                    .addToBackStack(null)
                    .commit();
        });

        progressRecommended = view.findViewById(R.id.progress_recommended);

        // 搜索功能实现
        android.widget.EditText etSearch = view.findViewById(R.id.et_search);
        android.widget.ImageButton btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) return;
            if (progressRecommended != null) progressRecommended.setVisibility(View.VISIBLE);
            recommendedAdapter.setCurrentLocation(currentCity, currentCountry);
            if (recommendedList.size() >= MAX_CACHE_SIZE) {
                recommendedDisplayList.clear();
                recommendedDisplayLoaded = 0;
                loadMoreRecommendedDisplay();
                if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                return;
            }
            recommendedList.clear();
            recommendedDisplayList.clear();
            recommendedDisplayLoaded = 0;
            String type = getCurrentChipType();
            String[] typesArr;
            if (type.equals("hot")) {
                typesArr = HOT_CATEGORIES;
            } else if (type.equals("adventure")) {
                typesArr = ADVENTURE_CATEGORIES;
            } else if (type.equals("relax")) {
                typesArr = RELAX_CATEGORIES;
            } else {
                typesArr = CULTURE_CATEGORIES;
            }
            List<com.bjtu.traveler.data.model.Attraction> allResults = new ArrayList<>();
            AtomicInteger finished = new AtomicInteger(0);
            for (String cat : typesArr) {
                com.bjtu.traveler.api.OverpassApi.fetchAttractionsByCategory(keyword, cat, 3, new com.bjtu.traveler.api.OverpassApi.Callback() {
                    @Override
                    public void onSuccess(List<com.bjtu.traveler.data.model.Attraction> attractions) {
                        if (attractions != null && !attractions.isEmpty()) {
                            synchronized (allResults) {
                                // 修正：为每个景点设置city为keyword，country为当前currentCountry（如有）
                                for (com.bjtu.traveler.data.model.Attraction a : attractions) {
                                    a.city = keyword;
                                    a.country = currentCountry != null ? currentCountry : "";
                                }
                                allResults.addAll(attractions);
                            }
                        }
                        if (finished.incrementAndGet() == typesArr.length) {
                            List<com.bjtu.traveler.data.model.Attraction> limited = allResults.size() > 20 ? allResults.subList(0, 20) : allResults;
                            recommendedList.clear();
                            recommendedList.addAll(limited);
                            recommendedDisplayList.clear();
                            recommendedDisplayLoaded = 0;
                            recommendedDisplayList.addAll(limited);
                            recommendedDisplayLoaded = recommendedDisplayList.size();
                            recommendedAdapter.setData(new ArrayList<>(recommendedDisplayList));
                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        if (finished.incrementAndGet() == typesArr.length) {
                            recommendedList.clear();
                            recommendedDisplayList.clear();
                            recommendedDisplayLoaded = 0;
                            recommendedAdapter.setData(new ArrayList<>());
                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        // 分类Chip点击逻辑
        View.OnClickListener chipClickListener = v -> {
            chipHot.setChecked(false);
            chipAdventure.setChecked(false);
            chipRelax.setChecked(false);
            chipCulture.setChecked(false);
            ((Chip) v).setChecked(true);
            recommendedAdapter.setCurrentLocation(currentCity, currentCountry);
            String type = chipHot.isChecked() ? "hot" : chipAdventure.isChecked() ? "adventure" : chipRelax.isChecked() ? "relax" : "culture";
            if (categoryCache.containsKey(type) && !categoryCache.get(type).isEmpty()) {
                recommendedDisplayList.clear();
                recommendedDisplayList.addAll(categoryCache.get(type));
                recommendedAdapter.setData(new ArrayList<>(recommendedDisplayList));
                if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                return;
            }
            if (progressRecommended != null) progressRecommended.setVisibility(View.VISIBLE);
            List<String> cities = new ArrayList<>(CityList.RECOMMEND_CITIES);
            Collections.shuffle(cities);
            List<String> selectedCities = cities.subList(0, Math.min(10, cities.size()));
            String[] typesArr;
            if (type.equals("hot")) {
                typesArr = HOT_CATEGORIES;
            } else if (type.equals("adventure")) {
                typesArr = ADVENTURE_CATEGORIES;
            } else if (type.equals("relax")) {
                typesArr = RELAX_CATEGORIES;
            } else {
                typesArr = CULTURE_CATEGORIES;
            }
            fetchRecommendedAttractionsBatch(type, typesArr, selectedCities, 2); // 取数据
        };

        chipHot.setOnClickListener(chipClickListener);
        chipAdventure.setOnClickListener(chipClickListener);
        chipRelax.setOnClickListener(chipClickListener);
        chipCulture.setOnClickListener(chipClickListener);
        // 默认选中热门
        chipHot.setChecked(true);
        // chipClickListener.onClick(chipHot); // 移除首次进入时的主动点击，避免二次刷新

        rvHotSpots.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= hotList.size() - 2) {
                    if (hotListLoaded < hotAllList.size()) {
                        fillHotListPage();
                    } else {
                        loadMoreHotAttractions();
                    }
                }
            }
        });
        return view;
    }

    // Tab选中状态切换
    private void updateTabSelected(TextView selected, TextView unselected1, TextView unselected2) {
        selected.setBackgroundResource(R.drawable.bg_tab_selected);
        unselected1.setBackgroundResource(R.drawable.bg_tab_unselected);
        unselected2.setBackgroundResource(R.drawable.bg_tab_unselected);
    }

    // 缓存页面数据，防止返回时重新加载
    private Bundle savedState = null;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 缓存推荐区和热门区数据
        outState.putSerializable("hotList", new ArrayList<>(hotList));
        outState.putSerializable("hotAllList", new ArrayList<>(hotAllList));
        outState.putInt("hotListLoaded", hotListLoaded);
        outState.putSerializable("recommendedList", new ArrayList<>(recommendedList));
        outState.putSerializable("recommendedDisplayList", new ArrayList<>(recommendedDisplayList));
        outState.putInt("recommendedDisplayLoaded", recommendedDisplayLoaded);
        outState.putBoolean("chipHotChecked", chipHot != null && chipHot.isChecked());
        outState.putBoolean("chipAdventureChecked", chipAdventure != null && chipAdventure.isChecked());
        outState.putBoolean("chipRelaxChecked", chipRelax != null && chipRelax.isChecked());
        outState.putBoolean("chipCultureChecked", chipCulture != null && chipCulture.isChecked());
        // 额外缓存搜索框内容
        android.widget.EditText etSearch = getView() != null ? getView().findViewById(R.id.et_search) : null;
        if (etSearch != null) {
            outState.putString("searchText", etSearch.getText().toString());
        }
        // 保存所有分区缓存
        HashMap<String, ArrayList<Attraction>> cacheCopy = new HashMap<>();
        for (Map.Entry<String, List<Attraction>> entry : categoryCache.entrySet()) {
            cacheCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        outState.putSerializable("categoryCache", cacheCopy);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            // 恢复数据
            hotList.clear();
            hotList.addAll((ArrayList<Attraction>) savedInstanceState.getSerializable("hotList"));
            hotAllList.clear();
            hotAllList.addAll((ArrayList<Attraction>) savedInstanceState.getSerializable("hotAllList"));
            hotListLoaded = savedInstanceState.getInt("hotListLoaded", 0);
            recommendedList.clear();
            recommendedList.addAll((ArrayList<Attraction>) savedInstanceState.getSerializable("recommendedList"));
            recommendedDisplayList.clear();
            recommendedDisplayList.addAll((ArrayList<Attraction>) savedInstanceState.getSerializable("recommendedDisplayList"));
            recommendedDisplayLoaded = savedInstanceState.getInt("recommendedDisplayLoaded", 0);
            boolean hotChecked = savedInstanceState.getBoolean("chipHotChecked", true);
            boolean adventureChecked = savedInstanceState.getBoolean("chipAdventureChecked", false);
            boolean relaxChecked = savedInstanceState.getBoolean("chipRelaxChecked", false);
            boolean cultureChecked = savedInstanceState.getBoolean("chipCultureChecked", false);
            if (chipHot != null) chipHot.setChecked(hotChecked);
            if (chipAdventure != null) chipAdventure.setChecked(adventureChecked);
            if (chipRelax != null) chipRelax.setChecked(relaxChecked);
            if (chipCulture != null) chipCulture.setChecked(cultureChecked);
            // 恢复搜索框内容
            android.widget.EditText etSearch = getView() != null ? getView().findViewById(R.id.et_search) : null;
            if (etSearch != null && savedInstanceState.containsKey("searchText")) {
                etSearch.setText(savedInstanceState.getString("searchText", ""));
            }
            // 恢复所有分区缓存
            Object cacheObj = savedInstanceState.getSerializable("categoryCache");
            if (cacheObj instanceof HashMap) {
                //noinspection unchecked
                categoryCache = (HashMap<String, List<Attraction>>) cacheObj;
            }
            // 恢复chip选中状态后，刷新推荐区内容
            if (recommendedAdapter != null) {
                String type = getCurrentChipType();
                List<Attraction> filtered = getDataForCategory(type);
                recommendedDisplayList.clear();
                recommendedDisplayList.addAll(filtered);
                recommendedAdapter.setData(new ArrayList<>(recommendedDisplayList));
            }
        }
        // 恢复附近景点区
        View root = getView();
        if (root != null && cachedNearbyAttractions != null) {
            ViewGroup left = root.findViewById(R.id.nearby_spot_left);
            ViewGroup right = root.findViewById(R.id.nearby_spot_right);
            renderNearbyViews(left, right, cachedNearbyAttractions, cachedNearbyLat, cachedNearbyLon);
        }
    }

    // 用于页面恢复的标志
    private boolean isRestored = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 检查热门和推荐区数据是否已存在，存在则直接恢复，不再重新加载
        if (hotList != null && !hotList.isEmpty() && recommendedList != null && !recommendedList.isEmpty()) {
            if (hotAdapter != null) hotAdapter.notifyDataSetChanged();
            if (recommendedAdapter != null) {
                // 只刷新当前chip对应的推荐区内容，避免二次刷新
                String type = getCurrentChipType();
                List<Attraction> filtered = getDataForCategory(type);
                recommendedDisplayList.clear();
                recommendedDisplayList.addAll(filtered);
                recommendedAdapter.setData(new ArrayList<>(recommendedDisplayList));
            }
            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
            isRestored = true;
            return;
        }
        if (savedInstanceState != null) {
            // 已在onViewStateRestored恢复数据，这里无需再加载
            isRestored = true;
            return;
        }
        // 检查是否有chipType参数（从详细页返回时）
        Bundle args = getArguments();
        if (args != null && args.containsKey("chipType")) {
            String chipType = args.getString("chipType", "hot");
            chipHot.setChecked("hot".equals(chipType));
            chipAdventure.setChecked("adventure".equals(chipType));
            chipRelax.setChecked("relax".equals(chipType));
            chipCulture.setChecked("culture".equals(chipType));
            // 刷新推荐区内容
            String type = getCurrentChipType();
            List<Attraction> filtered = getDataForCategory(type);
            recommendedDisplayList.clear();
            recommendedDisplayList.addAll(filtered);
            recommendedAdapter.setData(new ArrayList<>(recommendedDisplayList));
        }
        // 首次进入才加载数据
        if (AttractionCache.getInstance().isLoaded()) {
            hotList.clear();
            hotList.addAll(AttractionCache.getInstance().getHotAttractions());
            hotAllList.clear();
            hotAllList.addAll(AttractionCache.getInstance().getHotAttractions());
            fillHotListPage();
        } else {
            loadHotAttractions();
        }
        if (AttractionCache.getInstance().isRecommendLoaded()) {
            recommendedList.clear();
            recommendedList.addAll(AttractionCache.getInstance().getRecommendAttractions());
            recommendedDisplayList.clear();
            recommendedDisplayLoaded = 0;
            loadMoreRecommendedDisplay();
        } else {
            loadRecommendedAttractions();
        }
    }

    // 加载更多推荐区数据（分页）
    private void loadMoreRecommendedDisplay() {
        String type = chipHot.isChecked() ? "hot" : chipAdventure.isChecked() ? "adventure" : chipRelax.isChecked() ? "relax" : "culture";
        List<Attraction> filtered = getDataForCategory(type);
        int remain = filtered.size() - recommendedDisplayLoaded;
        int count = Math.min(RECOMMENDED_PAGE_SIZE, remain);
        if (count > 0) {
            recommendedDisplayList.addAll(filtered.subList(recommendedDisplayLoaded, recommendedDisplayLoaded + count));
            recommendedDisplayLoaded += count;
        }
        // 无论是否有新数据，都刷新一次
        new Handler(Looper.getMainLooper()).post(() -> {
            recommendedAdapter.setData(new ArrayList<>(recommendedDisplayList));
        });
        if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
    }

    // 加载热门景点（重置并加载第一页）
    private void loadHotAttractions() {
        hotList.clear();
        hotAdapter.notifyDataSetChanged();
        hotAllList.clear();
        hotListLoaded = 0;
        // 每次随机5个热门城市
        List<String> cities = new ArrayList<>(CityList.HOT_CITIES);
        Collections.shuffle(cities);
        List<String> selectedCities = cities.subList(0, Math.min(5, cities.size()));
        List<Attraction> allAttractions = new ArrayList<>();
        AtomicInteger finished = new AtomicInteger(0);
        if (progressRecommended != null) progressRecommended.setVisibility(View.VISIBLE);
        for (String city : selectedCities) {
            if (allAttractions.size() >= MAX_CACHE_SIZE) break;
            OverpassApi.fetchAttractions(city, 10, new OverpassApi.Callback() {
                @Override
                public void onSuccess(List<Attraction> attractions) {
                    if (attractions != null && !attractions.isEmpty()) {
                        synchronized (allAttractions) {
                            int canAdd = Math.min(MAX_CACHE_SIZE - allAttractions.size(), attractions.size());
                            for (int i = 0; i < canAdd; i++) {
                                Attraction a = attractions.get(i);
                                a.city = city;
                                // 过滤掉hotel、hostel、information
                                if (a.category == null) continue;
                                String cat = a.category.trim().toLowerCase();
                                if (cat.equals("hotel") || cat.equals("hostel") || cat.equals("information")) continue;
                                allAttractions.add(a);
                            }
                        }
                    }
                    if (finished.incrementAndGet() == selectedCities.size() || allAttractions.size() >= MAX_CACHE_SIZE) {
                        AttractionCache.getInstance().setHotAttractions(new ArrayList<>(allAttractions));
                        hotAllList.clear();
                        hotAllList.addAll(allAttractions);
                        fillHotListPage();
                        if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    if (finished.incrementAndGet() == selectedCities.size() || allAttractions.size() >= MAX_CACHE_SIZE) {
                        AttractionCache.getInstance().setHotAttractions(new ArrayList<>(allAttractions));
                        hotAllList.clear();
                        hotAllList.addAll(allAttractions);
                        fillHotListPage();
                        if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    // 填充一页热门景点（每次只显示8条），去除hotel和hostel
    private void fillHotListPage() {
        // 过滤掉hotel、hostel、information（忽略大小写和前后空格）
        List<Attraction> filteredHotAllList = new ArrayList<>();
        for (Attraction a : hotAllList) {
            if (a.category == null) continue;
            String cat = a.category.trim().toLowerCase();
            if (cat.equals("hotel") || cat.equals("hostel") || cat.equals("information")) continue;
            filteredHotAllList.add(a);
        }
        int remain = filteredHotAllList.size() - hotListLoaded;
        int count = Math.min(20, remain); // 每页20个
        if (count > 0) {
            hotList.addAll(filteredHotAllList.subList(hotListLoaded, hotListLoaded + count));
            hotListLoaded += count;
            new Handler(Looper.getMainLooper()).post(() -> {
                hotAdapter.notifyDataSetChanged();
            });
        }
    }

    // 加载更多热门景点（可根据实际需求实现）
    private void loadMoreHotAttractions() {
        // 示例：不做实际网络请求，直接调用 fillHotListPage()
        fillHotListPage();
    }

    // 推荐区加载更多（优化：只请求当前chip分区，所有请求并发，全部返回后合并结果）
    private void loadRecommendedAttractions() {
        if (isLoadingRecommended) return;
        isLoadingRecommended = true;
        String type = getCurrentChipType();
        if (categoryCache.containsKey(type)) {
            // 已有缓存，直接显示
            recommendedDisplayList.clear();
            recommendedDisplayLoaded = 0;
            loadMoreRecommendedDisplay();
            isLoadingRecommended = false;
            return;
        }
        // 只请求当前chip分区
        String[] typesArr;
        if (type.equals("hot")) {
            typesArr = HOT_CATEGORIES;
        } else if (type.equals("adventure")) {
            typesArr = ADVENTURE_CATEGORIES;
        } else if (type.equals("relax")) {
            typesArr = RELAX_CATEGORIES;
        } else {
            typesArr = CULTURE_CATEGORIES;
        }
        List<String> cities = new ArrayList<>(CityList.RECOMMEND_CITIES);
        Collections.shuffle(cities);
        // 让城市多一点，比如取前8个
        List<String> selectedCities = cities.subList(0, Math.min(8, cities.size()));
        fetchRecommendedAttractionsBatch(type, typesArr, selectedCities, 2);
    }

    // 获取当前选中的chip分区
    private String getCurrentChipType() {
        if (chipHot != null && chipHot.isChecked()) return "hot";
        if (chipAdventure != null && chipAdventure.isChecked()) return "adventure";
        if (chipRelax != null && chipRelax.isChecked()) return "relax";
        if (chipCulture != null && chipCulture.isChecked()) return "culture";
        return "hot";
    }

    // 封装：推荐区批量API请求（带超时保护）
    private void fetchRecommendedAttractionsBatch(String type, String[] typesArr, List<String> selectedCities, int limit) {
        List<Attraction> all = new ArrayList<>();
        int totalTasks = typesArr.length * selectedCities.size();
        AtomicInteger finished = new AtomicInteger(0);
        for (String cat : typesArr) {   //先请求分类
            for (String city : selectedCities) {  //再请求城市
                Handler timeoutHandler = new Handler(Looper.getMainLooper());
                Runnable timeoutRunnable = new Runnable() {
                    boolean called = false;
                    @Override
                    public void run() {
                        if (!called) {
                            called = true;
                            if (finished.incrementAndGet() == totalTasks) {
                                categoryCache.put(type, new ArrayList<>(all));
                                if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                                isLoadingRecommended = false;
                            }
                        }
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, 1000);
                OverpassApi.fetchAttractionsByCategory(city, cat, limit, new OverpassApi.Callback() {
                    boolean finishedOrTimeout = false;
                    @Override
                    public void onSuccess(List<Attraction> attractions) {
                        if (finishedOrTimeout) return;
                        finishedOrTimeout = true;
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        boolean updated = false;
                        if (attractions != null && !attractions.isEmpty()) {
                            synchronized (all) {
                                for (Attraction a : attractions) {
                                    if (a.category == null || !a.category.equalsIgnoreCase(cat)) continue;
                                    a.city = city;
                                    all.add(a);
                                    updated = true;
                                }
                            }
                        }
                        if (updated && getCurrentChipType().equals(type)) {
                            categoryCache.put(type, new ArrayList<>(all));
                            recommendedDisplayList.clear();
                            recommendedDisplayList.addAll(all);
                            recommendedAdapter.setData(new ArrayList<>(recommendedDisplayList));
                        }
                        if (finished.incrementAndGet() == totalTasks) {
                            categoryCache.put(type, new ArrayList<>(all));
                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                            isLoadingRecommended = false;
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        if (finishedOrTimeout) return;
                        finishedOrTimeout = true;
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        if (finished.incrementAndGet() == totalTasks) {
                            categoryCache.put(type, new ArrayList<>(all));
                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                            isLoadingRecommended = false;
                        }
                    }
                });
            }
        }
    }

    // 附近景点展示
    private void showNearbyAttractions(double lat, double lon) {
        View root = getView();
        if (root == null) return;
        ViewGroup left = root.findViewById(R.id.nearby_spot_left);
        ViewGroup right = root.findViewById(R.id.nearby_spot_right);
        if (left == null || right == null) return;
        left.removeAllViews();
        right.removeAllViews();
        // 判断是否有缓存且位置基本一致（1km内）
        if (cachedNearbyAttractions != null && Math.abs(lat-cachedNearbyLat)<0.01 && Math.abs(lon-cachedNearbyLon)<0.01) {
            renderNearbyViews(left, right, cachedNearbyAttractions, lat, lon);
            return;
        }
        int radius = 2000; // 1km
        int limit = 4;
        OverpassApi.fetchNearbyAttractions(lat, lon, radius, limit, new OverpassApi.Callback() {
            @Override
            public void onSuccess(List<Attraction> attractions) {
                cachedNearbyAttractions = attractions;
                cachedNearbyLat = lat;
                cachedNearbyLon = lon;
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    renderNearbyViews(left, right, attractions, lat, lon);
                });
            }
            @Override
            public void onFailure(Exception e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    addSimpleText(left, "加载失败");
                    addSimpleText(right, "");
                });
            }
        });
    }

    // 渲染附近景点卡片
    private void renderNearbyViews(ViewGroup left, ViewGroup right, List<Attraction> attractions, double lat, double lon) {
        // 过滤掉hotel、hostel、information
        List<Attraction> filtered = new ArrayList<>();
        if (attractions != null) {
            for (Attraction a : attractions) {
                if (a.category == null) continue;
                String cat = a.category.trim().toLowerCase();
                if (cat.equals("hotel") || cat.equals("hostel") || cat.equals("information")) continue;
                filtered.add(a);
            }
        }
        if (filtered.isEmpty()) {
            addSimpleText(left, "附近无景点");
            addSimpleText(right, "");
            return;
        }
        Attraction a0 = filtered.get(0);
        addAttractionView(left, a0, lat, lon);
        if (filtered.size() > 1) {
            Attraction a1 = filtered.get(1);
            addAttractionView(right, a1, lat, lon);
        } else {
            addSimpleText(right, "");
        }
    }

    // 简单文本填充
    private void addSimpleText(ViewGroup parent, String text) {
        parent.removeAllViews();
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(16);
        tv.setPadding(16, 32, 16, 32);
        parent.addView(tv);
    }

    // 填充景点卡片（使用item_nearby_place布局）
    private void addAttractionView(ViewGroup parent, Attraction attraction, double userLat, double userLon) {
        parent.removeAllViews();
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_nearby_place, parent, false);
        TextView tvName = v.findViewById(R.id.text_place_name);
        TextView tvDistance = v.findViewById(R.id.text_distance);
        ImageView imgPlace = v.findViewById(R.id.image_place);
        tvName.setText(attraction.name);
        // 计算距离
        double distance = calcDistance(userLat, userLon, attraction.lat, attraction.lon);
        String distStr = distance < 1 ? String.format("距离 %.0f m", distance * 1000) : String.format("距离 %.1f km", distance);
        tvDistance.setText(distStr);
        // 加载图片，优先用attraction.url，无则查百科
        // if (imgPlace != null) {
        //     if (!TextUtils.isEmpty(attraction.url)) {
        //         Glide.with(this).load(attraction.url).into(imgPlace);
        //     } else {
        //         WikipediaApi.fetchAttractionInfo(attraction.name, new WikipediaApi.Callback() {
        //             @Override
        //             public void onSuccess(String desc, String wikiImgUrl) {
        //                 if (!TextUtils.isEmpty(wikiImgUrl)) {
        //                     attraction.url = wikiImgUrl;
        //                     Glide.with(ExploreFragment.this).load(wikiImgUrl).into(imgPlace);
        //                 } else {
        //                     imgPlace.setImageResource(R.drawable.exam3);
        //                 }
        //             }
        //             @Override
        //             public void onFailure(int errorCode, String errorMsg) {
        //                 imgPlace.setImageResource(R.drawable.exam3);
        v.setOnClickListener(view -> {
            Bundle args = new Bundle();
            args.putString("name", attraction.name);
            args.putString("category", attraction.category);
            args.putString("description", attraction.description);
            args.putString("city", attraction.city);
            args.putString("country", attraction.country);
            args.putString("imgUrl", attraction.url);
            AttractionDetailFragment fragment = new AttractionDetailFragment();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        parent.addView(v);
    }

    // 计算两点间距离（单位：千米）
    private double calcDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // 地球半径，千米
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // 定位并更新UI和变量
    private void getLocationAndUpdate(Context context, TextView tvCurrentLocation) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvCurrentLocation.setText("定位失败: 无权限");
            return;
        }
        try {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String city = address.getLocality();
                            String country = address.getCountryName();
                            currentCity = city != null ? city : "";
                            currentCountry = country != null ? country : "";
                            String locStr = (currentCity.isEmpty() ? "" : currentCity) + (currentCountry.isEmpty() ? "" : (currentCity.isEmpty() ? currentCountry : ", " + currentCountry));
                            // 只更新左上角定位文本
                            tvCurrentLocation.setText(locStr.isEmpty() ? "定位失败" : locStr);
                        } else {
                            tvCurrentLocation.setText("定位失败");
                        }
                    } catch (Exception e) {
                        tvCurrentLocation.setText("定位失败");
                    }
                    // 定位成功后查找附近景点
                    showNearbyAttractions(lat, lon);
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {
                    tvCurrentLocation.setText("定位失败");
                }
            }, null);
        } catch (Exception e) {
            tvCurrentLocation.setText("定位失败");
        }
    }
}

