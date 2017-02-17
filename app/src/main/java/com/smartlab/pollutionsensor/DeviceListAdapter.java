package com.smartlab.pollutionsensor;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartlab.pollutionsensor.ui.BaseActivity;

import java.util.ArrayList;

/**
 * Created by Parsoa on 2/14/17.
 */

public class DeviceListAdapter extends BaseAdapter {

	private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>() ;

	private Activity activity ;

	public DeviceListAdapter(Activity activity) {
		this.activity = activity ;
	}

	public void addBluetoothDevice(BluetoothDevice bluetoothDevice) {
		for (BluetoothDevice b: this.bluetoothDevices) {
			if (b.getName().equals(bluetoothDevice.getName())) {
				return ;
			}
		}
		this.bluetoothDevices.add(bluetoothDevice) ;
		notifyDataSetChanged() ;
	}

	@Override
	public int getCount() {
		return bluetoothDevices.size() ;
	}

	@Override
	public BluetoothDevice getItem(int position) {
		return bluetoothDevices.get(position) ;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.e(Constants.DEBUG_TAG, "Position: " + position) ;
		BluetoothDevice bluetoothDevice = bluetoothDevices.get(position) ;
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.list_item_device, null) ;
			ViewHolder viewHolder = new ViewHolder(convertView, bluetoothDevice) ;
			convertView.setTag(viewHolder);
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag() ;
			viewHolder.update(bluetoothDevice);
		}
		return convertView ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	class ViewHolder {
		private TextView title ;

		public ViewHolder(View convertView, BluetoothDevice bluetoothDevice) {
			title = (TextView) convertView.findViewById(R.id.list_item_device_text_view_uuid) ;
			title.setText(bluetoothDevice.getName());
			title.setTypeface(BaseActivity.getFont(activity));
		}

		public void update(BluetoothDevice bluetoothDevice) {
			title.setText(bluetoothDevice.getName());
		}
	}
}
