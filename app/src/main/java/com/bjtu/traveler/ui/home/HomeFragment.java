package com.bjtu.traveler.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.CityCarouselAdapter;
import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bjtu.traveler.data.model.WeatherData;
import com.bjtu.traveler.ui.common.WebViewFragment;
import com.bjtu.traveler.ui.explore.ExploreFragment;
import com.bjtu.traveler.ui.profile.ProfileFragment;
import com.bjtu.traveler.ui.routes.RoutesFragment;
import com.bjtu.traveler.utils.FragmentSwitcher;
import com.bjtu.traveler.utils.QWeatherIconMapper;
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

    private TextView tvWeatherIcon; // 天气图标 TextView
    private TextView tvWeatherLocation; // 城市和天气描述 TextView
    private TextView tvWeatherDetails; // 温度 TextView

    private EditText etSearch;
    private ImageView ivDice;

    private LinearLayout btnGetRecommendations;
    private ImageView ivBtnExplore;
    private TextView tvBtnExplore;

    private LinearLayout btnAiPlan;

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

    private Typeface qWeatherIconFont; // 新增：字体变量

    private LinearLayout llWeatherContent; // 新增：天气详情容器
    private com.airbnb.lottie.LottieAnimationView lottieWeatherLoadingInternal; // 新增：内嵌的 Lottie 动画

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
        tvWeatherIcon = root.findViewById(R.id.tv_weather_icon);
        tvWeatherLocation = root.findViewById(R.id.tv_weather_city_desc);
        tvWeatherDetails = root.findViewById(R.id.tv_weather_temp);
        etSearch = root.findViewById(R.id.et_search);
        ivDice = root.findViewById(R.id.iv_dice);
        cityCarouselViewPager = root.findViewById(R.id.city_carousel_viewpager);
        bannerIndicator = root.findViewById(R.id.banner_indicator);
        btnBannerLeft = root.findViewById(R.id.btn_banner_left);
        btnBannerRight = root.findViewById(R.id.btn_banner_right);
        btnGetRecommendations = root.findViewById(R.id.btn_get_recommendations);
        ivBtnExplore = root.findViewById(R.id.iv_btn_explore);
        tvBtnExplore = root.findViewById(R.id.tv_btn_explore);
        btnAiPlan = root.findViewById(R.id.btn_ai_plan);

        // 在 weather_card_content 布局中查找内嵌的 View
        View weatherCardRoot = root.findViewById(R.id.weather_card_content); // 查找 included layout 的根 View
        if (weatherCardRoot != null) {
            tvWeatherIcon = weatherCardRoot.findViewById(R.id.tv_weather_icon);
            tvWeatherDetails = weatherCardRoot.findViewById(R.id.tv_weather_temp);
            llWeatherContent = weatherCardRoot.findViewById(R.id.ll_weather_main_content); // 查找天气详情容器
            lottieWeatherLoadingInternal = weatherCardRoot.findViewById(R.id.lottie_weather_loading_internal); // 查找内嵌 Lottie 动画

            // 查找城市/描述 TextView，它在 ll_weather_content 内部
            if (llWeatherContent != null) {
                 tvWeatherLocation = llWeatherContent.findViewById(R.id.tv_weather_city_desc);
            }
        }

        DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // 新增：城市轮播图加载动画
        com.airbnb.lottie.LottieAnimationView lottieLoading = root.findViewById(R.id.lottie_loading);

        // 确保天气内容区初始不可见，无论 ViewModel 状态如何
        if (llWeatherContent != null) {
            llWeatherContent.setVisibility(View.GONE);
        }
        // 初始时显示天气加载动画
        if (lottieWeatherLoadingInternal != null) {
             lottieWeatherLoadingInternal.setVisibility(View.VISIBLE);
             lottieWeatherLoadingInternal.playAnimation();
        }

        // 初始化位置服务相关
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        createLocationRequest(); // 创建位置请求参数
        createLocationCallback(); // 创建位置更新回调

        cityCarouselAdapter = new CityCarouselAdapter(new ArrayList<>());
        cityCarouselViewPager.setAdapter(cityCarouselAdapter);
        cityCarouselAdapter.setOnCityItemClickListener(this);
        
        // 加载和风天气图标字体
        try {
            // 从 res/font 目录加载字体
            qWeatherIconFont = ResourcesCompat.getFont(requireContext(), R.font.qweather_icons);
            if (qWeatherIconFont == null) {
                Log.e(TAG, "Failed to load qweather_icons.ttf font.");
                // 处理字体加载失败的情况
            }
        } catch (Exception e) {
             Log.e(TAG, "Error loading qweather-icons.ttf", e); // 日志名称可以保留，或者改为 qweather_icons.ttf
             // 字体加载失败时的处理，例如显示默认图标或文字
        }
        
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

        // 新增：AI助手按钮点击事件
        if (btnAiPlan != null) {
            btnAiPlan.setOnClickListener(v -> {
                // 跳转到路线页面
                FragmentSwitcher.switchFragmentAndSelectItem(requireActivity(), new RoutesFragment(), "RoutesFragment", R.id.nav_routes);
            });
        }

        // 确保在findViewById之后注册weatherLiveData观察者
        homeViewModel.getWeatherLiveData().observe(getViewLifecycleOwner(), weatherData -> {
            // 新增：根据weatherData切换动画和内容
            if (lottieWeatherLoadingInternal != null && llWeatherContent != null) {
                if (weatherData == null) {
                    // 数据未加载或加载失败，显示动画
                    lottieWeatherLoadingInternal.setVisibility(View.VISIBLE);
                    lottieWeatherLoadingInternal.playAnimation();
                    llWeatherContent.setVisibility(View.GONE); // 隐藏天气详情
                } else {
                    // 数据加载成功，隐藏动画，显示内容，并更新UI
                    lottieWeatherLoadingInternal.cancelAnimation();
                    lottieWeatherLoadingInternal.setVisibility(View.GONE);
                    llWeatherContent.setVisibility(View.VISIBLE); // 显示天气详情
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

        View btnDomestic = root.findViewById(R.id.btn_domestic);
        View btnInternational = root.findViewById(R.id.btn_international);
        btnDomestic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment domesticFragment = NationalFragment.newInstance(NationalFragment.TYPE_DOMESTIC);
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, domesticFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        btnInternational.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment internationalFragment = NationalFragment.newInstance(NationalFragment.TYPE_INTERNATIONAL);
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, internationalFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

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
        Log.d("HomeFragment", "updateWeatherUI called, weatherData=" + (weatherData == null ? "null" : weatherData.toString()));
        if (!isAdded() || getActivity() == null || weatherData == null) {
            Log.d("HomeFragment", "Fragment not attached, or weatherData is null, skip UI update");
            if (tvWeatherLocation != null) tvWeatherLocation.setText("");
            if (tvWeatherDetails != null) tvWeatherDetails.setText("");
            if (getView() != null) {
                TextView tvHumidity = getView().findViewById(R.id.tv_weather_humidity);
                TextView tvWind = getView().findViewById(R.id.tv_weather_wind);
                TextView tvPressure = getView().findViewById(R.id.tv_weather_pressure);
                if (tvHumidity != null) tvHumidity.setText("");
                if (tvWind != null) tvWind.setText("");
                if (tvPressure != null) tvPressure.setText("");
            }
            return;
        }
        requireActivity().runOnUiThread(() -> {
            String city = weatherData.getCityName();
            String desc = weatherData.getDescription();
            double temp = weatherData.getTemperature();
            int humidity = weatherData.getHumidity();
            String iconCode = weatherData.getIconCode();
            double windSpeed = weatherData.getWindSpeed();
            int pressure = weatherData.getPressure();
            if (city == null || city.isEmpty()) city = "未知城市";
            if (desc == null || desc.isEmpty()) desc = "未知天气";

            String cityAndDesc = city;
            if (!desc.equals("未知天气")) { // 如果描述不是默认值，则添加到城市后面
                 cityAndDesc += "，" + desc;
            }

            String tempStr = (temp > -100 && temp < 100) ? ((int) temp + "°C") : "--°C";

            // 构建湿度字符串并应用样式
            String humidityValue = (humidity >= 0 && humidity <= 100) ? String.valueOf(humidity) : "--";
            String humidityUnit = "%";
            SpannableString humiditySpannable = new SpannableString(humidityValue + humidityUnit);
            int humidityUnitStart = humidityValue.length();
            int humidityUnitEnd = humiditySpannable.length();
            // 设置湿度数值颜色为蓝色
            humiditySpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.explore_card_bg)), 0, humidityValue.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // 设置湿度单位样式
            humiditySpannable.setSpan(new RelativeSizeSpan(0.7f), humidityUnitStart, humidityUnitEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 缩小字体
            humiditySpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.text_secondary)), humidityUnitStart, humidityUnitEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置颜色

            // 构建风速字符串并应用样式
            String windValue = (windSpeed >= 0 && windSpeed < 200) ? String.format("%.1f", windSpeed) : "--";
            String windUnit = "km/h";
            SpannableString windSpannable = new SpannableString(windValue + windUnit);
            int windUnitStart = windValue.length();
            int windUnitEnd = windSpannable.length();
             // 设置风速单位样式
            windSpannable.setSpan(new RelativeSizeSpan(0.7f), windUnitStart, windUnitEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 缩小字体
            windSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.text_secondary)), windUnitStart, windUnitEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置颜色

            // 构建气压字符串并应用样式
            String pressureValue = (pressure > 0 && pressure < 2000) ? String.valueOf(pressure) : "--";
            String pressureUnit = "hPa";
             SpannableString pressureSpannable = new SpannableString(pressureValue + pressureUnit);
            int pressureUnitStart = pressureValue.length();
            int pressureUnitEnd = pressureSpannable.length();
             // 设置气压单位样式
            pressureSpannable.setSpan(new RelativeSizeSpan(0.7f), pressureUnitStart, pressureUnitEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 缩小字体
            pressureSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.text_secondary)), pressureUnitStart, pressureUnitEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置颜色

            if (tvWeatherLocation != null) {
                tvWeatherLocation.setText(cityAndDesc);
            }
            if (tvWeatherDetails != null) {
                tvWeatherDetails.setText(tempStr);
            }
            View root = getView();
            if (root != null) {
                TextView tvHumidity = root.findViewById(R.id.tv_weather_humidity);
                TextView tvWind = root.findViewById(R.id.tv_weather_wind);
                TextView tvPressure = root.findViewById(R.id.tv_weather_pressure);
                if (tvHumidity != null) tvHumidity.setText(humiditySpannable); // 使用 SpannableString
                if (tvWind != null) tvWind.setText(windSpannable); // 使用 SpannableString
                if (tvPressure != null) tvPressure.setText(pressureSpannable); // 使用 SpannableString
            }
            setWeatherIcon(iconCode);
            Log.d("HomeFragment", "Set weather: " + city + " / " + desc + " / " + tempStr + " | " + humidity + "% | " + windSpeed + "km/h | " + pressure + "hPa"); // Log with raw values
        });
    }

    // 根据和风天气iconCode设置天气图标
    private void setWeatherIcon(String iconCode) {
        if (tvWeatherIcon != null && qWeatherIconFont != null) {
            tvWeatherIcon.setTypeface(qWeatherIconFont); // 设置字体

            String iconCharacter = QWeatherIconMapper.getIconCharacter(iconCode); // 获取对应的字符

            if (iconCharacter != null && !iconCharacter.equals("?")) { // 检查是否找到有效字符，排除默认的问号
                tvWeatherIcon.setText(iconCharacter); // 设置文本为图标字符
                tvWeatherIcon.setVisibility(View.VISIBLE); // 如果找到有效图标，确保 TextView 可见
            } else {
                // 如果找不到对应的图标字符，可以设置一个默认文本或隐藏 TextView
                tvWeatherIcon.setText("?"); // 例如设置为问号
                tvWeatherIcon.setVisibility(View.VISIBLE); // 显示问号提示
                // 或者 tvWeatherIcon.setVisibility(View.GONE); // 或者直接隐藏
            }
             // 可以根据需要设置图标颜色，例如从天气数据中获取或固定颜色
             // tvWeatherIcon.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary));
        } else if (tvWeatherIcon != null) {
             // 如果字体加载失败，可以设置一个默认文本或隐藏 TextView
             tvWeatherIcon.setText("?");
             tvWeatherIcon.setVisibility(View.VISIBLE); // 显示问号提示
        }
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