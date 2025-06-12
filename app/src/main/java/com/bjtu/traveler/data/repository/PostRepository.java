package com.bjtu.traveler.data.repository;

import android.content.Context;

import com.bjtu.traveler.data.model.Post;
import com.bjtu.traveler.data.model.User;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class PostRepository {
    private static PostRepository instance;

    public static synchronized PostRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PostRepository();
        }
        return instance;
    }

    private PostRepository() {
        // Bmob初始化建议在Application中完成，这里无需处理
    }

    // 查询当前用户的帖子（异步，回调返回）
    public void getPostsByCurrentUser(String userObjectId, FindListener<Post> listener) {
        BmobQuery<Post> query = new BmobQuery<>();
        query.addWhereEqualTo("userId", userObjectId);
        query.findObjects(listener);
    }

    // 插入帖子（异步，回调返回）
    public void insertPost(Post post, SaveListener<String> listener) {
        post.save(listener);
    }

    // 可根据需要添加更多方法，如删除、更新等，均用Bmob API实现
}
