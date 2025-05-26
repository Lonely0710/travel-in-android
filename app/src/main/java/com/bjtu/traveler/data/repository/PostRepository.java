package com.bjtu.traveler.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bjtu.traveler.data.model.Post;
import com.bjtu.traveler.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class PostRepository extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "post.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "post";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_IMAGE_URL = "image_url";

    private static PostRepository instance;

    public static synchronized PostRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PostRepository(context.getApplicationContext());
        }
        return instance;
    }

    private PostRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_CONTENT + " TEXT, "
                + COLUMN_TIMESTAMP + " INTEGER, "
                + COLUMN_IMAGE_URL + " TEXT"
                + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<Post> getPostsByCurrentUser() {
        // 这里假设有一个User类和获取当前用户ID的方法
        long userId = User.getCurrentUserId();
        List<Post> posts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, COLUMN_TIMESTAMP + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
                posts.add(new Post(id, userId, title, content, timestamp, imageUrl));
            }
            cursor.close();
        }
        return posts;
    }

    public long insertPost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, post.getUserId());
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_CONTENT, post.getContent());
        values.put(COLUMN_TIMESTAMP, post.getTimestamp());
        values.put(COLUMN_IMAGE_URL, post.getImageUrl());
        return db.insert(TABLE_NAME, null, values);
    }

    // 可根据需要添加更多方法，如删除、更新等
}

