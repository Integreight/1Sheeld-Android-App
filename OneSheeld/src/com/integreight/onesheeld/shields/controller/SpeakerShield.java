package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.utils.ControllerParent;

public class SpeakerShield extends ControllerParent<ControllerParent<?>> {
	private SpeakerEventHandler eventHandler;
	private static final byte BUZZER_ON = (byte) 0x01;
	private static final byte BUZZER_OFF = (byte) 0x00;
	private boolean isResumed = false;
	public int connectedPin = -1;
	private boolean isLedOn;
	private MediaPlayer mp;
	private static final int soundResourceId = R.raw.buzzer_sound;

	public SpeakerShield() {
		super();
		requiredPinsIndex = 0;
		shieldPins = new String[] { "Buzzer" };
	}

	public SpeakerShield(Activity activity, String tag) {
		super(activity, tag);
	}

	public boolean refreshLed() {
		if (connectedPin != -1)
			isLedOn = getApplication().getAppFirmata()
					.digitalRead(connectedPin);
		else
			isLedOn = false;
		if (isLedOn)
			playSound();
		else
			stopBuzzer();
		return isLedOn;
	}

	@Override
	public void onDigital(int portNumber, int portData) {
		refreshLed();
		super.onDigital(portNumber, portData);
	}

	@Override
	public void setConnected(ArduinoConnectedPin... pins) {
		this.connectedPin = pins[0].getPinID();
		super.setConnected(pins);
	}

	public void setSpeakerEventHandler(SpeakerEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		CommitInstanceTotable();
	}

	public static interface SpeakerEventHandler {
		void onSpeakerChange(boolean isOn);
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {

		if (frame.getShieldId() == UIShield.BUZZER_SHIELD.getId()) {
			byte argumentValue = frame.getArgument(0)[0];
			switch (argumentValue) {
			case BUZZER_ON:
				// turn on bin
				playSound();
				if (isResumed)
					eventHandler.onSpeakerChange(true);
				break;
			case BUZZER_OFF:
				// turn off bin
				if (isResumed)
					eventHandler.onSpeakerChange(false);
				break;
			default:
				break;
			}

		}
	}

	public void doOnResume() {
		isResumed = true;
	}

	String uri;

	public synchronized void playSound() {
		uri = null;// getApplication().getBuzzerSound();
		if (mp == null) {
			if (uri == null)
				mp = MediaPlayer.create(getApplication(), soundResourceId);
			else {
				mp = new MediaPlayer();
				if (uri != null)
					try {
						mp = MediaPlayer.create(activity, Uri.parse(uri));
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						Toast.makeText(
								activity,
								"Can't play the current buzz! Please, replace it",
								Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					} catch (SecurityException e) {
						Toast.makeText(
								activity,
								"Can't play the current buzz! Please, replace it",
								Toast.LENGTH_SHORT).show();
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						Toast.makeText(
								activity,
								"Can't play the current buzz! Please, replace it",
								Toast.LENGTH_SHORT).show();
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		// else {
		// if (uri != null) {
		// try {
		// // Ring
		// if (!mp.isPlaying())
		// mp = MediaPlayer.create(activity, Uri.parse(uri));
		// } catch (IllegalArgumentException e) {
		// Toast.makeText(activity,
		// "Can't play the current buzz! Please, replace it",
		// Toast.LENGTH_SHORT).show();
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SecurityException e) {
		// Toast.makeText(activity,
		// "Can't play the current buzz! Please, replace it",
		// Toast.LENGTH_SHORT).show();
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalStateException e) {
		// Toast.makeText(activity,
		// "Can't play the current buzz! Please, replace it",
		// Toast.LENGTH_SHORT).show();
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// } else {
		// if (!mp.isPlaying())
		// mp = MediaPlayer.create(getApplication(), soundResourceId);
		// }
		// }
		mp.setLooping(true);
		if (!mp.isPlaying())
			mp.start();
	}

	public synchronized void stopBuzzer() {
		if (mp != null && mp.isPlaying()) {
			mp.setLooping(false);
			mp.pause();
		}
	}

	@Override
	public void reset() {
		if (mp != null) {
			if (mp.isPlaying())
				mp.stop();
			mp.release();
		}
		mp = null;
	}
}
