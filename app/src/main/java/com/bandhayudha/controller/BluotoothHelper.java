package com.bandhayudha.controller;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluotoothHelper {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // UUID Standar buat Serial Port Profile

    public void BluetoothHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

}
