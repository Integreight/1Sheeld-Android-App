package com.integreight.onesheeld.shieldsfragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.services.OneSheeldService;
import com.integreight.onesheeld.services.OneSheeldService.OneSheeldBinder;

public class LedFragment extends Fragment {

	ImageView ledImage;
	boolean isOn=false;
	OneSheeldService _1SheeldService;
    boolean mBound = false;
	
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            OneSheeldBinder binder = (OneSheeldBinder) service;
            _1SheeldService = binder.getService();
            
            mBound = true;
            if(mBound){
            	_1SheeldService.getFirmata().pinMode(3, ArduinoFirmata.INPUT);
            	_1SheeldService.getFirmata().addDataHandler(new ArduinoFirmataDataHandler() {
					
					@Override
					public void onSysex(byte command, byte[] data) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onDigital(int portNumber, int portData) {
						// TODO Auto-generated method stub
						
					if(_1SheeldService.getFirmata().digitalRead(3)){
						isOn=false;
						toggleLed();
					}
					else {
						isOn=true;
						toggleLed();
					}
					Log.d("Led","Digital");
					}
					
					@Override
					public void onAnalog(int pin, int value) {
						// TODO Auto-generated method stub
						
					}
				});
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.led_shield_fragment_layout,
				container, false);
		ledImage = (ImageView) v.findViewById(R.id.led_shield_led_imageview);
		
		return v;

	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Intent intent = new Intent(getActivity(), OneSheeldService.class);
		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void toggleLed(){
		if(isOn){
			ledImage.setImageResource(R.drawable.led_shield_led_off);
			isOn=false;
		} else{
			ledImage.setImageResource(R.drawable.led_shield_led_on);
			isOn=true;
		}
	}
	
	
}
