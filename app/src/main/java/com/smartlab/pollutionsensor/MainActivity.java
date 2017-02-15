package com.smartlab.pollutionsensor;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.smartlab.pollutionsensor.ui.BaseDialog;

public class MainActivity extends AppCompatActivity {

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	private BluetoothAdapter bluetoothAdapter ;
	private BluetoothGatt bluetoothGatt ;
	private int connectionState = STATE_DISCONNECTED;

	private static final long SCAN_PERIOD = 10000 ;

	private ListView keyValuePairsListView ;
	private ListView devicesListView ;

	private Handler scanHandler ;

	private DeviceListAdapter deviceListAdapter ;
	private KeyValueListAdapter keyValueListAdapter ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState) ;
		setContentView(R.layout.activity_main) ;

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1) ;
		}

		keyValuePairsListView = (ListView) findViewById(R.id.activity_main_list_view_key_value_pairs) ;
		keyValueListAdapter = new KeyValueListAdapter(this) ;
		keyValuePairsListView.setEmptyView(findViewById(R.id.activity_main_list_view_key_value_pairs_list_empty_layout)) ;
		keyValuePairsListView.setAdapter(keyValueListAdapter) ;

		devicesListView = (ListView) findViewById(R.id.activity_main_list_view_devices) ;
		deviceListAdapter = new DeviceListAdapter(this) ;
		devicesListView.setEmptyView(findViewById(R.id.activity_main_list_view_devices_list_empty_layout)) ;
		devicesListView.setAdapter(deviceListAdapter) ;
		devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				bluetoothGatt = deviceListAdapter.getItem(position).connectGatt(MainActivity.this, false, bluetoothGattCallback);
			}
		});

		findViewById(R.id.activity_main_floating_action_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AddKeyValuePairDialog(MainActivity.this).show() ;
			}
		});

		handleBluetoothAdapter() ;
		scanHandler = new Handler() ;
		handleScan() ;
	}

	private void handleBluetoothAdapter() {
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();
	}

	private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			String intentAction ;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED ;
				connectionState = STATE_CONNECTED ;
				//broadcastUpdate(intentAction) ;
				Log.i(Constants.DEBUG_TAG, "Connected to GATT server.");
				Log.i(Constants.DEBUG_TAG, "Attempting to start service discovery:" + bluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				connectionState = STATE_DISCONNECTED;
				Log.i(Constants.DEBUG_TAG, "Disconnected from GATT server.");
				//broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				//broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w(Constants.DEBUG_TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				//broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}
	};

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.e(Constants.DEBUG_TAG, "found new device: " + device.getName()) ;
					deviceListAdapter.addBluetoothDevice(device) ;
				}
			});
		}
	};

	private void handleScan() {
		bluetoothAdapter.startLeScan(scanCallback);
		Log.e(Constants.DEBUG_TAG, "starting scan ... ") ;
		scanHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.e(Constants.DEBUG_TAG, "stopping scan scan ... ") ;
				bluetoothAdapter.stopLeScan(scanCallback) ;
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						handleScan() ;
					}
				}, 3 * SCAN_PERIOD) ;
			}
		}, SCAN_PERIOD) ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	class AddKeyValuePairDialog extends BaseDialog {

		private EditText keyEditText ;
		private EditText valueEditText ;

		public AddKeyValuePairDialog(Activity context) {
			super(context) ;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			setContentView(R.layout.dialog_add_key_value_pair) ;
			handleTitle() ;
			handleButtons() ;
			title.setText(R.string.commons_attention) ;

			keyEditText = (EditText) findViewById(R.id.dialog_add_key_value_pair_edit_text_key) ;
			findViewById(R.id.dialog_add_key_value_pair_text_view_key) ;

			valueEditText = (EditText) findViewById(R.id.dialog_add_key_value_pair_edit_text_value) ;
			findViewById(R.id.dialog_add_key_value_pair_text_view_value) ;
		}

		@Override
		protected void onPositiveButtonClick() {
			keyValueListAdapter.addKeyValuePair(new KeyValuePair(keyEditText.getText().toString(), valueEditText.getText().toString()));
			super.onPositiveButtonClick() ;
		}

		@Override
		protected void onNegativeButtonClick() {
			super.onNegativeButtonClick() ;
		}
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\
}
