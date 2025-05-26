package com.bjtu.traveler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.Post;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostHistoryAdapter extends RecyclerView.Adapter<PostHistoryAdapter.ViewHolder> {
    private final List<Post> postList;

    public PostHistoryAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvContent.setText(post.getContent());
        holder.tvTime.setText(formatTime(post.getTimestamp()));
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;
        ImageView ivImage;
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvContent = itemView.findViewById(R.id.tv_post_content);
            tvTime = itemView.findViewById(R.id.tv_post_time);
            ivImage = itemView.findViewById(R.id.iv_post_image);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

