package com.example.mouse3d;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothConnection extends Thread {
    public static final String TAG = "MOUSE_3D_DEBUG";
    private static final int BUFFER_SIZE = 2048;
    private Handler handler;
    private final BluetoothSocket bluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private byte[] buffer;

    public BluetoothConnection(BluetoothSocket bluetoothSocket) {
        handler = new Handler(Looper.myLooper()); //TODO How it should be initialized?
        this.bluetoothSocket = bluetoothSocket;
        InputStream tmpInStr = null;
        OutputStream tmpOutStr = null;

        try {
            tmpInStr = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occured when creating input stream", e);
        }
        try {
            tmpOutStr = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occured when creating output stream", e);
        }

        inputStream = tmpInStr;
        outputStream = tmpOutStr;
    }

    @Override
    public void run() {
        buffer = new byte[BUFFER_SIZE];
        int readBytes;

        while (true) {
            try {
                readBytes = inputStream.read(buffer);
                Message readMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_READ.getValue(), readBytes, -1, buffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);

            Message writtenMsg = handler.obtainMessage(
                    MessageConstants.MESSAGE_WRITE.getValue(), -1, -1, buffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occured when sending data", e);

            Message writtenErrorMsg = handler.obtainMessage(
                    MessageConstants.MESSAGE_TOAST.getValue());
            Bundle bundle = new Bundle();
            bundle.putString("toast", "Couldn't send data to the other device");
            writtenErrorMsg.setData(bundle);
            handler.sendMessage(writtenErrorMsg);
        }
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
