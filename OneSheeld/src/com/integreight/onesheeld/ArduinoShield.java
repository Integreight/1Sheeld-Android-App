package com.integreight.onesheeld;

import android.content.Context;

public abstract class ArduinoShield {

	protected int id;
	protected String name;
	protected Context context;
	protected int stripResourceId;
	protected Integer iconResourceId;
	
	protected ArduinoShield(Context context){
		this.context=context;
	}
	
	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public Context getContext() {
		// TODO Auto-generated method stub
		return context;
	}
	
	public int getStripResourceId() {
		return stripResourceId;
	}
	
	public Integer getIconResourceId() {
		return iconResourceId;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
	
	
}
