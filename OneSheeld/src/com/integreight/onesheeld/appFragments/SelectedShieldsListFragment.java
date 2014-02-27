package com.integreight.onesheeld.appFragments;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.SelectedShieldsListAdapter;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.fragments.AccelerometerFragment;
import com.integreight.onesheeld.shields.fragments.BuzzerFragment;
import com.integreight.onesheeld.shields.fragments.CameraFragment;
import com.integreight.onesheeld.shields.fragments.EmptyShieldFragment;
import com.integreight.onesheeld.shields.fragments.FacebookFragment;
import com.integreight.onesheeld.shields.fragments.FlashlightFragment;
import com.integreight.onesheeld.shields.fragments.FoursquareFragment;
import com.integreight.onesheeld.shields.fragments.GamepadFragment;
import com.integreight.onesheeld.shields.fragments.GpsFragment;
import com.integreight.onesheeld.shields.fragments.GravityFragment;
import com.integreight.onesheeld.shields.fragments.GyroscopeFragment;
import com.integreight.onesheeld.shields.fragments.KeypadFragment;
import com.integreight.onesheeld.shields.fragments.LcdFragment;
import com.integreight.onesheeld.shields.fragments.LedFragment;
import com.integreight.onesheeld.shields.fragments.LightFragment;
import com.integreight.onesheeld.shields.fragments.MagnetometerFragment;
import com.integreight.onesheeld.shields.fragments.MicFragment;
import com.integreight.onesheeld.shields.fragments.MusicPlayerFragment;
import com.integreight.onesheeld.shields.fragments.NotificationFragment;
import com.integreight.onesheeld.shields.fragments.OrientationFragment;
import com.integreight.onesheeld.shields.fragments.PhoneFragment;
import com.integreight.onesheeld.shields.fragments.PressureFragment;
import com.integreight.onesheeld.shields.fragments.ProximityFragment;
import com.integreight.onesheeld.shields.fragments.PushButtonFragment;
import com.integreight.onesheeld.shields.fragments.SevenSegmentFragment;
import com.integreight.onesheeld.shields.fragments.SkypeFragment;
import com.integreight.onesheeld.shields.fragments.SliderFragment;
import com.integreight.onesheeld.shields.fragments.SmsFragment;
import com.integreight.onesheeld.shields.fragments.TemperatureFragment;
import com.integreight.onesheeld.shields.fragments.ToggleButtonFragment;
import com.integreight.onesheeld.shields.fragments.TwitterFragment;
import com.integreight.onesheeld.utils.ShieldFragmentParent;

public class SelectedShieldsListFragment extends ListFragment {
	private static SelectedShieldsListAdapter UIShieldAdapter;
	Map<UIShield, ShieldFragmentParent<?>> creadtedShields = new HashMap<UIShield, ShieldFragmentParent<?>>();

