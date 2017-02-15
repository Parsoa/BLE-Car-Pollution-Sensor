package com.smartlab.pollutionsensor.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartlab.pollutionsensor.R ;

/**
 * Created by Parsoa on 6/11/16.
 */

public class BaseActivity extends AppCompatActivity {

	protected Toolbar toolbar ;

	public static Typeface font ;
	public static Typeface latinFont ;

	protected void getToolbar() {
		//toolbar = (Toolbar) findViewById(R.id.toolbar) ;
		//TextView title = (TextView) toolbar.findViewById(R.id.action_bar_title) ;
		//title.setTypeface(BaseActivity.getFont(this));
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v instanceof TextView) {
			((TextView) v).setTypeface(getFont(this));
		}
		else if (v instanceof EditText) {
			((EditText) v).setTypeface(getFont(this));
		}
		else if (v instanceof Button) {
			((Button) v).setTypeface(getFont(this));
		}
		return v ;
	}

	public static Typeface getFont(Context context) {
		if (font == null) {
			font = Typeface.createFromAsset(context.getAssets(), "fonts/IRANSans-Light-web.ttf");
		}
		return font ;
	}

	public static Typeface getLatinFont(Context context) {
		if (latinFont == null) {
			latinFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto.ttf");
		}
		return latinFont ;
	}

	@Override
	public void setTitle(CharSequence title) {
		TextView titleTextView = (TextView) toolbar.findViewById(R.id.action_bar_title) ;
		titleTextView.setText(title);
	}

	public void setTitle(SpannableString title) {
		TextView titleTextView = (TextView) toolbar.findViewById(R.id.action_bar_title) ;
		titleTextView.setText(title);
	}
}
