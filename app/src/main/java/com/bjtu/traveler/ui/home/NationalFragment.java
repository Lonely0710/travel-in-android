package com.bjtu.traveler.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class NationalFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    public static final String TYPE_DOMESTIC = "domestic";
    public static final String TYPE_INTERNATIONAL = "international";

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

        if (TYPE_DOMESTIC.equals(type)) {
            tvTitle.setText("国内游");
            ivTitleIcon.setImageResource(R.drawable.ic_home_domestic);
        } else if (TYPE_INTERNATIONAL.equals(type)) {
            tvTitle.setText("出境游");
            ivTitleIcon.setImageResource(R.drawable.ic_home_world);
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
        viewModel.getHotCities().observe(getViewLifecycleOwner(), cities -> {
            if (cities != null && !cities.isEmpty()) {
                cityAdapter.setData(cities);
                cityAdapter.setSelectedIndex(0);
                recyclerCities.smoothScrollToPosition(0);
                viewModel.selectCity(cities.get(0));
            }
        });
        cityAdapter.setOnItemClickListener((city, pos) -> {
            selectedIndex[0] = pos;
            cityAdapter.setSelectedIndex(pos);
            recyclerCities.smoothScrollToPosition(pos);
            viewModel.selectCity(city);
        });
        viewModel.getAttractions().observe(getViewLifecycleOwner(), list -> {
            adapter.setData(list);
        });
        adapter.setOnItemClickListener(dest -> {
            WebViewFragment fragment = WebViewFragment.newInstance(dest.detailPageUrl);
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        });
        viewModel.loadHotCities(type);
    }
} 