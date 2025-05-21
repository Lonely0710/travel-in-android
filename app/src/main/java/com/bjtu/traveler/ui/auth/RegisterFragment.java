package com.bjtu.traveler.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bjtu.traveler.R;
import com.bjtu.traveler.viewmodel.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {

    private OnAuthFragmentInteractionListener listener;
    private UserViewModel userViewModel;
    private TextInputEditText etEmail, etPhone, etFullName, etPassword, etConfirmPassword;
    private MaterialButton btnCreateAccount;

    // 定义一个接口，用于Fragment与Activity通信
    public interface OnAuthFragmentInteractionListener {
        void onRequestSwitchToLogin();
        // 可以添加其他交互方法
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
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // 初始化ViewModel
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // 初始化视图
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etFullName = view.findViewById(R.id.et_full_name);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnCreateAccount = view.findViewById(R.id.btn_create_account);
        TextView signInTextView = view.findViewById(R.id.tv_go_to_login);

        // 登录链接点击事件
        signInTextView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRequestSwitchToLogin();
            }
        });

        // 注册按钮点击事件
        btnCreateAccount.setOnClickListener(v -> registerUser());

        // 观察注册结果
        userViewModel.getRegisterResult().observe(getViewLifecycleOwner(), result -> {
            btnCreateAccount.setEnabled(true);
            if (result != null) {
                if (result.isSuccess()) {
                    // 注册成功，自动跳转到登录页面
                    Toast.makeText(requireContext(), "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onRequestSwitchToLogin();
                    }
                } else {
                    // 注册失败，显示错误信息
                    Toast.makeText(requireContext(), "注册失败: " + result.getErrorMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
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

    /**
     * 执行注册操作
     */
    private void registerUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // 输入验证
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("邮箱不能为空");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("邮箱格式不正确");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("手机号不能为空");
            return;
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            etPhone.setError("手机号格式不正确");
            return;
        }
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("用户名不能为空");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("密码不能为空");
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("密码至少8位");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("确认密码不能为空");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次输入的密码不一致");
            return;
        }
        // 禁用注册按钮，防止重复点击
        btnCreateAccount.setEnabled(false);
        // 调用ViewModel执行注册
        userViewModel.register(email, phone, fullName, password);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
} 