package com.example.mouse3d;

import static com.example.mouse3d.BluetoothConnection.TAG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothConnectionListener extends Thread {
    private final BluetoothServerSocket bluetoothServerSocket;

    private BluetoothConnection bluetoothConnection;

    public BluetoothConnectionListener(BluetoothAdapter bluetoothAdapter) {
        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("Mouse 3D", UUID.randomUUID());
        } catch (IOException | SecurityException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        bluetoothServerSocket = tmp;
    }

    @Override
    public void run() {
        BluetoothSocket socket;
        while (true) {
            try {
                socket = bluetoothServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                try {
                    bluetoothServerSocket.close();
                    manageConnectedBluetoothSocket(socket);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void cancel() {
        try {
            bluetoothServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manageConnectedBluetoothSocket(BluetoothSocket socket) {
        bluetoothConnection = new BluetoothConnection(socket);
        bluetoothConnection.start();
        sendHelloWorldViaBluetooth();
    }

    //TODO delete after testing or transform into a test maybe?
    private void sendHelloWorldViaBluetooth() {
        String message = "Hello world!";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        bluetoothConnection.write(bytes);
    }
}
