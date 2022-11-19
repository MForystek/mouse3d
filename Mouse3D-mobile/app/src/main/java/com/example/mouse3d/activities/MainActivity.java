package com.example.mouse3d.activities;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mouse3d.R;
import com.example.mouse3d.bluetooth.BluetoothManagement;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private BluetoothManagement bluetoothManagement;
    private ActivityResultLauncher<Intent> deviceListActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothConfig();
        registerDeviceListActivityLauncher();

        Button selectDeviceButton = findViewById(R.id.selectDeviceButton);
        selectDeviceButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, DeviceListActivity.class);
            deviceListActivityLauncher.launch(intent);
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothManagement.REQUEST_DISCOVERABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                exitApplicationDisplayingToastWithMessage("Device must be discoverable to pair with potential new devices");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void bluetoothConfig() {
        BluetoothManagement.setBluetoothManager((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
        BluetoothManagement.setMainActivityReference(this);
        bluetoothManagement = BluetoothManagement.getInstance();
    }

    private void registerDeviceListActivityLauncher() {
        deviceListActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent resultIntent = result.getData();
                        if (resultIntent != null) {
                            String address = resultIntent.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                            Log.i(TAG, "Selected device with address: " + address);
                            bluetoothManagement.setRemoteDevice(address);

                            Intent intent = new Intent(MainActivity.this, MouseControlActivity.class);
                            startActivity(intent);
                        } else {
                            exitApplicationDisplayingToastWithMessage("Some error has occured");
                        }
                    } else {
                        exitApplicationDisplayingToastWithMessage("Some error has occured");
                    }
                });
    }

    public void exitApplicationDisplayingToastWithMessage(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
    }
}