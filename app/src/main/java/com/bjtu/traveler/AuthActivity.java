package com.bjtu.traveler;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import com.bjtu.traveler.ui.auth.LoginFragment;
import com.bjtu.traveler.ui.auth.RegisterFragment;

public class AuthActivity extends BaseActivity implements 
        LoginFragment.OnAuthFragmentInteractionListener, 
        RegisterFragment.OnAuthFragmentInteractionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // 在AuthActivity中，我们需要显示系统栏方便用户输入
        showSystemBars();

        // 默认加载LoginFragment
        if (savedInstanceState == null) {
            switchFragment(R.id.auth_fragment_container, new LoginFragment(), false);
        }
    }

    // 实现LoginFragment中的接口方法，用于切换到RegisterFragment
    @Override
    public void onRequestSwitchToRegister() {
        switchFragment(R.id.auth_fragment_container, new RegisterFragment(), true);
    }

    // 实现RegisterFragment中的接口方法，用于切换回LoginFragment
    @Override
    public void onRequestSwitchToLogin() {
        getSupportFragmentManager().popBackStack(); // 从返回栈弹出RegisterFragment
    }

} 