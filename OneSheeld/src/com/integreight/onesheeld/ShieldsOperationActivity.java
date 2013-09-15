package com.integreight.onesheeld;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class ShieldsOperationActivity extends SlidingFragmentActivity {

	protected SelectedShieldsListFragment mFrag;
	private Fragment mContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shields_operation);
		// Show the Up button in the action bar.

		// setTitle(mTitleRes);

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindWidth(150);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager()
					.beginTransaction();
			mFrag = new SelectedShieldsListFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (SelectedShieldsListFragment) this.getSupportFragmentManager()
					.findFragmentById(R.id.menu_frame);
		}

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		

		

		// // set the Behind View
		// setBehindContentView(R.layout.menu_frame);
		// getSupportFragmentManager()
		// .beginTransaction()
		// .replace(R.id.menu_frame, new ColorMenuFragment())
		// .commit();

//		// set the Behind View
//		setBehindContentView(R.layout.menu_frame);
//		if (savedInstanceState == null) {
//			FragmentTransaction t = this.getSupportFragmentManager()
//					.beginTransaction();
//			mFrag = new SelectedShieldsListFragment();
//			t.replace(R.id.menu_frame, mFrag);
//			t.commit();
//		} else {
//			mFrag = (ListFragment) this.getSupportFragmentManager()
//					.findFragmentById(R.id.menu_frame);
//		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (mContent == null)
			mContent =  mFrag.getShieldFragment(0);
		setTitle(mFrag.getUIShield(0).getName());
		// set the Above View
				setContentView(R.layout.content_frame);
				getSupportFragmentManager().beginTransaction()
						.replace(R.id.content_frame, mContent).commit();
				
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
	
//	public void toggleLed(View v){
////		((LedFragment)mContent).toggleLed(v);
//	}

}
