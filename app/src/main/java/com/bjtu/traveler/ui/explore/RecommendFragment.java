package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.AttractionAdapter;
import com.bjtu.traveler.data.model.Attraction;
import com.bjtu.traveler.viewmodel.RecommendViewModel;
import com.bjtu.traveler.viewmodel.ExploreViewModel;
import com.google.android.material.chip.Chip;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendFragment extends Fragment {
    private RecyclerView rvHotSpots, rvRecommended;
    private AttractionAdapter hotAdapter, recommendedAdapter;
    private ProgressBar progressRecommended;
    private Chip chipHot, chipAdventure, chipRelax, chipCulture;
    private RecommendViewModel viewModel;

    // 新增：缓存上次定位，避免重复刷新附近景点
    private Double lastLat = null;
    private Double lastLon = null;
    private static final double NEARBY_LOCATION_THRESHOLD_METERS = 100; // 100米
    // 附近景点只加载一次
    private boolean nearbyLoaded = false;

    // 进度条最少显示时间
    private long progressShowStartTime = 0;
    private static final int MIN_PROGRESS_SHOW_TIME = 300; // ms
    private Handler handler = new Handler(Looper.getMainLooper());

    // 搜索标志
    private boolean isSearching = false;

    // 新增：附近卡片只初始化一次
    private boolean leftNearbyInit = false, rightNearbyInit = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);
        viewModel = new ViewModelProvider(this).get(RecommendViewModel.class);

        // 获取 ExploreViewModel（与 ExploreFragment 共享定位）
        ExploreViewModel exploreViewModel = new ViewModelProvider(requireActivity()).get(ExploreViewModel.class);
        // 启动定位监听（只需启动一次，防止重复请求）
        exploreViewModel.startLocationUpdates(requireContext());
        // 监听城市和国家 LiveData，刷新卡片内容
        TextView tvLocation = view.findViewById(R.id.tv_location);
        exploreViewModel.getCurrentCity().observe(getViewLifecycleOwner(), city -> {
            String country = exploreViewModel.getCurrentCountry().getValue();
            if (city == null) city = "定位中...";
            if (country == null) country = "";
            String display = city;
            if (!country.isEmpty() && !country.equals(city)) display = city + ", " + country;
            tvLocation.setText(display);
        });
        exploreViewModel.getCurrentCountry().observe(getViewLifecycleOwner(), country -> {
            String city = exploreViewModel.getCurrentCity().getValue();
            if (city == null) city = "定位中...";
            if (country == null) country = "";
            String display = city;
            if (!country.isEmpty() && !country.equals(city)) display = city + ", " + country;
            tvLocation.setText(display);
        });

        rvRecommended = view.findViewById(R.id.rv_recommended);
        // 推荐区 Adapter 初始化时不传 null, null
        recommendedAdapter = new AttractionAdapter(new ArrayList<>(), R.layout.item_vertical_attraction);
        rvRecommended.setAdapter(recommendedAdapter);
        // 为推荐区 RecyclerView 设置布局管理器，确保数据显示
        rvRecommended.setLayoutManager(new LinearLayoutManager(getContext()));

        rvHotSpots = view.findViewById(R.id.rv_hot_spots);
        hotAdapter = new AttractionAdapter(new ArrayList<>(), R.layout.item_horizontal_attraction, null, null);
        rvHotSpots.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvHotSpots.setAdapter(hotAdapter);

        progressRecommended = view.findViewById(R.id.progress_recommended);

        chipHot = view.findViewById(R.id.chip_hot);
        chipAdventure = view.findViewById(R.id.chip_adventure);
        chipRelax = view.findViewById(R.id.chip_relax);
        chipCulture = view.findViewById(R.id.chip_culture);

        // 监听 Chip 切换
        View.OnClickListener chipClickListener = v -> {
            if (progressRecommended != null) {
                progressRecommended.setVisibility(View.VISIBLE);
                progressShowStartTime = System.currentTimeMillis();
            }
            chipHot.setChecked(false);
            chipAdventure.setChecked(false);
            chipRelax.setChecked(false);
            chipCulture.setChecked(false);
            ((Chip) v).setChecked(true);
            String type = getCurrentChipType();
            recommendedAdapter.setData(new ArrayList<>()); // 清空旧数据
            viewModel.loadRecommendedAttractions(type);
        };
        chipHot.setOnClickListener(chipClickListener);
        chipAdventure.setOnClickListener(chipClickListener);
        chipRelax.setOnClickListener(chipClickListener);
        chipCulture.setOnClickListener(chipClickListener);
        chipHot.setChecked(true);

        // 搜索功能
        EditText etSearch = view.findViewById(R.id.et_search);
        ImageButton btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            Toast.makeText(getContext(), "点击搜索，输入：" + keyword, Toast.LENGTH_SHORT).show();
            if (keyword.isEmpty()) return;
            isSearching = true;
            if (progressRecommended != null) {
                progressRecommended.setVisibility(View.VISIBLE);
                progressRecommended.bringToFront(); // 确保进度条在最上层
            }
            recommendedAdapter.setData(new ArrayList<>()); // 先清空数据，进度条依然可见
            String type = getCurrentChipType();
            String[] typesArr;
            if (type.equals("hot")) {
                typesArr = new String[]{"attraction"};
            } else if (type.equals("adventure")) {
                typesArr = new String[]{"camp_site", "wilderness_hut", "picnic_site", "caravan_site", "chalet"};
            } else if (type.equals("relax")) {
                typesArr = new String[]{"viewpoint", "zoo", "theme_park", "hot_spring", "beach", "park", "garden", "spa"};
            } else {
                typesArr = new String[]{"museum", "gallery", "artwork"};
            }
            Toast.makeText(getContext(), "开始调用ViewModel搜索", Toast.LENGTH_SHORT).show();
            viewModel.searchAttractionsByCity(keyword, typesArr, 20, new RecommendViewModel.CitySearchCallback() {
                @Override
                public void onSuccess(List<Attraction> attractions) {
                    Toast.makeText(getContext(), "搜索成功，结果数：" + attractions.size(), Toast.LENGTH_SHORT).show();
                    List<Attraction> withImg = new ArrayList<>();
                    List<Attraction> needFetch = new ArrayList<>();
                    for (Attraction a : attractions) {
                        if (a.getUrl() != null && !a.getUrl().isEmpty()) {
                            withImg.add(a);
                        } else {
                            needFetch.add(a);
                        }
                    }
                    rvRecommended.post(() -> recommendedAdapter.setData(withImg));
                    if (needFetch.isEmpty()) {
                        rvRecommended.post(() -> {
                            recommendedAdapter.setData(withImg);
                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                            isSearching = false;
                        });
                    } else {
                        final int[] finished = {0};
                        for (Attraction a : needFetch) {
                            viewModel.fetchAmapDetailForAttraction(requireContext(), a, new RecommendViewModel.AmapDetailCallback() {
                                @Override
                                public void onSuccess(Attraction updated) {
                                    if (updated.getUrl() != null && !updated.getUrl().isEmpty()) {
                                        withImg.add(updated);
                                    }
                                    finished[0]++;
                                    if (finished[0] == needFetch.size()) {
                                        rvRecommended.post(() -> {
                                            recommendedAdapter.setData(withImg);
                                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                                            isSearching = false;
                                        });
                                    }
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    finished[0]++;
                                    if (finished[0] == needFetch.size()) {
                                        rvRecommended.post(() -> {
                                            recommendedAdapter.setData(withImg);
                                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                                            isSearching = false;
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "搜索失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    rvRecommended.post(() -> {
                        recommendedAdapter.setData(new ArrayList<>());
                        if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                        isSearching = false;
                    });
                }
            });
        });

        // 监听热门区分页
        rvHotSpots.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 可扩展：分页加载更多热门景点
            }
        });

        // 监听推荐区分页
        rvRecommended.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 可扩展：分页加载更多推荐景点
            }
        });

        // 附近景点填充逻辑，直接调用viewModel的loadNearbyAttractions和getNearbyAttractions
        FrameLayout nearbyLeft = view.findViewById(R.id.nearby_spot_left);
        FrameLayout nearbyRight = view.findViewById(R.id.nearby_spot_right);
        // 只 observe 一次定位，首次定位到时加载附近景点
        exploreViewModel.getLastKnownLocation().observe(getViewLifecycleOwner(), location -> {
            if (!nearbyLoaded && location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                lastLat = lat;
                lastLon = lon;
                viewModel.setCurrentLocationLatLon(lat, lon);
                viewModel.loadNearbyAttractions();
                nearbyLoaded = true;
            }
        });
        // 优化：忽略首次空列表，仅初始化一次子View，后续只更新内容
        viewModel.getNearbyAttractions().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) return; // 忽略空列表
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            // 左侧
            if (list.size() > 0) {
                Attraction left = list.get(0);
                View cardLeft;
                if (!leftNearbyInit) {
                    cardLeft = layoutInflater.inflate(R.layout.item_nearby_place, nearbyLeft, false);
                    nearbyLeft.addView(cardLeft);
                    leftNearbyInit = true;
                } else {
                    cardLeft = nearbyLeft.getChildAt(0);
                }
                if (cardLeft != null) {
                    viewModel.fillNearbyPlaceCard(cardLeft, left, this);
                    cardLeft.setVisibility(View.VISIBLE);
                }
            }
            // 右侧
            if (list.size() > 1) {
                Attraction right = list.get(1);
                View cardRight;
                if (!rightNearbyInit) {
                    cardRight = layoutInflater.inflate(R.layout.item_nearby_place, nearbyRight, false);
                    nearbyRight.addView(cardRight);
                    rightNearbyInit = true;
                } else {
                    cardRight = nearbyRight.getChildAt(0);
                }
                if (cardRight != null) {
                    viewModel.fillNearbyPlaceCard(cardRight, right, this);
                    cardRight.setVisibility(View.VISIBLE);
                }
            }
            // 若数据不足2个，右侧不显示内容（用 GONE 避免占位）
            if (list.size() <= 1 && rightNearbyInit && nearbyRight.getChildCount() > 0) {
                View cardRight = nearbyRight.getChildAt(0);
                if (cardRight != null) cardRight.setVisibility(View.GONE);
            }
        });

        // 监听 Adapter item 点击
        recommendedAdapter.setOnAttractionClickListener((attraction, imgUrl, city, country) -> {
            Bundle args = new Bundle();
            args.putString("name", attraction.getName());
            args.putString("category", attraction.getCategory());
            args.putString("description", attraction.getDescription());
            args.putString("city", attraction.getCity());
            args.putString("country", attraction.getCountry());
            args.putString("imgUrl", imgUrl);
            AttractionDetailFragment fragment = new AttractionDetailFragment();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        hotAdapter.setOnAttractionClickListener((attraction, imgUrl, city, country) -> {
            Bundle args = new Bundle();
            args.putString("name", attraction.getName());
            args.putString("category", attraction.getCategory());
            args.putString("description", attraction.getDescription());
            args.putString("city", city);
            args.putString("country", country);
            args.putString("imgUrl", imgUrl);
            AttractionDetailFragment fragment = new AttractionDetailFragment();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // 观察 ViewModel 数据
        viewModel.getRecommendedDisplayList().observe(getViewLifecycleOwner(), list -> {
            if (isSearching) return;
            if (progressRecommended != null) progressRecommended.setVisibility(View.VISIBLE);
            if (list == null || list.isEmpty()) {
                rvRecommended.post(() -> recommendedAdapter.setData(new ArrayList<>()));
                // 保证进度条最少显示 MIN_PROGRESS_SHOW_TIME
                long elapsed = System.currentTimeMillis() - progressShowStartTime;
                if (elapsed < MIN_PROGRESS_SHOW_TIME) {
                    handler.postDelayed(() -> {
                        if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                    }, MIN_PROGRESS_SHOW_TIME - elapsed);
                } else {
                    if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                }
                return;
            }
            // 只对非附近景点做图片补全
            List<Attraction> attractions = new ArrayList<>(list);
            List<Attraction> needFetch = new ArrayList<>();
            for (Attraction attraction : attractions) {
                // 只对没有图片且不是附近景点的做图片补全
                if ((attraction.getUrl() == null || attraction.getUrl().isEmpty()) && !isNearbyAttraction(attraction)) {
                    needFetch.add(attraction);
                }
            }
            Runnable showFiltered = () -> {
                // 只显示有图片的（非附近景点），附近景点无图片也显示
                List<Attraction> filtered = new ArrayList<>();
                for (Attraction a : attractions) {
                    if (isNearbyAttraction(a) || (a.getUrl() != null && !a.getUrl().isEmpty())) {
                        filtered.add(a);
                    }
                }
                rvRecommended.post(() -> {
                    recommendedAdapter.setData(filtered);
                    viewModel.updateRecommendedDisplayList(filtered); // 强制触发刷新
                    // 保证进度条最少显示 MIN_PROGRESS_SHOW_TIME
                    long elapsed = System.currentTimeMillis() - progressShowStartTime;
                    if (elapsed < MIN_PROGRESS_SHOW_TIME) {
                        handler.postDelayed(() -> {
                            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                        }, MIN_PROGRESS_SHOW_TIME - elapsed);
                    } else {
                        if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
                    }
                });
            };
            if (needFetch.isEmpty()) {
                showFiltered.run();
            } else {
                final int[] finished = {0};
                for (Attraction attraction : needFetch) {
                    viewModel.fetchAmapDetailForAttraction(requireContext(), attraction, new RecommendViewModel.AmapDetailCallback() {
                        @Override
                        public void onSuccess(Attraction updated) {
                            finished[0]++;
                            if (finished[0] == needFetch.size()) {
                                showFiltered.run();
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            finished[0]++;
                            if (finished[0] == needFetch.size()) {
                                showFiltered.run();
                            }
                        }
                    });
                }
            }
        });
        viewModel.getHotList().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                rvHotSpots.post(() -> hotAdapter.setData(new ArrayList<>()));
                return;
            }
            List<Attraction> attractions = new ArrayList<>(list);
            List<Attraction> needFetch = new ArrayList<>();
            for (Attraction attraction : attractions) {
                // 只对没有图片且不是附近景点的做图片补全
                if ((attraction.getUrl() == null || attraction.getUrl().isEmpty()) && !isNearbyAttraction(attraction)) {
                    needFetch.add(attraction);
                }
            }
            Runnable showFiltered = () -> {
                List<Attraction> filtered = new ArrayList<>();
                for (Attraction a : attractions) {
                    if (isNearbyAttraction(a) || (a.getUrl() != null && !a.getUrl().isEmpty())) {
                        filtered.add(a);
                    }
                }
                rvHotSpots.post(() -> hotAdapter.setData(filtered));
            };
            if (needFetch.isEmpty()) {
                showFiltered.run();
            } else {
                final int[] finished = {0};
                for (Attraction attraction : needFetch) {
                    viewModel.fetchAmapDetailForAttraction(requireContext(), attraction, new RecommendViewModel.AmapDetailCallback() {
                        @Override
                        public void onSuccess(Attraction updated) {
                            finished[0]++;
                            if (finished[0] == needFetch.size()) {
                                showFiltered.run();
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            finished[0]++;
                            if (finished[0] == needFetch.size()) {
                                showFiltered.run();
                            }
                        }
                    });
                }
            }
        });
        viewModel.getIsLoadingRecommended().observe(getViewLifecycleOwner(), loading -> {
            if (progressRecommended != null) progressRecommended.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
        });

        // 默认加载数据
        viewModel.loadHotAttractions();
        viewModel.loadRecommendedAttractions(getCurrentChipType());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.bjtu.traveler.BaseActivity) {
            ((com.bjtu.traveler.BaseActivity) getActivity()).hideSystemBars();
        }
    }

    private String getCurrentChipType() {
        if (chipHot != null && chipHot.isChecked()) return "hot";
        if (chipAdventure != null && chipAdventure.isChecked()) return "adventure";
        if (chipRelax != null && chipRelax.isChecked()) return "relax";
        if (chipCulture != null && chipCulture.isChecked()) return "culture";
        return "hot";
    }

    // 判断是否为附近景点（可根据经纬度是否有值，或其他标记）
    private boolean isNearbyAttraction(Attraction attraction) {
        // 这里假设附近景点一定有经纬度且距离小于3km
        if (attraction == null) return false;
        if (attraction.getLat() != 0 && attraction.getLon() != 0) {
            // 距离小于3km认为是附近
            double distance = 0;
            try {
                distance = viewModel.calculateDistanceToCurrent(attraction.getLat(), attraction.getLon());
            } catch (Exception ignore) {}
            return distance > 0 && distance < 3000;
        }
        return false;
    }
}