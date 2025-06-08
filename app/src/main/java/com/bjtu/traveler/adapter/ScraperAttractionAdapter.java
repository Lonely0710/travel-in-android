package com.bjtu.traveler.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.utils.PlaceScraper;
import com.bumptech.glide.Glide;
import java.util.List;

public class ScraperAttractionAdapter extends RecyclerView.Adapter<ScraperAttractionAdapter.ViewHolder> {
    private List<PlaceScraper.Destination> data;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(PlaceScraper.Destination dest);
    }
    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    public void setData(List<PlaceScraper.Destination> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_home_national, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        PlaceScraper.Destination d = data.get(pos);
        h.tvTitle.setText(d.name);
        h.tvDesc.setText(d.description);
        h.tvScore.setText(String.valueOf(d.commentScore));
        h.tvLevel.setText(d.sightLevel);
        h.tvLevel.setVisibility(d.sightLevel != null && !d.sightLevel.isEmpty() ? View.VISIBLE : View.GONE);
        h.tvAddress.setText(d.addressDistance);
        h.tvHeat.setText("热度: " + d.heatScore);
        // 简介
        if (d.description != null && !d.description.isEmpty()) {
            h.tvDesc.setText(d.description);
        } else {
            h.tvDesc.setText("暂无简介");
        }
        // 价格
        if (d.price == 0) {
            h.tvPrice.setText("免费");
            h.tvPrice.setBackgroundResource(R.drawable.spotcard_bg_price_free);
            h.tvPrice.setTextColor(Color.parseColor("#27ae60"));
        } else if (d.price > 0) {
            h.tvPrice.setText("￥" + d.price);
            h.tvPrice.setBackgroundResource(R.drawable.spotcard_bg_price_paid);
            h.tvPrice.setTextColor(Color.parseColor("#8e44ad"));
        } else {
            h.tvPrice.setText("未知");
            h.tvPrice.setBackgroundResource(R.drawable.spotcard_bg_price_unknown);
            h.tvPrice.setTextColor(Color.parseColor("#7f8c8d"));
        }
        // 图片
        if (d.coverImageUrl != null && !d.coverImageUrl.isEmpty()) {
            Glide.with(context).load(d.coverImageUrl).centerCrop().into(h.img);
        } else {
            h.img.setImageResource(R.drawable.img_hongyadong);
        }
        // 标签流
        h.llTags.removeAllViews();
        if (d.tagNames != null) {
            for (String tag : d.tagNames) {
                TextView tagView = new TextView(context);
                tagView.setText(tag);
                tagView.setTextSize(12);
                tagView.setTextColor(Color.parseColor("#2c3e50"));
                tagView.setBackgroundResource(R.drawable.spotcard_bg_price_unknown);
                tagView.setPadding(24, 8, 24, 8);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 20, 0);
                tagView.setLayoutParams(lp);
                h.llTags.addView(tagView);
            }
        }
        h.btnDetail.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(d);
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvScore, tvLevel, tvAddress, tvPrice, tvHeat;
        LinearLayout llTags, btnDetail;
        ImageView img;
        ViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvScore = v.findViewById(R.id.tv_score);
            tvLevel = v.findViewById(R.id.tv_level);
            tvAddress = v.findViewById(R.id.tv_address);
            tvPrice = v.findViewById(R.id.tv_price);
            tvHeat = v.findViewById(R.id.tv_heat);
            llTags = v.findViewById(R.id.ll_tags);
            btnDetail = v.findViewById(R.id.btn_detail_container);
            img = v.findViewById(R.id.img_attraction);
        }
    }
} 