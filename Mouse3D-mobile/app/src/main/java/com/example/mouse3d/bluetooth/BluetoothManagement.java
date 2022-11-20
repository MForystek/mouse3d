package com.example.mouse3d.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.example.mouse3d.R;
import com.example.mouse3d.activities.DeviceListActivity;
import com.example.mouse3d.activities.MainActivity;
import com.example.mouse3d.exception.NoBluetoothManagerException;
import com.example.mouse3d.exception.NoMainActivityReferenceException;

import java.util.Set;

public class BluetoothManagement {
    public static final int REQUEST_ENABLE_BT = 1;

    private static MainActivity mainActivityReference;
    private static DeviceListActivity deviceListActivityReference;
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

    public static void setDeviceListActivityReference(DeviceListActivity deviceListActivityReference) {
        BluetoothManagement.deviceListActivityReference = deviceListActivityReference;
    }

    @SuppressLint("MissingPermission")
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String deviceNameAndAddress = device.getName() + "\n" + device.getAddress();
                    deviceListActivityReference.getNewDevicesArrayAdapter().add(deviceNameAndAddress);
                //}
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (deviceListActivityReference.getNewDevicesArrayAdapter().getCount() == 0) {
                    String noDevices = deviceListActivityReference.getResources().getText(R.string.none_found).toString();
                    deviceListActivityReference.getNewDevicesArrayAdapter().add(noDevices);
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void doDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    public void cancelDiscovery() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @SuppressWarnings("MissingPermission")
    public Set<BluetoothDevice> getBondedDevices() {
        return bluetoothAdapter.getBondedDevices();
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

    public void setRemoteDevice(String deviceName) {
        actualRemoteDevice = this.bluetoothAdapter.getRemoteDevice(deviceName);
    }

    public BluetoothDevice getActualRemoteDevice() {
        return actualRemoteDevice;
    }

    public BroadcastReceiver getReceiver() {
        return receiver;
    }
}
