package com.bjtu.traveler.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bjtu.traveler.ui.profile.ProfileFragment;
import com.bjtu.traveler.utils.FragmentSwitcher;
import com.bjtu.traveler.viewmodel.HomeViewModel;
import com.bjtu.traveler.viewmodel.UserViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

import com.bjtu.traveler.adapter.CityCarouselAdapter;
import com.bjtu.traveler.ui.common.WebViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment 类，首页 Fragment
 * 负责显示用户信息、搜索框、城市轮播图等 UI 元素，并处理相应的交互逻辑
 */
public class HomeFragment extends Fragment implements CityCarouselAdapter.OnCityItemClickListener {
    private HomeViewModel homeViewModel;
    private UserViewModel userViewModel;

    private ViewPager2 cityCarouselViewPager;
    private CityCarouselAdapter cityCarouselAdapter;
    private LinearLayout bannerIndicator;
    private ImageView btnBannerLeft;
    private ImageView btnBannerRight;

    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private final long AUTO_SCROLL_DELAY = 3000L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

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
        EditText etSearch = root.findViewById(R.id.et_search);
        cityCarouselViewPager = root.findViewById(R.id.city_carousel_viewpager);
        bannerIndicator = root.findViewById(R.id.banner_indicator);
        btnBannerLeft = root.findViewById(R.id.btn_banner_left);
        btnBannerRight = root.findViewById(R.id.btn_banner_right);

        DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

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
            } else {
                tvHiUser.setText("Hi, 游客");
                ivAvatar.setImageResource(R.drawable.ic_avatar);
            }
        });

        homeViewModel.getCityCarouselLiveData().observe(getViewLifecycleOwner(), cityItems -> {
            if (cityItems != null && !cityItems.isEmpty()) {
                cityCarouselAdapter.setCityList(cityItems);
                setupIndicator(cityItems.size());
                startAutoScroll();
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

        return root;
    }

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

    private void updateIndicator(int selectedPosition) {
        int dotCount = bannerIndicator.getChildCount();
        for (int i = 0; i < dotCount; i++) {
            ImageView dot = (ImageView) bannerIndicator.getChildAt(i);
            if (dot != null) {
                dot.setBackgroundResource(i == selectedPosition ? R.drawable.bg_banner_dot_selected : R.drawable.bg_banner_dot_unselected);
            }
        }
    }

    private void startAutoScroll() {
        stopAutoScroll();
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    private void stopAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cityCarouselAdapter != null && cityCarouselAdapter.getItemCount() > 0) {
             startAutoScroll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
    }

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
} 