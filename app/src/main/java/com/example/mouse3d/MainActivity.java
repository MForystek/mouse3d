package com.example.mouse3d;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        BluetoothManagement.setBluetoothManager((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        BluetoothManagement.setMainActivityReference(this);

        bluetoothManagement = BluetoothManagement.getInstance();

        axisTextViews = getAxisTextViews();
        gyroscopeConfig();

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);

        if (leftButton != null) {
            leftButton.setOnClickListener(view -> Toast.makeText(MainActivity.this, R.string.leftButton, Toast.LENGTH_SHORT).show());
        }

        if (rightButton != null) {
            rightButton.setOnClickListener(view -> Toast.makeText(MainActivity.this, R.string.rightButton, Toast.LENGTH_SHORT).show());
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

    private void gyroscopeConfig() {
        gyroscope = new GyroscopeManager(MainActivity.this);
        gyroscope.setListener((rx, ry, rz) -> {
            try {
                axisTextViews.get(0).setText(String.valueOf((int) (rx * 100)));
                axisTextViews.get(1).setText(String.valueOf((int) (ry * 100)));
                axisTextViews.get(2).setText(String.valueOf((int) (rz * 100)));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    private List<TextView> getAxisTextViews() {
        return List.of(findViewById(R.id.xAxisDisplay), findViewById(R.id.yAxisDisplay), findViewById(R.id.zAxisDisplay));
    }
}