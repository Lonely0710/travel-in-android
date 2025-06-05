package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.Post;
import com.bjtu.traveler.ui.common.ErrorFragment;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 获取传递过来的 Post 对象
        Post post = null;
        Bundle args = getArguments();
        if (args != null && args.containsKey("post")) {
            post = (Post) args.getSerializable("post");
        }

        // 如果获取到 Post 对象，则进行数据绑定
        if (post != null) {
            ImageView ivPostImage = view.findViewById(R.id.iv_post_image);
            TextView tvPostTitle = view.findViewById(R.id.tv_post_title);
            TextView tvPostAuthor = view.findViewById(R.id.tv_post_author);
            TextView tvPostContent = view.findViewById(R.id.tv_post_content);
            TextView tvPostAddress = view.findViewById(R.id.tv_post_address);
            TextView tvPostTimeLocation = view.findViewById(R.id.tv_post_time_location);
            ChipGroup chipGroup = view.findViewById(R.id.chip_group_categories);
            ImageView ivBack = view.findViewById(R.id.iv_back);
            ImageView ivAuthorAvatar = view.findViewById(R.id.iv_author_avatar);
            TextView tvPostLocationTop = view.findViewById(R.id.tv_post_location);

            // 绑定返回按钮事件
            ivBack.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack(); // 返回上一个Fragment
            });

            // 加载图片
            if (post.getImgUrl() != null && !post.getImgUrl().isEmpty()) {
                Glide.with(this)
                     .load(post.getImgUrl())
                     .placeholder(R.drawable.ic_image_placeholder)
                     .error(R.drawable.ic_image_placeholder)
                     .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }

            // 绑定其他数据
            tvPostTitle.setText(post.getTittle());
            if (post.getUserId() != null) {
                 tvPostAuthor.setText(post.getUserId().getUsername());
                 // 加载用户头像
                 if (post.getUserId().getAvatarUrl() != null && !post.getUserId().getAvatarUrl().isEmpty()) {
                     Glide.with(this)
                          .load(post.getUserId().getAvatarUrl())
                          .placeholder(R.drawable.ic_avatar) // 默认头像
                          .error(R.drawable.ic_avatar) // 加载失败显示默认头像
                          .into(ivAuthorAvatar);
                 } else {
                     ivAuthorAvatar.setImageResource(R.drawable.ic_avatar); // URL为空时显示默认头像
                 }
            } else {
                 tvPostAuthor.setText("未知作者");
                 ivAuthorAvatar.setImageResource(R.drawable.ic_avatar); // User对象为null时显示默认头像
            }

            tvPostContent.setText(post.getArticle());
            tvPostAddress.setText(post.getLocation());
            tvPostLocationTop.setText(post.getLocation());

            // 绑定发布时间
            String timeLocationText = "未知时间"; // Default value if processing fails
            Object createdAt = post.getCreatedAt(); // 获取createdAt对象
            java.util.Date postDate = null; // Variable to hold the Date object

            if (createdAt != null) {
                try {
                    // 尝试将 createdAt 直接作为 Date 或从 BmobDate 中获取 Date
                    if (createdAt instanceof java.util.Date) {
                        postDate = (java.util.Date) createdAt;
                    } else if (createdAt instanceof cn.bmob.v3.datatype.BmobDate) {
                        // 如果是 BmobDate，通过其 toString() 尝试解析，或者如果其内部Date仍有问题
                        // 这是一个针对之前错误的防御性处理：如果getDate()返回String
                        try {
                            // 尝试调用 getDate() 但捕获返回 String 的情况
                            Object dateCandidate = ((cn.bmob.v3.datatype.BmobDate) createdAt).getDate();
                            if (dateCandidate instanceof java.util.Date) {
                                postDate = (java.util.Date) dateCandidate;
                            } else if (dateCandidate instanceof String) {
                                // 如果getDate()意外返回String，尝试解析它
                                SimpleDateFormat bmobSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                postDate = bmobSdf.parse((String) dateCandidate);
                            }
                        } catch (Exception e) {
                             // 捕获从 BmobDate 获取内部 Date 的异常
                            // Log.e("PostDetailFragment", "Error extracting Date from BmobDate", e);
                        }

                        // 如果 getDate() 失败或返回 String 解析失败，可以尝试 BmobDate 的 toString()
                        if (postDate == null) {
                             SimpleDateFormat bmobDateToStringSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                             postDate = bmobDateToStringSdf.parse(createdAt.toString());
                        }

                    } else if (createdAt instanceof String) {
                         // 如果直接就是 String 类型，尝试解析（作为备用）
                         SimpleDateFormat stringSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                         postDate = stringSdf.parse((String) createdAt);
                    }

                } catch (Exception e) {
                    // 捕获获取或解析 Date 对象过程中的异常
                    // Log.e("PostDetailFragment", "Error processing createdAt object", e);
                    postDate = null; // 确保 postDate 为 null on error
                }
            }

            // 现在格式化获取到的 Date 对象
            if (postDate != null) {
                try {
                    // 计算时间差并格式化为 "发布于 ...前"
                    long timeDiffMillis = System.currentTimeMillis() - postDate.getTime();
                    long timeDiffSeconds = timeDiffMillis / 1000;

                    String timeAgoText;

                    if (timeDiffSeconds < 60) {
                        timeAgoText = "发布于 刚刚";
                    } else if (timeDiffSeconds < 3600) {
                        // 小于1小时但大于等于1分钟
                        long minutes = timeDiffSeconds / 60;
                        timeAgoText = "发布于 " + minutes + "分钟前";
                    } else if (timeDiffSeconds < 86400) {
                        // 小于1天但大于等于1小时
                        long hours = timeDiffSeconds / 3600;
                        timeAgoText = "发布于 " + hours + "小时前";
                    } else {                    // 大于等于1天
                        long days = timeDiffSeconds / 86400;
                        long remainingSeconds = timeDiffSeconds % 86400;
                        long hours = remainingSeconds / 3600;

                        if (hours > 0) {
                            timeAgoText = "发布于 " + days + "天" + hours + "小时前";
                        } else {
                            // 不显示0小时
                            timeAgoText = "发布于 " + days + "天前";
                        }
                    }
                    tvPostTimeLocation.setText(timeAgoText);

                } catch (Exception e) {
                    // 捕获时间差计算或格式化过程中的异常
                    // Log.e("PostDetailFragment", "Error calculating time ago", e);
                    timeLocationText = "时间计算错误"; // Fallback error text
                    tvPostTimeLocation.setText(timeLocationText);
                }
            } else {
                 // 如果 postDate 为 null，timeLocationText 会保持默认值 "未知时间"
                 tvPostTimeLocation.setText(timeLocationText);
            }

            // 动态添加分类标签
            chipGroup.removeAllViews(); // 清除现有视图
            String category = post.getCategory();
            if (category != null && !category.isEmpty()) {
                Chip chip = new Chip(getContext());
                chip.setText(category);
                // 根据分类设置不同的样式或图标
                int iconResId = 0;
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
                    // 可以根据需要添加更多分类
                }
                if (iconResId != 0) {
                    chip.setChipIconResource(iconResId);
                    chip.setChipIconVisible(true);
                    chip.setIconStartPadding(8f); // 添加 icon 和文本的间距
                } else {
                    chip.setChipIconVisible(false);
                }

                chip.setChipBackgroundColorResource(R.color.chip_category_selector); // 假设有此颜色资源
                chip.setTextColor(getResources().getColor(R.color.chip_category_text_selector)); // 假设有此颜色资源
                chip.setClickable(false); // 标签不可点击
                chipGroup.addView(chip);
            } else {
                chipGroup.setVisibility(View.GONE); // 没有分类则隐藏 ChipGroup
            }

        } else {
            // 没有获取到 Post 对象，跳转到错误页面
            requireActivity().getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.fragment_container, new ErrorFragment())
                 .commit();
            // 也可以选择关闭当前详情页而不是替换：
            // requireActivity().getSupportFragmentManager().popBackStack();
            // 或者显示错误信息：
            // Toast.makeText(getContext(), "无法加载帖子详情", Toast.LENGTH_SHORT).show();
        }
    }
} 