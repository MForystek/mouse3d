package com.example.mouse3d;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BluetoothManagement bluetoothManagement;
    private GyroscopeManager gyroscope;
    private List<TextView> axisTextViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Mouse 3D");

        bluetoothConfig();

        axisTextViews = getAxisTextViews();
        gyroscopeConfig();

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);
        Button resetOrientationButton = findViewById(R.id.resetOrientationButton);

        if (leftButton != null) {
            leftButton.setOnClickListener(view -> Toast.makeText(MainActivity.this, R.string.leftButton, Toast.LENGTH_SHORT).show());
        }

        if (rightButton != null) {
            rightButton.setOnClickListener(view -> Toast.makeText(MainActivity.this, R.string.rightButton, Toast.LENGTH_SHORT).show());
        }

        if (resetOrientationButton != null) {
            resetOrientationButton.setOnClickListener(view ->
                    gyroscope.setRelativeDirections(gyroscope.getCurrentDirections()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothManagement.REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                exitApplicationDisplayingToastWithMessage("Enabling Bluetooth is required for Mouse 3D");
            }
        }
        if (requestCode == BluetoothManagement.REQUEST_DISCOVERABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                exitApplicationDisplayingToastWithMessage("Device must be discoverable to pair with potential new devices");
            }
        }
    }

    public void exitApplicationDisplayingToastWithMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
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
    }

    private void bluetoothConfig() {
        BluetoothManagement.setBluetoothManager((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        BluetoothManagement.setMainActivityReference(this);
        bluetoothManagement = BluetoothManagement.getInstance();
    }

    private void gyroscopeConfig() {
        gyroscope = new GyroscopeManager(MainActivity.this);
        gyroscope.setListener((rx, ry, rz) -> {
            try {
                axisTextViews.get(0).setText(String.valueOf((int) (rx)));
                axisTextViews.get(1).setText(String.valueOf((int) (ry)));
                axisTextViews.get(2).setText(String.valueOf((int) (rz)));
                bluetoothManagement.write(ByteBuffer.allocate(12).putFloat(rx).putFloat(ry).putFloat(rz).array());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private List<TextView> getAxisTextViews() {
        return List.of(findViewById(R.id.xAxisDisplay), findViewById(R.id.yAxisDisplay), findViewById(R.id.zAxisDisplay));
    }
}