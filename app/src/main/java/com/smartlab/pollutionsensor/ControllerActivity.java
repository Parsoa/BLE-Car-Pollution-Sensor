package com.smartlab.pollutionsensor;

import android.bluetooth.BluetoothDevice ;
import android.bluetooth.BluetoothGatt ;
import android.bluetooth.BluetoothGattCallback ;
import android.bluetooth.BluetoothGattCharacteristic ;
import android.bluetooth.BluetoothGattService ;
import android.bluetooth.BluetoothProfile ;
import android.os.Bundle ;
import android.support.annotation.Nullable ;
import android.util.Log ;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView ;

import com.smartlab.pollutionsensor.ui.BaseActivity ;

import java.util.Stack;
import java.util.UUID ;

/**
 * Created by Parsoa on 2/22/17.
 */

public class ControllerActivity extends BaseActivity {

	private int connectionState = STATE_DISCONNECTED;
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private static final UUID RX_TX = UUID.fromString("0000cccc-0000-1000-8000-00805f9b34fb") ;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED" ;
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED" ;
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED" ;
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE" ;
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA" ;

	private TextView connectionStateTextView ;

	public static BluetoothDevice bluetoothDevice ;
	private BluetoothGatt bluetoothGatt ;

	private boolean reading = false ;

	private Stack<UUID> readQueue = new Stack<>() ;

	private ListView listView ;
	private ServiceListAdapter adapter ;

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState) ;
		setContentView(R.layout.activity_controller) ;

		connectionStateTextView = (TextView) findViewById(R.id.activity_controller_text_view_connection_state) ;
		connectionStateTextView.setText("Connecting ... ") ;

		listView = (ListView) findViewById(R.id.activity_controller_list_view) ;
		adapter = new ServiceListAdapter(this) ;
		listView.setAdapter(adapter) ;

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!reading) {
					reading = true ;
					bluetoothGatt.readCharacteristic(adapter.getItem(position)) ;
				}
			}
		});

		bluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback) ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				connectionState = STATE_CONNECTED ;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						connectionStateTextView.setText("Connected to " + bluetoothGatt.getDevice().getName());
					}
				});
				bluetoothGatt.discoverServices() ;
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				connectionState = STATE_DISCONNECTED ;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						connectionStateTextView.setText("Disconnected!");
						finish() ;
					}
				});
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.w(Constants.DEBUG_TAG, "onServicesDiscovered") ;
				for (BluetoothGattService service: bluetoothGatt.getServices()) {
					Log.w(Constants.DEBUG_TAG, "Service: " + service.getUuid().toString()) ;
					for (BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
						Log.w(Constants.DEBUG_TAG, "Characteristic: " + characteristic.getUuid().toString()) ;
						adapter.addCharacteristic(characteristic) ;
						if (!reading) {
							reading = true ;
							bluetoothGatt.readCharacteristic(characteristic) ;
						} else {
							readQueue.push(characteristic.getUuid()) ;
						}
					}
				}
			} else {
				Log.w(Constants.DEBUG_TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.e(Constants.DEBUG_TAG, "Value - " + characteristic.getUuid() + ": " + decodeValue(characteristic)) ;
			} else {
				Log.e(Constants.DEBUG_TAG, "Value - " + characteristic.getUuid() + ": Failed") ;
			}
			readNextCharacteristic() ;
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
		}
	};

	private void readNextCharacteristic() {
		if (!readQueue.isEmpty()) {
			reading = true ;
			BluetoothGattCharacteristic characteristic = adapter.getCharacteristic(readQueue.pop()) ;
			if (characteristic != null) {
				bluetoothGatt.readCharacteristic(characteristic) ;
			}
		} else {
			reading = false ;
		}
	}

	private String decodeValue(BluetoothGattCharacteristic characteristic) {
		String res = "" ;
		for (int i = 0 ; i < characteristic.getValue().length ; i++) {
			res += Character.toString((char)(characteristic.getValue()[i] & 0xff)) ;
		}
		return res ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\
}
