package com.bjtu.traveler.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.Post;
import java.util.List;

public class PostHistoryAdapter extends RecyclerView.Adapter<PostHistoryAdapter.ViewHolder> {
    private List<Post> postList;

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
        // 标题
        holder.tvTitle.setText(post.getTittle());
        // 图片加载
        if (post.getImgUrl() != null && !post.getImgUrl().isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(post.getImgUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }
        // 分类
        if (post.getCategory() != null && !post.getCategory().isEmpty()) {
            holder.tvCategory.setVisibility(View.VISIBLE);
            holder.tvCategory.setText(post.getCategory());
            holder.ivCategoryIcon.setVisibility(View.VISIBLE);
            setCategoryIcon(holder.ivCategoryIcon, post.getCategory());
        } else {
            holder.tvCategory.setVisibility(View.GONE);
            holder.ivCategoryIcon.setVisibility(View.GONE);
        }
        // 地点
        holder.tvLocation.setText(post.getLocation());
        // 用户信息
        if (post.getUserId() != null) {
            Log.d("PostHistoryAdapter", "userId 不为 null");
            String username = post.getUserId().getUsername();
            String avatarUrl = post.getUserId().getAvatarUrl();
            Log.d("PostHistoryAdapter", "username: " + username + ", avatarUrl: " + avatarUrl);
            if (username != null && !username.isEmpty()) {
                holder.tvAuthor.setVisibility(View.VISIBLE);
                holder.tvAuthor.setText(username);
            } else {
                holder.tvAuthor.setVisibility(View.GONE);
                Log.d("PostHistoryAdapter", "username 为空");
            }
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                holder.ivAuthorAvatar.setVisibility(View.VISIBLE);
                Log.d("PostHistoryAdapter", "加载头像: " + avatarUrl);
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .into(holder.ivAuthorAvatar);
            } else {
                holder.ivAuthorAvatar.setVisibility(View.VISIBLE);
                holder.ivAuthorAvatar.setImageResource(R.drawable.ic_avatar);
                Log.d("PostHistoryAdapter", "avatarUrl 为空，显示默认头像");
            }
        } else {
            holder.tvAuthor.setVisibility(View.GONE);
            holder.ivAuthorAvatar.setVisibility(View.GONE);
            Log.d("PostHistoryAdapter", "userId 为空");
        }
    }

    private void setCategoryIcon(ImageView imageView, String category) {
        int iconResId = 0;
        if (category != null) {
            switch (category) {
                case "美食":
                    iconResId = R.drawable.ic_chip_food;
                    break;
                case "住宿":
                    iconResId = R.drawable.ic_chip_hotel;
                    break;
                case "景观":
                    iconResId = R.drawable.ic_chip_scenery;
                    break;
            }
        }
        if (iconResId != 0) {
            imageView.setImageResource(iconResId);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPostImage;
        TextView tvTitle;
        TextView tvLocation;
        ImageView ivAuthorAvatar;
        TextView tvAuthor;
        TextView tvCategory;
        ImageView ivCategoryIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostImage = itemView.findViewById(R.id.iv_post_image);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvLocation = itemView.findViewById(R.id.tv_post_location);
            ivAuthorAvatar = itemView.findViewById(R.id.iv_author_avatar);
            tvAuthor = itemView.findViewById(R.id.tv_post_author);
            tvCategory = itemView.findViewById(R.id.tv_post_category);
            ivCategoryIcon = itemView.findViewById(R.id.iv_post_category_icon);
        }
    }
}