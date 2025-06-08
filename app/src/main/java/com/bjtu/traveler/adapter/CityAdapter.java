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

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
    private List<HotCityViewModel.City> data;
    private int selectedIndex = 0;
    private OnItemClickListener listener;

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
        // 获取当前文字颜色，用于动画的起始颜色
        int currentTextColor = h.tvName.getCurrentTextColor();

        h.tvName.setText(city.name);

        // 设置背景和文字颜色
        if (isSelected) {
            h.itemView.setBackgroundResource(R.drawable.bg_city_selector_selected);
            // 动画文字颜色
            h.animateTextColor(currentTextColor, Color.WHITE);
            h.ivIconBackground.setVisibility(View.VISIBLE); // 选中时显示白色背景
            h.ivIcon.setColorFilter(null); // 选中时图标不着色
        } else {
            h.itemView.setBackgroundResource(R.drawable.bg_city_selector_unselected);
            // 动画文字颜色
            h.animateTextColor(currentTextColor, Color.parseColor("#333333")); // 未选中时为深灰色
            h.ivIconBackground.setVisibility(View.GONE); // 未选中时隐藏白色背景
            h.ivIcon.setColorFilter(Color.parseColor("#000000")); // 假设默认是黑色，可以根据实际图标颜色调整
        }

        // 设置图标
        int iconResId = getIconResId(h.itemView.getContext(), city.pinyin); // 优先使用拼音获取图标
        if (iconResId == 0) {
            iconResId = getIconResId(h.itemView.getContext(), city.name); // 如果拼音找不到，尝试用中文/英文名直接获取
        }

        if (iconResId != 0) {
            h.ivIcon.setImageResource(iconResId);
            // 根据城市类型和选中状态设置图标着色
            if (isChinese(city.name)) { // 判断是否为中国城市
                if (isSelected) {
                    // 中国城市选中时图标不着色
                } else {
                    h.ivIcon.setColorFilter(Color.parseColor("#2196F3")); // 假设未选中时图标为蓝色
                }
            } else { // 国际城市
                if (isSelected) {
                    // 国际城市选中时图标不着色
                } else {
                    h.ivIcon.setColorFilter(Color.parseColor("#8E24AA")); // 假设未选中时图标为紫色
                }
            }
        } else {
            h.ivIcon.setImageResource(R.drawable.ic_launcher_background); // 设置一个默认图标或隐藏
            h.ivIconBackground.setVisibility(View.GONE);
            h.ivIcon.setColorFilter(null);
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
    private int getIconResId(Context context, String cityName) {
        // 将城市名转换为适合资源ID的格式 (小写，去除空格，特殊字符)
        String formattedName = cityName.toLowerCase().replaceAll("[\\s\\-.]", "");
        // 尝试获取 drawable 资源ID，例如 ic_city_beijing
        return context.getResources().getIdentifier("ic_city_" + formattedName, "drawable", context.getPackageName());
    }

    // 辅助方法：判断是否为中文（粗略判断，可根据实际需求优化）
    private boolean isChinese(String str) {
        Pattern p = Pattern.compile("[一-龥]");
        Matcher m = p.matcher(str);
        return m.find();
    }
} 