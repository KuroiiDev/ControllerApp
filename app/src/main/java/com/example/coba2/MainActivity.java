package com.example.coba2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.OutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    //    private ImageView joystick;
    private OutputStream outputStream;
    private BluetoothSocket bluetoothSocket;
    private BluetoothAdapter bluetoothAdapter;
    private ImageView joystick, joystickBase;
    private float centerX, centerY, baseRadius, knobRadius;
    private int jarak = 0;
    private int sudut = 0;
    private int value = 0;


    private static final String HC_05_MAC_ADDRESS = "00:22:04:00:41:4B"; // Kasih Mac Address HC_05
    private static final UUID HC_05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

//    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            if (newState == BluetoothGatt.STATE_CONNECTED) {
//                Log.d("GATT", "Connected to GATT server.");
//                try {
//                    // Check permission before discovering services
//                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                        bluetoothGatt.discoverServices();
//                    }
//                } catch (SecurityException e) {
//                    // Handle SecurityException if permission is not granted
//                    Toast.makeText(MainActivity.this, "Bluetooth permissions are required to discover services", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.d("GATT", "Services discovered.");
//                for (BluetoothGattService service : gatt.getServices()) {
//                    if (service.getUuid().equals(SERVICE_UUID)) {
//                        writeCharacteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
//                    }
//                }
//            } else {
//                Toast.makeText(MainActivity.this, "Service discovery failed with status: " + status, Toast.LENGTH_SHORT).show();
//            }
//        }

    // Move the sendJoystickData method inside this callback

//    };
//    private void sendJoystickData(float angle, float distance) {
//        if (writeCharacteristic != null && bluetoothGatt != null) {
//            String data = angle + "," + distance + ";";
//            writeCharacteristic.setValue(data.getBytes());
//
//            // Check if Bluetooth permissions are granted before writing
//            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                try {
//                    bluetoothGatt.writeCharacteristic(writeCharacteristic);
//                } catch (SecurityException e) {
//                    // Handle exception if permissions are not granted
//                }
//            } else {
//                // Optionally, you can request permissions here
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
//                        Manifest.permission.BLUETOOTH_CONNECT,
//                        Manifest.permission.BLUETOOTH_SCAN
//                }, REQUEST_BLUETOOTH_PERMISSIONS);
//            }
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//
//        joystick = findViewById(R.id.joystick);
        joystick = findViewById(R.id.knob);
        joystickBase = findViewById(R.id.base);
        final TextView teks = findViewById(R.id.sudut_value);
        final TextView teks2 = findViewById(R.id.jarak_value);
        Button connectButton = findViewById(R.id.konek);
        SeekBar slider = findViewById(R.id.slider);
        slider.setProgress(1);
        final TextView statusText = findViewById(R.id.nilai_value);
        connectButton.setOnClickListener(v -> checkBluetoothPermissions());

