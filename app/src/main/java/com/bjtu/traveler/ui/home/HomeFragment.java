package com.bjtu.traveler.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.CityCarouselAdapter;
import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bjtu.traveler.data.model.WeatherData;
import com.bjtu.traveler.ui.common.WebViewFragment;
import com.bjtu.traveler.ui.explore.ExploreFragment;
import com.bjtu.traveler.ui.profile.ProfileFragment;
import com.bjtu.traveler.utils.FragmentSwitcher;
import com.bjtu.traveler.viewmodel.HomeViewModel;
import com.bjtu.traveler.viewmodel.UserViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;


/**
 * HomeFragment 类，首页 Fragment
 * 负责显示用户信息、搜索框、城市轮播图、天气信息、推荐卡片等 UI 元素，并处理相应的交互逻辑
 */
public class HomeFragment extends Fragment implements CityCarouselAdapter.OnCityItemClickListener {
    private static final String TAG = "HomeFragment"; // 添加 TAG 用于日志

    private HomeViewModel homeViewModel;
    private UserViewModel userViewModel;

    private ViewPager2 cityCarouselViewPager;
    private CityCarouselAdapter cityCarouselAdapter;
    private LinearLayout bannerIndicator;
    private ImageView btnBannerLeft;
    private ImageView btnBannerRight;

    private LinearLayout llWeather;
    private ImageView ivWeatherIcon;
    private TextView tvWeatherLocation;
    private TextView tvWeatherDetails;
    private TextView tvWeatherDescription;

    private EditText etSearch;
    private ImageView ivDice;

    private LinearLayout btnGetRecommendations;
    private ImageView ivBtnExplore;
    private TextView tvBtnExplore;

    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private final long AUTO_SCROLL_DELAY = 3000L;

    // 位置服务客户端
    private FusedLocationProviderClient fusedLocationClient;
    // 位置请求参数
    private LocationRequest locationRequest;
    // 位置更新回调
    private LocationCallback locationCallback;
    // 位置权限请求码
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 查找 UI 元素
        View topBar = root.findViewById(R.id.topbar_home);
        if (topBar != null) {
            TextView tvTitle = topBar.findViewById(R.id.tv_title);
            if (tvTitle != null) {
                tvTitle.setText("Home");
            }
        }

        ImageView ivAvatar = root.findViewById(R.id.iv_avatar);
        TextView tvHiUser = root.findViewById(R.id.tv_hi_user);
        TextView tvPoints = root.findViewById(R.id.tv_points);
        llWeather = root.findViewById(R.id.ll_weather);
        ivWeatherIcon = root.findViewById(R.id.iv_weather_icon);
        tvWeatherLocation = root.findViewById(R.id.tv_weather_location);
        tvWeatherDetails = root.findViewById(R.id.tv_weather_details);
        tvWeatherDescription = root.findViewById(R.id.tv_weather_description);
        etSearch = root.findViewById(R.id.et_search);
        ivDice = root.findViewById(R.id.iv_dice);
        cityCarouselViewPager = root.findViewById(R.id.city_carousel_viewpager);
        bannerIndicator = root.findViewById(R.id.banner_indicator);
        btnBannerLeft = root.findViewById(R.id.btn_banner_left);
        btnBannerRight = root.findViewById(R.id.btn_banner_right);
        btnGetRecommendations = root.findViewById(R.id.btn_get_recommendations);
        ivBtnExplore = root.findViewById(R.id.iv_btn_explore);
        tvBtnExplore = root.findViewById(R.id.tv_btn_explore);

        DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // 新增：天气卡片加载动画
        com.airbnb.lottie.LottieAnimationView lottieWeatherLoading = root.findViewById(R.id.lottie_weather_loading);

        // 新增：城市轮播图加载动画
        com.airbnb.lottie.LottieAnimationView lottieLoading = root.findViewById(R.id.lottie_loading);

        // 确保天气内容区初始不可见，无论 ViewModel 状态如何
        if (llWeather != null) {
            llWeather.setVisibility(View.GONE);
        }
        // 初始时显示天气加载动画
        if (lottieWeatherLoading != null) {
             lottieWeatherLoading.setVisibility(View.VISIBLE);
             lottieWeatherLoading.playAnimation();
        }

        // 初始化位置服务相关
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        createLocationRequest(); // 创建位置请求参数
        createLocationCallback(); // 创建位置更新回调

