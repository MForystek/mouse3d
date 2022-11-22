package com.example.mouse3d.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mouse3d.model.MouseEventDto;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothClient {
    public static final String TAG = "BluetoothClient";
    public static final UUID SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final ObjectMapper objectMapper;
    private final BluetoothDevice bluetoothDevice;

    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    public BluetoothClient(BluetoothDevice bluetoothDevice) throws IOException {
        this.bluetoothDevice = bluetoothDevice;
        this.objectMapper = new ObjectMapper();
        initConnection();
    }

    @SuppressLint("MissingPermission")
    private void initConnection() throws IOException {
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SERVER_UUID);
            Log.i(TAG, "Created new socket");

            bluetoothSocket.connect();
            Log.i(TAG, "Connected to remote device");

            outputStream = bluetoothSocket.getOutputStream();
            Log.i(TAG, "Output stream created successfully");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to initialize connection");
            throw e;
        }
    }

    public void send(MouseEventDto mouseEventDto) {
        if (mouseEventDto == null) return;
        try {
           String json = objectMapper.writeValueAsString(mouseEventDto);
           send(json.getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void send(byte[] bytes) {
        if (bluetoothSocket.isConnected()) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Failed to send data");
            }
        }
    }

    public void close() {
        try {
            outputStream.close();
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
