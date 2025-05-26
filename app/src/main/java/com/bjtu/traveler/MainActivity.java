package com.bjtu.traveler;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bjtu.traveler.ui.home.HomeFragment;
import com.bjtu.traveler.ui.explore.ExploreFragment;
import com.bjtu.traveler.ui.routes.ChatFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        BottomNavigationView.OnItemSelectedListener switchListener = item -> {
            Fragment selected = null;
            if (item.getItemId() == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_explore) {
                selected = new ExploreFragment();
            } else if (item.getItemId() == R.id.nav_routes) {
                selected = new ChatFragment();
            }
            if (selected != null) {
                switchFragment(R.id.fragment_container, selected, false);
            }
            return true;
        };
        bottomNav.setOnItemSelectedListener(switchListener);
        bottomNav.setOnItemReselectedListener(item -> switchListener.onNavigationItemSelected(item));
        // 默认显示首页
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
} 