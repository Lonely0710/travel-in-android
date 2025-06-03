package com.bjtu.traveler.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.viewmodel.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private static final String ACG_AVATAR_API = "https://acg.toubiec.cn/";
    private static final OkHttpClient httpClient = new OkHttpClient();

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
                    // 注册成功
                    Toast.makeText(requireContext(), "注册成功，正在获取头像...", Toast.LENGTH_SHORT).show();
                    // 获取当前注册成功的用户对象
                    User registeredUser = BmobUser.getCurrentUser(User.class);
                    if (registeredUser != null) {
                        // 为注册成功的用户获取并设置头像
                        fetchAndSetAvatarForUser(registeredUser);
                    } else {
                         // 如果获取不到当前用户，直接跳转到登录页面
                         Log.e(TAG, "注册成功后无法获取当前用户对象");
                         Toast.makeText(requireContext(), "注册成功，请登录", Toast.LENGTH_SHORT).show();
                         if (listener != null) {
                             listener.onRequestSwitchToLogin();
                         }
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

    /**
     * 获取头像URL并更新用户对象
     * @param user 注册成功的用户对象
     */
    private void fetchAndSetAvatarForUser(User user) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String avatarUrl = null;
                Request request = new Request.Builder().url(ACG_AVATAR_API).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body().string();
                        // 简单判断返回的是否为图片URL
                        if (result.startsWith("http") && (result.endsWith(".jpg") || result.endsWith(".png") || result.endsWith(".jpeg") || result.endsWith(".webp"))) {
                            avatarUrl = result;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "从ACG接口获取头像发生网络错误: " + e.getMessage());
                }
                // 兜底：如果没获取到头像URL，使用默认头像
                if (avatarUrl == null) {
                    avatarUrl = "https://acg.toubiec.cn/random_avatar.png"; // 或者用本地默认头像
                }
                String finalAvatarUrl = avatarUrl;
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        user.setAvatarUrl(finalAvatarUrl);
                        user.update(user.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Log.i(TAG, "用户头像更新成功");
                                    Toast.makeText(requireContext(), "注册成功，请登录", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "用户头像更新失败: " + e.getMessage());
                                    Toast.makeText(requireContext(), "注册成功但头像设置失败，请登录", Toast.LENGTH_SHORT).show();
                                }
                                if (listener != null) {
                                    listener.onRequestSwitchToLogin();
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
} 