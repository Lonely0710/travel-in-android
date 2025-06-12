// app/src/main/java/com/bjtu/traveler/viewmodel/ProfileViewModel.java
package com.bjtu.traveler.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;
import com.bjtu.traveler.data.model.Post;
import com.bjtu.traveler.data.model.User;
import com.bjtu.traveler.data.repository.UserRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UploadFileListener;
import android.database.Cursor;
import android.provider.MediaStore;

public class ProfileViewModel extends AndroidViewModel {
    public final UserRepository userRepository;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
    }

    // 直接返回当前用户对象
    public User getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    // 加载当前用户的帖子列表
    public void loadUserPosts(Observer<List<Post>> observer) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            observer.onChanged(new ArrayList<>());
            return;
        }
        // 假设有 PostRepository，实际可直接用 Bmob 查询
        cn.bmob.v3.BmobQuery<Post> query = new cn.bmob.v3.BmobQuery<>();
        query.addWhereEqualTo("userId", currentUser);
        query.order("-createdAt");
        query.findObjects(new FindListener<Post>() {
            @Override
            public void done(List<Post> list, BmobException e) {
                if (e == null && list != null) {
                    observer.onChanged(list);
                } else {
                    observer.onChanged(new ArrayList<>());
                }
            }
        });
    }

    // 更新用户信息（用户名、邮箱、手机号）
    public void updateUserProfile(String username, String email, String phone, final Observer<Boolean> observer) {
        User user = getCurrentUser();
        if (user == null) {
            observer.onChanged(false);
            return;
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setMobilePhoneNumber(phone);
        user.update(new cn.bmob.v3.listener.UpdateListener() {
            @Override
            public void done(BmobException e) {
                observer.onChanged(e == null);
            }
        });
    }

    // 通过Uri获取文件真实路径（兼容本地图片选择）
    private String getPathFromUri(Context context, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                String path = cursor.getString(column_index);
                cursor.close();
                return path;
            }
            cursor.close();
        }
        return null;
    }

    // 上传头像，成功后返回url
    public void uploadAvatar(Context context, Uri imageUri, Observer<String> observer) {
        String filePath = getPathFromUri(context, imageUri);
        if (filePath == null) {
            observer.onChanged(null);
            return;
        }
        BmobFile bmobFile = new BmobFile(new File(filePath));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    observer.onChanged(bmobFile.getFileUrl());
                } else {
                    observer.onChanged(null);
                }
            }
        });
    }

    // 更新用户信息（含头像）
    public void updateUserProfile(String username, String email, String phone, String avatarUrl, final Observer<Boolean> observer) {
        User user = getCurrentUser();
        if (user == null) {
            observer.onChanged(false);
            return;
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setMobilePhoneNumber(phone);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        user.update(new cn.bmob.v3.listener.UpdateListener() {
            @Override
            public void done(cn.bmob.v3.exception.BmobException e) {
                observer.onChanged(e == null);
            }
        });
    }
}