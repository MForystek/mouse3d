package com.example.mouse3d.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.mouse3d.R;
import com.example.mouse3d.Util;
import com.example.mouse3d.bluetooth.BluetoothClient;
import com.example.mouse3d.bluetooth.BluetoothManagement;
import com.example.mouse3d.sensors.GyroscopeManager;
import com.example.mouse3d.sensors.SwipeListener;
import com.example.mouse3d.sensors.UserAction;
import com.mouse3d.model.MouseAction;
import com.mouse3d.model.MouseEventDto;

import java.io.IOException;
import java.util.List;

public class MouseControlActivity extends AppCompatActivity {
    public static final String TAG = "MouseControlActivity";

    private BluetoothClient bluetoothClient;
    private GyroscopeManager gyroscope;
    private List<TextView> axisTextViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse_control);

        Util.hideNavigationBar(getWindow());

        ConstraintLayout backgroundLayout = findViewById(R.id.mouse_control_layout);
        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);
        Button middleButton = findViewById(R.id.middleButton);
        Button resetOrientationButton = findViewById(R.id.resetOrientationButton);
        new SwipeListener(backgroundLayout);
        new SwipeListener(middleButton);

        axisTextViews = getAxisTextViews();

        initBluetoothClient();
        gyroscopeConfig();

        if (leftButton != null) {
            leftButton.setOnClickListener(view -> UserAction.LEFT_CLICK.value = 1);
        }
        if (rightButton != null) {
            rightButton.setOnClickListener(view -> UserAction.RIGHT_CLICK.value = 1);
        }
        if (middleButton != null) {
            middleButton.setOnClickListener(view -> UserAction.MIDDLE_CLICK.value = 1);
        }
        if (resetOrientationButton != null) {
            resetOrientationButton.setOnClickListener(view -> gyroscope.setRelativeDirections(gyroscope.getCurrentDirections()));
        }
    }

    private void initBluetoothClient() {
        try {
            bluetoothClient = new BluetoothClient(BluetoothManagement.getInstance().getActualRemoteDevice());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed initializing BluetoothClient");
        }
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

    private List<TextView> getAxisTextViews() {
        return List.of(findViewById(R.id.xAxisDisplay), findViewById(R.id.yAxisDisplay), findViewById(R.id.zAxisDisplay));
    }

    private void gyroscopeConfig() {
        gyroscope = new GyroscopeManager(MouseControlActivity.this);
        gyroscope.setListener((rx, ry, rz) -> {
            try {
                MouseEventDto mouseEventDto = new MouseEventDto();;

                int x_norm = normalise((int) rx);
                int y_norm = normalise((int) ry);

                axisTextViews.get(0).setText(String.valueOf(x_norm));
                axisTextViews.get(1).setText(String.valueOf(y_norm));
                axisTextViews.get(2).setText(String.valueOf((int) (rz)));

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
                System.out.println(e.getMessage());
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int normalise(int value) {
        final int plus_minus_threshold = 180;
        final int max_value = 30;

        if (value > plus_minus_threshold) {
            value = -value;
        }
        if (value < -max_value) {
            return -max_value;
        }
        if (value > max_value) {
            return max_value;
        }
        return value;
    }
}