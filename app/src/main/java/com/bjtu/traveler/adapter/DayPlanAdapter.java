package com.bjtu.traveler.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.DayPlan; // 使用您的 DayPlan 类
import com.bjtu.traveler.ui.routes.RoutesFragment; // 用于内部 Attraction 模型

// Gson for parsing the JSON string within DayPlan activity fields
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class DayPlanAdapter extends RecyclerView.Adapter<DayPlanAdapter.DayPlanViewHolder> {

    private List<DayPlan> dayPlans;
    private Context context;
    private Gson gson = new Gson(); // 用于解析 DayPlan 中的 JSON 字符串

    public DayPlanAdapter(Context context, List<DayPlan> dayPlans) {
        this.context = context;
        this.dayPlans = dayPlans != null ? dayPlans : new ArrayList<>();
    }

    public void setDayPlans(List<DayPlan> dayPlans) {
        this.dayPlans = dayPlans != null ? dayPlans : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_plan, parent, false);
        return new DayPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayPlanViewHolder holder, int position) {
        if (dayPlans == null || dayPlans.isEmpty()) {
            return;
        }
        DayPlan plan = dayPlans.get(position);

        holder.tvDayTitle.setText(plan.getDayTitle()); // AI 会生成 "Day X: 主题"

        // 解析上午活动
        bindActivityToView(plan.getMorningActivity(), plan.getMorningIconType(), holder.tvMorningActivity, holder.ivMorningIcon);
        // 解析下午活动
        bindActivityToView(plan.getAfternoonActivity(), plan.getAfternoonIconType(), holder.tvAfternoonActivity, holder.ivAfternoonIcon);
        // 解析晚上活动
        bindActivityToView(plan.getEveningActivity(), plan.getEveningIconType(), holder.tvEveningActivity, holder.ivEveningIcon);
    }

    private void bindActivityToView(String activityJson, String iconType, TextView tvActivity, ImageView ivIcon) {
        if (activityJson == null || activityJson.isEmpty() || activityJson.equals("null") || activityJson.equals("{}")) {
            tvActivity.setText("暂无安排");
            ivIcon.setImageResource(getIconResourceForType(iconType)); // 使用AI建议的图标类型或默认
        } else {
            try {
                // 解析 JSON 字符串为 Attraction 对象
                RoutesFragment.Attraction attraction = gson.fromJson(activityJson, RoutesFragment.Attraction.class);
                if (attraction != null && !TextUtils.isEmpty(attraction.getName())) {
                    tvActivity.setText(attraction.getName());
                    ivIcon.setImageResource(getIconResourceForType(iconType)); // 使用AI建议的图标类型或根据景点名称/标签
                } else {
                    tvActivity.setText("暂无安排");
                    ivIcon.setImageResource(getIconResourceForType(iconType));
                }
            } catch (JsonSyntaxException e) {
                Log.e("DayPlanAdapter", "Error parsing activity JSON: " + activityJson, e);
                tvActivity.setText("数据解析错误");
                ivIcon.setImageResource(R.drawable.ic_error); // 错误图标
            }
        }
    }


    /**
     * 根据IconType字符串返回对应的Drawable资源ID
     * @param iconType 图标类型字符串，例如 "bus", "walk"
     * @return Drawable 资源 ID
     */
    @DrawableRes
    private int getIconResourceForType(String iconType) {
        if (iconType == null) {
            return R.drawable.ic_walk; // 默认图标
        }
        switch (iconType.toLowerCase()) {
            case "历史": case "文化": case "古迹": case "建筑": case "长城":
                return R.drawable.ic_castle;
            case "自然": case "山水": case "户外": case "公园": case "山": case "湖":
                return R.drawable.ic_forest;
            case "美食": case "餐饮": case "餐厅": case "火锅":
                return R.drawable.ic_restaurant_menu;
            case "购物": case "商场": case "商业":
                return R.drawable.ic_shopping_bag;
            case "水上活动": case "游船": case "索道":
                return R.drawable.ic_sailing;
            case "亲子": case "乐园": case "动物园":
                return R.drawable.ic_toy;
            case "广场":
                return R.drawable.ic_location_pin;
            // 可以根据需要添加更多case
            default:
                return R.drawable.ic_walk;
        }
    }

    @Override
    public int getItemCount() {
        return dayPlans != null ? dayPlans.size() : 0;
    }

    static class DayPlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayTitle, tvMorningActivity, tvAfternoonActivity, tvEveningActivity;
        ImageView ivMorningIcon, ivAfternoonIcon, ivEveningIcon; // 确保ID正确

        public DayPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayTitle = itemView.findViewById(R.id.tvDayTitle);
            tvMorningActivity = itemView.findViewById(R.id.tvMorningActivity);
            ivMorningIcon = itemView.findViewById(R.id.ivMorningIcon);
            tvAfternoonActivity = itemView.findViewById(R.id.tvAfternoonActivity);
            ivAfternoonIcon = itemView.findViewById(R.id.ivAfternoonIcon);
            tvEveningActivity = itemView.findViewById(R.id.tvEveningActivity);
            ivEveningIcon = itemView.findViewById(R.id.ivEveningIcon); // 确保ID正确
        }
    }
}