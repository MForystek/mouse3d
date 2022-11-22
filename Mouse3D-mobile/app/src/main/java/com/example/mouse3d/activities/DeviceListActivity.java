package com.example.mouse3d.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mouse3d.R;
import com.example.mouse3d.Utils;
import com.example.mouse3d.bluetooth.BluetoothManagement;

public class DeviceListActivity extends AppCompatActivity {
	private static final String TAG = "DeviceListActivity";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothManagement bluetoothManagement;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	private Button pairModeButton;
	private boolean isDiscovering;

	@SuppressLint("MissingPermission")
	private final OnItemClickListener deviceClickListener = (parent, view, position, id) -> {
		// Get the device MAC address, which is the last 17 chars in the View
		String info = ((TextView) view).getText().toString();
		int MACAddressCharsLength = 17;
		String MACAddress = info.substring(info.length() - MACAddressCharsLength);

		// Create the result Intent and include the MAC address
		Intent intent = new Intent();
		intent.putExtra(EXTRA_DEVICE_ADDRESS, MACAddress);
		setResult(Activity.RESULT_OK, intent);
		finish();
	};

	@SuppressLint("UseCompatLoadingForDrawables")
	@SuppressWarnings("deprecation")
	private final Runnable bluetoothDiscoveryTimer = () -> {
		int time = BluetoothManagement.DISCOVERY_TIME_SECONDS;
		String text;
		for (int i = time; i >= 0; i--) {
			text = i + "s";
			pairModeButton.setText(text);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		pairModeButton.setText(R.string.pair_mode_button);
		pairModeButton.setBackground(getResources().getDrawable(R.drawable.gradient_primary_clickable));
		refreshPairedDevices();
		isDiscovering = false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		setResult(Activity.RESULT_CANCELED);
		Utils.hideNavigationBar(getWindow());

		bluetoothManagement = BluetoothManagement.getInstance();
		initializeComponents();
	}

	@SuppressWarnings("deprecation")
	private void initializeComponents() {
		pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
		pairModeButton = findViewById(R.id.pair_mode_button);
		ListView pairedListView = findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(deviceClickListener);
		Button backButton = findViewById(R.id.back_button);
		SwipeRefreshLayout pairedDevicesSwipeRefreshWrapper = findViewById(R.id.paired_devices_swipe_refresh_wrapper);
		Handler handler = new Handler();
		int delay = 1000;

		backButton.setOnClickListener(view -> {
			setResult(Activity.RESULT_OK);
			finish();
		});
		pairModeButton.setOnClickListener(view -> {
			if (!isDiscovering) {
				bluetoothManagement.enableBeingDiscoverable(this);
			}
		});
		pairedDevicesSwipeRefreshWrapper.setOnRefreshListener(() -> {
			refreshPairedDevices();
			pairedDevicesSwipeRefreshWrapper.setRefreshing(false);
		});
		handler.postDelayed(new Runnable() {
			public void run() {
				refreshPairedDevices();
				handler.postDelayed(this, delay);
			}
		}, delay);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshPairedDevices();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//TODO seems that it's not possible to cancel discoverable without user interaciton
		//bluetoothManagement.cancelBeingDiscoverable();
	}

	private void refreshPairedDevices() {
		findViewById(R.id.nothing_paired_textview).setVisibility(View.GONE);
		pairedDevicesArrayAdapter.clear();
		bluetoothManagement.fillArrayWithPairedDevices(pairedDevicesArrayAdapter, this);
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == BluetoothManagement.REQUEST_DISCOVERABLE_BT) {
			if (resultCode == BluetoothManagement.DISCOVERY_TIME_SECONDS) {
				if (!isDiscovering) {
					pairModeButton.setBackground(getResources().getDrawable(R.drawable.gradient_primary_darker));
					new Thread(bluetoothDiscoveryTimer).start();
					isDiscovering = true;
				}
			}
			if (resultCode == RESULT_CANCELED) {
				String message = "Device must be discoverable to pair with new devices";
				Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			}
		}
	}
}
