package com.bjtu.traveler.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bjtu.traveler.MainActivity;
import com.bjtu.traveler.R;
import com.bjtu.traveler.viewmodel.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private OnAuthFragmentInteractionListener listener;
    private UserViewModel userViewModel;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;

    // 定义一个接口，用于Fragment与Activity通信
    public interface OnAuthFragmentInteractionListener {
        void onRequestSwitchToRegister();
        // 可以添加其他交互方法，例如登录成功等
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof OnAuthFragmentInteractionListener) {
            listener = (OnAuthFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAuthFragmentInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // 初始化ViewModel
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // 初始化视图
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnSignIn = view.findViewById(R.id.btn_sign_in);
        MaterialButton btnSignUp = view.findViewById(R.id.btn_sign_up);

        // 注册按钮点击事件
        btnSignUp.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestSwitchToRegister();
            }
        });

        // 登录按钮点击事件
        btnSignIn.setOnClickListener(v -> loginUser());

        // 观察登录结果
        userViewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            btnSignIn.setEnabled(true);
            if (result != null) {
                if (result.isSuccess()) {
                    // 登录成功，跳转到MainActivity
                    Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(requireActivity(), MainActivity.class));
                    requireActivity().finish(); // 结束当前Activity
                } else {
                    // 登录失败，显示错误信息
                    Toast.makeText(requireContext(), "登录失败: " + result.getErrorMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    /**
     * 执行登录操作
     */
    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // 输入验证
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("邮箱不能为空");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("邮箱格式不正确");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("密码不能为空");
            return;
        }
        // 禁用登录按钮，防止重复点击
        btnSignIn.setEnabled(false);
        // 调用ViewModel执行登录
        userViewModel.login(email, password);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 使邮箱输入框获取焦点并显示键盘
        if (etEmail != null) {
            etEmail.requestFocus();
            showKeyboard(etEmail);
        }
    }
    
    /**
     * 显示软键盘
     */
    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager)
                    requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }
} 