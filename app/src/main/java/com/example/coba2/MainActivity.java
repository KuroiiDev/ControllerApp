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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joystick = findViewById(R.id.knob);
        joystickBase = findViewById(R.id.base);
        final TextView sudutText = findViewById(R.id.sudut_value);
        final TextView jarakText = findViewById(R.id.jarak_value);
        final TextView nilaiText = findViewById(R.id.nilai_value);
        Button connectButton = findViewById(R.id.connect);
        SeekBar slider = findViewById(R.id.slider);
        slider.setProgress(1);
        connectButton.setOnClickListener(v -> checkBluetoothPermissions());

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

                    float distanceRaw = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                    float distance = Math.min(distanceRaw, baseRadius - knobRadius); // Coba tak batasi Jarak e
                    float someThreshold = 20;

                    if (distanceRaw > someThreshold) {
                        if (distanceRaw > baseRadius - knobRadius) {

                            float ratio = (baseRadius - knobRadius) / distanceRaw;
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

                    sudutText.setText("" + sudut);
                    jarakText.setText("" + jarak);

                    perbaruiData();
//                    sendJoystickData(sudut, jarak, value);
                    break;

                case MotionEvent.ACTION_UP:


                    joystick.animate()
                            .x(centerX - knobRadius)
                            .y(centerY - knobRadius)
                            .setDuration(200)
                            .withEndAction(() -> {})
                            .start();
                    sudut = 0;
                    jarak = 0;
                    perbaruiData();
//                    sendJoystickData(0, 0, value);
                    sudutText.setText("0");
                    jarakText.setText("0");
                    break;
            }
            return true;
        });

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value = progress - 1; //
                nilaiText.setText(""+value);
                perbaruiData();
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

    //    ini hc-05
    private void checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("BluetoothPermissions", "Attempting Location Permission");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            Log.d("BluetoothPermissions", "Location Permission is Allowed");
            connectToHC05();
        }
    }
    //sampe sini

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
        Log.d("Bluetooth", "Attempting to connect to HC-05...");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d("Bluetooth", "Bluetooth Unsupported");
            Toast.makeText(this, "Device Not Supporting bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.d("Bluetooth", "Bluetooth Inactive");
            Toast.makeText(this, "Please Turn On your bluetooth!", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice hc05 = bluetoothAdapter.getRemoteDevice(HC_05_MAC_ADDRESS);
        try {
            bluetoothSocket = hc05.createRfcommSocketToServiceRecord(HC_05_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Successfully connected to HC-05", Toast.LENGTH_SHORT).show();
            Log.d("Bluetooth", "Successfully connected to HC-05");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Bluetooth", "Connection Error : " + e.getMessage());
            Toast.makeText(this, "Connection Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.d("Bluetooth", "Please Allow Bluetooth");
            Toast.makeText(this, "Please Allow Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }
// sampe sini

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
}