        cityCarouselAdapter = new CityCarouselAdapter(new ArrayList<>());
        cityCarouselViewPager.setAdapter(cityCarouselAdapter);
        cityCarouselAdapter.setOnCityItemClickListener(this);
        
        homeViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String username = user.getUsername();
                String avatarUrl = user.getAvatarUrl();
                tvHiUser.setText(!TextUtils.isEmpty(username) ? "Hi, " + username : "Hi, 游客");
                if (!TextUtils.isEmpty(avatarUrl)) {
                    Glide.with(this)
                         .load(avatarUrl)
                         .placeholder(R.drawable.ic_avatar)
                         .error(R.drawable.ic_avatar)
                         .apply(RequestOptions.circleCropTransform())
                         .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_avatar);
                }
                root.findViewById(R.id.ll_points).setVisibility(View.VISIBLE);
            } else {
                tvHiUser.setText("Hi, 游客");
                ivAvatar.setImageResource(R.drawable.ic_avatar);
                root.findViewById(R.id.ll_points).setVisibility(View.GONE);
            }
        });

        homeViewModel.getCityCarouselLiveData().observe(getViewLifecycleOwner(), cityItems -> {
            if (cityItems != null && !cityItems.isEmpty()) {
                cityCarouselAdapter.setCityList(cityItems);
                setupIndicator(cityItems.size());
                startAutoScroll();
                // 数据加载完成后隐藏城市轮播图Lottie动画，显示ViewPager
                if (lottieLoading != null) {
                    lottieLoading.cancelAnimation();
                    lottieLoading.setVisibility(View.GONE);
                }
                if (cityCarouselViewPager != null) {
                    cityCarouselViewPager.setVisibility(View.VISIBLE);
                }
            } else {
                cityCarouselViewPager.setVisibility(View.GONE);
                bannerIndicator.setVisibility(View.GONE);
                // 数据为空时，显示城市轮播图Lottie动画
                if (lottieLoading != null) {
                    lottieLoading.setVisibility(View.VISIBLE);
                    lottieLoading.playAnimation();
                }
            }
        });

        cityCarouselViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicator(position);
            }
        });

        btnBannerLeft.setOnClickListener(v -> {
            int currentItem = cityCarouselViewPager.getCurrentItem();
            if (currentItem > 0) {
                cityCarouselViewPager.setCurrentItem(currentItem - 1, true);
            } else if (cityCarouselAdapter.getItemCount() > 0) {
                cityCarouselViewPager.setCurrentItem(cityCarouselAdapter.getItemCount() - 1, true);
            }
        });

        btnBannerRight.setOnClickListener(v -> {
            int currentItem = cityCarouselViewPager.getCurrentItem();
            int totalItems = cityCarouselAdapter.getItemCount();
            if (totalItems > 0) {
                int nextItem = (currentItem + 1) % totalItems;
                cityCarouselViewPager.setCurrentItem(nextItem, true);
            }
        });

        autoScrollHandler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = cityCarouselViewPager.getCurrentItem();
                int totalItems = cityCarouselAdapter.getItemCount();
                if (totalItems > 0) {
                    int nextItem = (currentItem + 1) % totalItems;
                    cityCarouselViewPager.setCurrentItem(nextItem, true);
                    autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
                }
            }
        };

        ivAvatar.setOnClickListener(v -> {
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            Fragment fragment = fm.findFragmentByTag("ProfileFragment");
            if (fragment == null || !fragment.isAdded()) {
                fm.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, 0, 0, R.anim.slide_out_left)
                        .add(R.id.fragment_container, new ProfileFragment(), "ProfileFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });

        if (btnGetRecommendations != null) {
            btnGetRecommendations.setOnClickListener(v -> {
                FragmentSwitcher.switchFragmentAndSelectItem(requireActivity(), new ExploreFragment(), "ExploreFragment", R.id.nav_explore);
            });
        }

        // 确保在findViewById之后注册weatherLiveData观察者
        homeViewModel.getWeatherLiveData().observe(getViewLifecycleOwner(), weatherData -> {
            // updateWeatherUI(weatherData); // 不在这里直接调用 updateWeatherUI
            // 新增：根据weatherData切换动画和内容
            if (lottieWeatherLoading != null && llWeather != null) {
                if (weatherData == null) {
                    // 数据未加载或加载失败，显示动画
                    lottieWeatherLoading.setVisibility(View.VISIBLE);
                    lottieWeatherLoading.playAnimation();
                    llWeather.setVisibility(View.GONE);
                } else {
                    // 数据加载成功，隐藏动画，显示内容，并更新UI
                    lottieWeatherLoading.cancelAnimation();
                    lottieWeatherLoading.setVisibility(View.GONE);
                    llWeather.setVisibility(View.VISIBLE);
                    updateWeatherUI(weatherData); // 数据加载成功后再更新UI
                }
            }
        });

        // 点击骰子图标弹出对话框事件
        if (ivDice != null) {
            ivDice.setOnClickListener(v -> {
                showRandomCityDialogFragment();
            });
        }

        return root;
    }

    // 创建位置请求参数
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 设置位置更新间隔（毫秒）
        locationRequest.setFastestInterval(5000); // 设置最快位置更新间隔
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 设置定位优先级
    }

    // 创建位置更新回调
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) { // 使用 @NonNull 标记参数
                if (locationResult == null) {
                    return;
                }
                // 处理位置更新
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // 获取到位置信息，调用 ViewModel 获取天气方法
                        homeViewModel.fetchWeatherByLocation(location.getLatitude(), location.getLongitude());
                        // 获取到位置后可以停止位置更新以节省电量
                        stopLocationUpdates();
                        break; // 只处理最新的位置
                    }
                }
            }
        };
    }

    // 检查位置权限并获取天气
    // 在用户未登录时，如果权限已授予，启动位置更新；否则请求权限
    // 这个方法主要在 onResume 中被调用
    private void checkLocationPermissionAndFetchWeather() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 权限已授予，开始请求位置更新
            startLocationUpdates();
        } else {
            // 权限未授予，请求权限
            // requestPermissions 是 Fragment 的方法
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // 开始位置更新
    // 需要检查权限，尽管调用前可能已检查
    private void startLocationUpdates() {
        try {
            // 确保 Fragment 附加到 Activity 且 Context 非空
            if (getContext() != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                 fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            // 处理没有权限的情况
            Log.e(TAG, "位置权限未授予: " + e.getMessage());
            // 如果因权限问题无法获取位置，更新 UI 显示错误
             updateWeatherUI(null); // 显示错误状态
        }
    }

    // 停止位置更新
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
             // 确保 Fragment 附加到 Activity
             if (isAdded()) {
                 fusedLocationClient.removeLocationUpdates(locationCallback);
             }
        }
    }

    // 根据 ViewModel 提供的 WeatherData 更新 UI
    // 这个方法现在只负责实际更新UI元素的内容，不控制可见性
    private void updateWeatherUI(WeatherData weatherData) {
        Log.d("HomeFragment", "updateWeatherUI called, weatherData=" + (weatherData == null ? "null" : weatherData.getCityName() + "," + weatherData.getDescription() + "," + weatherData.getTemperature() + "," + weatherData.getHumidity() + "," + weatherData.getIconCode()));
        if (!isAdded() || getActivity() == null || weatherData == null) {
            Log.d("HomeFragment", "Fragment not attached, or weatherData is null, skip UI update");
            if (tvWeatherLocation != null) tvWeatherLocation.setText("");
            if (tvWeatherDescription != null) tvWeatherDescription.setText("");
            if (tvWeatherDetails != null) tvWeatherDetails.setText("");
            return;
        }
        requireActivity().runOnUiThread(() -> {
            String city = weatherData.getCityName();
            String desc = weatherData.getDescription();
            double temp = weatherData.getTemperature();
            int humidity = weatherData.getHumidity();
            String iconCode = weatherData.getIconCode();
            if (city == null || city.isEmpty()) city = "未知城市";
            if (desc == null || desc.isEmpty()) desc = "未知天气";
            String tempStr = (temp > -100 && temp < 100) ? ((int) temp + "°C") : "--°C";
            String humidityStr = (humidity >= 0 && humidity <= 100) ? (humidity + "%") : "--%";

            if (tvWeatherLocation != null) {
                tvWeatherLocation.setText(city);
            }
            if (tvWeatherDescription != null) {
                 tvWeatherDescription.setText(desc);
            }
            if (tvWeatherDetails != null) {
                tvWeatherDetails.setText(tempStr + " | " + humidityStr);
            }

            setWeatherIcon(iconCode);
            Log.d("HomeFragment", "Set weather: " + city + " / " + desc + " / " + tempStr + " | " + humidityStr);
        });
    }

    // 根据和风天气iconCode设置天气图标
    private void setWeatherIcon(String iconCode) {
        // 你可以根据iconCode映射本地drawable，或直接用和风天气的icon资源
        // 这里假设有一套本地drawable，命名规则如ic_qweather_100, ic_qweather_101等
        int iconResId = getResources().getIdentifier("ic_qweather_" + iconCode, "drawable", requireContext().getPackageName());
        if (iconResId == 0) iconResId = R.drawable.ic_weather_routine; // fallback
        ivWeatherIcon.setImageResource(iconResId);
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，开始获取位置
                startLocationUpdates();
            } else {
                // 权限被拒绝
                Log.e(TAG, "位置权限被拒绝");
                 updateWeatherUI(null); // 显示错误状态
            }
        }
    }

    // Fragment 可见时调用
    @Override
    public void onStart() {
        super.onStart();
        // 如果用户是游客并且位置权限已经授予，则开始位置更新
        // 在 onStart 启动位置更新是推荐的做法，因为它与 Fragment 可见性关联
        if (userViewModel.getUserLiveData().getValue() == null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             startLocationUpdates();
        }
        // 在 Fragment 可见时开始城市轮播图的自动滚动
        if (cityCarouselAdapter != null && cityCarouselAdapter.getItemCount() > 0) {
             startAutoScroll();
        }
    }

    // Fragment 重新回到活跃状态时调用
    @Override
    public void onResume() {
        super.onResume();
    }

    // Fragment 不活跃时调用
    @Override
    public void onPause() {
        super.onPause();
        // 在 Fragment 不活跃时停止城市轮播图的自动滚动
        stopAutoScroll();
        // 停止位置更新通常放在 onStop 中更合适，因为它与可见性关联
    }

    // Fragment 不可见时调用
    @Override
    public void onStop() {
        super.onStop();
        // 在 Fragment 不可见时停止位置更新，节省电量和资源
        stopLocationUpdates();
    }

    // Fragment 的视图被销毁时调用
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 在视图销毁时确保停止自动滚动和位置更新，避免内存泄漏
        stopAutoScroll();
        stopLocationUpdates();
        // TODO: 清理其他资源，如解除监听器等（如果需要）
    }

    // 设置轮播图指示器
    private void setupIndicator(int count) {
        bannerIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(getContext());
            dot.setBackgroundResource(i == 0 ? R.drawable.bg_banner_dot_selected : R.drawable.bg_banner_dot_unselected);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            bannerIndicator.addView(dot, params);
        }
    }

    // 更新轮播图指示器选中状态
    private void updateIndicator(int selectedPosition) {
        int dotCount = bannerIndicator.getChildCount();
        for (int i = 0; i < dotCount; i++) {
            ImageView dot = (ImageView) bannerIndicator.getChildAt(i);
            if (dot != null) {
                dot.setBackgroundResource(i == selectedPosition ? R.drawable.bg_banner_dot_selected : R.drawable.bg_banner_dot_unselected);
            }
        }
    }

    // 开始城市轮播图自动滚动
    private void startAutoScroll() {
        stopAutoScroll(); // 先停止之前的任务，避免重复
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    // 停止城市轮播图自动滚动
    private void stopAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    // 城市轮播图项点击事件
    @Override
    public void onItemClick(CityCarouselItem city) {
        if (city != null && city.getDetailUrl() != null && !city.getDetailUrl().isEmpty()) {
            WebViewFragment webViewFragment = WebViewFragment.newInstance(city.getDetailUrl());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, webViewFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // 新增：显示随机推荐DialogFragment
    private void showRandomCityDialogFragment() {
        RandomCityDialogFragment dialog = new RandomCityDialogFragment();
        dialog.setOnDetailClickListener((cityPinyin, cityId, cityChineseName) -> {
            // 跳转到WebViewFragment，URL可按PlaceScraper逻辑拼接
            // 这里只拼接城市景点列表页，实际可根据业务调整
            String url = "https://you.ctrip.com/sight/" + cityPinyin + "/sightlist" + cityId + ".html";
            WebViewFragment webViewFragment = WebViewFragment.newInstance(url);
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, webViewFragment)
                .addToBackStack(null)
                .commit();
            dialog.dismiss();
        });
        dialog.show(getParentFragmentManager(), "RandomCityDialogFragment");
    }
} 