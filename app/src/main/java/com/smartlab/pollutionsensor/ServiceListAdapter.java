package com.smartlab.pollutionsensor;

import android.app.Activity ;
import android.bluetooth.BluetoothGattCharacteristic ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.BaseAdapter ;
import android.widget.TextView ;

import java.util.ArrayList ;
import java.util.UUID ;

/**
 * Created by Parsoa on 2/24/17.
 */

public class ServiceListAdapter extends BaseAdapter {

	private ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>() ;

	private Activity activity ;

	public ServiceListAdapter(Activity activity) {
		this.activity = activity ;
	}

	public void addCharacteristic(BluetoothGattCharacteristic characteristic) {
		characteristics.add(characteristic) ;
		notifyDataSetChanged() ;
	}

	public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
		for (BluetoothGattCharacteristic characteristic: characteristics) {
			if (characteristic.getUuid().equals(uuid)) {
				return characteristic ;
			}
		}
		return null ;
	}

	@Override
	public int getCount() {
		return characteristics.size() ;
	}

	@Override
	public BluetoothGattCharacteristic getItem(int position) {
		return characteristics.get(position) ;
	}

	@Override
	public long getItemId(int position) {
		return 0 ;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.list_item_characteristic, null);
			ViewHolder viewHolder = new ViewHolder(convertView, getItem(position)) ;
			convertView.setTag(viewHolder);
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag() ;
			viewHolder.update(convertView, getItem(position));
		}
		return convertView ;
	}

	class ViewHolder {

		private TextView value ;
		private TextView uuid ;
		private TextView service ;

		public ViewHolder(View convertView, BluetoothGattCharacteristic characteristic) {
			value = (TextView) convertView.findViewById(R.id.list_item_characteristic_text_view_value_value) ;
			fillValue(characteristic);

			uuid = (TextView) convertView.findViewById(R.id.list_item_characteristic_text_view_uuid_value) ;
			uuid.setText(characteristic.getUuid().toString());

			service = (TextView) convertView.findViewById(R.id.list_item_characteristic_text_view_service_value) ;
			service.setText(characteristic.getService().getUuid().toString());
		}

		public void update(View convertView, BluetoothGattCharacteristic characteristic) {
			uuid.setText(characteristic.getUuid().toString());
			service.setText(characteristic.getService().getUuid().toString());
			fillValue(characteristic);
		}

		private void fillValue(BluetoothGattCharacteristic characteristic) {
			value.setText(decodeValue(characteristic));
		}
	}

	private String decodeValue(BluetoothGattCharacteristic characteristic) {
		String res = "" ;
		for (int i = 0 ; i < characteristic.getValue().length ; i++) {
			res += Character.toString((char)(characteristic.getValue()[i] & 0xff)) ;
		}
		return res ;
	}
}
