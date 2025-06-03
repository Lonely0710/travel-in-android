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
    private static final String COLUMN_TITTLE = "tittle";
    private static final String COLUMN_ARTICLE = "article";
    private static final String COLUMN_IMG_URL = "img_url";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_AUTHOR_ID = "author_id";

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
                + COLUMN_TITTLE + " TEXT, "
                + COLUMN_ARTICLE + " TEXT, "
                + COLUMN_IMG_URL + " TEXT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_AUTHOR_ID + " TEXT"
                + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 查询当前用户的帖子
    public List<Post> getPostsByCurrentUser(String userObjectId) {
        List<Post> posts = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_AUTHOR_ID + "=?", new String[]{userObjectId}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Post post = new Post();
                post.setTittle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITTLE)));
                post.setArticle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTICLE)));
                post.setImgUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMG_URL)));
                post.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                post.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                // author 只存objectId，如需完整User对象可扩展
                User author = new User();
                author.setObjectId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR_ID)));
                post.setAuthor(author);
                posts.add(post);
            }
            cursor.close();
        }
        return posts;
    }

    // 插入帖子
    public long insertPost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITTLE, post.getTittle());
        values.put(COLUMN_ARTICLE, post.getArticle());
        values.put(COLUMN_IMG_URL, post.getImgUrl());
        values.put(COLUMN_CATEGORY, post.getCategory());
        values.put(COLUMN_LOCATION, post.getLocation());
        if (post.getAuthor() != null) {
            values.put(COLUMN_AUTHOR_ID, post.getAuthor().getObjectId());
        } else {
            values.put(COLUMN_AUTHOR_ID, "");
        }
        return db.insert(TABLE_NAME, null, values);
    }

    // 可根据需要添加更多方法，如删除、更新等
}

