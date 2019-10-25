package com.mtsealove.github.iot;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mtsealove.github.iot.Database.IpDbHelper;
import com.mtsealove.github.iot.Design.SystemUiTuner;
import retrofit2.http.POST;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SystemUiTuner systemUiTuner = new SystemUiTuner(this);
        systemUiTuner.setStatusBarWhite();
        CheckPermission();
    }

    //권한 체크
    private void CheckPermission() {
        TedPermission.with(this).
                setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
                .check();
    }

    //권한 체크 리스너
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent;
                    if (HasIp())
                        intent = new Intent(SplashActivity.this, LoginActivity.class);
                    else intent = new Intent(SplashActivity.this, SetIpActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 700);
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(SplashActivity.this, "권한을 허용하지 않으셨습니다.\n잠시 후 프로그램이 종료됩니다", Toast.LENGTH_LONG).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 3000);
        }
    };

    //IP가 저장되어 있는지 판단
    private boolean HasIp() {
        boolean result = false;
        IpDbHelper dbHelper = new IpDbHelper(this, IpDbHelper.IpTable, null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "select * from " + IpDbHelper.IpTable;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.getCount() != 0) {
            result = true;
        }
        cursor.close();
        db.close();
        dbHelper.close();
        return result;
    }
}
