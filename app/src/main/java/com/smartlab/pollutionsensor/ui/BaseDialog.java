package com.smartlab.pollutionsensor.ui;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartlab.pollutionsensor.R;

/**
 * Created by Parsoa on 6/19/16.
 */
public class BaseDialog extends Dialog {

	protected TextView title ;
	protected Activity context ;
	protected Button acceptButton ;
	protected Button cancelButton;

	public BaseDialog(Activity context) {
		super(context);
		this.context = context ;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	protected void handleTitle() {
		title = (TextView) findViewById(R.id.base_dialog_text_view_title) ;
		title.setTypeface(BaseActivity.getFont(context));
	}

	protected void handleButtons() {
		acceptButton = (Button) findViewById(R.id.base_dialog_button_accept) ;
		cancelButton = (Button) findViewById(R.id.base_dialog_button_cancel) ;
		acceptButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPositiveButtonClick();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNegativeButtonClick();
			}
		});
	}

	protected void onPositiveButtonClick() {
		dismiss();
	}

	protected void onNegativeButtonClick() {
		dismiss();
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v instanceof TextView) {
			((TextView) v).setTypeface(BaseActivity.getFont(context));
		}
		else if (v instanceof EditText) {
			((EditText) v).setTypeface(BaseActivity.getFont(context));
		}
		else if (v instanceof Button) {
			((Button) v).setTypeface(BaseActivity.getFont(context));
		}
		return v ;
	}
}
