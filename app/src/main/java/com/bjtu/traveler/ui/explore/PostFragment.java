package com.bjtu.traveler.ui.explore;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bjtu.traveler.R;
import com.bjtu.traveler.viewmodel.ExploreViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.InputStream;

public class PostFragment extends Fragment {
    private ImageView ivSelectedImage;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private EditText etLocationText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        EditText etTitle = view.findViewById(R.id.et_post_title);
        EditText etContent = view.findViewById(R.id.et_post_content);
        Button btnSubmit = view.findViewById(R.id.btn_submit_post);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        FrameLayout imageContainer = view.findViewById(R.id.image_container);
        etLocationText = view.findViewById(R.id.et_location_text);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_categories);

        ivSelectedImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {
                            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                            ivSelectedImage.setImageBitmap(bitmap);
                            int maxSize = dp2px(240);
                            int w = bitmap.getWidth();
                            int h = bitmap.getHeight();
                            float scale = Math.min(1f, Math.min((float) maxSize / w, (float) maxSize / h));
                            int newW = (int) (w * scale);
                            int newH = (int) (h * scale);
                            FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(newW, newH);
                            imgParams.gravity = android.view.Gravity.CENTER;
                            ivSelectedImage.setLayoutParams(imgParams);
                            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(newW, newH);
                            containerParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
                            imageContainer.setLayoutParams(containerParams);
                        } catch (Exception e) {
                            ivSelectedImage.setImageResource(R.drawable.ic_add);
                        }
                    }
                }
        );

        ivSelectedImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 点击箭头展开地点输入框（ImageView）
        ImageView icArrowRight = view.findViewById(R.id.ic_arrow_right);
        icArrowRight.setOnClickListener(v -> {
            if (etLocationText.getVisibility() == View.GONE) {
                etLocationText.setVisibility(View.VISIBLE);
                etLocationText.requestFocus();
            }
        });

        // 默认选中第一个Chip（如有需要）
        if (chipGroup.getCheckedChipId() == View.NO_ID && chipGroup.getChildCount() > 0) {
            Chip firstChip = (Chip) chipGroup.getChildAt(0);
            firstChip.setChecked(true);
        }

        btnSubmit.setOnClickListener(v -> {
            imageContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (selectedImageUri == null) {
                        int defaultSize = dp2px(96);
                        FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(defaultSize, defaultSize);
                        imgParams.gravity = android.view.Gravity.CENTER;
                        ivSelectedImage.setLayoutParams(imgParams);
                        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(defaultSize, defaultSize);
                        containerParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
                        imageContainer.setLayoutParams(containerParams);
                    }
                    imageContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });

            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "标题和内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            com.bjtu.traveler.data.model.Post post = new com.bjtu.traveler.data.model.Post();
            post.setTittle(title);
            post.setArticle(content);
            post.setImgUrl(selectedImageUri != null ? selectedImageUri.toString() : "");
            post.setLocation(etLocationText.getText().toString().trim());
            // 获取选中的分类
            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID) {
                Chip checkedChip = chipGroup.findViewById(checkedChipId);
                if (checkedChip != null) {
                    post.setCategory(checkedChip.getText().toString());
                } else {
                    post.setCategory("");
                }
            } else {
                post.setCategory("");
            }
            ExploreViewModel viewModel = new ViewModelProvider(requireActivity()).get(ExploreViewModel.class);
            viewModel.insertPost(requireContext(), post,
                    () -> requireActivity().getSupportFragmentManager().popBackStack(),
                    () -> Toast.makeText(requireContext(), "发布失败", Toast.LENGTH_SHORT).show()
            );
        });

        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
