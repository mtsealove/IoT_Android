package com.mtsealove.github.iot.Database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class IpDbHelper extends SQLiteOpenHelper {
    public static final String IpTable="Ip";
    public IpDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public IpDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public IpDbHelper(Context context, String name, int version, SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query="create table "+IpTable+" (IP varchar(45))";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void UpdateIp(SQLiteDatabase db, String ip) {
        String removeQuery="delete from "+IpTable;
        String insertQuery="insert into "+IpTable+" values('"+ip+"')";
        db.beginTransaction();
        try{
            db.execSQL(removeQuery);
            db.execSQL(insertQuery);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
}
