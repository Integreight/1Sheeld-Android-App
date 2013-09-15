package com.integreight.onesheeld.shieldsfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.integreight.onesheeld.R;

public class LedFragment extends Fragment {

	ImageView ledImage;
	boolean isOn=false;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.led_shield_fragment_layout,
				container, false);
		ledImage = (ImageView) v.findViewById(R.id.led_shield_led_imageview);
		return v;

	}
	
//	public void toggleLed(View v){
//		if(isOn){
//			ledImage.setImageResource(R.drawable.led_shield_led_off);
//			isOn=false;
//		} else{
//			ledImage.setImageResource(R.drawable.led_shield_led_on);
//			isOn=true;
//		}
//	}
	
	
}
