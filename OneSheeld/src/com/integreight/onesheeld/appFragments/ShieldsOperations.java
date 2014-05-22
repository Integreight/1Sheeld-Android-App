package com.integreight.onesheeld.appFragments;

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
import android.widget.ToggleButton;

import com.integreight.onesheeld.ArduinoConnectivityPopup;
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
						if (!settingsSlidingView.isOpened()
								&& !((CheckBox) getView().findViewById(
										R.id.isMenuOpening)).isChecked())
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
						if (!pinsSlidingView.isOpened()
								&& !((CheckBox) getView().findViewById(
										R.id.isMenuOpening)).isChecked())
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
		((ToggleButton) getView().findViewById(R.id.shieldStatus))
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (MainActivity.currentShieldTag != null)
							((OneSheeldApplication) getActivity()
									.getApplication()).getRunningShields().get(
									MainActivity.currentShieldTag).isInteractive = isChecked;
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();

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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void switchContent(Fragment fragment) {
		mContent = fragment;
		((MainActivity) getActivity()).getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.appTransitionsContainer, fragment).commit();
		// ((MainActivity) getActivity()).getSlidingMenu().showContent();
	}

	@Override
	public void onDestroy() {
		mContent = null;
		super.onDestroy();
	}

	@Override
	public void onResume() {
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
		((MainActivity) getActivity()).closeMenu();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				getActivity().findViewById(R.id.getAvailableDevices)
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								((MainActivity) getActivity()).closeMenu();
								if (getActivity().getSupportFragmentManager()
										.getBackStackEntryCount() > 1) {
									getActivity().getSupportFragmentManager()
											.popBackStack();
									getActivity().getSupportFragmentManager()
											.executePendingTransactions();
								}
								((MainActivity) getActivity()).stopService();
								if (!ArduinoConnectivityPopup.isOpened) {
									ArduinoConnectivityPopup.isOpened = true;
									new ArduinoConnectivityPopup(getActivity())
											.show();
								}
							}
						});
			}
		}, 500);
		((ViewGroup) getActivity().findViewById(R.id.getAvailableDevices))
				.getChildAt(1).setBackgroundResource(
						R.drawable.bluetooth_disconnect_button);
		((ViewGroup) getActivity().findViewById(R.id.cancelConnection))
				.getChildAt(1).setBackgroundResource(R.drawable.back_button);

		getActivity().findViewById(R.id.cancelConnection).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						boolean isMenuClosed = ((MainActivity) getActivity()).appSlidingMenu != null
								&& !((MainActivity) getActivity()).appSlidingMenu
										.isOpen();
						getActivity().onBackPressed();
						if (isMenuClosed)
							getActivity().findViewById(R.id.cancelConnection)
									.setOnClickListener(
											new View.OnClickListener() {

												@Override
												public void onClick(View v) {
													// TODO Auto-generated
													// method stub

												}
											});
					}
				});
		super.onResume();
	}
}
