package com.integreight.onesheeld.activities;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.onesheeld.Key;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.SelectedShieldsListFragment;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.services.OneSheeldService.OneSheeldBinder;
import com.integreight.onesheeld.shieldsfragments.KeypadFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class ShieldsOperationActivity extends SlidingFragmentActivity {

	protected SelectedShieldsListFragment mFrag;
	private Fragment mContent;
	private ArduinoFirmata firmata;
	private List<OneSheeldServiceHandler> serviceEventHandlers;
	MediaPlayer mp;
	
	private ArduinoFirmataEventHandler arduinoEventHandler= new ArduinoFirmataEventHandler() {
		
		@Override
		public void onError(String errorMessage) {

			finish();
		}
		
		@Override
		public void onConnect() {
			// TODO Auto-generated method stub

		}
		
		@Override
		public void onClose(boolean closedManually) {
			// TODO Auto-generated method stub
			finish();

			
		}
	};

	OneSheeldService _1SheeldService;
	boolean mBound = false;

	public ArduinoFirmata getFirmata() {
		return firmata;
	}

	public void addServiceEventHandler(
			OneSheeldServiceHandler serviceEventHandler) {
		if (!this.serviceEventHandlers.contains(serviceEventHandler))
			this.serviceEventHandlers.add(serviceEventHandler);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_shields_operation);
		// Show the Up button in the action bar.

		// setTitle(mTitleRes);
		serviceEventHandlers = new ArrayList<OneSheeldServiceHandler>();
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindWidth(150);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(true);

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager()
					.beginTransaction();
			mFrag = new SelectedShieldsListFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (SelectedShieldsListFragment) this
					.getSupportFragmentManager().findFragmentById(
							R.id.menu_frame);
		}

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");

		// // set the Behind View
		// setBehindContentView(R.layout.menu_frame);
		// if (savedInstanceState == null) {
		// FragmentTransaction t = this.getSupportFragmentManager()
		// .beginTransaction();
		// mFrag = new SelectedShieldsListFragment();
		// t.replace(R.id.menu_frame, mFrag);
		// t.commit();
		// } else {
		// mFrag = (ListFragment) this.getSupportFragmentManager()
		// .findFragmentById(R.id.menu_frame);
		// }
    	Key.normalColor  = getResources().getInteger( R.color.normal_control  );
		Key.pressedColor = getResources().getInteger( R.color.pressed_control );
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (mContent == null) {
			mContent = mFrag.getShieldFragment(0);
			setTitle(mFrag.getUIShield(0).getName() + " Shield");
			// set the Above View
			setContentView(R.layout.content_frame);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.content_frame, mContent).commit();
		}

		bindFirmataService();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unBindFirmataService();
	}

	private void unBindFirmataService() {
		// TODO Auto-generated method stub
		if (mBound)
			this.unbindService(mConnection);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.shields_operation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// NavUtils.navigateUpFromSameTask(this);
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchContent(Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		getSlidingMenu().showContent();
	}

	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				toggle();
			}
		}, 500);
	}

	private void bindFirmataService() {
		if (!mBound) {
			Intent intent = new Intent(this, OneSheeldService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			OneSheeldBinder binder = (OneSheeldBinder) service;
			_1SheeldService = binder.getService();

			mBound = true;
			firmata = _1SheeldService.getFirmata();
			firmata.addEventHandler(arduinoEventHandler);
			for (OneSheeldServiceHandler serviceHandler : serviceEventHandlers) {
				serviceHandler.onServiceConnected(firmata);
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
			for (OneSheeldServiceHandler serviceHandler : serviceEventHandlers) {
				serviceHandler.onServiceDisconnected();
			}
		}
	};

	// public void toggleLed(View v){
	// // ((LedFragment)mContent).toggleLed(v);
	// }

	public static interface OneSheeldServiceHandler {
		void onServiceConnected(ArduinoFirmata firmata);

		void onServiceDisconnected();
	}
	
	public void playSound(int soundResourceId){
		if(mp==null)mp = MediaPlayer.create(this, soundResourceId);
		if(mp.isPlaying()){
			Resources res = getResources();
			AssetFileDescriptor afd = res.openRawResourceFd(soundResourceId);
			FileDescriptor fd = afd.getFileDescriptor();
			mp.reset();
			try {
				mp.setDataSource(fd);
				mp.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mp.start();
		}
		else
			mp.start();
	}

	public void onKeypadKeyPress(View v){
		((KeypadFragment)mContent).onKeypadKeyPress(v);
	}
}
