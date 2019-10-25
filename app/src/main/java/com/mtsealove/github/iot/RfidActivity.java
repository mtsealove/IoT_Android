package com.mtsealove.github.iot;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.mtsealove.github.iot.Database.*;
import com.mtsealove.github.iot.Design.AItemView;
import com.mtsealove.github.iot.Design.SystemUiTuner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    int pariedDeviceCount;
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid);
        drawerLayout = findViewById(R.id.drawerLayout);
        aItemView = findViewById(R.id.infoView);
        homeBtn = findViewById(R.id.homeBtn);
        listBtn = findViewById(R.id.listBtn);
        messageIv=findViewById(R.id.messageIv);
        messageTv=findViewById(R.id.messageTv);
        titleTv=findViewById(R.id.titleTv);

        SystemUiTuner systemUiTuner = new SystemUiTuner(this);
        systemUiTuner.setStatusBarWhite();

        status = getIntent().getIntExtra("status", 0);
        account = (Account) getIntent().getSerializableExtra("account");

        SetStatusView("123456789012");
        initBluetooth();

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

    public static void OpenDrawer() {
        if (!drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void CloseDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
    }

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
    }

    private void SetResult(AItem aItem) {
        //하단 표시
        aItemView.SetItem(aItem);
        //상단 표시
        titleTv.setText("RFID 태그 성공");
        messageTv.setText(StatusMap.GetStatus(status)+" 완료");
        Drawable drawable=getDrawable(R.drawable.icon_checked);
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
                } else if(response.isSuccessful()){
                    Log.d(tag, response.body().toString());
                }
                else {
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

    private void initBluetooth() {
        // 블루투스 활성화하기
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
        if (bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
            // 여기에 처리 할 코드를 작성하세요.
        } else { // 디바이스가 블루투스를 지원 할 때
            if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                // 블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택한 값이 onActivityResult 함수에서 콜백된다.
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        pariedDeviceCount = devices.size();
        // 페어링 되어있는 장치가 없는 경우
        if (pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출=

        }
        // 페어링 되어있는 장치가 있는 경우

        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
                Log.e(tag, String.valueOf(bluetoothDevice.getBluetoothClass()));
            }
            list.add("취소");
            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);
            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });
            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            //alertDialog.show();
        }
    }

    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            // 데이터 송,수신 스트림을 얻어옵니다.\
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            // 데이터 수신 함수 호출
            receiveData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //데이터 받아오기
    public void receiveData() {
        final Handler handler = new Handler();
        // 데이터를 수신하기 위한 버퍼를 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        // 데이터를 수신하기 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (Thread.currentThread().isInterrupted()) {
                    try {
                        // 데이터를 수신했는지 확인합니다.
                        int byteAvailable = inputStream.available();
                        // 데이터가 수신 된 경우
                        if (byteAvailable > 0) {
                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                            for (int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                // 개행문자를 기준으로 받음(한줄)
                                if (tempByte == '\n') {
                                    // readBuffer 배열을 encodedBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    // 인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 전달받은 데이터 출력
                                            Log.d(tag, text);
                                        }
                                    });
                                } // 개행 문자가 아닐 경우
                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        // 1초마다 받아옴
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }
    //데이터 송신
    void sendData(String text) {
        // 문자열에 개행문자("\n")를 추가해줍니다.
        text += "\n";
        try{
            // 데이터 송신
            outputStream.write(text.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //모든 상태를 체크하여 도어락에 신호를 보내줌
    private void CheckStateAll() {
        RestApi restApi=new RestApi(this);
        Call<RestResult> call=restApi.getRetrofitService().GetStatusDone(account.getID(), status);
        call.enqueue(new Callback<RestResult>() {
            @Override
            public void onResponse(Call<RestResult> call, Response<RestResult> response) {
                if(response.isSuccessful()){
                    //모든 상태가 현재 상태와 일치할 경우
                    if(response.body().getResult().equals("OK")){
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

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (requestCode == RESULT_OK) { // '사용'을 눌렀을 때
                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                } else { // '취소'를 눌렀을 때
                    // 여기에 처리 할 코드를 작성하세요.
                    Toast.makeText(this, "블루투스 연결을 허용하지 않으면 애플리케이션을 구동시킬 수 없습니다", Toast.LENGTH_SHORT).show();
                    initBluetooth();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else{
            moveHome();
        }
    }
}
