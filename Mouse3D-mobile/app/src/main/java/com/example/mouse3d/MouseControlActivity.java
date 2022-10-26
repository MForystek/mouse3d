package com.example.mouse3d;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mouse3d.model.MouseAction;
import com.mouse3d.model.MouseEventDto;

import java.io.IOException;
import java.util.List;

public class MouseControlActivity extends AppCompatActivity {
    private BluetoothClient bluetoothClient;
    private GyroscopeManager gyroscope;
    private List<TextView> axisTextViews;

    public static final String TAG = "MouseControlActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse_control);
        setTitle("Mouse 3D");

        axisTextViews = getAxisTextViews();

        initBluetoothClient();
        gyroscopeConfig();

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);
        Button resetOrientationButton = findViewById(R.id.resetOrientationButton);

        if (leftButton != null) {
            leftButton.setOnClickListener(view -> Toast.makeText(MouseControlActivity.this, R.string.leftButton, Toast.LENGTH_SHORT).show());
        }

        if (rightButton != null) {
            rightButton.setOnClickListener(view -> Toast.makeText(MouseControlActivity.this, R.string.rightButton, Toast.LENGTH_SHORT).show());
        }

        if (resetOrientationButton != null) {
            resetOrientationButton.setOnClickListener(view ->
                    gyroscope.setRelativeDirections(gyroscope.getCurrentDirections()));
        }
    }

    private void initBluetoothClient() {
      BluetoothDevice bluetoothDevice = BluetoothManagement.getInstance().getActualRemoteDevice();
        try {
            bluetoothClient = new BluetoothClient(bluetoothDevice);
        } catch (IOException e) {
            //TODO
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
        bluetoothClient.close();
    }

    private void gyroscopeConfig() {
        gyroscope = new GyroscopeManager(MouseControlActivity.this);
        gyroscope.setListener((rx, ry, rz) -> {
            try {
                axisTextViews.get(0).setText(String.valueOf((int) (rx)));
                axisTextViews.get(1).setText(String.valueOf((int) (ry)));
                axisTextViews.get(2).setText(String.valueOf((int) (rz)));

                MouseEventDto mouseEventDto = new MouseEventDto();
                mouseEventDto.setAction(MouseAction.MOVE);
                mouseEventDto.setX((int) rx);
                mouseEventDto.setY((int) ry);

                if (bluetoothClient.send(mouseEventDto)) {
                    Log.i(TAG, mouseEventDto.toString());
                } else {
                    Log.w(TAG, "Failed to send message: " + mouseEventDto);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private List<TextView> getAxisTextViews() {
        return List.of(findViewById(R.id.xAxisDisplay), findViewById(R.id.yAxisDisplay), findViewById(R.id.zAxisDisplay));
    }
}