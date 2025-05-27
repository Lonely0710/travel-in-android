package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class RecommendFragment extends Fragment {
    private RecyclerView rvHotSpots, rvRecommended;
    private AttractionAdapter hotAdapter, recommendedAdapter;
    private ProgressBar progressRecommended;
    private Chip chipHot, chipAdventure, chipRelax, chipCulture;
    private RecommendViewModel viewModel;

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
        recommendedAdapter = new AttractionAdapter(new ArrayList<>(), R.layout.item_vertical_attraction, null, null);
        rvRecommended.setAdapter(recommendedAdapter);

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
            chipHot.setChecked(false);
            chipAdventure.setChecked(false);
            chipRelax.setChecked(false);
            chipCulture.setChecked(false);
            ((Chip) v).setChecked(true);
            String type = getCurrentChipType();
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
            if (!keyword.isEmpty()) {
                // 可扩展：调用 viewModel 的搜索方法
            }
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
            recommendedAdapter.setData(list != null ? list : new ArrayList<>());
            if (progressRecommended != null) progressRecommended.setVisibility(View.GONE);
        });
        viewModel.getHotList().observe(getViewLifecycleOwner(), list -> {
            hotAdapter.setData(list != null ? list : new ArrayList<>());
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
} 