package com.bresiu.testfield;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by Bresiu on 09-09-2015
 */
public class User extends BaseObservable {
	private String username;
	private int number;

	public User() {
		this.username = "";
		this.number = 0;
	}

	@Bindable
	public String getUsername() {
		return username;
	}

	@Bindable
	public String getNumber() {
		return Integer.toString(number);
	}

	public void setUsername(String username) {
		this.username = username;
		notifyPropertyChanged(com.bresiu.testfield.BR.username);
	}

	public void setNumber(int number) {
		this.number = number;
		notifyPropertyChanged(com.bresiu.testfield.BR.number);
	}
}
