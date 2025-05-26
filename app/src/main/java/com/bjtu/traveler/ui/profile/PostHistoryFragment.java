package com.bjtu.traveler.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.PostHistoryAdapter;
import com.bjtu.traveler.data.model.Post;
import com.bjtu.traveler.data.repository.PostRepository;

import java.util.List;

public class PostHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_history, container, false);
        recyclerView = view.findViewById(R.id.rv_post_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 获取当前用户的发帖记录
        List<Post> postList = PostRepository.getInstance(getContext()).getPostsByCurrentUser();
        // 如果没有数据，填充一些演示内容
        if (postList == null || postList.isEmpty()) {
            postList = new java.util.ArrayList<>();
            long now = System.currentTimeMillis();
            postList.add(new Post(1, 1, "我的第一篇游记", "今天去了颐和园，风景很好！", now - 86400000, "https://img1.baidu.com/it/u=1234567890,1234567890&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300"));
            postList.add(new Post(2, 1, "美食分享", "推荐一家好吃的火锅店！", now - 43200000, ""));
            postList.add(new Post(3, 1, "夜景随拍", "夜晚的城市灯光很漂亮。", now - 3600000, "https://img1.baidu.com/it/u=987654321,987654321&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=300"));
        }
        adapter = new PostHistoryAdapter(postList);
        recyclerView.setAdapter(adapter);
        return view;
    }
}

