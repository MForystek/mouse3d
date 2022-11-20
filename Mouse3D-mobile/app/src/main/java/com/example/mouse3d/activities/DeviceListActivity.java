package com.example.mouse3d.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.mouse3d.R;
import com.example.mouse3d.Util;
import com.example.mouse3d.bluetooth.BluetoothManagement;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
	private static final String TAG = "DeviceListActivity";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothManagement bluetoothManagement;
	private Button scanButton;
	private ProgressBar scanProgressSpinner;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private ArrayAdapter<String> newDevicesArrayAdapter;

	@SuppressLint("MissingPermission")
	private final OnItemClickListener deviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			bluetoothManagement.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the View
			String info = ((TextView) view).getText().toString();
			int MACAddressCharsLength = 17;
			String MACAddress = info.substring(info.length() - MACAddressCharsLength);

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, MACAddress);
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		setResult(Activity.RESULT_CANCELED);
		Util.hideNavigationBar(getWindow());

		configureBluetooth();
		initializeComponents();
	}

	private void configureBluetooth() {
		BluetoothManagement.setDeviceListActivityReference(this);
		bluetoothManagement = BluetoothManagement.getInstance();
		registerReceiverForBluetoothDiscoveryBroadcasts();
	}

	/**
	 * Must be used in onCreate() method to avoid exception with unregistered receiver
	 */
	private void registerReceiverForBluetoothDiscoveryBroadcasts() {
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(bluetoothManagement.getReceiver(), foundFilter);

		IntentFilter discoveryFinishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(bluetoothManagement.getReceiver(), discoveryFinishedFilter);
	}

	private void initializeComponents() {
		pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
		ListView pairedListView = findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(deviceClickListener);
		fillArrayWithPairedDevices();

		newDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
		ListView newDevicesListView = findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(newDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(deviceClickListener);

		ConstraintLayout scanButtonWrapper = findViewById(R.id.scan_button_wrapper);
		scanButton = findViewById(R.id.scan_button);
		scanProgressSpinner = findViewById(R.id.scan_progress_spinner);

		scanButton.setOnClickListener(view -> bluetoothManagement.doDiscovery());
		scanButtonWrapper.setOnClickListener(view -> {
			if (scanButton.getVisibility() == View.VISIBLE) {
				scanButton.callOnClick();
				scanButton.setVisibility(View.GONE);
				scanProgressSpinner.setVisibility(View.VISIBLE);
			}
		});
	}

	@SuppressLint("MissingPermission")
	private void fillArrayWithPairedDevices() {
		Set<BluetoothDevice> pairedDevices = bluetoothManagement.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				String deviceNameAndAddress = device.getName() + "\n" + device.getAddress();
				pairedDevicesArrayAdapter.add(deviceNameAndAddress);
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired).toString();
			pairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bluetoothManagement.cancelDiscovery();
		unregisterReceiver(bluetoothManagement.getReceiver());
	}

	public ArrayAdapter<String> getNewDevicesArrayAdapter() {
		return newDevicesArrayAdapter;
	}
}
