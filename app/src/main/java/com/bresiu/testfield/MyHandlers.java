package com.bresiu.testfield;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Bresiu on 09-09-2015
 */
public class MyHandlers {
	private static final String TAG = "TEST";

	@SuppressWarnings("unused")
	public void onClickUsername(View view) {
		CharSequence username = ((TextView) view).getText();
		Log.d(TAG, "onClickUsername " + username);
	}

	@SuppressWarnings("unused")
	public void onClickNumber(View view) {
		CharSequence number = ((TextView) view).getText();
		Log.d(TAG, "onClickNumber " + number);
	}
}
