package com.bjtu.traveler.data.repository;

import com.bjtu.traveler.data.model.FavoriteAttraction;
import java.util.List;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class FavoriteRepository {
    // 添加收藏
    public static void addFavorite(FavoriteAttraction fav, SaveListener<String> listener) {
        fav.save(listener);
    }

    // 取消收藏
    public static void removeFavorite(String objectId, UpdateListener listener) {
        FavoriteAttraction fav = new FavoriteAttraction();
        fav.setObjectId(objectId);
        fav.delete(listener);
    }

    // 查询当前用户是否已收藏该景点
    public static void queryFavorite(String name, String userId, FindListener<FavoriteAttraction> listener) {
        BmobQuery<FavoriteAttraction> query = new BmobQuery<>();
        query.addWhereEqualTo("name", name);
        query.addWhereEqualTo("userId", userId);
        query.findObjects(listener);
    }

    // 查询当前用户所有收藏
    public static void queryAllFavorites(String userId, FindListener<FavoriteAttraction> listener) {
        BmobQuery<FavoriteAttraction> query = new BmobQuery<>();
        query.addWhereEqualTo("userId", userId);
        query.order("-createdAt");
        query.findObjects(listener);
    }
}

