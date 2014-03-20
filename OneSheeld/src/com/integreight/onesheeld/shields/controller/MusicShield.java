package com.integreight.onesheeld.shields.controller;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.media.MediaPlayer;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.model.PlaylistItem;
import com.integreight.onesheeld.utils.ControllerParent;
import com.integreight.onesheeld.utils.database.MusicPlaylist;

public class MusicShield extends ControllerParent<MusicShield> {
	private MediaPlayer mediaPlayer;
	private ArrayList<PlaylistItem> mediaFiles = new ArrayList<PlaylistItem>();
	private int currentIndex = 0;

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
			if (mediaFiles.size() > currentIndex) {
				mediaPlayer.setDataSource(mediaFiles.get(currentIndex).path);
				mediaPlayer.prepare();
			} else {
				if (currentIndex > 0) {
					currentIndex -= 1;
					init();
				}
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MusicPlaylist db = new MusicPlaylist(activity);
			db.openToWrite();
			db.delete(mediaFiles.get(currentIndex).id);
			mediaFiles = db.getPlaylist();
			db.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MusicPlaylist db = new MusicPlaylist(activity);
			db.openToWrite();
			db.delete(mediaFiles.get(currentIndex).id);
			mediaFiles = db.getPlaylist();
			db.close();
		}
	}

	public void seekTo(int pos) {
		if (mediaPlayer != null) {
			int p = mediaPlayer.getDuration();
			int pos2 = (int) (pos * mediaPlayer.getDuration() / 100);
			mediaPlayer.seekTo(pos2);
			mediaPlayer.start();
		}
	}

	public void next() {
		currentIndex += 1;
		init();
	}

	public void prev() {
		currentIndex -= 1;
		init();
	}

	public void togglePlayOrPause() {
		checkMedia();
		checkMediaFilesList();
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		} else {
			mediaPlayer.start();
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
		// TODO Auto-generated method stub

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
