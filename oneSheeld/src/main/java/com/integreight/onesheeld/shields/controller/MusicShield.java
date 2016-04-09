package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.PlaylistItem;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.database.MusicPlaylist;

import java.util.ArrayList;

public class MusicShield extends ControllerParent<MusicShield> {
    public MediaPlayer mediaPlayer;
    public String musicFileName = "";
    private ArrayList<PlaylistItem> mediaFiles = new ArrayList<PlaylistItem>();
    private int currentIndex = 0;
    private MusicEventHandler eventHandler;
    public int mediaDuration = 0;

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
    public ControllerParent<MusicShield> init(String tag) {
        checkMedia();
        checkMediaFilesList();
        return super.init(tag);
    }

    public void setEventHandler(MusicEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public ControllerParent<MusicShield> invalidate(SelectionAction selectionAction, boolean isToastable) {
        this.selectionAction = selectionAction;
        if(Build.VERSION.SDK_INT >=16)
        addRequiredPremission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkForPermissions())
            selectionAction.onSuccess();
        else
            selectionAction.onFailure();
        return super.invalidate(selectionAction, isToastable);
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
                mediaPlayer
                        .setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mediaDuration = mediaPlayer.getDuration();
                            }
                        });
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
            musicFileName = mediaFiles.get(currentIndex).name;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("TAG", "Music::init()", e);
            try {
                MusicPlaylist db = new MusicPlaylist(activity);
                db.openToWrite();
                db.delete(mediaFiles.get(currentIndex).id);
                mediaFiles = db.getPlaylist();
                db.close();
            } catch (Exception e1) {
            }
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
                if (eventHandler != null) {
                    eventHandler.play();
                    eventHandler.setMusicName(musicFileName);
                }
            } else if (frame.getFunctionId() == METHOD.PAUSE) {
                pause();
                if (eventHandler != null) {
                    eventHandler.pause();
                    eventHandler.setMusicName(musicFileName);
                }
            } else if (frame.getFunctionId() == METHOD.NEXT) {
                next();
                if (eventHandler != null) {
                    eventHandler.play();
                    eventHandler.setMusicName(musicFileName);
                    eventHandler.seekTo(0);
                }
            } else if (frame.getFunctionId() == METHOD.PREV) {
                prev();
                if (eventHandler != null) {
                    eventHandler.play();
                    eventHandler.setMusicName(musicFileName);
                    eventHandler.seekTo(0);
                }
            } else if (frame.getFunctionId() == METHOD.SEEK_FORWARD) {
                if (mediaPlayer != null) {
                    int pos = (int) (((int) frame.getArgument(0)[0])
                            * mediaDuration / 100);
                    seekTo(pos + mediaPlayer.getCurrentPosition());
                    if (eventHandler != null) {
                        eventHandler.play();
                        eventHandler.setMusicName(musicFileName);
                        eventHandler.seekTo(mediaPlayer.getCurrentPosition());
                    }
                }
            } else if (frame.getFunctionId() == METHOD.SEEK_BACKWARD) {
                if (mediaPlayer != null) {
                    int pos = (int) (((int) frame.getArgument(0)[0])
                            * mediaDuration / 100);
                    seekTo(mediaPlayer.getCurrentPosition() - pos);
                    if (eventHandler != null) {
                        eventHandler.play();
                        eventHandler.setMusicName(musicFileName);
                        eventHandler.seekTo(mediaPlayer.getCurrentPosition());
                    }
                }
            } else if (frame.getFunctionId() == METHOD.VOLUME) {
                if (mediaPlayer != null) {
                    int pos = ((int) frame.getArgument(0)[0]);
                    final float volume = (float) (1 - (Math.log(10 - pos) / Math
                            .log(10)));
                    mediaPlayer.setVolume(volume, volume);
                }
            } else if (frame.getFunctionId() == METHOD.SEEK) {
                if (mediaPlayer != null) {
                    int pos = ((int) frame.getArgument(0)[0]);
                    seekTo(pos);
                    if (eventHandler != null) {
                        eventHandler.play();
                        eventHandler.setMusicName(musicFileName);
                        eventHandler.seekTo(mediaPlayer.getCurrentPosition());
                    }
                }
            }
        }
    }

    public interface MusicEventHandler {
        public void play();

        public void pause();

        public void seekTo(int pos);

        public void setMusicName(String name);
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
