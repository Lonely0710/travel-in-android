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
import com.bjtu.traveler.ui.explore.AttractionDetailFragment;
import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_collect_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: 替换为实际收藏数据
        List<Attraction> favoriteList = new ArrayList<>();
        favoriteList.add(new Attraction(1, "克林齐火山", 0, 0, "Hiking", "火山简介", "占碑省", "占碑", "苏门答腊", null));
        favoriteList.add(new Attraction(2, "布罗莫火山", 0, 0, "Hiking", "布罗莫简介", "东爪哇", "泗水", "印尼", null));
        AttractionAdapter adapter = new AttractionAdapter(favoriteList, R.layout.item_vertical_attraction);
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
        recyclerView.setAdapter(adapter);
        return view;
    }
}

