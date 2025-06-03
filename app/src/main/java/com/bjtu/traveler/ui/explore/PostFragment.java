package com.bjtu.traveler.ui.explore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bjtu.traveler.R;
import com.bjtu.traveler.data.repository.PostRepository;
import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.data.model.Post;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.IOException;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class PostFragment extends Fragment {
    private static final int REQUEST_IMAGE_PICK = 1001;
    private ImageView ivSelectedImage;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        EditText etTitle = view.findViewById(R.id.et_post_title);
        EditText etContent = view.findViewById(R.id.et_post_content);
        Button btnSubmit = view.findViewById(R.id.btn_submit_post);

        // 新的图片选择回调
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                            ivSelectedImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            ivSelectedImage.setVisibility(View.GONE);
                        }
                    }
                }
            }
        );

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(getContext(), "标题和内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            // 数据库插入
            Context ctx = requireContext().getApplicationContext();
            PostRepository repo = PostRepository.getInstance(ctx);
            long userId = User.getCurrentUserId();
            long timestamp = System.currentTimeMillis();
            String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";
            Post post = new Post(0, userId, title, content, timestamp, imageUrl);
            repo.insertPost(post);
            Toast.makeText(getContext(), "发布成功", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new DiscoverFragment())
                .commit();
        });

        // 新增：返回按钮点击事件
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 新增：标记地点行点击事件
        LinearLayout layoutMarkLocation = view.findViewById(R.id.layout_location);
        layoutMarkLocation.setOnClickListener(v -> {
            Toast.makeText(getContext(), "标记地点", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
