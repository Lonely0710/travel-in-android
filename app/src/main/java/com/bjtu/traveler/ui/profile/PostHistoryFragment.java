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
import com.bjtu.traveler.viewmodel.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class PostHistoryFragment extends Fragment {
    private RecyclerView rvPostHistory;
    private PostHistoryAdapter adapter;
    private List<Post> postList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_history, container, false);
        rvPostHistory = view.findViewById(R.id.rv_post_history);
        rvPostHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostHistoryAdapter(postList);
        rvPostHistory.setAdapter(adapter);
        // 通过 ViewModel 加载数据
        loadUserPostsByViewModel();
        return view;
    }

    private void loadUserPostsByViewModel() {
        ProfileViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.loadUserPosts(new androidx.lifecycle.Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> list) {
                postList.clear();
                if (list != null) postList.addAll(list);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
