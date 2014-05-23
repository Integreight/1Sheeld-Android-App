package com.integreight.onesheeld.shields.controller;

import java.util.ArrayList;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.PlaylistItem;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.database.MusicPlaylist;

public class MusicShield extends ControllerParent<MusicShield> {
	private MediaPlayer mediaPlayer;
	private ArrayList<PlaylistItem> mediaFiles = new ArrayList<PlaylistItem>();
	private int currentIndex = 0;

	private static class METHOD {
		public static byte PLAY = 0x02;
		public static byte PAUSE = 0x03;
		public static byte STOP = 0x01;
		public static byte PREV = 0x04;
		public static byte NEXT = 0x05;
		public static byte SEEK_FORWARD = 0x06;
		public static byte SEEK_BACKWARD = 0x07;
		public static byte VOLUME = 0x08;
		public static byte SEEK = 0x09;
	}

	public MusicShield() {
		super();
	}

	public MusicShield(Activity activity, String tag) {
		super(activity, tag);
		checkMedia();
		checkMediaFilesList();
	}

	@Override
	public ControllerParent<MusicShield> setTag(String tag) {
		checkMedia();
		checkMediaFilesList();
		return super.setTag(tag);
	}

	private void init() {
		try {
			if (currentIndex < mediaFiles.size() && currentIndex >= 0) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				mediaPlayer = MediaPlayer.create(activity,
						Uri.parse(mediaFiles.get(currentIndex).path));
				// mediaPlayer.prepare();
			} else {
				if (mediaFiles.size() != 0) {
					if (currentIndex > mediaFiles.size()) {
						currentIndex = 0;
					} else {
						currentIndex = mediaFiles.size() - 1;
					}
					init();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MusicPlaylist db = new MusicPlaylist(activity);
			db.openToWrite();
			db.delete(mediaFiles.get(currentIndex).id);
			mediaFiles = db.getPlaylist();
			db.close();
		}
	}

	public synchronized void seekTo(int pos) {
		if (mediaPlayer != null) {
			boolean playAfter = mediaPlayer.isPlaying();
			mediaPlayer.seekTo(pos);
			if (playAfter)
				mediaPlayer.start();
		}
	}

	public synchronized void next() {
		currentIndex += 1;
		init();
		play();
	}

	public synchronized void prev() {
		currentIndex -= 1;
		init();
		play();
	}

	public synchronized void play() {
		checkMedia();
		checkMediaFilesList();
		if (!mediaPlayer.isPlaying())
			mediaPlayer.start();
	}

	public synchronized void pause() {
		checkMedia();
		checkMediaFilesList();
		if (mediaPlayer.isPlaying())
			mediaPlayer.pause();
	}

	public synchronized void stop() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			init();
		}
	}

	private void checkMedia() {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		}
	}

	private void checkMediaFilesList() {
		if (mediaFiles == null
				|| (mediaFiles != null && mediaFiles.size() == 0)) {
			MusicPlaylist db = new MusicPlaylist(activity);
			db.openToWrite();
			mediaFiles = db.getPlaylist();
			db.close();
			init();
		}
	}

	@Override
	public void onNewShieldFrameReceived(ShieldFrame frame) {
		if (frame.getShieldId() == UIShield.MUSICPLAYER_SHIELD.getId()) {
			if (frame.getFunctionId() == METHOD.STOP) {
				stop();
			} else if (frame.getFunctionId() == METHOD.PLAY) {
				play();
			} else if (frame.getFunctionId() == METHOD.PAUSE) {
				pause();
			} else if (frame.getFunctionId() == METHOD.NEXT)
				next();
			else if (frame.getFunctionId() == METHOD.PREV)
				prev();
			else if (frame.getFunctionId() == METHOD.SEEK_FORWARD) {
				if (mediaPlayer != null) {
					int pos = (int) (((int) frame.getArgument(0)[0])
							* mediaPlayer.getDuration() / 100);
					seekTo(pos + mediaPlayer.getCurrentPosition());
				}
			} else if (frame.getFunctionId() == METHOD.SEEK_BACKWARD) {
				if (mediaPlayer != null) {
					int pos = (int) (((int) frame.getArgument(0)[0])
							* mediaPlayer.getDuration() / 100);
					seekTo(mediaPlayer.getCurrentPosition() - pos);
				}
			} else if (frame.getFunctionId() == METHOD.VOLUME) {
				if (mediaPlayer != null) {
					int pos = ((int) frame.getArgument(0)[0]);
					mediaPlayer.setVolume(pos / 10, pos / 10);
				}
			} else if (frame.getFunctionId() == METHOD.SEEK) {
				if (mediaPlayer != null) {
					int pos = ((int) frame.getArgument(0)[0]);
					seekTo(pos);
				}
			}
		}
	}

	@Override
	public void reset() {
		if (mediaPlayer != null)
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
				mediaPlayer.release();
			} else
				mediaPlayer.release();
		if (mediaFiles != null)
			mediaFiles.clear();
		mediaFiles = null;
		mediaPlayer = null;
	}

}
