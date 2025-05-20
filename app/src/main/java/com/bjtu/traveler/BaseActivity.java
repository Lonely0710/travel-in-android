package com.bjtu.traveler;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class BaseActivity extends AppCompatActivity {
    private WindowInsetsControllerCompat insetsController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        
        // 设置输入法和系统栏的行为
        insetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                
        // 默认隐藏状态栏和导航栏，但在输入时应该显示
        if (!(this instanceof AuthActivity)) {
            hideSystemBars();
        }
        
        // 监听手势滑动显示/隐藏
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // 下滑显示状态栏
                insetsController.show(WindowInsetsCompat.Type.statusBars());
            } else {
                insetsController.hide(WindowInsetsCompat.Type.statusBars());
            }
            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                // 上滑显示底部导航栏
                insetsController.show(WindowInsetsCompat.Type.navigationBars());
            } else {
                insetsController.hide(WindowInsetsCompat.Type.navigationBars());
            }
        });
    }

    // 显示系统栏
    protected void showSystemBars() {
        insetsController.show(WindowInsetsCompat.Type.systemBars());
    }

    // 隐藏系统栏
    protected void hideSystemBars() {
        insetsController.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE);
    }

    // 显示键盘
    protected void showKeyboard(View view) {
        if (view != null && view.requestFocus()) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager)
                    getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // 隐藏键盘
    protected void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager)
                    getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void switchFragment(int containerId, Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(containerId, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
} 