//        joystick.getViewTreeObserver().addOnGlobalLayoutListener(
//                new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        centerX = joystick.getX() + joystick.getWidth() / 2;
//                        centerY = joystick.getY() + joystick.getHeight() / 2;
//                        joystick.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    }
//                }
//        );

        joystickBase.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                centerX = joystickBase.getX() + joystickBase.getWidth() / 2;
                centerY = joystickBase.getY() + joystickBase.getHeight() / 2;
                baseRadius = joystickBase.getWidth() / 2;
                knobRadius = joystick.getWidth() / 2;
                joystickBase.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        joystick.setOnTouchListener((v, event) -> {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_MOVE:
//                    float x = event.getRawX() - centerX;
//                    float y = event.getRawY() - centerY;
//
//                    float distance = (float) Math.sqrt(x * x + y * y);
//
//                    if (distance > MAX_DISTANCE) {
//                        x = (x / distance) * MAX_DISTANCE;
//                        y = (y / distance) * MAX_DISTANCE;
//                        distance = MAX_DISTANCE;
//                    }
//                    joystick.setX(centerX + x - joystick.getWidth() / 2);
//                    joystick.setY(centerY + y - joystick.getHeight() / 2);
//
//                    float angle = (float) Math.toDegrees(Math.atan2(y, x));
//                    if (angle < 0) {
//                        angle += 360;
//                    }
//                    int sudut = (int) angle;
//                    int jarak = (int) ((distance / MAX_DISTANCE) * 100);
//                    teks.setText("Sudut : " + sudut);
//                    teks2.setText("Jarak : " + jarak);
//
//                    sendJoystickData(angle, distance);
//                    break;
//
//                case MotionEvent.ACTION_UP:
//                    joystick.animate()
//                            .x(centerX - joystick.getWidth() / 2)
//                            .y(centerY - joystick.getHeight() / 2)
//                            .setDuration(100)
//                            .start();
//                    sendJoystickData(0, 0);
//                    jarak = 0;
//                    sudut = 0;
//                    teks.setText("Sudut : " + sudut);
//                    teks2.setText("Jarak : " + jarak);
//                    break;
//            }
//            return true;
//        });
        joystick.setOnTouchListener((v, event) -> {

            if (centerX == 0 && centerY == 0) {
                centerX = joystickBase.getX() + joystickBase.getWidth() / 2;

                centerY = joystickBase.getY() + joystickBase.getHeight() / 2;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    break;

                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX() - joystickBase.getX();
                    float y = event.getRawY() - joystickBase.getY();


                    float distance = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                    float someThreshold = 20;

                    if (distance > someThreshold) {
                        if (distance > baseRadius - knobRadius) {

                            float ratio = (baseRadius - knobRadius) / distance;
                            x = centerX + (x - centerX) * ratio;
                            y = centerY + (y - centerY) * ratio;
                        }


                        joystick.setX(x - knobRadius);
                        joystick.setY(y - knobRadius);
                    }
                    // Hitung sudut dan jarak
                    float angle = (float) Math.toDegrees(Math.atan2(y - centerY, x - centerX));
                    if (angle < 0) {
                        angle += 360;
                    }
                    sudut = (int) angle;
                    jarak = (int) ((distance / (baseRadius - knobRadius)) * 100);


                    teks.setText("" + sudut);
                    teks2.setText("" + jarak);

                    perbaruiData();
//                    sendJoystickData(sudut, jarak, value);
                    break;

                case MotionEvent.ACTION_UP:


                    joystick.animate()
                            .x(centerX - knobRadius)
                            .y(centerY - knobRadius)
                            .setDuration(200)
                            .withEndAction(() -> {

                            })
                            .start();
                    sudut = 0;
                    jarak = 0;
                    perbaruiData();
//                    sendJoystickData(0, 0, value);
                    teks.setText("0");
                    teks2.setText("0");
                    break;

            }
            return true;
        });


//        joystick.setOnTouchListener((v, event) -> {
//            float x = 0; // Inisialisasi dengan nilai default
//            float y = 0;
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    x = event.getRawX() - (centerX - knobRadius);
//                    y = event.getRawY() - (centerY - knobRadius);
//                    break;
//
//                case MotionEvent.ACTION_MOVE:
//                    x = event.getRawX() - centerX;
//                    y = event.getRawY() - centerY;
//
//                    float distance = (float) Math.sqrt(x * x + y * y);
//                    float someThreshold = 15;
//                    // Membatasi jarak knob ke radius base joystick
//                    if (distance > someThreshold) {
//                        if (distance > baseRadius - knobRadius) {
//                            x = (x / distance) * (baseRadius - knobRadius);
//                            y = (y / distance) * (baseRadius - knobRadius);
//                            distance = baseRadius - knobRadius;
//                        }
//
//                        // Atur posisi knob
//                        joystick.setX(centerX + x - knobRadius);
//                        joystick.setY(centerY + y - knobRadius);
//                    }
//                    // Hitung sudut dan jarak
//                    float angle = (float) Math.toDegrees(Math.atan2(y, x));
//                    if (angle < 0) {
//                        angle += 360;
//                    }
//                    int sudut = (int) angle;
//                    int jarak = (int) ((distance / (baseRadius - knobRadius)) * 100);
//
//                    // Tampilkan sudut dan jarak
//                    teks.setText("Sudut : " + sudut);
//                    teks2.setText("Jarak : " + jarak);
//
//                    // Kirim data joystick
//                    sendJoystickData(angle, distance);
//                    break;
//
//                case MotionEvent.ACTION_UP:
//                    // Kembalikan joystick ke tengah saat dilepas
//                    joystick.animate()
//                            .x(centerX + x - knobRadius)
//                            .y(centerY + y - knobRadius)
//                            .setDuration(500)
//                            .start();
//
//                    // Reset data sudut dan jarak ke 0
//                    sendJoystickData(0, 0);
//                    teks.setText("Sudut : 0");
//                    teks2.setText("Jarak : 0");
//                    break;
//            }
//            return true;
//        });

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value = progress - 1; //
                statusText.setText(""+value);
                perbaruiData();


//                if (value == 1) {
//                    sendJoystickData(jarak, sudut, value);
//                } else if (value == 0) {
//                    sendJoystickData(jarak, sudut, value);
//                } else if (value == -1) {
//                    sendJoystickData(jarak, sudut, value);
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(1);

            }
        });
    }

    private void perbaruiData() {
        kirimData(jarak, sudut, value);
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1) { // Pastikan ini sama dengan yang digunakan di requestPermissions
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d("Bluetooth", "Bluetooth permissions already granted");
//                startBLEConnection();
//            } else {
//                Log.d("Bluetooth", "Bluetooth permissions not granted");
//                Toast.makeText(this, "Bluetooth permissions are required to connect", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void checkBluetoothPermissions() {
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
////                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
////                ActivityCompat.requestPermissions(this, new String[]{
////                        Manifest.permission.BLUETOOTH_CONNECT,
////                        Manifest.permission.BLUETOOTH_SCAN
////                }, 1);
////            } else {
////                startBLEConnection();
////            }
////        } else {
////            if ( ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
////                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
//        Log.d("Permissions", "Checking Bluetooth permissions...");
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("Permissions", "Requesting Bluetooth permissions");
//            ActivityCompat.requestPermissions(this, new String[]{
////                        Manifest.permission.BLUETOOTH_CONNECT,
////                        Manifest.permission.BLUETOOTH_SCAN,
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//            }, REQUEST_BLUETOOTH_PERMISSIONS);
//        } else {
//            Log.d("BluetoothPermissions", "Location permissions already granted");
//            startBLEConnection();
//        }
////        }
//    }

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) { // Pastikan ini sama dengan yang digunakan di requestPermissions
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Izin diberikan, coba lagi untuk memulai koneksi
//                startBLEConnection();
//            } else {
//                Log.d("Bluetooth", "gaiso");
//                Toast.makeText(this, "Bluetooth permissions are required to connect", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startBLEConnection();
//            } else {
//                Toast.makeText(this, "Bluetooth permissions are required to connect", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void startBLEConnection() {
//        checkBluetoothPermissions();
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
//        if (scanner != null) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
//
//                scanner.startScan(new ScanCallback() {
//                    @Override
//                    public void onScanResult(int callbackType, ScanResult result) {
//                        BluetoothDevice device = result.getDevice();
//                        if (device.getAddress().equals(BLE_MAC_ADDRESS)) {
//                            // Cek izin sebelum menghentikan pemindaian
//                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
//
//                            scanner.stopScan(this);
//                            bluetoothGatt = device.connectGatt(MainActivity.this, false, gattCallback);
//                            }
//
//                            // Cek izin sebelum melakukan koneksi Bluetooth
////                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
////                                bluetoothGatt = device.connectGatt(MainActivity.this, false, gattCallback);
////                            }
//                        }
//                    }
//
//                    @Override
//                    public void onScanFailed(int errorCode) {
//                        Toast.makeText(MainActivity.this, "Scan failed with error: " + errorCode, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } else {
//                Toast.makeText(this, "BluetoothLeScanner is not available", Toast.LENGTH_SHORT).show();
//            }
//        }
//private void startBLEConnection() {
//    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//        Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
//        return;
//    }
//
//    BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
//    if (scanner == null) {
//        Toast.makeText(this, "BluetoothLeScanner is not available", Toast.LENGTH_SHORT).show();
//        return; // Hentikan eksekusi jika scanner tidak tersedia
//    }
//
//    // Memeriksa izin lokasi di sini
//    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//        Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
//        return; // Hentikan jika izin lokasi tidak diberikan
//    }
//    Log.d("BLE", "Starting Bluetooth scan...");
//    Toast.makeText(this, "Starting Bluetooth scan..", Toast.LENGTH_SHORT).show();
//    // Lanjutkan dengan memulai pemindaian
//    scanner.startScan(new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            Log.d("BLE", "Scan result received");
//            BluetoothDevice device = result.getDevice();
//            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
//                Log.d("BLE", "Device found: " + device.getName() + " Address: " + device.getAddress());
//
//                if (device.getAddress().equals(BLE_MAC_ADDRESS)) {
//                    // Lakukan koneksi GATT
//                    Log.d("BLE", "Target device found, attempting to connect...");
//                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
//
//                        scanner.stopScan(this); // Hentikan pemindaian setelah menemukan perangkat
//                        bluetoothGatt = device.connectGatt(MainActivity.this, false, gattCallback);
//                        Log.d("BLE", "Connecting to GATT server...");
//                    }
//                }
//            }else {
//                    Log.d("BLE", "Bluetooth scan permission not granted");
//                }
//
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            Toast.makeText(MainActivity.this, "Scan failed with error: " + errorCode, Toast.LENGTH_SHORT).show();
//            Log.e("BLE", "Scan failed with error: " + errorCode);
//
//        }
//    });
//}


    //    ini hc-05
    private void checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("BluetoothPermissions", "Minta izin lokasi");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            Log.d("BluetoothPermissions", "Izin Lokasi diberikan");
            connectToHC05();
        }
    }
    //sampe sini
