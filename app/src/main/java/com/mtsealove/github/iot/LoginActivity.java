package com.mtsealove.github.iot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.mtsealove.github.iot.Database.AcDbHelper;
import com.mtsealove.github.iot.Database.Account;
import com.mtsealove.github.iot.Database.LoginData;
import com.mtsealove.github.iot.Database.RestApi;
import com.mtsealove.github.iot.Design.SystemUiTuner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText IdEt, PwEt;
    CheckBox keepCb;
    Button loginBtn;
    ImageView logoIv;
    private String ID, password;
    private AcDbHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SystemUiTuner systemUiTuner = new SystemUiTuner(this);
        systemUiTuner.setStatusBarWhite();

        IdEt = findViewById(R.id.IdEt);
        PwEt = findViewById(R.id.PwEt);
        keepCb = findViewById(R.id.keepCb);
        loginBtn = findViewById(R.id.loginBtn);
        logoIv = findViewById(R.id.logoIv);

        logoIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(10);
                Intent intent = new Intent(LoginActivity.this, SetIpActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckInput();
            }
        });

        GetAccount();
    }

    //입력 값 체크
    private void CheckInput() {
        if (IdEt.getText().toString().length() == 0) {
            Toast.makeText(this, "아이디를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        } else if (PwEt.getText().toString().length() == 0) {
            Toast.makeText(this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Login();
        }
    }

    ProgressDialog progressDialog;

    private void Login() {
        //다이얼로그 출력
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("로그인 중입니다");
        progressDialog.setCancelable(false);
        progressDialog.show();

        dbHelper = new AcDbHelper(this, AcDbHelper.AccountDB, null, 1);
        database = dbHelper.getWritableDatabase();
        ID = IdEt.getText().toString();
        password = PwEt.getText().toString();

        //로그인 유지 확인
        if (keepCb.isChecked()) {
            dbHelper.WriteAccount(database, ID, password);
        } else {
            dbHelper.CleanAccount(database);
        }
        //서버 통신
        RestApi restApi = new RestApi(this);
        Call<Account> call = restApi.getRetrofitService().PostLogin(new LoginData(ID, password));
        call.enqueue(new Callback<Account>() {
            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                progressDialog.dismiss();
                Log.d("rest", "결과옴");
                if (response.isSuccessful()) {
                    Account account = response.body();
                    Log.d("rest", account.toString());
                    //로그인 실패
                    if (account.getID() == null) {
                        Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                        return;
                    } else {    //로그인 성공
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("account", account);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "아이디나 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("rest", "결과 반환 실패");
                Toast.makeText(LoginActivity.this, "서버연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //이미 저장되어 있는 아이디 읽기
    private void GetAccount() {
        dbHelper = new AcDbHelper(this, AcDbHelper.AccountDB, null, 1);
        database = dbHelper.getReadableDatabase();
        String query = "select * from " + AcDbHelper.AccountDB;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.getCount() != 0) {
                cursor.moveToNext();
                //데이터를 표시하고
                ID = cursor.getString(0);
                password = cursor.getString(1);
                IdEt.setText(ID);
                PwEt.setText(password);
                keepCb.setChecked(true);
                //로그인 수행
                Login();
            }
        }
        //데이터베이스 닫기
        cursor.close();
        database.close();
        dbHelper.close();
        cursor = null;
        database = null;
        dbHelper = null;
    }
}
