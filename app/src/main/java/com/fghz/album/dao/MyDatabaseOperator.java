package com.fghz.album.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.fghz.album.Config;
/**
 * Created by dongchangzhang on 1/19/17.
 */

public class MyDatabaseOperator {
    private Cursor cursor;
    private Context context;
    private SQLiteDatabase db;
    private MyDatabaseHelper dbHelper;

    public MyDatabaseOperator(Context context, String dbName, int dbversion) {
        this.context = context;
        this.dbHelper = new MyDatabaseHelper(context, dbName, null, dbversion);
        this.db = dbHelper.getWritableDatabase();
    }

    public List<Map> search(String tableName) {
        return search(tableName, null);
    }
    public List<Map> search(String tableName, String searchWhat) {
        cursor = db.query(tableName, null, searchWhat, null, null, null, null);
        if (cursor != null) {
            Map<String, String> item;
            List<Map> result = new ArrayList<>();
            while (cursor.moveToNext()) {
                String[] columnNames = cursor.getColumnNames();
                item = new HashMap<>();
                for (String colnmnName : columnNames) {
                    int columnIndex = cursor.getColumnIndex(colnmnName);
                    String columnValue = cursor.getString(columnIndex);
                    item.put(colnmnName, columnValue);
                }
                result.add(item);
            }
            cursor.close();
            return result;
        }
        cursor.close();
        return null;
    }
    public long insert(String tableName, ContentValues values) {
        return db.insert(tableName, null, values);
    }
    public int erase(String tableName, String whereClause, String[] whereArgs) {
        return db.delete(tableName, whereClause, whereArgs);
    }
    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(tableName, values, whereClause, whereArgs);
    }
    public boolean close() {
        try {
            this.db.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
