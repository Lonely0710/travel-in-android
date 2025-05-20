package com.bjtu.traveler.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bjtu.traveler.R;
import com.bjtu.traveler.viewmodel.HomeViewModel;
import com.bjtu.traveler.viewmodel.UserViewModel;
import com.bumptech.glide.Glide;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ImageView ivAvatar = view.findViewById(R.id.iv_avatar);
        TextView tvHiUser = view.findViewById(R.id.tv_hi_user);
        TextView tvPoints = view.findViewById(R.id.tv_points);
        EditText etSearch = view.findViewById(R.id.et_search);

        // 初始化ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // 监听用户信息
        homeViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                String username = user.getUsername();
                String avatarUrl = user.getAvatarUrl();
                tvHiUser.setText(!TextUtils.isEmpty(username) ? "Hi, " + username : "Hi, 游客");
                // 加载头像
                if (!TextUtils.isEmpty(avatarUrl)) {
                    Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_avatar).into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_avatar);
                }
            } else {
                tvHiUser.setText("Hi, 游客");
                ivAvatar.setImageResource(R.drawable.ic_avatar);
            }
        });


        return view;
    }
} 