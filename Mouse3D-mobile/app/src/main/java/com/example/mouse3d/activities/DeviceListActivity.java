package com.example.mouse3d.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mouse3d.R;
import com.example.mouse3d.bluetooth.BluetoothManagement;

import java.util.Set;

public class DeviceListActivity extends Activity {
	private static final String TAG = "DeviceListActivity";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothManagement bluetoothManagement;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private ArrayAdapter<String> newDevicesArrayAdapter;

	@SuppressLint("MissingPermission")
	private final OnItemClickListener deviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			bluetoothManagement.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the View
			String info = ((TextView) view).getText().toString();
			String address = info.substring(info.length() - 17);

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and changes the title when discovery is finished
	@SuppressWarnings("deprecation")
	@SuppressLint("MissingPermission")
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					newDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.select_device);
				if (newDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(R.string.none_found).toString();
					newDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

	@SuppressWarnings("deprecation")
	@SuppressLint("MissingPermission")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bluetoothManagement = BluetoothManagement.getInstance();

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button scanButton = findViewById(R.id.button_scan);
		scanButton.setOnClickListener(view -> {
			doDiscovery();
			view.setVisibility(View.GONE);
		});

		// Initialize array adapters. One for already paired devices and one for newly discovered devices
		pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
		newDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

		// Find and set up the ListView for paired devices
		ListView pairedListView = findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(deviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(newDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(deviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = bluetoothManagement.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		} else {
			String noDevices = getResources().getText(R.string.none_paired).toString();
			pairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@SuppressWarnings("deprecation")
	private void doDiscovery() {
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		bluetoothManagement.doDiscovery();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bluetoothManagement.cancelDiscovery();
		unregisterReceiver(receiver);
	}

}