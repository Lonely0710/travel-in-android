package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.PostHistoryAdapter;
import com.bjtu.traveler.data.model.Post;
import java.util.List;

public class DiscoverFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        recyclerView = view.findViewById(R.id.rv_post_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 这里直接填充演示内容
        List<Post> postList = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();
        postList.add(new Post(1, 1, "发现页-游记", "今天发现了一个新景点，值得一去！", now - 86400000, "https://img1.baidu.com/it/u=1234567890,1234567890&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300"));
        postList.add(new Post(2, 1, "发现页-美食", "发现了一家好吃的餐厅！", now - 43200000, ""));
        postList.add(new Post(3, 1, "发现页-夜景", "夜晚的灯光很美。", now - 3600000, "https://img1.baidu.com/it/u=987654321,987654321&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300"));
        adapter = new PostHistoryAdapter(postList);
        recyclerView.setAdapter(adapter);
        // 新增：我要发帖按钮跳转
        ImageButton btnPost = view.findViewById(R.id.btn_post);
        btnPost.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new PostFragment())
                .addToBackStack(null)
                .commit();
        });
        return view;
    }
}
