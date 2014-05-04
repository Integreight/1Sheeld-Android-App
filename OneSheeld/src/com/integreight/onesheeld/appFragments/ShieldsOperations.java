package com.integreight.onesheeld.appFragments;

import java.io.FileDescriptor;
import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.BaseContainerFragment;
import com.integreight.onesheeld.utils.customviews.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.MultiDirectionSlidingDrawer;

public class ShieldsOperations extends BaseContainerFragment {
	private View v;
	private static ShieldsOperations thisInstance;
	protected SelectedShieldsListFragment mFrag;
	private Fragment mContent;
	private MediaPlayer mp;

	public static ShieldsOperations getInstance() {
		if (thisInstance == null) {
			thisInstance = new ShieldsOperations();
		}
		return thisInstance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.activity_shields_operation, container,
				false);
		setRetainInstance(true);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		initView(savedInstanceState);
		super.onActivityCreated(savedInstanceState);
	}

	private void initView(Bundle savedInstanceState) {
		final MainActivity myActivity = (MainActivity) getActivity();
		myActivity
				.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.pinsViewContainer,
						ConnectingPinsView.getInstance()).commit();
		// SlidingMenu sm = ((MainActivity) getActivity()).getSlidingMenu();
		// sm.setShadowWidthRes(R.dimen.shadow_width);
		// sm.setShadowDrawable(R.drawable.shadow);
		// sm.setBehindWidth(150);
		// sm.setFadeDegree(0.35f);
		// sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		myActivity.enableMenu();
		((CheckBox) getView().findViewById(R.id.isMenuOpening))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (arg1) {
							myActivity.disableMenu();
						} else
							myActivity.enableMenu();
					}
				});
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				myActivity.openMenu();
			}
		}, 500);
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// myActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		// myActivity.getSupportActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			FragmentTransaction t = myActivity.getSupportFragmentManager()
					.beginTransaction();
			mFrag = SelectedShieldsListFragment.newInstance(myActivity);
			t.replace(R.id.selectedShieldsContainer, mFrag);
			t.commit();
		} else {
			mFrag = (SelectedShieldsListFragment) myActivity
					.getSupportFragmentManager().findFragmentById(
							R.id.menu_frame);
		}
		if (mContent == null) {
			mContent = mFrag.getShieldFragment(0);
			((MainActivity) getActivity()).setTitle(mFrag.getUIShield(0)
					.getName() + " Shield");
			// set the Above View
			// setContentView(R.layout.content_frame);
			((MainActivity) getActivity()).getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.shieldsContainerFrame, mContent).commit();
		}
		// myActivity.replaceCurrentFragment(R.id.pinsViewContainer,
		// ConnectingPinsView.getInstance(), "", false, false);
		final MultiDirectionSlidingDrawer pinsSlidingView = (MultiDirectionSlidingDrawer) getView()
				.findViewById(R.id.pinsViewSlidingView);
		final MultiDirectionSlidingDrawer settingsSlidingView = (MultiDirectionSlidingDrawer) getView()
				.findViewById(R.id.settingsSlidingView);
		getView().findViewById(R.id.pinsFixedHandler).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						pinsSlidingView.animateOpen();
					}
				});
		pinsSlidingView
				.setOnDrawerOpenListener(new MultiDirectionSlidingDrawer.OnDrawerOpenListener() {

					@Override
					public void onDrawerOpened() {
						if (settingsSlidingView.isOpened())
							settingsSlidingView.animateOpen();
						myActivity.disableMenu();
					}
				});
		pinsSlidingView
				.setOnDrawerCloseListener(new MultiDirectionSlidingDrawer.OnDrawerCloseListener() {

					@Override
					public void onDrawerClosed() {
						if (!settingsSlidingView.isOpened())
							myActivity.enableMenu();
					}
				});
		pinsSlidingView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return pinsSlidingView.isOpened();
			}
		});
		getView().findViewById(R.id.settingsFixedHandler).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						settingsSlidingView.animateOpen();
					}
				});
		settingsSlidingView
				.setOnDrawerOpenListener(new MultiDirectionSlidingDrawer.OnDrawerOpenListener() {

					@Override
					public void onDrawerOpened() {
						if (pinsSlidingView.isOpened()) {
							pinsSlidingView.animateOpen();
						}
						myActivity.disableMenu();
					}
				});
		settingsSlidingView
				.setOnDrawerCloseListener(new MultiDirectionSlidingDrawer.OnDrawerCloseListener() {

					@Override
					public void onDrawerClosed() {
						if (!pinsSlidingView.isOpened())
							myActivity.enableMenu();
					}
				});
		settingsSlidingView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return settingsSlidingView.isOpened();
			}
		});
		// if (savedInstanceState != null)
		// mContent = myActivity.getSupportFragmentManager().getFragment(
		// savedInstanceState, "mContent");

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
	}

	@Override
	public void onStart() {
		((CheckBox) getView().findViewById(R.id.isMenuOpening))
				.setChecked(false);
		((MainActivity) getActivity()).getOnConnectionLostHandler().canInvokeOnCloseConnection = false;
		if (((OneSheeldApplication) getActivity().getApplication())
				.getAppFirmata() == null
				|| !((OneSheeldApplication) getActivity().getApplication())
						.getAppFirmata().isOpen()) {
			((MainActivity) getActivity()).getOnConnectionLostHandler().connectionLost = true;
		}
		((MainActivity) getActivity()).getOnConnectionLostHandler()
				.sendEmptyMessage(0);

		super.onStart();

		// bindFirmataService();
		// DisplayMetrics metr=getResources().getDisplayMetrics();

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shields_operation, menu);
		super.onCreateOptionsMenu(menu, inflater);
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
			// ((MainActivity) getActivity()).toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// ((MainActivity)
		// getActivity()).getSupportFragmentManager().putFragment(
		// outState, "mContent", mContent);
	}

	public void switchContent(Fragment fragment) {
		mContent = fragment;
		((MainActivity) getActivity()).getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.appTransitionsContainer, fragment).commit();
		// ((MainActivity) getActivity()).getSlidingMenu().showContent();
	}

	// public void toggleLed(View v){
	// // ((LedFragment)mContent).toggleLed(v);
	// }

	// public static interface OneSheeldServiceHandler {
	// void onServiceConnected(ArduinoFirmata firmata);
	//
	// void onServiceDisconnected();
	// }

	public void playSound(int soundResourceId) {
		if (mp == null)
			mp = MediaPlayer.create(getActivity(), soundResourceId);
		if (mp.isPlaying()) {
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
		} else
			mp.start();
	}

	@Override
	public void onDestroy() {
		mContent = null;
		super.onDestroy();
	}

	@Override
	public void onResume() {
		// getSherlockActivity().getSupportActionBar().hide();
		super.onResume();
	}
}
