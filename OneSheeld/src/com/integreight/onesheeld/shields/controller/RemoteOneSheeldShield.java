package com.integreight.onesheeld.shields.controller;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.push.PushMessagesReceiver;
import com.integreight.onesheeld.shields.ControllerParent;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

public class RemoteOneSheeldShield extends ControllerParent<RemoteOneSheeldShield> {
	private static final byte PIN_MODE = (byte) 0x01;
	private static final byte DIGITAL_WRITE = (byte) 0x02;
	private static final byte DIGITAL_READ = (byte) 0x03;
	private static final byte ANALOG_WRITE= (byte) 0x04;
	
//	public static final String INTENT="com.integreight.onesheeld.push.DigitalPushMessages";
	public RemoteOneSheeldShield() {
		super();
	}

	public RemoteOneSheeldShield(Activity activity, String tag) {
		super(activity, tag);
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.REMOTEONESHEELD_SHIELD.getId()) {
			switch (frame.getFunctionId()) {
			case PIN_MODE:processPinModeMessage(frame);break;
			case DIGITAL_WRITE:processDigitalWriteMessage(frame);break;
			case DIGITAL_READ:processDigitalReadRequestMessage(frame);break;
			case ANALOG_WRITE:processAnalogWriteMessage(frame);break;
			}
		}
	}
	
	private void processPinModeMessage(ShieldFrame frame){
		if(frame==null)return;
		if(frame.getArguments().size()!=3)return;
		String address=frame.getArgumentAsString(0);
		int pin=(int)(frame.getArgument(1)[0]&0xff);
		int mode=(int)(frame.getArgument(2)[0]&0xff);
		JSONObject json=getPinModeMessage(pin, mode);
		try {
			json.put("action", PushMessagesReceiver.PinModePushMessageAction);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		sendPushMessage(address, json);
	}
	
	private void processDigitalReadRequestMessage(ShieldFrame frame){
		if(frame==null)return;
		if(frame.getArguments().size()!=2)return;
		String address=frame.getArgumentAsString(0);
		int pin=(int)(frame.getArgument(1)[0]&0xff);
		JSONObject json=getDigitalRequestReadMessage(pin);
		try {
			json.put("action", PushMessagesReceiver.DigitalReadRequestPushMessageAction);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		sendPushMessage(address, json);
	}
	
	private void processDigitalWriteMessage(ShieldFrame frame){
		if(frame==null)return;
		if(frame.getArguments().size()!=3)return;
		String address=frame.getArgumentAsString(0);
		int pin=(int)(frame.getArgument(1)[0]&0xff);
		boolean value=(int)(frame.getArgument(2)[0]&0xff)!=0;
		JSONObject json=getDigitalWriteMessage(pin,value);
		try {
			json.put("action", PushMessagesReceiver.DigitalWritePushMessageAction);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		sendPushMessage(address, json);
	}
	
	private void processAnalogWriteMessage(ShieldFrame frame){
		if(frame==null)return;
		if(frame.getArguments().size()!=3)return;
		String address=frame.getArgumentAsString(0);
		int pin=(int)(frame.getArgument(1)[0]&0xff);
		int value=(int)(frame.getArgument(2)[0]&0xff);
		JSONObject json=getAnalogWriteMessage(pin, value);
		try {
			json.put("action", PushMessagesReceiver.AnalogWritePushMessageAction);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		sendPushMessage(address, json);
	}
	
	private void sendPushMessage(String installationId, JSONObject json){
		try {
			json.put("from", ParseInstallation.getCurrentInstallation().getInstallationId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ParseQuery<ParseInstallation> query = ParseInstallation.getQuery(); 
		query.whereEqualTo("installationId", installationId);    
		ParsePush push = new ParsePush();
		push.setExpirationTimeInterval(10);//10 seconds timeout
		push.setQuery(query);
		push.setData(json);//push.setMessage(json.toString());
		push.sendInBackground();
	}
	
	private JSONObject getDigitalRequestReadMessage(int pin){
		try {
			JSONObject json=new JSONObject();
//			json.put("type","DIGITAL_READ");
			json.put("pin", pin);
			json.put("from", ParseInstallation.getCurrentInstallation().getInstallationId());
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getPinModeMessage(int pin, int mode){
		try {
			JSONObject json=new JSONObject();
//			json.put("type","PIN_MODE");
			json.put("pin", pin);
			json.put("mode", mode);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject getDigitalWriteMessage(int pin, boolean value){
		try {
			JSONObject json=new JSONObject();
//			json.put("type","DIGITAL_WRITE");
			json.put("pin", pin);
			json.put("value", true);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private JSONObject getAnalogWriteMessage(int pin, int value){
		try {
			JSONObject json=new JSONObject();
//			json.put("type","ANALOG_WRITE");
			json.put("pin", pin);
			json.put("value", value);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private JSONObject getKeyValueMessage(String key, float value){
		try {
			JSONObject json=new JSONObject();
			json.put("type","KEYVALUE_MESSAGE");
			json.put("value_type", "FLOAT");
			json.put("key", key);
			json.put("value", value);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private JSONObject getKeyValueMessage(String key, String value){
		try {
			JSONObject json=new JSONObject();
			json.put("type","KEYVALUE_MESSAGE");
			json.put("value_type", "STRING");
			json.put("key", value);
			json.put("value", value);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void reset() {
	}
}
