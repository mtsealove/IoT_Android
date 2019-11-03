package com.mtsealove.github.iot;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import java.io.*;
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

    ProgressDialog bluetoothDialog;

    //블루투스 관련
    SharedPreferences sharedPreferences;
    ConnectedTask mConnectedTask = null;
    static BluetoothAdapter mBluetoothAdapter;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    static boolean isConnectionError = false;
    private static final String TAG = "BluetoothClient";
    int temp_power = 0;
    int initial_power = 1;
    int pulling_power = 0;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private final int REQUEST_BLUETOOTH_ENABLE = 100;


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

        initBluetooth();

        /*
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SetStatusView("123456789012");
            }
        }, 2000);
         */

        messageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                sendMessage("test");
                return false;
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(RfidActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("화물 정보를 받아오는 중입니다");
                progressDialog.show();
            }
        });

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

    //결과를 화면에 표시
    private void SetResult(final AItem aItem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //하단 표시
                aItemView.SetItem(aItem);
                //상단 표시
                titleTv.setText("RFID 태그 성공");
                messageTv.setText(StatusMap.GetStatus(status) + " 완료");
                Drawable drawable = getDrawable(R.drawable.icon_checked);
                messageIv.setImageDrawable(drawable);
            }
        });
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
                        Log.d(tag, "도어락 잠금");
                        sendMessage("Lock");
                    } else {    //상태가 일치하지 않을 경우
                        Log.d(tag, "도어락 열기");
                        sendMessage("Open");
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

    //블루투스 초기화
    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showErrorDialog("This device is not implement Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        } else {
            Log.d(TAG, "Initialisation successful.");
            showPairedDevicesListDialog(1);
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveHome();
        }
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d(TAG, "create socket for " + mConnectedDeviceName);
            } catch (IOException e) {
                Log.e(TAG, "socket create failed " + e.getMessage());
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " +
                            " socket during connection failure", e2);
                }
                return false;
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean isSuccess) {
            //연결 성공 시
            if (isSuccess) {
                connected(mBluetoothSocket);
            } else {
                //연결 실패 시
                isConnectionError = true;
                Log.d(TAG, "Unable to connect device");
                bluetoothDialog.dismiss();
                Toast.makeText(RfidActivity.this, "블루투스 연결에 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //연결이 성공했을 때 실행할 메서드
    public void connected(BluetoothSocket socket) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }

    //연결 성공 후 수행할 메서드
    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {
        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket) {
            //소켓 생성
            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "socket not created", e);
            }
            Log.d(TAG, "connected to " + mConnectedDeviceName);

            bluetoothDialog.dismiss();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //메세지 수신 대기
            Log.d(TAG, "메세지 수신 대기중");
            byte[] readBuffer = new byte[1024];
            int readBufferPosition = 0;
            while (true) {
                if (isCancelled()) {
                    Log.e(TAG, "수신 취소");
                    return false;
                }
                try {
                    int bytesAvailable = mInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mInputStream.read(packetBytes);
                        //버퍼 형식으로 수신받은 byte 변환
                        BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            Log.d("message", line);
                            String recvMessage = line;
                            SetStatusView(recvMessage);
                            publishProgress(recvMessage);
                        }

                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    return false;
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {
            try {
                recvMessage[0] = recvMessage[0].replaceAll("\\n", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                recvMessage[0] = recvMessage[0].replaceAll("\\r", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);
            if (!isSucess) {
                closeSocket();
                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
                showErrorDialog("Device connection was lost");
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            closeSocket();
        }

        void closeSocket() {
            try {
                if (mBluetoothSocket != null)
                    mBluetoothSocket.close();
                Log.d(TAG, "close socket()");

            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        void write(String msg) {
            msg += "\n";
            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e);
            }
        }
    }


    //블루투스 페어링 리스트 출력
    public void showPairedDevicesListDialog(int flag) {
        Log.d(TAG, "showPaired Devices List");
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if (pairedDevices.length == 0) {
            showQuitDialog("No devices have been paired.\n"
                    + "You must pair it with another device.");
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];

        if (flag == 0) {
            String Dev_name, Dev_addr;
            sharedPreferences = getSharedPreferences("bt", MODE_PRIVATE);
            Dev_name = sharedPreferences.getString("dev_name", "");
            Dev_addr = sharedPreferences.getString("dev_addr", "");

            for (int i = 0; i < pairedDevices.length; i++) {
                items[i] = pairedDevices[i].getName();
                if (Dev_name.equals(items[i]) && Dev_addr.equals(pairedDevices[i].getAddress())) {
                    ConnectTask task = new ConnectTask(pairedDevices[i]);
                    task.execute();
                    return;
                }
            }
        } else if (flag == 1) {
            for (int i = 0; i < pairedDevices.length; i++) {
                items[i] = pairedDevices[i].getName();
            }
        }

        //기기 선택 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("기기 선택");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                sharedPreferences = getSharedPreferences("bt", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("dev_addr", pairedDevices[which].getAddress());
                editor.putString("dev_name", pairedDevices[which].getName());
                //
                bluetoothDialog = new ProgressDialog(RfidActivity.this);
                bluetoothDialog.setMessage("블루투스 연결중입니다");
                bluetoothDialog.setCancelable(false);
                bluetoothDialog.show();
                editor.commit();
                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }


    public void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isConnectionError) {
                    isConnectionError = false;
                    //finish();

                }
            }
        });
        builder.create().show();
    }


    public void showQuitDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    //메세지 전송
    void sendMessage(String msg) {
        if (mConnectedTask != null) {
            mConnectedTask.write(msg);
            Log.d(TAG, "send message: " + msg);
            //mConversationArrayAdapter.insert("Me:  " + msg, 0);
        } else {
            Log.e(TAG, "메세지 전송 불가");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_OK) {
                //BlueTooth is now Enabled
                showPairedDevicesListDialog(0);
            }
            if (resultCode == RESULT_CANCELED) {
                showQuitDialog("You need to enable bluetooth");
            }
        }
    }
}
