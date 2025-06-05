package com.bjtu.traveler.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bjtu.traveler.R;

public class ErrorFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_error, container, false);

        ImageView ivBack = view.findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> {
            // 返回上一个Fragment，通常是跳转到错误页之前的页面
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
} 