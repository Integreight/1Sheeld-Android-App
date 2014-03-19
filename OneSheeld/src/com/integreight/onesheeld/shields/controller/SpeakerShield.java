package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.media.MediaPlayer;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.ControllerParent;

public class SpeakerShield extends ControllerParent<ControllerParent<?>> {
	private SpeakerEventHandler eventHandler;
	private static final byte BUZZER_COMMAND = (byte) 0x08;
	private static final byte BUZZER_ON = (byte) 0x01;
	private static final byte BUZZER_OFF = (byte) 0x00;
	private boolean isResumed = false;
	MediaPlayer mp;
	private static final int soundResourceId = R.raw.door_chime_sound;

	public SpeakerShield() {
		super();
	}

	public SpeakerShield(Activity activity, String tag) {
		super(activity, tag);
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

		if (frame.getShieldId() == BUZZER_COMMAND) {
			byte argumentValue = frame.getArgument(0)[0];
			switch (argumentValue) {
			case BUZZER_ON:
				// turn on bin
				playSound(soundResourceId);
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

	public void playSound(int soundResourceId) {
		if (mp == null)
			mp = MediaPlayer.create(getApplication(), soundResourceId);
		/*
		 * if(mp.isPlaying()){ Resources res = getActivity().getResources();
		 * AssetFileDescriptor afd = res.openRawResourceFd(soundResourceId);
		 * FileDescriptor fd = afd.getFileDescriptor(); mp.reset(); try {
		 * mp.setDataSource(fd); mp.prepare(); } catch (IllegalArgumentException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (IllegalStateException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 * 
		 * mp.start(); } else
		 */
		mp.start();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
}
