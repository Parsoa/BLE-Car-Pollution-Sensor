package com.smartlab.pollutionsensor;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.smartlab.pollutionsensor.ui.BaseActivity;

import java.util.ArrayList;

/**
 * Created by Parsoa on 2/14/17.
 */

public class KeyValueListAdapter extends BaseAdapter {

	private Activity activity ;

	private ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>() ;

	public KeyValueListAdapter(Activity activity) {
		this.activity = activity ;
	}

	public void addKeyValuePair(KeyValuePair keyValuePair) {
		keyValuePairs.add(keyValuePair) ;
		notifyDataSetChanged() ;
	}

	@Override
	public int getCount() {
		return keyValuePairs.size() ;
	}

	@Override
	public Object getItem(int position) {
		return keyValuePairs.get(position) ;
	}

	@Override
	public long getItemId(int position) {
		return 0 ;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		KeyValuePair keyValuePair = keyValuePairs.get(position) ;
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(R.layout.list_item_key_value_pair, null) ;
			ViewHolder viewHolder = new ViewHolder(convertView, keyValuePair, position) ;
			convertView.setTag(viewHolder);
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag() ;
			viewHolder.update(keyValuePair, position);
		}
		return convertView ;
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\

	class ViewHolder {

		private TextView keyTextView ;
		private TextView valueTextView ;
		private ImageButton imageButtonDelete ;
		private int position ;

		public ViewHolder(View convertView, KeyValuePair keyValuePair, int position) {
			keyTextView = (TextView) convertView.findViewById(R.id.list_item_key_value_pair_text_view_key) ;
			valueTextView = (TextView) convertView.findViewById(R.id.list_item_key_value_pair_text_view_value) ;
			keyTextView.setTypeface(BaseActivity.getFont(activity));
			valueTextView.setTypeface(BaseActivity.getFont(activity));
			keyTextView.setText(keyValuePair.getKey());
			imageButtonDelete = (ImageButton) convertView.findViewById(R.id.list_item_key_value_pair_image_button_delete) ;
			this.position = position ;
			imageButtonDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					keyValuePairs.remove(ViewHolder.this.position) ;
					notifyDataSetChanged() ;
				}
			});
		}

		public void update(KeyValuePair keyValuePair, int position) {
			keyTextView.setText(keyValuePair.getKey());
			valueTextView.setText(keyValuePair.getValue());
			this.position = position ;
		}
	}

	// ================================================================================================================ \\
	// ================================================================================================================ \\
	// ================================================================================================================ \\
}
