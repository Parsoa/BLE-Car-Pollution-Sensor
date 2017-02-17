package com.smartlab.pollutionsensor;

import android.Manifest ;
import android.app.Activity ;
import android.bluetooth.BluetoothAdapter ;
import android.bluetooth.BluetoothDevice ;
import android.bluetooth.BluetoothGatt ;
import android.bluetooth.BluetoothGattCallback ;
import android.bluetooth.BluetoothGattCharacteristic ;
import android.bluetooth.BluetoothManager ;
import android.bluetooth.BluetoothProfile ;
import android.content.BroadcastReceiver ;
import android.content.Context ;
import android.content.Intent ;
import android.content.IntentFilter ;
import android.content.pm.PackageManager ;
import android.os.Handler ;
import android.support.v4.app.ActivityCompat ;
import android.support.v4.content.ContextCompat ;
import android.os.Bundle ;
import android.util.Log ;
import android.view.View ;
import android.widget.AdapterView ;
import android.widget.EditText ;
import android.widget.ListView ;
import android.widget.TextView ;

import com.smartlab.pollutionsensor.ui.BaseActivity;
import com.smartlab.pollutionsensor.ui.BaseDialog;

public class MainActivity extends BaseActivity {

	private static final int REQUEST_ENABLE_BT = 1 ;

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

	private boolean scanningBLE = false ;
	private boolean scanningNormal = false ;

	private ListView keyValuePairsListView ;
	private ListView devicesListView ;

	private Handler scanHandlerBLE ;
	private Handler scanHandlerNormal ;

	private TextView scanTextView ;
	private TextView connectionStateTextView ;

	private DeviceListAdapter deviceListAdapter ;
	private KeyValueListAdapter keyValueListAdapter ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState) ;
		setContentView(R.layout.activity_main) ;

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1) ;
		}

		keyValuePairsListView = (ListView) findViewById(R.id.activity_main_list_view_key_value_pairs) ;
		keyValueListAdapter = new KeyValueListAdapter(this) ;
		keyValuePairsListView.setEmptyView(findViewById(R.id.activity_main_list_view_key_value_pairs_list_empty_layout)) ;
		keyValuePairsListView.setAdapter(keyValueListAdapter) ;

		devicesListView = (ListView) findViewById(R.id.activity_main_list_view_devices) ;
		deviceListAdapter = new DeviceListAdapter(this) ;
		devicesListView.setAdapter(deviceListAdapter) ;
		devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				bluetoothGatt = deviceListAdapter.getItem(position).connectGatt(MainActivity.this, false, bluetoothGattCallback);
			}
		});

		findViewById(R.id.activity_main_floating_action_button_add_key_value_pair).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AddKeyValuePairDialog(MainActivity.this).show() ;
			}
		});

		findViewById(R.id.activity_main_floating_action_button_scan_ble).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleBLEScan();
			}
		});

		findViewById(R.id.activity_main_floating_action_button_scan_normal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleNormalScan();
			}
		});

		scanTextView = (TextView) findViewById(R.id.activity_main_text_view_scan_status) ;
		scanTextView.setText("No devices found!");

		connectionStateTextView = (TextView) findViewById(R.id.activity_main_text_view_connection_state) ;
		connectionStateTextView.setText("No connection!");

		handleBluetoothAdapter() ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			initializeScan() ;
		}
	}

	private void handleBluetoothAdapter() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ;
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			Log.e(Constants.DEBUG_TAG, "Enabling BT") ;
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE) ;
			startActivityForResult(intent, REQUEST_ENABLE_BT) ;
		} else {
			initializeScan() ;
		}
	}

	private void initializeScan() {
		scanHandlerBLE = new Handler() ;
		scanHandlerNormal = new Handler() ;
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND) ;
		registerReceiver(broadcastReceiver, filter) ;
	}

	private void makeDeviceDiscoverable() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent) ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				connectionState = STATE_CONNECTED ;
				connectionStateTextView.setText("Connected to " + bluetoothGatt.getDevice().getName());
				bluetoothGatt.discoverServices() ;

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				connectionState = STATE_DISCONNECTED ;
				connectionStateTextView.setText("No connection!");
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
			} else {
				Log.w(Constants.DEBUG_TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				connectionStateTextView.setText("Connected to " + bluetoothGatt.getDevice().getName() + "\n Received data: " +
					characteristic.getStringValue(0)) ;
				//broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic) ;
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
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

	private void handleBLEScan() {
		if (scanningNormal) {
			bluetoothAdapter.cancelDiscovery() ;
			scanningNormal = false ;
		}
		bluetoothAdapter.startLeScan(scanCallback) ;
		scanningBLE = true ;
		scanHandlerBLE.postDelayed(new Runnable() {
			@Override
			public void run() {
				bluetoothAdapter.stopLeScan(scanCallback) ;
				scanningBLE = false ;
				scanTextView.setText("Scan stopped") ;
				Log.e(Constants.DEBUG_TAG, "Scan stopped") ;
			}
		}, SCAN_PERIOD) ;
		Log.e(Constants.DEBUG_TAG, "starting BLE scan ... ") ;
		scanTextView.setText("Scanning BLE ... ") ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	private void handleNormalScan() {
		if (scanningBLE) {
			bluetoothAdapter.stopLeScan(scanCallback) ;
			scanningBLE = false ;
		}
		bluetoothAdapter.startDiscovery() ;
		scanningNormal = true ;
		scanHandlerNormal.postDelayed(new Runnable() {
			@Override
			public void run() {
				bluetoothAdapter.cancelDiscovery() ;
				scanningNormal = false ;
				scanTextView.setText("Scan stopped") ;
				Log.e(Constants.DEBUG_TAG, "Scan stopped") ;
			}
		}, SCAN_PERIOD) ;
		Log.e(Constants.DEBUG_TAG, "starting normal scan ... ") ;
		scanTextView.setText("Scanning Normal ... ") ;
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction() ;
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.e(Constants.DEBUG_TAG, "Device Found") ;
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ;
				deviceListAdapter.addBluetoothDevice(device) ;
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.e(Constants.DEBUG_TAG, "Discovery Finished") ;
			}
		}
	};

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}
}
