package com.example.mouse3d;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.widget.Toast;

import com.example.mouse3d.exception.NoBluetoothManagerException;
import com.example.mouse3d.exception.NoMainActivityReferenceException;

public class BluetoothManagement {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_DISCOVERABLE_BT = 2;
    public static final int DISCOVERY_TIME_SECONDS = 120;

    private static MainActivity mainActivityReference;
    private static BluetoothManager bluetoothManager;
    private static BluetoothManagement instance;
    private static BluetoothDevice actualDevice;

    private BluetoothAdapter bluetoothAdapter;

    public static BluetoothManagement getInstance() {
        if (bluetoothManager == null) {
            throw new NoBluetoothManagerException();
        }

        if (mainActivityReference == null) {
            throw new NoMainActivityReferenceException();
        }

        if (instance == null) {
            instance = new BluetoothManagement();
        }
        return instance;
    }

    public static void setBluetoothManager(BluetoothManager bluetoothManager) {
        BluetoothManagement.bluetoothManager = bluetoothManager;
    }

    public static void setMainActivityReference(MainActivity mouseControlActivityReference) {
        BluetoothManagement.mainActivityReference = mouseControlActivityReference;
    }

    private BluetoothManagement() {
        bluetoothAdapter = bluetoothManager.getAdapter();
        configureBluetooth();
    }

    private void configureBluetooth() {
        ensureBluetoothExists();
        ensureBluetoothIsEnabled();
        enableBeingDiscoverable();
    }

    private void ensureBluetoothExists() {
        if (bluetoothAdapter == null) {
            mainActivityReference.exitApplicationDisplayingToastWithMessage("Bluetooth is required for Mouse 3D");
        }
    }

    @SuppressWarnings("deprecation")
    private void ensureBluetoothIsEnabled() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                mainActivityReference.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } catch (SecurityException e) {
                Toast.makeText(mainActivityReference, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void enableBeingDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERY_TIME_SECONDS);
        try {
            mainActivityReference.startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);
        } catch (SecurityException e) {
            Toast.makeText(mainActivityReference, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public BluetoothDevice getRemoteDevice(String deviceName) {
        actualDevice = this.bluetoothAdapter.getRemoteDevice(deviceName);
        return actualDevice;
    }

    public BluetoothDevice getActualDevice() {
        return actualDevice;
    }
}
