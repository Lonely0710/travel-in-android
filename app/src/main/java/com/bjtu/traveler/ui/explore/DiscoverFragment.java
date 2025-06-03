package com.bjtu.traveler.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bjtu.traveler.R;
import com.bjtu.traveler.adapter.PostAdapter;
import com.bjtu.traveler.data.model.Post;
import com.bjtu.traveler.viewmodel.ExploreViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 发现页面 Fragment，展示帖子列表（瀑布流布局）
 */
public class DiscoverFragment extends Fragment {
    private ExploreViewModel exploreViewModel;
    private RecyclerView rvPosts;
    private PostAdapter postAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        exploreViewModel = new ViewModelProvider(requireActivity()).get(ExploreViewModel.class);

        rvPosts = view.findViewById(R.id.rv_post_history);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvPosts.setLayoutManager(layoutManager);

        postAdapter = new PostAdapter(new ArrayList<>());
        rvPosts.setAdapter(postAdapter);

        exploreViewModel.getPostList().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.updatePostList(posts);
        });

        exploreViewModel.getPostErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        exploreViewModel.loadPosts();
    }
}
