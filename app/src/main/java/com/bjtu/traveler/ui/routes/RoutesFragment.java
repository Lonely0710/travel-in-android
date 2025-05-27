package com.bjtu.traveler.ui.routes;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.UiSettings;
import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.DayPlanAdapter;
import com.bjtu.traveler.utils.DeepSeekApiClient;
import com.bjtu.traveler.data.model.TravelPlan;
import com.bjtu.traveler.data.model.DayPlan;
import com.bjtu.traveler.utils.PlaceScraper;
import com.bjtu.traveler.viewmodel.RoutesViewModel;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;
import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import com.bjtu.traveler.data.model.BmobPlan;
import com.bjtu.traveler.data.model.Attraction;

public class RoutesFragment extends Fragment {

    private static final String TAG = "RoutesFragment";

    private RoutesViewModel routesViewModel;
    private RecyclerView recyclerViewDayPlans;
    private DayPlanAdapter dayPlanAdapter;
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private TextView tvMainItineraryTitle;
    private TextView tvUserPreferences1, tvUserPreferences2, tvUserPreferences3, tvUserPreferences4;
    private ImageView ivAttractionImage;
    private TextView tvAttractionName;
    private TextView tvAttractionAddress;
    private TextView tvAttractionStarRate;
    private TextView tvAttractionFee;
    private TextView tvAttractionDescription;
    private FrameLayout loadingOverlay;
    private ScrollView scrollViewContent;
    private View attractionDetailCardView; // 用于设置点击事件的视图
    private Attraction currentDisplayedAttraction;

