package com.example.mouse3d;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Mouse 3D");

        bluetoothAdapter = (((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter());

        configureBluetooth();

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);

        if (leftButton != null) {
            leftButton.setOnClickListener(view ->
                    Toast.makeText(MainActivity.this, R.string.leftButton, Toast.LENGTH_SHORT).show()
            );
        }

        if (rightButton != null) {
            rightButton.setOnClickListener(view ->
                    Toast.makeText(MainActivity.this, R.string.rightButton, Toast.LENGTH_SHORT).show()
            );
        }

    }

    private void configureBluetooth() {
        ensureBluetoothExists();
        ensureBluetoothIsEnabled();
    }

    private void ensureBluetoothExists() {
        if (bluetoothAdapter == null) {
            exitApplicationIfBluetoothNotAvailable();
        }
    }

    private void ensureBluetoothIsEnabled() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } catch (SecurityException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exitApplicationIfBluetoothNotAvailable() {
        Toast.makeText(MainActivity.this, "Bluetooth is required for Mouse 3D", Toast.LENGTH_LONG).show();
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                exitApplicationIfBluetoothNotAvailable();
            }
        }
    }
}