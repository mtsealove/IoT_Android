package com.mtsealove.github.iot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mtsealove.github.iot.Database.AItem;
import com.mtsealove.github.iot.Database.AItemList;
import com.mtsealove.github.iot.Database.Account;
import com.mtsealove.github.iot.Database.RestApi;
import com.mtsealove.github.iot.Design.AitemAdapter;
import com.mtsealove.github.iot.Design.SystemUiTuner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class AItemListActivity extends AppCompatActivity {
    String tag = getClass().getSimpleName();
    RecyclerView recyclerView;
    AitemAdapter aitemAdapter;
    ArrayList<AItem> aItemLists;
    private Account account;
    ProgressDialog progressDialog;
    Spinner sortSp;
    Button homeBtn, tagBtn;
    int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aitem_list);

        //인텐트로부터 데이터 받기
        account = (Account) getIntent().getSerializableExtra("account");
        status = getIntent().getIntExtra("status", 0);

        //레이아웃 설정
        recyclerView = findViewById(R.id.recylerVidw);
        sortSp = findViewById(R.id.sortSp);
        homeBtn = findViewById(R.id.homeBtn);
        tagBtn = findViewById(R.id.tagBtn);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        SystemUiTuner systemUiTuner = new SystemUiTuner(this);
        systemUiTuner.setStatusBarWhite();

//        GetItems(null);

        sortSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        GetItems("status");
                        break;
                    case 1:
                        GetItems("ItemName");
                        break;
                    case 2:
                        GetItems("DstAddress");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveHome();
            }
        });
        tagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTag();
            }
        });
    }

    //화물 목록 데이터
    private void GetItems(@Nullable String sort) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("데이터를 받아오는 중입니다");
        progressDialog.setCancelable(false);
        progressDialog.show();
        RestApi restApi = new RestApi(this);
        Call<AItemList> call = restApi.getRetrofitService().GetLItemList(account.getID(), sort);
        call.enqueue(new Callback<AItemList>() {
            @Override
            public void onResponse(Call<AItemList> call, Response<AItemList> response) {
                if (response.isSuccessful()) {
                    aItemLists = response.body().getData();
                    aitemAdapter = new AitemAdapter(aItemLists);
                    recyclerView.setAdapter(aitemAdapter);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<AItemList> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    //홈화면 이동
    private void moveHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("account", account);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

    //태그 화면 이동
    private void moveTag() {
        Intent intent = new Intent(this, RfidActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("status", status);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

    //뒤로가기를 누르면 무조건 홈화면으로 이동
    @Override
    public void onBackPressed() {
        moveHome();
    }
}