	public static SelectedShieldsListFragment newInstance(Activity activity) {
		UIShieldAdapter = new SelectedShieldsListAdapter(activity);
		return new SelectedShieldsListFragment();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.selected_shields_list, null);
	}

	public ShieldFragmentParent<?> getShieldFragment(int position) {
		UIShield uiShield = UIShieldAdapter.getItem(position);
		if (creadtedShields.containsKey(uiShield))
			return creadtedShields.get(uiShield);
		switch (uiShield) {
		case LED_SHIELD:
			return addToCreatedListAndReturn(UIShield.LED_SHIELD,
					new LedFragment());
		case ACCELEROMETER_SHIELD:
			return addToCreatedListAndReturn(UIShield.ACCELEROMETER_SHIELD,
					new AccelerometerFragment());
		case FACEBOOK_SHIELD:
			return addToCreatedListAndReturn(UIShield.FACEBOOK_SHIELD,
					new FacebookFragment());
		case KEYPAD_SHIELD:
			return addToCreatedListAndReturn(UIShield.KEYPAD_SHIELD,
					new KeypadFragment());
		case LCD_SHIELD:
			return addToCreatedListAndReturn(UIShield.LCD_SHIELD,
					new LcdFragment());
		case MAGNETOMETER_SHIELD:
			return addToCreatedListAndReturn(UIShield.MAGNETOMETER_SHIELD,
					new MagnetometerFragment());
		case PUSHBUTTON_SHIELD:
			return addToCreatedListAndReturn(UIShield.PUSHBUTTON_SHIELD,
					new PushButtonFragment());
		case SEVENSEGMENT_SHIELD:
			return addToCreatedListAndReturn(UIShield.SEVENSEGMENT_SHIELD,
					new SevenSegmentFragment());
		case SLIDER_SHIELD:
			return addToCreatedListAndReturn(UIShield.SLIDER_SHIELD,
					new SliderFragment());
		case BUZZER_SHIELD:
			return addToCreatedListAndReturn(UIShield.BUZZER_SHIELD,
					new BuzzerFragment());
		case TOGGLEBUTTON_SHIELD:
			return addToCreatedListAndReturn(UIShield.TOGGLEBUTTON_SHIELD,
					new ToggleButtonFragment());
		case TWITTER_SHIELD:
			return addToCreatedListAndReturn(UIShield.TWITTER_SHIELD,
					new TwitterFragment());
		case NOTIFICATION_SHIELD:
			return addToCreatedListAndReturn(UIShield.NOTIFICATION_SHIELD,
					new NotificationFragment());
		case GAMEDPAD_SHIELD:
			return addToCreatedListAndReturn(UIShield.GAMEDPAD_SHIELD,
					new GamepadFragment());
		case FOURSQUARE_SHIELD:
			return addToCreatedListAndReturn(UIShield.FOURSQUARE_SHIELD,
					new FoursquareFragment());
		case GPS_SHIELD:
			return addToCreatedListAndReturn(UIShield.GPS_SHIELD,
					new GpsFragment());
		case SMS_SHIELD:
			return addToCreatedListAndReturn(UIShield.SMS_SHIELD,
					new SmsFragment());
		case MUSICPLAYER_SHIELD:
			return addToCreatedListAndReturn(UIShield.MUSICPLAYER_SHIELD,
					new MusicPlayerFragment());
		case GYROSCOPE_SHIELD:
			return addToCreatedListAndReturn(UIShield.GYROSCOPE_SHIELD,
					new GyroscopeFragment());
		case FLASHLIGHT_SHIELD:
			return addToCreatedListAndReturn(UIShield.FLASHLIGHT_SHIELD,
					new FlashlightFragment());
		case MIC_SHIELD:
			return addToCreatedListAndReturn(UIShield.MIC_SHIELD,
					new MicFragment());
		case SKYPE_SHIELD:
			return addToCreatedListAndReturn(UIShield.SKYPE_SHIELD,
					new SkypeFragment());
		case PROXIMITY_SHIELD:
			return addToCreatedListAndReturn(UIShield.PROXIMITY_SHIELD,
					new ProximityFragment());
		case GRAVITY_SHIELD:
			return addToCreatedListAndReturn(UIShield.GRAVITY_SHIELD,
					new GravityFragment());
		case ORIENTATION_SHIELD:
			return addToCreatedListAndReturn(UIShield.ORIENTATION_SHIELD,
					new OrientationFragment());
		case LIGHT_SHIELD:
			return addToCreatedListAndReturn(UIShield.LIGHT_SHIELD,
					new LightFragment());
		case PRESSURE_SHIELD:
			return addToCreatedListAndReturn(UIShield.PRESSURE_SHIELD,
					new PressureFragment());
		case TEMPERATURE_SHIELD:
			return addToCreatedListAndReturn(UIShield.TEMPERATURE_SHIELD,
					new TemperatureFragment());
		case CAMERA_SHIELD:
			return addToCreatedListAndReturn(UIShield.CAMERA_SHIELD,
					new CameraFragment());
		case PHONE_SHIELD:
			return addToCreatedListAndReturn(UIShield.PHONE_SHIELD,
					new PhoneFragment());

		default:
			return new EmptyShieldFragment();
		}
	}

	private ShieldFragmentParent<?> addToCreatedListAndReturn(
			UIShield uiShield, ShieldFragmentParent<?> fragment) {
		fragment.setControllerTag(uiShield.getName());
		creadtedShields.put(uiShield, fragment);
		return fragment;
	}

	public UIShield getUIShield(int position) {
		return UIShieldAdapter.getItem(position);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(UIShieldAdapter);
	}

	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
		newContent = getShieldFragment(position);
		getActivity().setTitle(
				UIShieldAdapter.getItem(position).getName() + " Shield");
		if (newContent != null)
			switchFragment(newContent);
	}

	private void switchFragment(Fragment fragment) {
		((MainActivity) getActivity()).replaceCurrentFragment(
				R.id.appTransitionsContainer, fragment, "", false, false);
		((MainActivity) getActivity()).closeMenu();
	}
}
