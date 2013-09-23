package com.integreight.onesheeld.shields;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataDataHandler;

public class TwitterShield {
	private ArduinoFirmata firmata;
	private TwitterEventHandler eventHandler;
	private String lastTweet;
	private static final byte TWITTER_COMMAND=(byte)0x30;
	
	public String getLastTweet() {
		return lastTweet;
	}


	public TwitterShield(ArduinoFirmata firmata){
		this.firmata = firmata;
	}
	

	private void setFirmataEventHandler() {
		firmata.addDataHandler(new ArduinoFirmataDataHandler() {

			@Override
			public void onSysex(byte command, byte[] data) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDigital(int portNumber, int portData) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnalog(int pin, int value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUartReceive(byte[] data) {
				// TODO Auto-generated method stub
				byte command=data[0];
				int n=data.length-1;
				byte[] newArray=new byte[n];
				System.arraycopy(data,1,newArray,0,n);
				if (command==TWITTER_COMMAND){
					String tweet=new String(newArray);
					lastTweet=tweet;
					eventHandler.onRecieveTweet(tweet);
				}
				
			}
		});
	}

	
	public void setTwitterEventHandler(TwitterEventHandler eventHandler){
		this.eventHandler = eventHandler;
		firmata.initUart();
		setFirmataEventHandler();
	}

	public static interface TwitterEventHandler {
		void onRecieveTweet(String tweet);
	}
}
