package com.bjtu.traveler.ui.home;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.CityList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RandomCityDialogFragment extends DialogFragment {
    private TextView tvRandomLocation;
    private View btnRetry;
    private View btnDetail;
    private ImageView ivDialogClose;

    private List<CityEntry> cityList = new ArrayList<>();
    private CityEntry currentCity;
    private Random random = new Random();

    public interface OnDetailClickListener {
        void onDetailClick(String cityPinyin, String cityId, String cityChineseName);
    }
    private OnDetailClickListener detailClickListener;

    public void setOnDetailClickListener(OnDetailClickListener listener) {
        this.detailClickListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_home_random, container, false);
        tvRandomLocation = view.findViewById(R.id.tv_random_location);
        btnRetry = null;
        btnDetail = null;
        ivDialogClose = view.findViewById(R.id.iv_dialog_close);

        loadCityList(requireContext());
        showRandomCity();

        View retryBtn = view.findViewById(R.id.btn_random_retry);
        View detailBtn = view.findViewById(R.id.btn_random_detail);
        if (retryBtn != null) {
            retryBtn.setOnClickListener(v -> showRandomCity());
        }
        if (detailBtn != null) {
            detailBtn.setOnClickListener(v -> {
                if (currentCity != null) {
                    String cityName = currentCity.chineseName;
                    try {
                        String url = "file:///android_asset/gaode/index.html?city=" + java.net.URLEncoder.encode(cityName, "utf-8");
                        com.bjtu.traveler.ui.common.WebViewFragment webViewFragment = com.bjtu.traveler.ui.common.WebViewFragment.newInstance(url);
                        requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(com.bjtu.traveler.R.id.fragment_container, webViewFragment)
                            .addToBackStack(null)
                            .commit();
                        dismiss();
                    } catch (Exception e) {
                        android.widget.Toast.makeText(requireContext(), "跳转高德地图失败: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        ivDialogClose.setOnClickListener(v -> dismiss());
        return view;
    }

    private void loadCityList(Context context) {
        cityList.clear();
        // 合并所有城市列表，去重
        List<String> allCities = new ArrayList<>();
        allCities.addAll(CityList.HOT_CITIES);
        for (String city : CityList.RECOMMEND_CITIES) {
            if (!allCities.contains(city)) {
                allCities.add(city);
            }
        }
        for (String cityName : allCities) {
            // 这里简单处理：拼音和id都用空字符串，后续如需可完善
            cityList.add(new CityEntry("", "", cityName));
        }
    }

    private void showRandomCity() {
        if (cityList.isEmpty()) return;
        int idx = random.nextInt(cityList.size());
        currentCity = cityList.get(idx);
        tvRandomLocation.setText(currentCity.chineseName);
    }

    // 简单拼音转中文映射（可根据需要完善）
    private String pinyinToChinese(String pinyin) {
        // 由于现在直接用CityList的中文名，这里直接返回
        return pinyin;
    }

    private static class CityEntry {
        String pinyin;
        String id;
        String chineseName;
        CityEntry(String pinyin, String id, String chineseName) {
            this.pinyin = pinyin;
            this.id = id;
            this.chineseName = chineseName;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.85);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
} 