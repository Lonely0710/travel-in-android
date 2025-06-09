package com.bjtu.traveler.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.ScraperAttractionAdapter;
import com.bjtu.traveler.adapter.CityAdapter;
import com.bjtu.traveler.utils.PlaceScraper;
import com.bjtu.traveler.viewmodel.HotCityViewModel;
import com.bjtu.traveler.ui.common.WebViewFragment;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import android.content.res.Resources;
import androidx.core.content.res.ResourcesCompat;

public class NationalFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    public static final String TYPE_DOMESTIC = "domestic";
    public static final String TYPE_INTERNATIONAL = "international";

    private LottieAnimationView lottieAnimationView;

    public static NationalFragment newInstance(String type) {
        NationalFragment fragment = new NationalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String type = getArguments() != null ? getArguments().getString(ARG_TYPE) : TYPE_DOMESTIC;
        int layoutRes = R.layout.fragment_home_domestic;
        if (TYPE_INTERNATIONAL.equals(type)) {
            layoutRes = R.layout.fragment_home_international;
        }
        return inflater.inflate(layoutRes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String type = getArguments() != null ? getArguments().getString(ARG_TYPE) : TYPE_DOMESTIC;

        // Initialize top bar
        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        ImageView ivTitleIcon = view.findViewById(R.id.iv_title_icon);
        lottieAnimationView = view.findViewById(R.id.lottie_animation_view);

        // 设置标题和icon
        if (TYPE_DOMESTIC.equals(type)) {
            tvTitle.setText("China Traveling");
            ivTitleIcon.setImageResource(R.drawable.ic_home_domestic);
            if (lottieAnimationView != null) {
                lottieAnimationView.setAnimation(R.raw.dragging_woman);
                lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
                lottieAnimationView.setVisibility(View.GONE);
            }
        } else if (TYPE_INTERNATIONAL.equals(type)) {
            tvTitle.setText("World Traveling");
            ivTitleIcon.setImageResource(R.drawable.ic_home_world);
            if (lottieAnimationView != null) {
                lottieAnimationView.setAnimation(R.raw.city_changing);
                lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
                lottieAnimationView.setVisibility(View.GONE);
            }
        }
        // 设置字体
        try {
            tvTitle.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.pacifico));
        } catch (Exception e) {
            // 字体资源不存在时忽略
        }

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        RecyclerView recyclerCities = view.findViewById(R.id.recycler_cities);
        recyclerCities.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        CityAdapter cityAdapter = new CityAdapter();
        recyclerCities.setAdapter(cityAdapter);
        RecyclerView recycler = view.findViewById(R.id.recycler_attractions);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ScraperAttractionAdapter adapter = new ScraperAttractionAdapter();
        recycler.setAdapter(adapter);
        HotCityViewModel viewModel = new ViewModelProvider(this).get(HotCityViewModel.class);
        final int[] selectedIndex = {0};
        final boolean[] isFirstLoad = {true};
        viewModel.getHotCities().observe(getViewLifecycleOwner(), cities -> {
            if (cities != null && !cities.isEmpty()) {
                cityAdapter.setData(cities);
                cityAdapter.setSelectedIndex(0);
                recyclerCities.smoothScrollToPosition(0);
                if (isFirstLoad[0]) {
                    isFirstLoad[0] = false;
                    if (lottieAnimationView != null) {
                        lottieAnimationView.setVisibility(View.VISIBLE);
                        lottieAnimationView.playAnimation();
                    }
                    recycler.setAlpha(0.3f);
                    recycler.setEnabled(false);
                    new android.os.Handler().postDelayed(() -> {
                        if (lottieAnimationView != null) {
                            lottieAnimationView.cancelAnimation();
                            lottieAnimationView.setVisibility(View.GONE);
                        }
                        recycler.setAlpha(1f);
                        recycler.setEnabled(true);
                        viewModel.selectCity(cities.get(0));
                    }, 1500);
                } else {
                    viewModel.selectCity(cities.get(0));
                }
            }
        });
        cityAdapter.setOnItemClickListener((city, pos) -> {
            selectedIndex[0] = pos;
            cityAdapter.setSelectedIndex(pos);
            recyclerCities.smoothScrollToPosition(pos);
            if (lottieAnimationView != null) {
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();
            }
            recycler.setAlpha(0.3f);
            recycler.setEnabled(false);
            new android.os.Handler().postDelayed(() -> {
                if (lottieAnimationView != null) {
                    lottieAnimationView.cancelAnimation();
                    lottieAnimationView.setVisibility(View.GONE);
                }
                recycler.setAlpha(1f);
                recycler.setEnabled(true);
                viewModel.selectCity(city);
            }, 1500);
        });
        viewModel.getAttractions().observe(getViewLifecycleOwner(), list -> {
            adapter.setData(list);
        });
        adapter.setOnItemClickListener(dest -> {
            String formattedUrl = "https://you.ctrip.com/sight/" + dest.cityPinyin + dest.cityId + "/" + dest.businessId + ".html";
            WebViewFragment fragment = WebViewFragment.newInstance(formattedUrl);
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        });
        viewModel.loadHotCities(type);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lottieAnimationView != null) {
            lottieAnimationView.resumeAnimation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lottieAnimationView != null) {
            lottieAnimationView.pauseAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (lottieAnimationView != null) {
            lottieAnimationView.cancelAnimation();
        }
    }
} 