//    private void checkBluetoothPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH_SCAN
//            }, REQUEST_BLUETOOTH_PERMISSIONS);
//        } else {
//            connectToHC05();
//        }
//    }

    // Metode ini menangani hasil permintaan izin

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToHC05();
            } else {
                Toast.makeText(this, "Izin Bluetooth diperlukan", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //hc-05
    private void connectToHC05() {
        Log.d("Bluetooth", "mencoba connect ke HC-05...");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d("Bluetooth", "Bluetooth tidak support");
            Toast.makeText(this, "Bluetooth tidak support", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.d("Bluetooth", "Bluetooth tidak nyala");
            Toast.makeText(this, "Nyalakan bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice hc05 = bluetoothAdapter.getRemoteDevice(HC_05_MAC_ADDRESS);
        try {
            bluetoothSocket = hc05.createRfcommSocketToServiceRecord(HC_05_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Berhasil connect ke HC-05", Toast.LENGTH_SHORT).show();
            Log.d("Bluetooth", "Berhasil connect ke HC-05");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Bluetooth", "Koneksi gagal : " + e.getMessage());
            Toast.makeText(this, "Koneksi gagal : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.d("Bluetooth", "Izin Bluetooth diperlukan");
            Toast.makeText(this, "Izin Bluetooth diperlukan", Toast.LENGTH_SHORT).show();
        }
    }
// sampe sini
//    private void connectToHC05() {
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
////            Toast.makeText(this, "Bluetooth permissions are required to connect", Toast.LENGTH_SHORT).show();
////            return;
////        }
//
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        BluetoothDevice hc05 = bluetoothAdapter.getRemoteDevice(HC_05_MAC_ADDRESS);
//        try {
//            bluetoothSocket = hc05.createRfcommSocketToServiceRecord(HC_05_UUID);
//            bluetoothSocket.connect();
//            outputStream = bluetoothSocket.getOutputStream();
//            Toast.makeText(this, "Connected to HC-05", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
//        } catch (SecurityException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Bluetooth permissions are required to connect", Toast.LENGTH_SHORT).show();
//        }
//    }
//        private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//            @Override
//            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                if (newState == BluetoothGatt.STATE_CONNECTED) {
//                    try {
//                        // Cek izin sebelum memanggil discoverServices
//                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                            bluetoothGatt.discoverServices();
//                        }
////                    } else {
////                        Toast.makeText(MainActivity.this, "Bluetooth permission required to discover services", Toast.LENGTH_SHORT).show();
////                    }
//                    } catch (SecurityException e) {
//                        // Menangani SecurityException jika izin tidak diberikan
//                        Toast.makeText(MainActivity.this, "Bluetooth permissions are required to discover services", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//
//    }


    private void kirimData(int jarak, int sudut, int value) {
        if (outputStream != null && bluetoothSocket.isConnected()) {
            try {
                String data = jarak + " " + sudut + " " + value;
                Log.d("sebelum dikirim", "nih : " + data);
                outputStream.write(data.getBytes());
                Log.d("Kirim", "udah ke kirim" + data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//private void kirimdata(int jumlah) {
//        jumlah = 5;
//    if (outputStream != null && bluetoothSocket.isConnected()) {
//        try {
//            String data = String.valueOf(jumlah);
//            outputStream.write(data.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}

}

