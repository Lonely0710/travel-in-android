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
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.bjtu.traveler.R;
import com.bjtu.traveler.ui.profile.ProfileFragment;
import com.bjtu.traveler.utils.FragmentSwitcher;
import com.bjtu.traveler.viewmodel.HomeViewModel;
import com.bjtu.traveler.viewmodel.UserViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        View topBar = root.findViewById(R.id.topbar_home);
        if (topBar != null) {
            TextView tvTitle = topBar.findViewById(R.id.tv_title);
            if (tvTitle != null) {
                tvTitle.setText("Home");
            }
        }

        ImageView ivAvatar = root.findViewById(R.id.iv_avatar);
        TextView tvHiUser = root.findViewById(R.id.tv_hi_user);
        TextView tvPoints = root.findViewById(R.id.tv_points);
        EditText etSearch = root.findViewById(R.id.et_search);

        // 获取侧边栏布局
        DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);

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

        // HomeFragment.java 头像点击事件
        ivAvatar.setOnClickListener(v -> {
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            Fragment fragment = fm.findFragmentByTag("ProfileFragment");
            if (fragment == null || !fragment.isAdded()) {
                fm.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, 0, 0, R.anim.slide_out_left)
                        .add(R.id.fragment_container, new ProfileFragment(), "ProfileFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return root;
    }
} 