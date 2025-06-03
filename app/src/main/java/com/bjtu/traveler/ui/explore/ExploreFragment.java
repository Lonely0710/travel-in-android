package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.bjtu.traveler.R;
import com.bjtu.traveler.viewmodel.ExploreViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.core.content.ContextCompat;
import android.graphics.Color;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ExploreFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ExploreViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        View topBar = view.findViewById(R.id.topbar_explore);
        if (topBar != null) {
            TextView tvTitle = topBar.findViewById(R.id.tv_title);
            if (tvTitle != null) {
                tvTitle.setText("Explore");
            }
        }
        tabLayout = view.findViewById(R.id.tab_explore);
        viewPager = view.findViewById(R.id.viewpager_explore);

        // 设置 ViewPager2 和 TabLayout
        FragmentStateAdapter adapter = new ExplorePagerAdapter(this);
        viewPager.setAdapter(adapter);
        String[] tabTitles = {"推荐", "发现", "VR"};
        int[] tabIcons = {R.drawable.ic_explore_recommend, R.drawable.ic_explore_find, R.drawable.ic_explore_vr};
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setCustomView(R.layout.tab_explore);
            TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
            ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
            tabText.setText(tabTitles[position]);
            tabIcon.setImageResource(tabIcons[position]);
            if (position == tabLayout.getSelectedTabPosition()) {
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary));
                tabIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary));
            } else {
                tabText.setTextColor(Color.GRAY);
                tabIcon.setColorFilter(Color.GRAY);
            }
        }).attach();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
    @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary));
                tabIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary));
            }
    @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                tabText.setTextColor(Color.GRAY);
                tabIcon.setColorFilter(Color.GRAY);
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        TabLayout.Tab initialTab = tabLayout.getTabAt(0);
        if (initialTab != null && initialTab.getCustomView() != null) {
            TextView tabText = initialTab.getCustomView().findViewById(R.id.tab_text);
            ImageView tabIcon = initialTab.getCustomView().findViewById(R.id.tab_icon);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_primary));
            tabIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_primary));
        }
        // 观察城市和国家 LiveData，刷新 UI
        viewModel.getCurrentCity().observe(getViewLifecycleOwner(), city -> {
            // 可扩展：刷新 UI 上的城市显示
        });
        viewModel.getCurrentCountry().observe(getViewLifecycleOwner(), country -> {
            // 可扩展：刷新 UI 上的国家显示
        });
        // 启动定位监听
        viewModel.startLocationUpdates(requireContext());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getWindow().getDecorView().post(() -> {
            if (getActivity() instanceof com.bjtu.traveler.BaseActivity) {
                ((com.bjtu.traveler.BaseActivity) getActivity()).hideSystemBars();
            }
        });
    }

    private static class ExplorePagerAdapter extends FragmentStateAdapter {
        public ExplorePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }
        @NonNull
    @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new RecommendFragment();
                case 1:
                    return new DiscoverFragment();
                case 2:
                    return new VRFragment();
                default:
                    return new RecommendFragment();
            }
        }
        @Override
        public int getItemCount() {
            return 3;
        }
    }
}

