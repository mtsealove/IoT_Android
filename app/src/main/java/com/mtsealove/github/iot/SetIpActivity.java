package com.mtsealove.github.iot;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.mtsealove.github.iot.Database.IpDbHelper;

import java.util.Set;

public class SetIpActivity extends AppCompatActivity {
    EditText IPet;
    Button confirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ip);

        IPet = findViewById(R.id.IPet);
        confirmBtn = findViewById(R.id.confirmBtn);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetIp();
            }
        });

        GetIp();
        GetIp();
    }

    private void SetIp() {
        if (IPet.getText().toString().length() != 0) {
            IpDbHelper dbHelper = new IpDbHelper(this, IpDbHelper.IpTable, null, 1);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.UpdateIp(db, IPet.getText().toString());
            db.close();
            dbHelper.close();

            Intent intent=new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "IP를 입력하세요", Toast.LENGTH_SHORT).show();
        }
    }

    private void GetIp() {
        IpDbHelper dbHelper=new IpDbHelper(this, IpDbHelper.IpTable, null, 1);
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String query="select * from "+IpDbHelper.IpTable;
        Cursor cursor=db.rawQuery(query, null);
        if(cursor!=null&&cursor.getCount()!=0) {
            cursor.moveToNext();
            String ip=cursor.getString(0);
            IPet.setText(ip);
        }
    }
}
