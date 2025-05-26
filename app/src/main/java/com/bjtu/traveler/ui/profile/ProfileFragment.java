// app/src/main/java/com/bjtu/traveler/ui/profile/ProfileFragment.java
package com.bjtu.traveler.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.ui.auth.LoginFragment;
import com.bjtu.traveler.viewmodel.ProfileViewModel;
import com.bumptech.glide.Glide;
import com.bjtu.traveler.utils.FragmentSwitcher;

public class ProfileFragment extends Fragment {
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        View bg = view.findViewById(R.id.profile_bg);

        bg.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        ImageView ivAvatar = view.findViewById(R.id.iv_profile_avatar);
        TextView tvUsername = view.findViewById(R.id.tv_profile_username);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        User user = profileViewModel.getCurrentUser();

        if (user != null) {
            tvUsername.setText(!TextUtils.isEmpty(user.getUsername()) ? user.getUsername() : "游客");
            if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                Glide.with(this).load(user.getAvatarUrl()).placeholder(R.drawable.ic_avatar).into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_avatar);
            }
        } else {
            tvUsername.setText("游客");
            ivAvatar.setImageResource(R.drawable.ic_avatar);
        }

        // 处理菜单点击
        FragmentSwitcher switcher = new FragmentSwitcher(requireActivity().getSupportFragmentManager(), R.id.fragment_container);

        view.findViewById(R.id.tv_edit_profile).setOnClickListener(v -> {
            // 跳转到编辑个人信息 Fragment
            switcher.switchTo(new EditFragment());
        });

        view.findViewById(R.id.tv_favorite).setOnClickListener(v -> {
            // 跳转到收藏列表 Fragment
            switcher.switchTo(new FavoriteFragment());
        });

        view.findViewById(R.id.tv_post_history).setOnClickListener(v -> {
            // 跳转到发帖记录 Fragment
            switcher.switchTo(new PostHistoryFragment());
        });

        view.findViewById(R.id.tv_logout).setOnClickListener(v -> {
            // 退出登录逻辑
            profileViewModel.getApplication().getApplicationContext()
                    .getSharedPreferences("user", 0).edit().clear().apply();
            profileViewModel.userRepository.logout();
            // 跳转到登录 Fragment
            switcher.switchTo(new LoginFragment());
        });

        // 点击空白处关闭侧边栏
        view.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }
}

