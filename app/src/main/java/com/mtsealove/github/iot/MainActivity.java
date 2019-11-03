package com.mtsealove.github.iot;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.mtsealove.github.iot.Database.*;
import com.mtsealove.github.iot.Design.SlideView;
import com.mtsealove.github.iot.Design.SystemUiTuner;
import com.mtsealove.github.iot.Service.UpdateLocationService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView driverNameTv, locationTv, statusTv, status_kr_tv;
    Button checkListBtn, rfidBtn, setStatusBtn;
    ProgressBar locationPb;
    Account account;
    LocationManager locationManager;
    Location location;
    final String tag = "메인 액티비티";
    Geocoder geocoder;
    RestApi restApi;
    ArrayList<AItem> AItemList;
    public static int Status = 0;
    static DrawerLayout drawerLayout;
    ImageView statusIV;
    SlideView slideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        driverNameTv = findViewById(R.id.driverNameTv);
        locationTv = findViewById(R.id.locationTv);
        locationPb = findViewById(R.id.locationPb);
        statusTv = findViewById(R.id.statusTv);
        status_kr_tv = findViewById(R.id.status_kr_tv);
        checkListBtn = findViewById(R.id.checkListBtn);
        rfidBtn = findViewById(R.id.RfidBtn);
        drawerLayout = findViewById(R.id.drawerLayout);
        slideView = findViewById(R.id.slideView);
        setStatusBtn = findViewById(R.id.setStatusBtn);
        statusIV = findViewById(R.id.statusIv);

        setStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetStatus();
            }
        });

        //상단바 설정
        SystemUiTuner systemUiTuner = new SystemUiTuner(this);
        systemUiTuner.setStatusBarWhite();

        //계정정보 설정
        account = (Account) getIntent().getSerializableExtra("account");
        slideView.SetDriverName(account.getDriverName());
        //위치 설정
        GetLocation();
        //화물 데이터 받아오기
        restApi = new RestApi(this);
        GetItemList();

        StartService();
    }

    Intent RfidIntent;

    //위치 정보 확인하기
    private void GetLocation() {
        RfidIntent = new Intent(this, RfidActivity.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(tag, "위치정보 권한 없음");
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, GpsListener);
        geocoder = new Geocoder(this);
    }

    //위치정보 리스너
    private LocationListener GpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            try {
                //위경도를 주소로 변환
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);
                if (addresses != null && addresses.size() != 0) {
                    String addr = addresses.get(0).getAddressLine(0).replace("대한민국 ", "");
                    RfidIntent.putExtra("address", addr);
                    locationPb.setVisibility(View.GONE);
                    locationTv.setText(addr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    //화물 데이터 받아오기
    ProgressDialog progressDialog;

    private void GetItemList() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("화물 정보를 받아오는 중입니다");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Call<AItemList> call = restApi.getRetrofitService().GetLItemList(account.getID(), null);
        call.enqueue(new Callback<AItemList>() {
            @Override
            public void onResponse(Call<AItemList> call, Response<AItemList> response) {
                if (response.isSuccessful()) {
                    //데이터 받아오기 성공
                    Log.d(tag, response.body().toString());
                    AItemList = response.body().getData();

                    //상태 얻어오기
                    Status = response.body().getStatus();

                    //상태 한글 표시
                    String status_kr = StatusMap.GetStatus(Status);
                    status_kr_tv.setText(status_kr);
                    //상태 이미지 표시
                    Drawable drawable;
                    switch (Status) {
                        case 1:
                            drawable = getDrawable(R.drawable.icon_delivery_house);
                            break;
                        case 2:
                            drawable = getDrawable(R.drawable.icon_delivery_load);
                            break;
                        case 3:
                            drawable = getDrawable(R.drawable.icon_delivery_unload);
                            break;
                        case 4:
                            drawable = getDrawable(R.drawable.icon_delivery);
                            break;
                        default:
                            drawable = getDrawable(R.drawable.icon_delivery_done);
                    }
                    statusIV.setImageDrawable(drawable);

                    //전체 중 완료한 개수 표시
                    int processed = 0;
                    for (AItem aItem : AItemList) {
                        if (Integer.parseInt(aItem.getStatus()) == Status)
                            processed++;
                    }
                    statusTv.setText(AItemList.size() + "개 중 " + processed + "개 완료");
                    //버튼 클릭 리스너 생성
                    checkListBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SetAitemListBtn();
                        }
                    });
                    rfidBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TagRFID();
                        }
                    });
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<AItemList> call, Throwable t) {
                Log.e(tag, "화물 데이터 받기 실패");
                //progressDialog.dismiss();
            }
        });
    }

    //화물 목록 조회
    private void SetAitemListBtn() {
        Intent intent = new Intent(this, AItemListActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("status", Status);
        startActivity(intent);
    }

    //RFID 태그 화면으로 이동
    private void TagRFID() {
        Intent intent = new Intent(this, RfidActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("status", Status);
        startActivity(intent);
    }

    public static void OpenDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void CloseDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
    }

    //배송 상태 변경
    private void SetStatus() {
        ListView listView = new ListView(this);
        ArrayList<String> StatusList = new ArrayList<>();
        StatusList.add("집하처리");
        StatusList.add("화물상차");
        StatusList.add("화물하차");
        StatusList.add("배송출발");
        StatusList.add("배송종료");

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, StatusList);
        listView.setAdapter(arrayAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("배송 상태 변경");
        builder.setView(listView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UpdateStatus(position + 1);
                if (position == 4) {   //배송 종료
                    StopService();
                } else { //서비스가 시작 가능하면 시작
                    StartService();
                }
                dialog.cancel();
            }
        });
    }

    //배송기사 상태 업데이트
    ProgressDialog statusDialog;

    private void UpdateStatus(int status) {
        statusDialog = new ProgressDialog(this);
        statusDialog.setMessage("정보를 수정중입니다");
        statusDialog.setCancelable(false);
        statusDialog.show();
        RestApi restApi = new RestApi(this);
        Call<RestResult> call = restApi.getRetrofitService().UpdateDriverStatus(new RequestDriverStatus(account.getID(), status));
        call.enqueue(new Callback<RestResult>() {
            @Override
            public void onResponse(Call<RestResult> call, Response<RestResult> response) {
                if (response.isSuccessful() && response.body().getResult().equals("OK")) {
                    Toast.makeText(MainActivity.this, "상태가 수정되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "서버 오류가 발생하였습니다", Toast.LENGTH_SHORT).show();
                }
                GetItemList();
                statusDialog.dismiss();
            }

            @Override
            public void onFailure(Call<RestResult> call, Throwable t) {
                Toast.makeText(MainActivity.this, "서버 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
                statusDialog.dismiss();
            }
        });
    }

    //서비스 실행중인지 확인
    public boolean isServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (UpdateLocationService.class.getName().equals(serviceInfo.service.getClassName())) return true;
        }
        return false;
    }

    //서비스 시작
    private void StartService() {
        if (!isServiceRunning()) {  //서비스가 실행중이 아니라면
            Intent service = new Intent(getApplicationContext(), UpdateLocationService.class);   //인텐트에 서비스를 넣어줌
            service.putExtra("account", account);
            startService(service);  //서비스 시작
        }
    }

    //서비스 종료
    private void StopService() {
        if (isServiceRunning()) {   //서비스가 실행중이면
            Intent service = new Intent(getApplicationContext(), UpdateLocationService.class);   //인텐트에 서비스를 넣어줌
            stopService(service);
        }
    }

    //뒤로가기 2번 눌러 종료
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한번 더 누르면 종료합니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(GpsListener);
        locationManager = null;
    }
}
