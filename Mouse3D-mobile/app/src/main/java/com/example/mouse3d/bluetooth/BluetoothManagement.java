package com.example.mouse3d.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mouse3d.R;
import com.example.mouse3d.Utils;
import com.example.mouse3d.activities.MainActivity;
import com.example.mouse3d.exception.NoBluetoothManagerException;
import com.example.mouse3d.exception.NoMainActivityReferenceException;

import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothManagement {
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_DISCOVERABLE_BT = 2;
    public static final int DISCOVERY_TIME_SECONDS = 60;

    private static MainActivity mainActivityReference;
    private static BluetoothManager bluetoothManager;
    private static BluetoothManagement instance;

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice actualRemoteDevice;

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

    @SuppressLint("MissingPermission")
    public void fillArrayWithPairedDevices(ArrayAdapter<String> pairedDevicesArrayAdapter, Activity activity) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //TODO show only devices with correct profile (e.g. no audio, no mouse, etc.)
                String deviceNameAndAddress = device.getName() + "\n" + device.getAddress();
                pairedDevicesArrayAdapter.add(deviceNameAndAddress);
            }
        } else {
            //TODO test how it looks
            activity.findViewById(R.id.nothing_paired_textview).setVisibility(View.VISIBLE);
        }
    }

    private BluetoothManagement() {
        bluetoothAdapter = bluetoothManager.getAdapter();
        configureBluetooth();
    }

    private void configureBluetooth() {
        ensureBluetoothExists();
        ensureBluetoothIsEnabled();
    }

    private void ensureBluetoothExists() {
        if (bluetoothAdapter == null) {
            Utils.exitApplicationDisplayingToastWithMessage(mainActivityReference, "Bluetooth is required for Mouse 3D");
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
    public void enableBeingDiscoverable(AppCompatActivity activity) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERY_TIME_SECONDS);
        try {
            activity.startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);
        } catch (SecurityException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void cancelBeingDiscoverable() {
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setDiscoverableTimeout.setAccessible(true);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(bluetoothAdapter, 1);
            setScanMode.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);

            setDiscoverableTimeout.setAccessible(false);
            setScanMode.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRemoteDevice(String deviceName) {
        actualRemoteDevice = this.bluetoothAdapter.getRemoteDevice(deviceName);
    }

    public BluetoothDevice getActualRemoteDevice() {
        return actualRemoteDevice;
    }
}
