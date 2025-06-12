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
import com.bjtu.traveler.data.model.User;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList; // Import ArrayList
import android.util.Log;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> { // Renamed class
    private List<Post> postList;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // 添加点击事件监听器接口和成员变量
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(List<Post> postList) { // Renamed constructor
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
        // 日志输出
        Log.d("PostAdapter", "onBindViewHolder: imgUrl=" + post.getImgUrl());
        User user = post.getUserId();
        // 标题
        holder.tvTitle.setText(post.getTittle());
        // 图片加载
        if (post.getImgUrl() != null && !post.getImgUrl().isEmpty()) {
            holder.ivImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getImgUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setVisibility(View.GONE);
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
        if (user != null) {
            String username = user.getUsername();
            String avatarUrl = user.getAvatarUrl();
            Log.d("PostAdapter", "user != null, username=" + username + ", avatarUrl=" + avatarUrl);
            if (username != null && !username.isEmpty()) {
                holder.tvAuthor.setVisibility(View.VISIBLE);
                holder.tvAuthor.setText(username);
            } else {
                holder.tvAuthor.setVisibility(View.GONE);
                Log.d("PostAdapter", "username 为空或 null");
            }
            if (holder.ivAuthorAvatar != null) {
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    holder.ivAuthorAvatar.setVisibility(View.VISIBLE);
                    Log.d("PostAdapter", "加载头像: " + avatarUrl);
                    Glide.with(holder.itemView.getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .into(holder.ivAuthorAvatar);
                } else {
                    holder.ivAuthorAvatar.setVisibility(View.VISIBLE);
                    holder.ivAuthorAvatar.setImageResource(R.drawable.ic_avatar);
                    Log.d("PostAdapter", "avatarUrl 为空或 null，显示默认头像");
                }
            }
        } else {
            if (holder.tvAuthor != null) holder.tvAuthor.setVisibility(View.GONE);
            if (holder.ivAuthorAvatar != null) holder.ivAuthorAvatar.setVisibility(View.GONE);
            Log.d("PostAdapter", "user == null");
        }

        // 设置item点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            } else {
                Log.w("PostAdapter", "OnPostClickListener not set.");
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList == null ? 0 : postList.size();
    }

    // Method to update the list of posts
    public void updatePostList(List<Post> newPostList) {
        this.postList.clear();
        if (newPostList != null) {
            this.postList.addAll(newPostList);
        } else {
            this.postList = new ArrayList<>(); // Ensure postList is not null
        }
        notifyDataSetChanged(); // Notify adapter that data has changed
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvCategory, tvAuthor;
        ImageView ivImage, ivAuthorAvatar, ivCategoryIcon;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvLocation = itemView.findViewById(R.id.tv_post_location);
            tvCategory = itemView.findViewById(R.id.tv_post_category);
            tvAuthor = itemView.findViewById(R.id.tv_post_author);
            ivImage = itemView.findViewById(R.id.iv_post_image);
            ivAuthorAvatar = itemView.findViewById(R.id.iv_author_avatar);
            ivCategoryIcon = itemView.findViewById(R.id.iv_post_category_icon);
        }
    }
}
