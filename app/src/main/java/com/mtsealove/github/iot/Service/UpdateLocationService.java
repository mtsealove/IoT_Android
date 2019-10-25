package com.mtsealove.github.iot.Service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.mtsealove.github.iot.Database.Account;
import com.mtsealove.github.iot.Database.RequestAddress;
import com.mtsealove.github.iot.Database.RestApi;
import com.mtsealove.github.iot.Database.RestResult;
import com.mtsealove.github.iot.LoginActivity;
import com.mtsealove.github.iot.MainActivity;
import com.mtsealove.github.iot.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class UpdateLocationService extends Service {    //백그라운드에서 작동(서비스)
    private Notification notification;
    Account account;
    String tag = getClass().getSimpleName();
    Geocoder geocoder;
    RestApi restApi;
    String driver_id;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "운행이 시작되었습니다", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //서비스가 시작될 때
        account = (Account) intent.getSerializableExtra("account"); //계정 정보 받아오기
        driver_id = account.getID();
        if (Build.VERSION.SDK_INT > 26)
            CreateNotificationHigh();   //노티 만들기
        else CreateNotificationLow();

        restApi = new RestApi(getBaseContext());
        //위치정보 갱신 활성화
        GetLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {   //종료되었을 때
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "운행이 중지되었습니다", Toast.LENGTH_SHORT).show();
        Log.d(tag, "서비스 종료");
    }


    @RequiresApi(api = Build.VERSION_CODES.O)   //안드로이드 8.0이상에서 작동
    public void CreateNotificationHigh() {  //알림 만들기
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);    //노티(알림) 매니저
        String channelId = "notify";  //채널 ID
        CharSequence channelName = "알림";  //채널 이름
        String description = "일반 알림입니다";  //채널 설명
        int importance = NotificationManager.IMPORTANCE_HIGH; //중요도 높음
        //안드로이드 8.0 이상부터는 알림 채널을 설정해야 알림 표시가 가능하다
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);    //알림 채널
        notificationChannel.setDescription(description);    //채널 설명 설정
        notificationManager.createNotificationChannel(notificationChannel); //채널 생성

        Intent intent = new Intent(UpdateLocationService.this, LoginActivity.class);   //메인 액티비티로 가는 인텐트(화면 전환 객체)
        PendingIntent pendingIntent = PendingIntent.getActivity(UpdateLocationService.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT); //펜딩 인텐트, 알림을 클릭하면 위의 인텐트 실행

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(UpdateLocationService.this)   //알림 만들기
                .setContentTitle("IoT 물류추적 서비스") //제목
                .setContentText("현재 운행중입니다")  //내용
                .setChannelId("notify") //채널 ID를 기반으로 위에서 만든 채널을 할당
                .setSmallIcon(R.drawable.app_logo_no_background) //아이콘 설정
                //.setContentIntent(pendingIntent)    //클릭시 메인으로 가는 인텐트 설정
                .setOngoing(true);  //상단바에 띄우기
        notification = notificationBuilder.build();   //실제 노티 빌드
        startForeground(001, notification); //항상 실행
    }

    public void CreateNotificationLow() {   //안드로이드 8.0 미만에서 작동
        Intent intent = new Intent(UpdateLocationService.this, LoginActivity.class);   //메인 액티비티로 가는 인텐트(화면 전환 객체)
        PendingIntent pendingIntent = PendingIntent.getActivity(UpdateLocationService.this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT); //펜딩 인텐트, 알림을 클릭하면 위의 인텐트 실행

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(UpdateLocationService.this)   //알림 만들기
                .setContentTitle("IoT 물류추적 서비스") //제목
                .setContentText("현재 운행중입니다")  //내용
                .setSmallIcon(R.drawable.app_logo_no_background) //아이콘 설정
                // .setContentIntent(pendingIntent)    //클릭시 메인으로 가는 인텐트 설정
                .setOngoing(true);  //상단바에 띄우기
        notification = notificationBuilder.build();   //실제 노티 빌드
        startForeground(001, notification); //항상 실행
    }

    LocationManager locationManager;
    Location location;

    //위치 정보 확인하기
    @SuppressLint("MissingPermission")
    private void GetLocation() {
        Log.d(tag, "서비스 시작");
        locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //5분에 한 번씩, 10m마다 한번씩 위치 정보 업데이트
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, GpsListener);
        geocoder = new Geocoder(this);
    }

    //위치정보 리스너
    private LocationListener GpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d(tag, "제공자: " + provider + "위도: " + latitude + "경도: " + longitude);

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);
                if (addresses != null && addresses.size() != 0) {
                    String addr = addresses.get(0).getAddressLine(0);
                    addr.replace("대한민국 ", "");
                    Log.d(tag, addr);
                    UpdateLocation(addr);
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

    //데이터베이스에 위치정보 업데이트
    private void UpdateLocation(final String address) {
        Log.d(tag, "address: "+address);
        RequestAddress requestAddress = new RequestAddress(driver_id, address);
        Call<RestResult> call = restApi.getRetrofitService().UpdateLocation(requestAddress);
        call.enqueue(new Callback<RestResult>() {
            @Override
            public void onResponse(Call<RestResult> call, Response<RestResult> response) {
                if (response.isSuccessful() && response.body().getResult().equals("OK")) {
                    Log.d(tag, "위치정보 업데이트: " + address);
                }
            }

            @Override
            public void onFailure(Call<RestResult> call, Throwable t) {
                Log.e(tag, "위치정보 업데이트 실패");
            }
        });
    }
}