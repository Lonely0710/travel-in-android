package com.bjtu.traveler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.Attraction;
import com.bumptech.glide.Glide;
import android.text.TextUtils;
import android.content.Context;
import android.util.LruCache;
import com.bjtu.traveler.api.WikipediaApi;

import java.util.List;

public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.ViewHolder> {
    private List<Attraction> data;
    private int layoutResId;
    private OnAttractionClickListener onAttractionClickListener;
    private String currentCity = null;
    private String currentCountry = null;

    public AttractionAdapter(List<Attraction> data) {
        this.data = data;
    }

    public AttractionAdapter(List<Attraction> data, int layoutResId) {
        this.data = data;
        this.layoutResId = layoutResId;
    }

    public AttractionAdapter(List<Attraction> data, int layoutResId, String currentCity, String currentCountry) {
        this.data = data;
        this.layoutResId = layoutResId;
        this.currentCity = currentCity;
        this.currentCountry = currentCountry;
    }

    public void setData(List<Attraction> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setOnAttractionClickListener(OnAttractionClickListener listener) {
        this.onAttractionClickListener = listener;
    }

    public void setCurrentLocation(String city, String country) {
        this.currentCity = city;
        this.currentCountry = country;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attraction a = data.get(position);
        if (layoutResId == R.layout.item_horizontal_attraction) {
            if (holder.tvName != null) holder.tvName.setText(a.getName());
            if (holder.tvCategory != null)
                holder.tvCategory.setText(a.getCategory() != null ? a.getCategory() : "");
            if (holder.imgAttraction != null)
                loadAttractionImage(holder.itemView.getContext(), a, holder.imgAttraction);
            // 横向item整体点击跳转到详情页（Fragment跳转）
            holder.itemView.setOnClickListener(v -> {
                String imgUrl = holder.imgAttraction != null && holder.imgAttraction.getTag() != null ? holder.imgAttraction.getTag().toString() : null;
                if (onAttractionClickListener != null) {
                    onAttractionClickListener.onAttractionClick(a, imgUrl, currentCity, currentCountry);
                }
            });
        } else if (layoutResId == R.layout.item_vertical_attraction) {
            if (holder.tvTitle != null) holder.tvTitle.setText(a.getName());
            if (holder.tvSubCategory != null)
                holder.tvSubCategory.setText(a.getCategory() != null ? a.getCategory() : "");
            // 位置
            TextView tvLocation = holder.itemView.findViewById(R.id.tvLocation);
            String cityToShow = a.getCity() != null ? a.getCity() : "";
            String countryToShow = a.getCountry() != null ? a.getCountry() : "";
            StringBuilder locationBuilder = new StringBuilder();
            if (!cityToShow.isEmpty()) locationBuilder.append(cityToShow);
            if (!countryToShow.isEmpty()) {
                if (locationBuilder.length() > 0) locationBuilder.append(", ");
                locationBuilder.append(countryToShow);
            }
            tvLocation.setText(locationBuilder.toString());
            if (holder.tvScore != null) holder.tvScore.setText("");
            holder.itemView.setOnClickListener(v -> {
                String imgUrl = holder.imgThumb != null && holder.imgThumb.getTag() != null ? holder.imgThumb.getTag().toString() : null;
                if (onAttractionClickListener != null) {
                    onAttractionClickListener.onAttractionClick(a, imgUrl, cityToShow, countryToShow);
                }
            });
            if (holder.imgThumb != null) {
                loadAttractionImage(holder.itemView.getContext(), a, holder.imgThumb);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    // 图片URL缓存，避免重复请求
    private static final LruCache<String, String> imageUrlCache = new LruCache<>(100);

    // 获取景点图片URL（优先用Attraction.url，无则查百科，查到后缓存）
    private void loadAttractionImage(Context context, Attraction attraction, ImageView imageView) {
        String cacheKey = attraction.getName();
        String url = attraction.getUrl();
        if (!TextUtils.isEmpty(url)) {
            Glide.with(context).load(url).into(imageView);
            imageView.setTag(url);
            return;
        }
        String cachedUrl = imageUrlCache.get(cacheKey);
        if (cachedUrl != null) {
            Glide.with(context).load(cachedUrl).into(imageView);
            imageView.setTag(cachedUrl);
            return;
        }
        WikipediaApi.fetchAttractionInfo(attraction.getName(), new WikipediaApi.Callback() {
            @Override
            public void onSuccess(String desc, String wikiImgUrl) {
                if (!TextUtils.isEmpty(wikiImgUrl)) {
                    imageUrlCache.put(cacheKey, wikiImgUrl);
                    attraction.setUrl(wikiImgUrl);
                    Glide.with(context).load(wikiImgUrl).into(imageView);
                    imageView.setTag(wikiImgUrl);
                } else {
                    // 热门区用exam1，推荐区用exam2
                    if (layoutResId == R.layout.item_horizontal_attraction) {
                        imageView.setImageResource(R.drawable.exam1);
                    } else if (layoutResId == R.layout.item_vertical_attraction) {
                        imageView.setImageResource(R.drawable.exam2);
                    } else {
                        imageView.setImageResource(R.drawable.ic_favorite_border);
                    }
                    imageView.setTag(null);
                }
            }
            @Override
            public void onFailure(int errorCode, String errorMsg) {
                if (layoutResId == R.layout.item_horizontal_attraction) {
                    imageView.setImageResource(R.drawable.exam1);
                } else if (layoutResId == R.layout.item_vertical_attraction) {
                    imageView.setImageResource(R.drawable.exam2);
                } else {
                    imageView.setImageResource(R.drawable.ic_favorite_border);
                }
                imageView.setTag(null);
            }
        });
    }

    // 新增接口
    public interface OnAttractionClickListener {
        void onAttractionClick(Attraction attraction, String imgUrl, String city, String country);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // 横向item控件
        ImageView imgAttraction;
        TextView tvCategory, tvName, tvRating;
        // 纵向item控件
        ImageView imgThumb;
        TextView tvTitle, tvSubCategory, tvLocation, tvScore;

        ViewHolder(View itemView) {
            super(itemView);
            // 横向布局
            imgAttraction = itemView.findViewById(R.id.imgAttraction);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvName = itemView.findViewById(R.id.tvName);
            tvRating = itemView.findViewById(R.id.tvRating);
            // 纵向布局
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubCategory = itemView.findViewById(R.id.tvSubCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}

