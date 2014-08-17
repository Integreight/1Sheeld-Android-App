package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.MusicShield;
import com.integreight.onesheeld.shields.controller.MusicShield.MusicEventHandler;
import com.integreight.onesheeld.shields.fragments.sub.MusicShieldSettings;
import com.integreight.onesheeld.utils.Log;

public class MusicPlayerFragment extends
		ShieldFragmentParent<MusicPlayerFragment> {
	SeekBar seekBar;
	TextView musicFileName, playingStatus;
	ImageView playingBtn;
	Thread seekBarTracker;
	private boolean isTracking = false;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		v = inflater.inflate(R.layout.musicplayer_shield_fragment_layout,
				container, false);
		return v;
	}

	@Override
	public void onStart() {
		activity.getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settingsViewContainer,
						MusicShieldSettings.getInstance()).commit();
		hasSettings = true;
		startTracking();
		super.onStart();
	}

	private void stopTracking() {
		if (seekBarTracker != null && seekBarTracker.isAlive()) {
			isTracking = false;
			seekBarTracker.interrupt();
			seekBarTracker = null;
		}
	}

	private void startTracking() {
		stopTracking();
		isTracking = true;
		seekBarTracker = new Thread(new Runnable() {

			@Override
			public void run() {
				while (isTracking) {
					if ((MusicShield) getApplication().getRunningShields().get(
							getControllerTag()) != null
							&& ((MusicShield) getApplication()
									.getRunningShields()
									.get(getControllerTag())).mediaPlayer != null) {
						seekBar.setMax(((MusicShield) getApplication()
								.getRunningShields().get(getControllerTag())).mediaDuration);
						try {
							seekBar.setProgress(((MusicShield) getApplication()
									.getRunningShields()
									.get(getControllerTag())).mediaPlayer
									.getCurrentPosition());
						} catch (IllegalStateException e) {
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Log.e("TAG", "MusicPlayer::seekBarTracker", e);
						break;
					}

				}
			}
		});
		seekBarTracker.start();
	}

	@Override
	public void onResume() {
		MusicShield control = (MusicShield) getApplication()
				.getRunningShields().get(getControllerTag());
		if (control != null) {
			((MusicShield) getApplication().getRunningShields().get(
					getControllerTag())).setEventHandler(eventHandler);
			control = (MusicShield) getApplication().getRunningShields().get(
					getControllerTag());
			if (control.mediaPlayer != null && control.mediaDuration > 0) {
				eventHandler.seekTo(control.mediaPlayer.getCurrentPosition()
						* 100 / control.mediaDuration);
				eventHandler.setMusicName(control.musicFileName);
				if (control.mediaPlayer.isPlaying())
					eventHandler.play();
				else
					eventHandler.pause();
			} else {
				eventHandler.seekTo(0);
				eventHandler.pause();
				eventHandler.setMusicName("Music file name");
			}
		}
		super.onResume();
	}

	@Override
	public void onStop() {
		stopTracking();
		super.onStop();
	}

	MusicEventHandler eventHandler = new MusicEventHandler() {

		@Override
		public void setMusicName(final String name) {
			if (canChangeUI())
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						musicFileName.setText(name);
					}
				});
		}

		@Override
		public void seekTo(final int pos) {
			if (canChangeUI())
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						seekBar.setMax(((MusicShield) getApplication()
								.getRunningShields().get(getControllerTag())).mediaDuration);
						seekBar.setProgress(pos);
					}
				});
		}

		@Override
		public void play() {
			if (canChangeUI())
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						playingBtn
								.setBackgroundResource(R.drawable.musicplayer_play_symbol);
						playingStatus.setText("Playing");
					}
				});
		}

		@Override
		public void pause() {
			if (canChangeUI())
				uiHandler.post(new Runnable() {

					@Override
					public void run() {
						playingBtn
								.setBackgroundResource(R.drawable.musicplayer_pause_symbol);
						playingStatus.setText("Paused");
					}
				});
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		musicFileName = (TextView) v.findViewById(R.id.playingMusic);
		seekBar = (SeekBar) v.findViewById(R.id.seekBar);
		playingBtn = (ImageView) v.findViewById(R.id.playingBtn);
		playingStatus = (TextView) v.findViewById(R.id.playingStatus);
	}
}
