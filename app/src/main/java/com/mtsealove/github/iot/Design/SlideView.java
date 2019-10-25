package com.mtsealove.github.iot.Design;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.IntentCompat;
import com.mtsealove.github.iot.Database.AcDbHelper;
import com.mtsealove.github.iot.LoginActivity;
import com.mtsealove.github.iot.R;

import java.util.List;

public class SlideView extends RelativeLayout {
    TextView driverNameTv, logoutTv;
    Context context;

    public SlideView(Context context) {
        super(context);
        init(context);
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SlideView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.view_slide, this, false);
        addView(layout);
        driverNameTv = layout.findViewById(R.id.driverNameTv);
        logoutTv = layout.findViewById(R.id.logoutTv);

        logoutTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOut();
            }
        });
    }

    public void SetDriverName(String name) {
        driverNameTv.setText(name);
    }

    //로그아웃
    private void LogOut() {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setCancelable(false)
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //자동로그인 해제
                AcDbHelper acDbHelper = new AcDbHelper(context, AcDbHelper.AccountDB, null, 1);
                SQLiteDatabase db = acDbHelper.getWritableDatabase();
                acDbHelper.CleanAccount(db);
                db.close();
                acDbHelper.close();
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                ((Activity) context).finish();
                ActivityCompat.finishAffinity((Activity)context);

            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }
}