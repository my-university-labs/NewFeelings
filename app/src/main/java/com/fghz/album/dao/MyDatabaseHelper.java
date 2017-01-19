package com.fghz.album.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by me on 17-1-3.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_ALBUM_PHOTOS = "create table AlbumPhotos ("
            + "id integer primary key autoincrement, "
            + "album_name text, "
            + "url text)";
    private static final String CREATE_ALBUM = "create table Album ("
            + "id integer primary key autoincrement, "
            + "album_name text, "
            + "show_image text)";
    private static final String CREATE_TF_INFORMATION = "create table TFInformation ("
            + "id integer primary key autoincrement, "
            + "url text, "
            + "tf_type text, "
            + "confidence text)";
    private static final String CREATE_SETTINGS = "create table Settings ("
            + "id integer primary key autoincrement, "
            + "notFirstIn text, "
            + "updateTime integer)";
    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory
            factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_ALBUM_PHOTOS);
            db.execSQL(CREATE_ALBUM);
            db.execSQL(CREATE_TF_INFORMATION);
            db.execSQL(CREATE_SETTINGS);
            Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(mContext, "Error", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table if exists AlbumPhotos");
            db.execSQL("drop table if exists Album");
            db.execSQL("drop table if exists TFInformation");
            db.execSQL("drop table if exists Settings");
            onCreate(db);
        } catch (Exception e) {
            ;
        }
    }
}
