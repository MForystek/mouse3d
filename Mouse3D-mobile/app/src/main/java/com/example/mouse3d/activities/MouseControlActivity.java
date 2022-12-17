package com.example.mouse3d.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mouse3d.R;
import com.example.mouse3d.Utils;
import com.example.mouse3d.bluetooth.BluetoothClient;
import com.example.mouse3d.bluetooth.BluetoothManagement;
import com.example.mouse3d.sensors.GyroscopeManager;
import com.example.mouse3d.sensors.SwipeListener;
import com.example.mouse3d.sensors.UserAction;
import com.mouse3d.model.MouseAction;
import com.mouse3d.model.MouseEventDto;

import java.io.IOException;

public class MouseControlActivity extends AppCompatActivity {
    public static final String TAG = "MouseControlActivity";

    private BluetoothClient bluetoothClient;
    private GyroscopeManager gyroscope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse_control);
        Utils.hideNavigationBar(getWindow());

        initBluetoothClient();
        gyroscopeConfig();
        initializeComponents();
    }

    private void initBluetoothClient() {
        try {
            bluetoothClient = new BluetoothClient(BluetoothManagement.getInstance().getActualRemoteDevice());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed initializing BluetoothClient");
        }
    }

    private void gyroscopeConfig() {
        gyroscope = new GyroscopeManager(MouseControlActivity.this);
        gyroscope.setListener((rx, ry, rz) -> {
            try {
                MouseEventDto mouseEventDto = new MouseEventDto();

                int x_norm = normaliseX((int) rx);
                int y_norm = normaliseY((int) ry);
                //System.out.println(x_norm + " " + y_norm);

                if (UserAction.LEFT_CLICK.value != 0) {
                    mouseEventDto.setAction(MouseAction.LEFT_CLICK);
                    UserAction.LEFT_CLICK.value = 0;
                } else if (UserAction.RIGHT_CLICK.value != 0) {
                    mouseEventDto.setAction(MouseAction.RIGHT_CLICK);
                    UserAction.RIGHT_CLICK.value = 0;
                } else if (UserAction.MIDDLE_CLICK.value != 0) {
                    mouseEventDto.setAction(MouseAction.MIDDLE_CLICK);
                    UserAction.MIDDLE_CLICK.value = 0;
                } else if (UserAction.SWIPE.value != 0) {
                    mouseEventDto.setAction(MouseAction.SCROLL);
                    mouseEventDto.setY(UserAction.SWIPE.value);
                    UserAction.SWIPE.value = 0;
                } else {
                    mouseEventDto.setAction(MouseAction.MOVE);
                    mouseEventDto.setX(x_norm);
                    mouseEventDto.setY(y_norm);
                }

                bluetoothClient.send(mouseEventDto);
            } catch (Exception e) {
                //System.out.println(e.getMessage());
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeComponents() {
        TextView deviceLabel = findViewById(R.id.device_label);

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);
        Button middleButton = findViewById(R.id.middleButton);
        Button resetOrientationButton = findViewById(R.id.resetOrientationButton);
        Button cancelButton = findViewById(R.id.cancel_button);

        BluetoothDevice bluetoothDevice = BluetoothManagement.getInstance().getActualRemoteDevice();
        deviceLabel.setText(bluetoothDevice.getName());
        new SwipeListener(middleButton);

        leftButton.setOnClickListener(view -> UserAction.LEFT_CLICK.value = 1);
        rightButton.setOnClickListener(view -> UserAction.RIGHT_CLICK.value = 1);
        middleButton.setOnClickListener(view -> UserAction.MIDDLE_CLICK.value = 1);
        resetOrientationButton.setOnClickListener(view -> gyroscope.setRelativeDirections(gyroscope.getCurrentDirections()));
        cancelButton.setOnClickListener(view -> {
            setResult(Activity.RESULT_OK);
            onPause();
            finish();
        });
    }

    private int normaliseX(int value) {
//        System.out.print("X " + value);
        final int maxValueX = 60;
        value = Math.min(value, maxValueX);
        value = Math.max(value, -maxValueX);
        value += maxValueX;
        return 100 * value / (2 * maxValueX);
    }

    private int normaliseY(int value) {
//        System.out.println(" Y " + value);
        final int maxValueY = 60;
        value = Math.min(value, maxValueY);
        value = Math.max(value, -maxValueY);
        value += maxValueY;
        return 100 * value / (2 * maxValueY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroscope.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gyroscope.unregister();
        if (bluetoothClient != null) {
            bluetoothClient.close();
        }
    }
}