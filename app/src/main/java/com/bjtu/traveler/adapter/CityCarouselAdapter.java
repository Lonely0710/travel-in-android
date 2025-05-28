package com.bjtu.traveler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.CityCarouselItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class CityCarouselAdapter extends RecyclerView.Adapter<CityCarouselAdapter.CityViewHolder> {

    private List<CityCarouselItem> cityList;
    private OnCityItemClickListener listener;

    public interface OnCityItemClickListener {
        void onItemClick(CityCarouselItem city);
    }

    public void setOnCityItemClickListener(OnCityItemClickListener listener) {
        this.listener = listener;
    }

    public CityCarouselAdapter(List<CityCarouselItem> cityList) {
        this.cityList = cityList;
    }

    public void setCityList(List<CityCarouselItem> cityList) {
        this.cityList = cityList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_carousel, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        CityCarouselItem city = cityList.get(position);
        holder.bind(city);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cityList != null ? cityList.size() : 0;
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCityImage;
        TextView tvCityName;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCityImage = itemView.findViewById(R.id.iv_city_image);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
        }

        public void bind(CityCarouselItem city) {
            Glide.with(itemView.getContext())
                 .load(city.getImageUrl())
                 .placeholder(R.drawable.ic_launcher_background)
                 .error(R.drawable.ic_launcher_background)
                 .centerCrop()
                 .into(ivCityImage);

            tvCityName.setText(city.getCityName());
        }
    }
} 