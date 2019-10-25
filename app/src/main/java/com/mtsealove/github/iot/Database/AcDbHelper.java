package com.mtsealove.github.iot.Database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class AcDbHelper extends SQLiteOpenHelper {
    public final static String AccountDB = "Account";

    public AcDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public AcDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public AcDbHelper(Context context, String name, int version, SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "create table " + AccountDB + " (" +
                "ID varchar(45), " +
                "Password varchar(45)" +
                ")";
        try {
            db.execSQL(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void WriteAccount(SQLiteDatabase db, String ID, String password) {
        String removeQuery = "delete from " + AccountDB;
        String query = "insert into " + AccountDB + " values (" +
                "'" + ID + "', " +
                "'" + password + "'" +
                ")";
        db.beginTransaction();
        try {
            db.execSQL(removeQuery);
            db.execSQL(query);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void CleanAccount(SQLiteDatabase db){
        String query="delete from "+AccountDB;
        db.beginTransaction();
        try{
            db.execSQL(query);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
}
