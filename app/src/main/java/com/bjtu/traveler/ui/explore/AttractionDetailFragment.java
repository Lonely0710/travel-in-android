package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bjtu.traveler.R;
import com.bumptech.glide.Glide;
import com.bjtu.traveler.api.WikipediaApi;
import com.bjtu.traveler.data.model.FavoriteAttraction;
import com.bjtu.traveler.data.repository.FavoriteRepository;
import com.bjtu.traveler.data.model.User;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import java.util.List;

public class AttractionDetailFragment extends Fragment {
    public static final String ARG_NAME = "name";
    public static final String ARG_CATEGORY = "category";
    public static final String ARG_DESCRIPTION = "description";
    public static final String ARG_CITY = "city";
    public static final String ARG_COUNTRY = "country";
    public static final String ARG_IMG_URL = "imgUrl";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attraction_detail, container, false);
        TextView tvTitle = view.findViewById(R.id.tv_detail_title);
        TextView tvLocation = view.findViewById(R.id.tv_detail_location);
        TextView tvCategory = view.findViewById(R.id.tv_detail_category);
        TextView tvDescription = view.findViewById(R.id.tv_detail_description);
        ImageView imgThumb = view.findViewById(R.id.img_detail_thumb);
        View btnBack = view.findViewById(R.id.btn_back);
        ImageView ivFavorite = view.findViewById(R.id.iv_favorite);
        ivFavorite.setImageResource(R.drawable.ic_favorite_border); // 默认未收藏

        Bundle args = getArguments();
        String name = args != null ? args.getString(ARG_NAME) : "";
        String category = args != null ? args.getString(ARG_CATEGORY) : "";
        String description = args != null ? args.getString(ARG_DESCRIPTION) : "";
        String city = args != null ? args.getString(ARG_CITY) : "";
        String country = args != null ? args.getString(ARG_COUNTRY) : "";
        String imgUrl = args != null ? args.getString(ARG_IMG_URL) : "";

        tvTitle.setText(name);
        tvCategory.setText(!TextUtils.isEmpty(category) ? category : "");
        StringBuilder locationBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(city)) locationBuilder.append(city);
        if (!TextUtils.isEmpty(country)) {
            if (locationBuilder.length() > 0) locationBuilder.append(", ");
            locationBuilder.append(country);
        }
        tvLocation.setText(locationBuilder.toString());
        // 优先显示传入的description，否则自动查百科
        if (!TextUtils.isEmpty(description) && !"null".equals(description)) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("暂无详细介绍");
            // WikipediaApi.fetchAttractionInfo(name, new WikipediaApi.Callback() {
            //     @Override
            //     public void onSuccess(String desc, String wikiImgUrl) {
            //         tvDescription.setText(!TextUtils.isEmpty(desc) ? desc : "暂无详细介绍");
            //         if (!TextUtils.isEmpty(wikiImgUrl)) {
            //             Glide.with(AttractionDetailFragment.this).load(wikiImgUrl).into(imgThumb);
            //         }
            //     }
            //     @Override
            //     public void onFailure(int errorCode, String errorMsg) {
            //         tvDescription.setText("暂无详细介绍\n(" + errorMsg + ")");
            //     }
            // });
        }
        if (!TextUtils.isEmpty(imgUrl)) {
            Glide.with(this).load(imgUrl).into(imgThumb);
        } else {
            imgThumb.setImageResource(R.drawable.ic_favorite_border);
        }

        // 收藏功能
        User user = BmobUser.getCurrentUser(User.class);
        String userId = user != null ? user.getObjectId() : null;
        final boolean[] isFavorite = {false};
        final String[] favoriteObjectId = {null};
        // 查询是否已收藏
        if (userId != null) {
            FavoriteRepository.queryFavorite(name, userId, new FindListener<FavoriteAttraction>() {
                @Override
                public void done(List<FavoriteAttraction> list, BmobException e) {
                    if (e == null && list != null && !list.isEmpty()) {
                        isFavorite[0] = true;
                        favoriteObjectId[0] = list.get(0).getObjectId();
                        ivFavorite.setImageResource(R.drawable.ic_favorite); // 红色，已收藏
                    } else {
                        isFavorite[0] = false;
                        favoriteObjectId[0] = null;
                        ivFavorite.setImageResource(R.drawable.ic_favorite_border); // 灰色，未收藏
                    }
                }
            });
        } else {
            Toast.makeText(requireContext(), "请先登录后再收藏", Toast.LENGTH_SHORT).show();
        }
        // 点击收藏/取消收藏
        ivFavorite.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(requireContext(), "请先登录后再收藏", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isFavorite[0]) {
                // 添加收藏
                FavoriteAttraction fav = new FavoriteAttraction();
                fav.setName(name);
                fav.setCategory(category);
                fav.setDescription(tvDescription.getText().toString());
                fav.setCity(city);
                fav.setCountry(country);
                fav.setImgUrl(imgUrl);
                fav.setUserId(userId);
                FavoriteRepository.addFavorite(fav, new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        if (e == null) {
                            isFavorite[0] = true;
                            favoriteObjectId[0] = objectId;
                            ivFavorite.setImageResource(R.drawable.ic_favorite); // 红色
                            Toast.makeText(requireContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "收藏失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                // 取消收藏
                if (favoriteObjectId[0] != null) {
                    FavoriteRepository.removeFavorite(favoriteObjectId[0], new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                isFavorite[0] = false;
                                favoriteObjectId[0] = null;
                                ivFavorite.setImageResource(R.drawable.ic_favorite_border); // 灰色
                                Toast.makeText(requireContext(), "已取消收藏", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "取消收藏失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        return view;
    }

    public static AttractionDetailFragment newInstance(String name, String category, String description, String city, String country, String imgUrl) {
        AttractionDetailFragment fragment = new AttractionDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_CATEGORY, category);
        args.putString(ARG_DESCRIPTION, description);
        args.putString(ARG_CITY, city);
        args.putString(ARG_COUNTRY, country);
        args.putString(ARG_IMG_URL, imgUrl);
        fragment.setArguments(args);
        return fragment;
    }
}

