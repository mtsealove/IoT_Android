package com.mtsealove.github.iot;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.mtsealove.github.iot.Database.*;
import com.mtsealove.github.iot.Design.AItemView;
import com.mtsealove.github.iot.Design.SystemUiTuner;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.BluetoothCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class RfidActivity extends AppCompatActivity {
    public static DrawerLayout drawerLayout;
    AItemView aItemView;
    String tag = getClass().getSimpleName();
    int status;
    private Account account;
    Button homeBtn, listBtn;
    ImageView messageIv;
    TextView titleTv, messageTv;

    //블루투스 관련
    Bluetooth bluetooth;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    //위치정보 관련
    Location location;
    LocationManager locationManager;
    Geocoder geocoder;
    String Address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid);
        drawerLayout = findViewById(R.id.drawerLayout);
        aItemView = findViewById(R.id.infoView);
        homeBtn = findViewById(R.id.homeBtn);
        listBtn = findViewById(R.id.listBtn);
        messageIv = findViewById(R.id.messageIv);
        messageTv = findViewById(R.id.messageTv);
        titleTv = findViewById(R.id.titleTv);

        SystemUiTuner systemUiTuner = new SystemUiTuner(this);
        systemUiTuner.setStatusBarWhite();

        status = getIntent().getIntExtra("status", 0);
        account = (Account) getIntent().getSerializableExtra("account");
        GetLocation();

        messageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SetStatusView("123456789012");
                return false;
            }
        });

        /*
        bluetooth = new Bluetooth(this);
        bluetooth.setBluetoothCallback(bluetoothCallback);
//        bluetooth.startScanning();
        //List<BluetoothDevice> devices = bluetooth.getPairedDevices();

//        bluetooth.connectToName("TEST");
        bluetooth.send(new byte[]{61, 62, 63});
         */
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveHome();
            }
        });
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveList();
            }
        });

    }

    private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override
        public void onBluetoothTurningOn() {
        }

        @Override
        public void onBluetoothTurningOff() {
        }

        @Override
        public void onBluetoothOff() {
        }

        @Override
        public void onBluetoothOn() {
            // doStuffWhenBluetoothOn() ...

        }

        @Override
        public void onUserDeniedActivation() {
            // handle activation denial...
        }
    };

    public static void OpenDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void CloseDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
    }

    //위치 정보 확인하기
    private void GetLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(tag, "위치정보 권한 없음");
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, GpsListener);
        geocoder = new Geocoder(this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10);
            if (addresses != null && addresses.size() != 0) {
                String addr = addresses.get(0).getAddressLine(0);
                addr.replace("대한민국 ", "");
                Address = addr;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //위치정보 리스너
    private LocationListener GpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);
                if (addresses != null && addresses.size() != 0) {
                    String addr = addresses.get(0).getAddressLine(0);
                    addr.replace("대한민국 ", "");
                    Address = addr;
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

    ProgressDialog progressDialog;

    //RFID로 송장 번호를 입력했을 때 화면에 표시
    private void SetStatusView(String invoice) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("화물 정보를 받아오는 중입니다");
        progressDialog.show();
        RestApi restApi = new RestApi(this);
        Call<AItem> call = restApi.getRetrofitService().GetAItem(invoice);
        call.enqueue(new Callback<AItem>() {
            @Override
            public void onResponse(Call<AItem> call, Response<AItem> response) {
                if (response.isSuccessful()) {
                    AItem aItem = response.body();
                    SetResult(aItem);
                    UpdateItemStatus(new RequestAitemStatus(aItem.getInvoiceNum(), status));
                }
            }

            @Override
            public void onFailure(Call<AItem> call, Throwable t) {

            }
        });

        //위치정보를 가지고 있을 때만 수행
        if (Address != null) {
            Call<RestResult> call1 = restApi.getRetrofitService().UpdateTimeline(new RequestTimeLine(invoice, Address, status));
            call1.enqueue(new Callback<RestResult>() {
                @Override
                public void onResponse(Call<RestResult> call, Response<RestResult> response) {
                    if (response.isSuccessful()) {
                        Log.d(tag, "타임라인 업데이트");
                    } else {
                        Log.d(tag, "클라이언트 오류 발생");
                    }
                }

                @Override
                public void onFailure(Call<RestResult> call, Throwable t) {
                    Log.d(tag, "서버연결 오류 발생");
                }
            });
        }
    }

    private void SetResult(AItem aItem) {
        //하단 표시
        aItemView.SetItem(aItem);
        //상단 표시
        titleTv.setText("RFID 태그 성공");
        messageTv.setText(StatusMap.GetStatus(status) + " 완료");
        Drawable drawable = getDrawable(R.drawable.icon_checked);
        messageIv.setImageDrawable(drawable);
    }

    //화물 상태 업데이트
    private void UpdateItemStatus(final RequestAitemStatus requestAitemStatus) {
        RestApi restApi = new RestApi(this);
        Call<RestResult> call = restApi.getRetrofitService().UpdateAitemStatus(requestAitemStatus);
        call.enqueue(new Callback<RestResult>() {
            @Override
            public void onResponse(Call<RestResult> call, Response<RestResult> response) {
                if (response.isSuccessful() && response.body().getResult().equals("OK")) {
                    Log.d(tag, "화물 상태 업데이트: " + requestAitemStatus.getInvoice());
                    CheckStateAll();
                } else if (response.isSuccessful()) {
                    Log.d(tag, response.body().toString());
                } else {
                    Log.e(tag, "화물 상태 업데이트 실패");
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<RestResult> call, Throwable t) {
                Log.e(tag, "화물 상태 업데이트 실패 - 서버 연결 실패");
                progressDialog.dismiss();
            }
        });
    }

    //모든 상태를 체크하여 도어락에 신호를 보내줌
    private void CheckStateAll() {
        RestApi restApi = new RestApi(this);
        Call<RestResult> call = restApi.getRetrofitService().GetStatusDone(account.getID(), status);
        call.enqueue(new Callback<RestResult>() {
            @Override
            public void onResponse(Call<RestResult> call, Response<RestResult> response) {
                if (response.isSuccessful()) {
                    //모든 상태가 현재 상태와 일치할 경우
                    if (response.body().getResult().equals("OK")) {
                        Log.d(tag, "도어락 오픈");
                        //sendData("Open");
                    } else {    //상태가 일치하지 않을 경우
                        Log.d(tag, "도어락 잠금");
                        //sendData("Lock");
                    }
                }
            }

            @Override
            public void onFailure(Call<RestResult> call, Throwable t) {

            }
        });
    }

    //홈 화면 이동
    private void moveHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("status", status);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

    //화물 목록화면 이동
    private void moveList() {
        Intent intent = new Intent(this, AItemListActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("status", status);
        startActivity(intent);
        ActivityCompat.finishAffinity(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveHome();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
        bluetooth.onStart();
        if (bluetooth.isEnabled()) {
            // doStuffWhenBluetoothOn() ...
        } else {
            bluetooth.showEnableDialog(RfidActivity.this);
        }
         */
    }

    @Override
    protected void onStop() {
        super.onStop();
        //bluetooth.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //bluetooth.onActivityResult(requestCode, resultCode);
    }
}