    private TravelPlan receivedTravelPlan; // 从ChatFragment接收的规划信息
    private Gson gson = new Gson(); // 用于解析JSON

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SDKInitializer.isInitialized()) {
            SDKInitializer.setAgreePrivacy(requireContext(), true);
            SDKInitializer.initialize(requireContext());
        }
        routesViewModel = new ViewModelProvider(this).get(RoutesViewModel.class);

        if (getArguments() != null) {
            receivedTravelPlan = (TravelPlan) getArguments().getSerializable("travelPlan");
            if (receivedTravelPlan != null) {
                Log.d(TAG, "Received Travel Plan: " + receivedTravelPlan.toString());
                try {
                    PlaceScraper.initCityData(requireContext());
                    startTravelPlanning(receivedTravelPlan);
                } catch (RuntimeException e) {
                    Log.e(TAG, "城市数据初始化失败: " + e.getMessage());
                    Toast.makeText(getContext(), "城市数据加载失败，无法爬取。", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Received Travel Plan is null.");
                Toast.makeText(getContext(), "获取旅行需求失败，请重试。", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "No arguments received in RoutesFragment.");
            Toast.makeText(getContext(), "未获取到旅行需求。", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);

        mMapView = view.findViewById(R.id.mapView);
        if (mMapView != null) {
            mBaiduMap = mMapView.getMap();
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            mBaiduMap.setMyLocationEnabled(false);
        }

        if (mBaiduMap != null) {
            UiSettings uiSettings = mBaiduMap.getUiSettings();
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15f));
            uiSettings.setScrollGesturesEnabled(true);
            uiSettings.setZoomGesturesEnabled(true);
            uiSettings.setRotateGesturesEnabled(false);
        } else {
            Log.e(TAG, "MapView 或 BaiduMap 未能成功初始化。");
        }

        recyclerViewDayPlans = view.findViewById(R.id.recyclerViewDayPlans);
        setupRecyclerView();

        tvMainItineraryTitle = view.findViewById(R.id.tvMainItineraryTitle);
        // 假设您在 fragment_routes.xml 中定义了这些 ID 用于显示偏好
        // 如果没有，请在 XML 中添加，或使用一个 TextView 来显示所有偏好
        tvUserPreferences1 = view.findViewById(R.id.tvUserPreferences1); // 对应 XML 中的"美食"
        tvUserPreferences2 = view.findViewById(R.id.tvUserPreferences2); // 对应 XML 中的"文化"
        tvUserPreferences3 = view.findViewById(R.id.tvUserPreferences3); // 对应 XML 中的"购物"
        tvUserPreferences4 = view.findViewById(R.id.tvUserPreferences4); // 对应 XML 中的"休闲"

        ivAttractionImage = view.findViewById(R.id.ivAttractionImage);
        tvAttractionName = view.findViewById(R.id.tvAttractionName);
        tvAttractionAddress = view.findViewById(R.id.tvAttractionAddress);
        tvAttractionStarRate = view.findViewById(R.id.tvAttractionStarRate);
        tvAttractionFee = view.findViewById(R.id.tvAttractionFee);
        tvAttractionDescription = view.findViewById(R.id.tvAttractionDescription);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
        scrollViewContent = view.findViewById(R.id.scroll_view_content);
        attractionDetailCardView = view.findViewById(R.id.cardAttractionDetail);

        showRoutesLoading(true);

        if (attractionDetailCardView != null) {
            attractionDetailCardView.setOnClickListener(v -> {
                if (currentDisplayedAttraction != null && currentDisplayedAttraction.getDetailPageUrl() != null && !currentDisplayedAttraction.getDetailPageUrl().isEmpty()) {
                    navigateToWebView(currentDisplayedAttraction.getDetailPageUrl(), currentDisplayedAttraction.getName());
                } else {
                    Log.w(TAG, "Attraction detail URL is missing or currentDisplayedAttraction is null.");
                    Log.w(TAG, "currentAttraction: " + currentDisplayedAttraction.toString());
                    Log.w(TAG, "Attraction detail URL: " + currentDisplayedAttraction.getDetailPageUrl());
                    Toast.makeText(getContext(), "无法打开详情页面", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Attraction detail card view (R.id.card_attraction_detail) not found in layout.");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupRecyclerView() {
        dayPlanAdapter = new DayPlanAdapter(getContext(), new ArrayList<>());
        recyclerViewDayPlans.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewDayPlans.setAdapter(dayPlanAdapter);
    }

    private void startTravelPlanning(TravelPlan travelPlan) {
        // Toast.makeText(getContext(), "正在为您规划 " + travelPlan.getDestination() + " 的行程...", Toast.LENGTH_LONG).show();

        CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 爬取城市景点数据
                List<PlaceScraper.Destination> attractions = PlaceScraper.scrapeCityDestinations(requireContext(), travelPlan.getDestination()).get();
                if (attractions == null || attractions.isEmpty()) {
                    Log.w(TAG, "未爬取到有效景点数据，返回空规划。");
                    return new ArrayList<DayPlan>(); // 返回一个空的 DayPlan 列表
                }
                // 缓存
                HashMap cachedAttractionsMap = new HashMap<>();
                for (PlaceScraper.Destination dest : attractions) {
                    if (dest.name != null && !dest.name.isEmpty()) {
                        cachedAttractionsMap.put(dest.name.toLowerCase(), dest);
                    }
                }
                Log.d(TAG, "Cached " + cachedAttractionsMap.size() + " scraped attractions.");

                String attractionsJson = gson.toJson(attractions);
                Log.d(TAG, "Scraped " + attractions.size() + " attractions. JSON: " + attractionsJson);

                // 2. 调用 DeepSeek AI 进行规划
                String aiPlanJson = DeepSeekApiClient.getTravelPlan(travelPlan, attractionsJson);
                Log.d(TAG, "AI generated plan JSON: " + aiPlanJson);
                if (aiPlanJson == null || aiPlanJson.isEmpty() || aiPlanJson.equalsIgnoreCase("null")) {
                    Log.w(TAG, "AI 未返回有效规划 JSON。");
                    return new ArrayList<DayPlan>(); // 返回一个空的 DayPlan 列表
                }

                // === 清理 AI 返回的 JSON 字符串 ===
                String cleanedAiPlanJson = aiPlanJson.trim();
                if (cleanedAiPlanJson.startsWith("```json")) {
                    cleanedAiPlanJson = cleanedAiPlanJson.substring(7); // 移除 "```json" (7个字符)
                } else if (cleanedAiPlanJson.startsWith("```")) {
                    cleanedAiPlanJson = cleanedAiPlanJson.substring(3); // 移除 "```"
                }
                if (cleanedAiPlanJson.endsWith("```")) {
                    cleanedAiPlanJson = cleanedAiPlanJson.substring(0, cleanedAiPlanJson.length() - 3); // 移除末尾的 "```"
                }
                cleanedAiPlanJson = cleanedAiPlanJson.trim(); // 再次 trim 以防万一
                Log.d(TAG, "AI generated plan cleaned JSON: " + cleanedAiPlanJson);

                // === 新增调试日志：检查第一个字符 ===
                if (cleanedAiPlanJson != null && !cleanedAiPlanJson.isEmpty()) {
                    char firstChar = cleanedAiPlanJson.charAt(0);
                    Log.d(TAG, "First character of cleanedAiPlanJson: '" + firstChar + "' (Unicode: " + (int)firstChar + ")");
                } else {
                    Log.d(TAG, "cleanedAiPlanJson is null or empty before parsing!");
                }

                // 3. 解析 AI 返回的 JSON 规划
                Type dayPlanListType = new TypeToken<List<DayPlan>>(){}.getType();
                List<DayPlan> dailyPlans = gson.fromJson(cleanedAiPlanJson, dayPlanListType);
                if (dailyPlans == null) { // gson.fromJson 可能会返回 null 如果输入是 "null" 字符串
                    Log.w(TAG, "Gson 解析 AI 规划 JSON 后返回 null。");
                    return new ArrayList<DayPlan>(); // 返回一个空的 DayPlan 列表
                }
                return dailyPlans;

            } catch (Exception e) {
                Log.e(TAG, "Travel planning failed within supplyAsync: " + e.getMessage(), e);
                return new ArrayList<DayPlan>(); // *** 关键修改：发生异常时返回一个空的 List<DayPlan> ***
            }
        }).thenAccept(plannedDayPlans -> { // 现在 plannedDayPlans 的类型会被正确推断为 List<DayPlan>
            requireActivity().runOnUiThread(() -> {
                showRoutesLoading(false);
                // 后续代码中 plannedDayPlans.isEmpty() 就可以正常工作了
                if (plannedDayPlans != null && !plannedDayPlans.isEmpty()) { // 其实 null 判断可以省略，因为我们保证了返回非null
                    Toast.makeText(getContext(), "行程规划成功！", Toast.LENGTH_LONG).show();
                    displayTravelPlan(plannedDayPlans);
                    saveTravelPlanToBmob(plannedDayPlans, receivedTravelPlan);
                } else {
                    Toast.makeText(getContext(), "未能生成有效的行程规划，请尝试调整输入。", Toast.LENGTH_LONG).show();
                    tvMainItineraryTitle.setText("规划失败：无有效行程");
                    // ... (清空UI的代码)
                }
            });
        }).exceptionally(e -> {
            requireActivity().runOnUiThread(() -> {
                showRoutesLoading(false);
                Log.e(TAG, "Error in travel planning CompletableFuture: " + e.getMessage(), e);
                Toast.makeText(getContext(), "行程规划遇到问题: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), Toast.LENGTH_LONG).show();
                tvMainItineraryTitle.setText("规划错误");
                // ... (清空UI的代码)
            });
            return null;
        });
    }

    private void showRoutesLoading(boolean isLoading) {
        if (loadingOverlay != null && scrollViewContent != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            scrollViewContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }

    // 显示规划到UI
    private void displayTravelPlan(List<DayPlan> plannedDayPlans) {
        if (plannedDayPlans == null || plannedDayPlans.isEmpty()) { // 增加对 plannedDayPlans 本身的 null 检查
            tvMainItineraryTitle.setText("未生成详细行程");
            // 隐藏或清空其他UI元素
            if (attractionDetailCardView != null) {
                attractionDetailCardView.setVisibility(View.GONE);
            }
            tvUserPreferences1.setVisibility(View.GONE);
            tvUserPreferences2.setVisibility(View.GONE);
            tvUserPreferences3.setVisibility(View.GONE);
            tvUserPreferences4.setVisibility(View.GONE);
            dayPlanAdapter.setDayPlans(new ArrayList<>()); // 清空 RecyclerView
            return;
        }

        // 更新偏好 (将 List<String> 显示到四个 TextView 中)
        if (receivedTravelPlan != null && receivedTravelPlan.getPreferences() != null) {
            List<String> preferences = receivedTravelPlan.getPreferences();
            TextView[] preferenceTextViews = {tvUserPreferences1, tvUserPreferences2, tvUserPreferences3, tvUserPreferences4};
            for (int i = 0; i < preferenceTextViews.length; i++) {
                if (i < preferences.size() && !TextUtils.isEmpty(preferences.get(i))) { // 额外检查偏好字符串是否为空
                    preferenceTextViews[i].setText(preferences.get(i));
                    preferenceTextViews[i].setVisibility(View.VISIBLE);
                } else {
                    preferenceTextViews[i].setVisibility(View.GONE);
                }
            }
        } else {
            tvUserPreferences1.setVisibility(View.GONE);
            tvUserPreferences2.setVisibility(View.GONE);
            tvUserPreferences3.setVisibility(View.GONE);
            tvUserPreferences4.setVisibility(View.GONE);
        }

        // 更新行程总标题
        String title = receivedTravelPlan.getDestination() + " " + receivedTravelPlan.getDays() + "天行程";
        if (receivedTravelPlan.getBudget() > 0) {
            title += " (" + receivedTravelPlan.getBudget() + "元预算)";
        }
        tvMainItineraryTitle.setText(title);

        // 更新 DayPlanAdapter
        dayPlanAdapter.setDayPlans(plannedDayPlans);

        // --- 显示第一天上午的景点详细信息，并管理 currentDisplayedAttraction 和卡片可见性 ---
        currentDisplayedAttraction = null; // 每次显示新规划时先重置
        boolean detailCardVisible = false;

        DayPlan firstDayPlan = plannedDayPlans.get(0); // 假设 plannedDayPlans 不为空 (已在方法开头检查)

        if (firstDayPlan != null && !TextUtils.isEmpty(firstDayPlan.getMorningActivity()) &&
                !firstDayPlan.getMorningActivity().equals("null") && !firstDayPlan.getMorningActivity().equals("{}")) {
            try {
                // 解析上午的活动为一个 Attraction 对象
                Attraction plannedAttraction = gson.fromJson(firstDayPlan.getMorningActivity(), Attraction.class);
                Log.w(TAG, "First Day Plan Morning Activity: " + firstDayPlan.getMorningActivity());

                if (plannedAttraction != null) {
                    currentDisplayedAttraction = plannedAttraction; // **在这里赋值**

                    tvAttractionName.setText(plannedAttraction.getName());
                    tvAttractionAddress.setText(plannedAttraction.getAddress());
                    tvAttractionFee.setText(plannedAttraction.getPrice() == 0 ? "免费" : plannedAttraction.getPrice() + "元");

                    // 使用您代码中的 tvAttractionStarRate 和 tvAttractionDescription
                    tvAttractionStarRate.setText(String.format("%s分", plannedAttraction.getCommentScore())); // 格式化评分
                    if (plannedAttraction.getDescription() != null && !plannedAttraction.getDescription().isEmpty()) {
                        // 您可能需要进一步处理 description 中的转义字符，例如 `[\"内容\"]`
                        String desc = plannedAttraction.getDescription();
                        if (desc.startsWith("[\"") && desc.endsWith("\"]")) {
                            desc = desc.substring(2, desc.length() - 2).replace("\\\"", "\"");
                        }
                        tvAttractionDescription.setText(desc);
                    } else {
                        tvAttractionDescription.setText("有人说这里很美？不，亲眼所见才是'哇塞'级体验！");
                    }

                    // Glide 加载图片
                    if (plannedAttraction.getCoverImageUrl() != null && !plannedAttraction.getCoverImageUrl().isEmpty() && getContext() != null) {
                        Glide.with(getContext())
                                .load(plannedAttraction.getCoverImageUrl())
                                .placeholder(android.R.drawable.sym_def_app_icon)
                                .error(R.drawable.ic_error) // 假设你有 R.drawable.ic_error
                                .into(ivAttractionImage);
                    } else {
                        ivAttractionImage.setImageResource(android.R.drawable.sym_def_app_icon);
                    }
                    detailCardVisible = true;
                }
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "Error parsing morning activity JSON for detail card: " + firstDayPlan.getMorningActivity(), e);
                // currentDisplayedAttraction 保持为 null
            }
        }

        // 根据是否有数据显示或隐藏详情卡片
        if (attractionDetailCardView != null) {
            attractionDetailCardView.setVisibility(detailCardVisible ? View.VISIBLE : View.GONE);
        }
        if (!detailCardVisible) { // 如果没有数据显示或解析失败，清空文本并确保 currentDisplayedAttraction 为 null
            clearAttractionDetailCard(); // clearAttractionDetailCard 内部应该也将 currentDisplayedAttraction 设为 null
            currentDisplayedAttraction = null;
        }
    }

    private void clearAttractionDetailCard() {
        tvAttractionName.setText("暂无推荐景点");
        tvAttractionAddress.setText("");
        if (tvAttractionStarRate != null) tvAttractionStarRate.setText("");
        if (tvAttractionDescription != null) tvAttractionDescription.setText("暂无详细描述");
        tvAttractionFee.setText("");
        ivAttractionImage.setImageResource(android.R.drawable.sym_def_app_icon);
        currentDisplayedAttraction = null;
    }

    // 将规划保存到 Bmob
    private void saveTravelPlanToBmob(List<DayPlan> plannedDayPlans, TravelPlan userDemand) {
        BmobPlan bmobPlan = new BmobPlan();
        bmobPlan.setDestination(userDemand.getDestination());
        bmobPlan.setDays(userDemand.getDays());
        bmobPlan.setBudget(userDemand.getBudget());
        bmobPlan.setPreferences(userDemand.getPreferences()); // TravelPlan的preferences是List<String>
        bmobPlan.setRawDestinationInput(userDemand.getRawDestinationInput());
        bmobPlan.setRawDaysInput(userDemand.getRawDaysInput());
        bmobPlan.setRawBudgetInput(userDemand.getRawBudgetInput());
        bmobPlan.setRawPreferencesInput(userDemand.getRawPreferencesInput());

        // 将 List<DayPlan> 转换为 JSON 字符串保存
        bmobPlan.setPlannedDetailsJson(gson.toJson(plannedDayPlans));

        bmobPlan.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Log.d(TAG, "旅行规划成功保存到Bmob，ObjectId: " + objectId);
                    Toast.makeText(getContext(), "规划已保存。", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "保存旅行规划到Bmob失败: " + e.getMessage());
                    Toast.makeText(getContext(), "保存规划失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void navigateToWebView(String url, String title) {
        if (getContext() == null) return;

        DetailedAttractionFragment webViewFragment = DetailedAttractionFragment.newInstance(url, title); // 不再传递 logoUrl

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, webViewFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMapView != null) {
            mMapView.onDestroy();
            mMapView = null;
            mBaiduMap = null;
        }
    }
}