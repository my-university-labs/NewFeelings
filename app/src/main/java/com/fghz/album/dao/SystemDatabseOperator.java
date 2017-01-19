package com.fghz.album.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dongchangzhang on 1/19/17.
 */

public class SystemDatabseOperator {
    public static List<Map> getExternalImageInfo(Context ctx) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return getMediaImageInfo(ctx, uri, null);
    }
    public static List<Map> getExternalImageInfo(Context ctx, String[] columns) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return getMediaImageInfo(ctx, uri, columns);
    }
    public static List<Map> getInternalImageInfo(Context ctx) {
        Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        return getMediaImageInfo(ctx, uri, null);
    }
    public static List<Map> getInternalImageInfo(Context ctx, String[] columns) {
        Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        return getMediaImageInfo(ctx, uri, columns);
    }
    private static List<Map> getMediaImageInfo(Context ctx, Uri uri, String[] columns) {
//        String[] columns = new String[]{
//                MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DISPLAY_NAME
//                , MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.DATE_TAKEN};

        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(uri, columns, null, null, null);
        if (cursor != null) {
            Map<String, String> item;
            List<Map> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                String[] columnNames = cursor.getColumnNames();
                item = new HashMap<>();
                for (String columnName : columnNames) {
                    int columnIndex = cursor.getColumnIndex(columnName);
                    String columnValue = cursor.getString(columnIndex);
                    item.put(columnName, columnValue);
                }
                result.add(item);
            }
            cursor.close();
            return result;
        }
        cursor.close();
        return null;
    }
}
