package com.bjtu.traveler.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bjtu.traveler.R; // 确保导入 R 文件以使用资源ID

public class FragmentSwitcher {
    private final FragmentManager fragmentManager;
    private final int containerId;

    // 构造函数，用于创建 FragmentSwitcher 实例（如果需要实例方式切换）
    public FragmentSwitcher(@NonNull FragmentManager fragmentManager, int containerId) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }

    // 使用实例方式切换 Fragment 的方法
    public void switchTo(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerId, fragment);
        transaction.commit();
    }

    /**
     * 切换指定容器中的当前 Fragment，并选中底部导航栏中对应的菜单项。
     * @param activity 包含 Fragment 容器和 BottomNavigationView 的 FragmentActivity。
     * @param fragment 要切换到的目标 Fragment。
     * @param tag 可选的 Fragment 标签。
     * @param navMenuItemId 底部导航栏中要选中的菜单项的 ID (例如 R.id.navigation_explore)。
     */
    public static void switchFragmentAndSelectItem(@NonNull FragmentActivity activity,
                                                 @NonNull Fragment fragment,
                                                 @Nullable String tag,
                                                 int navMenuItemId) {
        // 获取 Activity 的 FragmentManager
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        int containerId = R.id.fragment_container;

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 可以选择添加切换动画
        // transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);

        // 替换容器中的 Fragment，并添加到返回栈
        transaction.replace(containerId, fragment, tag);
        transaction.addToBackStack(null); // 添加到返回栈，允许用户按返回键回到上一个 Fragment
        transaction.commit(); // 提交事务

        // 更新底部导航栏的选中状态
        BottomNavigationView bottomNavView = activity.findViewById(R.id.bottom_navigation); // 假设 BottomNavigationView 的ID为 R.id.nav_view
        if (bottomNavView != null) {
            bottomNavView.setSelectedItemId(navMenuItemId); // 设置底部导航栏的选中项
        }
    }
} 