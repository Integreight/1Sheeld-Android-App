package com.integreight.onesheeld;



import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.adapters.SelectedShieldsListAdapter;
import com.integreight.onesheeld.shieldsfragments.AccelerometerFragment;
import com.integreight.onesheeld.shieldsfragments.EmptyShieldFragment;
import com.integreight.onesheeld.shieldsfragments.FacebookFragment;
import com.integreight.onesheeld.shieldsfragments.FlashlightFragment;
import com.integreight.onesheeld.shieldsfragments.FoursquareFragment;
import com.integreight.onesheeld.shieldsfragments.GamepadFragment;
import com.integreight.onesheeld.shieldsfragments.GpsFragment;
import com.integreight.onesheeld.shieldsfragments.GyroscopeFragment;
import com.integreight.onesheeld.shieldsfragments.InternetFragment;
import com.integreight.onesheeld.shieldsfragments.KeypadFragment;
import com.integreight.onesheeld.shieldsfragments.LcdFragment;
import com.integreight.onesheeld.shieldsfragments.LedFragment;
import com.integreight.onesheeld.shieldsfragments.MagnetometerFragment;
import com.integreight.onesheeld.shieldsfragments.MicFragment;
import com.integreight.onesheeld.shieldsfragments.MusicPlayerFragment;
import com.integreight.onesheeld.shieldsfragments.PushButtonFragment;
import com.integreight.onesheeld.shieldsfragments.SevenSegmentFragment;
import com.integreight.onesheeld.shieldsfragments.SliderFragment;
import com.integreight.onesheeld.shieldsfragments.SmsFragment;
import com.integreight.onesheeld.shieldsfragments.BuzzerFragment;
import com.integreight.onesheeld.shieldsfragments.ToggleButtonFragment;
import com.integreight.onesheeld.shieldsfragments.TwitterFragment;
import com.integreight.onesheeld.shieldsfragments.NotificationFragment;

public class SelectedShieldsListFragment extends ListFragment {
	SelectedShieldsListAdapter UIShieldAdapter;
	Map<UIShield, Fragment> creadtedShields=new HashMap<UIShield, Fragment>();

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.selected_shields_list, null);
	}

	public Fragment getShieldFragment(int position){
		UIShield uiShield=UIShieldAdapter.getItem(position);
		if(creadtedShields.containsKey(uiShield)) return creadtedShields.get(uiShield);
		switch (uiShield) {
		case LED_SHIELD:return addToCreatedListAndReturn(UIShield.LED_SHIELD,new LedFragment());
		case ACCELEROMETER_SHIELD:return addToCreatedListAndReturn(UIShield.ACCELEROMETER_SHIELD,new AccelerometerFragment());
		case FACEBOOK_SHIELD:return addToCreatedListAndReturn(UIShield.FACEBOOK_SHIELD,new FacebookFragment());
		case KEYPAD_SHIELD:return addToCreatedListAndReturn(UIShield.KEYPAD_SHIELD,new KeypadFragment());
		case LCD_SHIELD:return addToCreatedListAndReturn(UIShield.LCD_SHIELD,new LcdFragment());
		case MAGNETOMETER_SHIELD:return addToCreatedListAndReturn(UIShield.MAGNETOMETER_SHIELD,new MagnetometerFragment());
		case PUSHBUTTON_SHIELD:return addToCreatedListAndReturn(UIShield.PUSHBUTTON_SHIELD,new PushButtonFragment());
		case SEVENSEGMENT_SHIELD:return addToCreatedListAndReturn(UIShield.SEVENSEGMENT_SHIELD,new SevenSegmentFragment());
		case SLIDER_SHIELD:return addToCreatedListAndReturn(UIShield.SLIDER_SHIELD,new SliderFragment());
		case BUZZER_SHIELD:return addToCreatedListAndReturn(UIShield.BUZZER_SHIELD,new BuzzerFragment());
		case TOGGLEBUTTON_SHIELD:return addToCreatedListAndReturn(UIShield.TOGGLEBUTTON_SHIELD,new ToggleButtonFragment());
		case TWITTER_SHIELD:return addToCreatedListAndReturn(UIShield.TWITTER_SHIELD,new TwitterFragment());
		case NOTIFICATION_SHIELD:return addToCreatedListAndReturn(UIShield.NOTIFICATION_SHIELD,new NotificationFragment());
		case GAMEDPAD_SHIELD:return addToCreatedListAndReturn(UIShield.GAMEDPAD_SHIELD,new GamepadFragment());
		case INTERNET_SHIELD:return addToCreatedListAndReturn(UIShield.INTERNET_SHIELD,new InternetFragment());
		case FOURSQUARE_SHIELD:return addToCreatedListAndReturn(UIShield.FOURSQUARE_SHIELD,new FoursquareFragment());
		case GPS_SHIELD:return addToCreatedListAndReturn(UIShield.GPS_SHIELD,new GpsFragment());
		case SMS_SHIELD:return addToCreatedListAndReturn(UIShield.SMS_SHIELD,new SmsFragment());
		case MUSICPLAYER_SHIELD:return addToCreatedListAndReturn(UIShield.MUSICPLAYER_SHIELD,new MusicPlayerFragment());
		case GYROSCOPE_SHIELD:return addToCreatedListAndReturn(UIShield.GYROSCOPE_SHIELD,new GyroscopeFragment());
		case FLASHLIGHT_SHIELD:return addToCreatedListAndReturn(UIShield.FLASHLIGHT_SHIELD,new FlashlightFragment());
		case MIC_SHIELD:return addToCreatedListAndReturn(UIShield.MIC_SHIELD,new MicFragment());
		
		default:return new EmptyShieldFragment();
		}
	}
	
	private Fragment addToCreatedListAndReturn(UIShield uiShield, Fragment fragment){
		creadtedShields.put(uiShield, fragment);
		return fragment;
	}
	
	public UIShield getUIShield(int position){
		return UIShieldAdapter.getItem(position);
	}
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		UIShieldAdapter = new SelectedShieldsListAdapter(getActivity());
		setListAdapter(UIShieldAdapter);
	}
	
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
		newContent=getShieldFragment(position);
		getActivity().setTitle(UIShieldAdapter.getItem(position).getName()+" Shield");
		if (newContent != null)
			switchFragment(newContent);
	}
	
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
		
		if (getActivity() instanceof ShieldsOperationActivity) {
			ShieldsOperationActivity fca = (ShieldsOperationActivity) getActivity();
			fca.switchContent(fragment);
		}
	}
}
