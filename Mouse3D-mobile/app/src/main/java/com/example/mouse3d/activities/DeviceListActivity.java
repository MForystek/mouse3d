package com.example.mouse3d.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class DeviceListActivity extends AppCompatActivity {
	private static final String TAG = "DeviceListActivity";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothManagement bluetoothManagement;
	private Button backButton;
	private ProgressBar refreshProgressSpinner;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		setResult(Activity.RESULT_CANCELED);
		Util.hideNavigationBar(getWindow());

		bluetoothManagement = BluetoothManagement.getInstance();
		initializeComponents();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshPairedDevices();
	}

	private void initializeComponents() {
		pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
		ListView pairedListView = findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(deviceClickListener);
		bluetoothManagement.fillArrayWithPairedDevices(pairedDevicesArrayAdapter, this);

		ConstraintLayout backButtonWrapper = findViewById(R.id.back_button_wrapper);
		backButton = findViewById(R.id.back_button);

		backButton.setOnClickListener(view -> {
			setResult(Activity.RESULT_OK);
			finish();
		});
		backButtonWrapper.setOnClickListener(view -> backButton.callOnClick());
	}

	private void refreshPairedDevices() {
		findViewById(R.id.nothing_paired_textview).setVisibility(View.GONE);
		pairedDevicesArrayAdapter.clear();
		bluetoothManagement.fillArrayWithPairedDevices(pairedDevicesArrayAdapter, this);
	}
}
