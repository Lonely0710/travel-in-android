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
import com.bjtu.traveler.adapter.AttractionAdapter;
import com.bjtu.traveler.data.model.Attraction;
import com.bjtu.traveler.data.model.FavoriteAttraction;
import com.bjtu.traveler.data.repository.FavoriteRepository;
import com.bjtu.traveler.ui.explore.AttractionDetailFragment;
import java.util.ArrayList;
import java.util.List;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import com.bjtu.traveler.data.model.User;

public class FavoriteFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_collect_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 查询当前用户所有收藏
        User user = BmobUser.getCurrentUser(User.class);
        String userId = user != null ? user.getObjectId() : null;
        List<Attraction> favoriteList = new ArrayList<>();
        AttractionAdapter adapter = new AttractionAdapter(favoriteList, R.layout.item_vertical_attraction);
        recyclerView.setAdapter(adapter);
        if (userId != null) {
            FavoriteRepository.queryAllFavorites(userId, new FindListener<FavoriteAttraction>() {
                @Override
                public void done(List<FavoriteAttraction> list, cn.bmob.v3.exception.BmobException e) {
                    if (e == null && list != null) {
                        favoriteList.clear();
                        for (FavoriteAttraction fav : list) {
                            Attraction attraction = new Attraction(
                                    0,
                                    fav.getName(),
                                    0,
                                    0,
                                    fav.getCategory(),
                                    fav.getDescription(),
                                    "",
                                    fav.getCity(),
                                    fav.getCountry(),
                                    fav.getImgUrl()
                            );
                            favoriteList.add(attraction);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
        adapter.setOnAttractionClickListener((attraction, imgUrl, city, country) -> {
            AttractionDetailFragment fragment = AttractionDetailFragment.newInstance(
                    attraction.getName(),
                    attraction.getCategory(),
                    attraction.getDescription(),
                    city,
                    country,
                    imgUrl
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        return view;
    }
}
