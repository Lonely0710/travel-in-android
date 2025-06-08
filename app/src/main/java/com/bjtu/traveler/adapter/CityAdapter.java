package com.bjtu.traveler.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.viewmodel.HotCityViewModel;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.animation.ValueAnimator;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
    private List<HotCityViewModel.City> data;
    private int selectedIndex = 0;
    private OnItemClickListener listener;

    private static final String TAG = "CityAdapter";

    // 中文城市名到英文图标名的映射
    private static final Map<String, String> CITY_ICON_NAME_MAP = new HashMap<>();
    static {
        CITY_ICON_NAME_MAP.put("巴黎", "paris");
        CITY_ICON_NAME_MAP.put("伦敦", "london");
        CITY_ICON_NAME_MAP.put("纽约", "newyork");
        CITY_ICON_NAME_MAP.put("迪拜", "dubai");
        CITY_ICON_NAME_MAP.put("洛杉矶", "losangeles");
        CITY_ICON_NAME_MAP.put("罗马", "roman");
        CITY_ICON_NAME_MAP.put("首尔", "seoul");
        CITY_ICON_NAME_MAP.put("悉尼", "sydney");
        CITY_ICON_NAME_MAP.put("东京", "tokyo");
        CITY_ICON_NAME_MAP.put("新加坡", "singapore");
    }

    public interface OnItemClickListener {
        void onItemClick(HotCityViewModel.City city, int position);
    }
    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    public void setData(List<HotCityViewModel.City> list) {
        this.data = list;
        notifyDataSetChanged();
    }
    public void setSelectedIndex(int idx) {
        selectedIndex = idx;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_selector, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        HotCityViewModel.City city = data.get(pos);
        boolean isSelected = (pos == selectedIndex);
        int currentTextColor = h.tvName.getCurrentTextColor();
        boolean isInternational = isInternationalCity(city); // 使用新方法判断是否为国际城市

        Log.d(TAG, "onBindViewHolder: City Name: " + city.name + ", Position: " + pos + ", Is Selected: " + isSelected + ", Is International: " + isInternational);

        h.tvName.setText(city.name);

        // 设置背景和文字颜色
        if (isSelected) {
            h.itemView.setBackgroundResource(R.drawable.bg_city_selector_selected);
            h.animateTextColor(currentTextColor, Color.WHITE);

            // 控制白色圆形背景的可见性
            if (isInternational) {
                Log.d(TAG, "onBindViewHolder: International city selected, hiding background.");
                h.ivIconBackground.setVisibility(View.GONE); // 国际城市选中：不显示背景
            } else {
                Log.d(TAG, "onBindViewHolder: Domestic city selected, showing background.");
                h.ivIconBackground.setVisibility(View.VISIBLE); // 国内城市选中：显示白色背景
            }
            h.ivIcon.setColorFilter(null); // 选中时图标不着色
            Log.d(TAG, "onBindViewHolder: Icon tint set to NULL (selected).");
        } else {
            h.itemView.setBackgroundResource(R.drawable.bg_city_selector_unselected);
            h.animateTextColor(currentTextColor, Color.parseColor("#333333")); // 未选中时为深灰色文字
            h.ivIconBackground.setVisibility(View.GONE); // 未选中时：隐藏白色背景

            // 根据城市类型设置图标着色（仅在未选中时）
            if (isInternational) {
                Log.d(TAG, "onBindViewHolder: International city unselected, tint set to NULL.");
                h.ivIcon.setColorFilter(null); // 国际城市未选中：不着色
            } else {
                Log.d(TAG, "onBindViewHolder: Domestic city unselected, tint set to BLUE.");
                h.ivIcon.setColorFilter(Color.parseColor("#2196F3")); // 国内城市未选中：蓝色着色
            }
        }

        // 设置图标
        int iconResId = getIconResId(h.itemView.getContext(), city.name.trim(), city.pinyin.trim()); // 传递中文名和拼音
        Log.d(TAG, "onBindViewHolder: Icon Res ID for " + city.name + ": " + iconResId);

        if (iconResId != 0) {
            h.ivIcon.setImageResource(iconResId);
            Log.d(TAG, "onBindViewHolder: Icon set for " + city.name);
        } else {
            // 如果找不到图标，则不设置任何Drawable，确保显示空白而不是默认的方形背景
            h.ivIcon.setImageDrawable(null);
            h.ivIcon.setColorFilter(null); // 确保没有意外的着色
            Log.d(TAG, "onBindViewHolder: Icon not found for " + city.name + ", setting to null.");
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(city, pos);
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIconBackground;
        ImageView ivIcon;
        TextView tvName;
        ViewHolder(@NonNull View v) {
            super(v);
            ivIconBackground = v.findViewById(R.id.iv_city_icon_background);
            ivIcon = v.findViewById(R.id.iv_city_icon);
            tvName = v.findViewById(R.id.tv_city_name);
        }

        public void animateTextColor(int fromColor, int toColor) {
            ValueAnimator colorAnimation = ValueAnimator.ofArgb(fromColor, toColor);
            colorAnimation.setDuration(300); // 动画时长
            colorAnimation.addUpdateListener(animator -> tvName.setTextColor((Integer) animator.getAnimatedValue()));
            colorAnimation.start();
        }
    }

    // 辅助方法：根据城市名称获取图标资源ID
    private int getIconResId(Context context, String cityName, String cityPinyin) {
        // 首先尝试通过映射获取英文图标名（用于国际城市）
        String iconLookupName = CITY_ICON_NAME_MAP.get(cityName.trim());
        Log.d(TAG, "getIconResId: Lookup for city \"" + cityName + "\" in map, mapped name: " + iconLookupName);

        if (iconLookupName == null) {
            // 如果映射中没有，则使用提供的拼音（用于国内城市或未映射的国际城市）
            iconLookupName = cityPinyin; // 直接使用传入的拼音
            Log.d(TAG, "getIconResId: No mapping found for \"" + cityName + "\", using provided pinyin: " + iconLookupName);
        }

        if (iconLookupName != null) {
            // 确保拼音是小写且清理了特殊字符（如果 getPinyin 已经处理，这里可省略）
            String finalIconName = iconLookupName.toLowerCase().replaceAll("[\\s\\-.]", "");
            String resourceName = "ic_city_" + finalIconName;
            int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            Log.d(TAG, "getIconResId: Attempting to find resource \"" + resourceName + "\", Result ID: " + resId);
            return resId;
        } else {
            Log.d(TAG, "getIconResId: No valid lookup name for city \"" + cityName + "\", returning 0.");
            return 0; // 没有找到合适的图标名
        }
    }

    // 辅助方法：判断是否为国际城市
    private boolean isInternationalCity(HotCityViewModel.City city) {
        // 如果城市名（trim后）在 CITY_ICON_NAME_MAP 中存在，则认为是国际城市
        return CITY_ICON_NAME_MAP.containsKey(city.name.trim());
    }
} 