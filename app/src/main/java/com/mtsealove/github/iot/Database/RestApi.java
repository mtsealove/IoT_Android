package com.mtsealove.github.iot.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RestApi {
    Retrofit retrofit;
    RetrofitService retrofitService;
    OkHttpClient okHttpClient;
    Context context;

    public RestApi(Context context) {
        this.context = context;
        //통신 연결 클라이언트
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        //데이터를 받아올 API
        retrofit = new Retrofit.Builder()
                .baseUrl("http://"+getIP()+":3800")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitService = retrofit.create(RetrofitService.class);
    }

    //바로 사용할 수 있는 인터페이스 반환
    public RetrofitService getRetrofitService() {
        return retrofitService;
    }

    private String getIP() {
        IpDbHelper dbHelper = new IpDbHelper(context, IpDbHelper.IpTable, null, 1);
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String query="select * from "+IpDbHelper.IpTable;
        Cursor cursor=db.rawQuery(query, null);
        if(cursor!=null&&cursor.getCount()!=0) {
            cursor.moveToNext();
            String ip=cursor.getString(0);
            cursor.close();
            db.close();
            dbHelper.close();
            return ip;
        } else {
            cursor.close();
            db.close();
            dbHelper.close();
            return null;
        }
    }
}
