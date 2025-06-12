package com.bjtu.traveler.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.data.repository.UserRepository;
import com.bjtu.traveler.viewmodel.ProfileViewModel;

public class EditFragment extends Fragment {
    private EditText etName, etEmail, etPhone;
    private ImageView ivAvatar;
    private Button btnSave;
    private UserRepository userRepository;
    private User currentUser;
    private ProfileViewModel profileViewModel;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedAvatarUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        etName = view.findViewById(R.id.et_edit_name);
        etEmail = view.findViewById(R.id.et_edit_email);
        etPhone = view.findViewById(R.id.et_edit_phone);
        ivAvatar = view.findViewById(R.id.iv_avatar_edit);
        btnSave = view.findViewById(R.id.btn_save_edit);
        userRepository = UserRepository.getInstance();
        currentUser = userRepository.getCurrentUser();
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        if (currentUser != null) {
            etName.setText(currentUser.getUsername());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getMobilePhoneNumber());
            // 头像加载可用 Glide/Picasso，这里简单省略
        }
        btnSave.setOnClickListener(v -> saveEdit());
        ivAvatar.setOnClickListener(v -> openSystemGallery());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            ivAvatar.setImageURI(imageUri);
                            selectedAvatarUri = imageUri;
                            // 这里可以进一步上传头像到服务器，或调用ViewModel方法
                        }
                    }
                }
        );
    }

    private void saveEdit() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        if (selectedAvatarUri != null) {
            // 先上传头像
            profileViewModel.uploadAvatar(requireContext(), selectedAvatarUri, url -> {
                if (url != null) {
                    profileViewModel.updateUserProfile(newName, newEmail, newPhone, url, success -> {
                        if (success) {
                            Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                            requireActivity()
                                    .getOnBackPressedDispatcher()
                                    .onBackPressed();
                        } else {
                            Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "头像上传失败", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 不修改头像
            profileViewModel.updateUserProfile(newName, newEmail, newPhone, null, success -> {
                if (success) {
                    Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                    requireActivity()
                            .getOnBackPressedDispatcher()
                            .onBackPressed();
                } else {
                    Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openSystemGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
}