package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

import java.io.IOException;

public class SpeakerShield extends ControllerParent<ControllerParent<?>> {
    private final String VOLUME_PREF_KEY = "buzzerVolume";
    private final int MAX_VOLUME = 100;
    private SpeakerEventHandler eventHandler;
    private static final byte BUZZER_ON = (byte) 0x01;
    private static final byte BUZZER_OFF = (byte) 0x00;
    private boolean isResumed = false;
    public int connectedPin = -1;
    private boolean isLedOn;
    private MediaPlayer mp;

    public SpeakerShield() {
        super();
        requiredPinsIndex = 0;
        shieldPins = new String[]{OneSheeldApplication.getContext().getString(R.string.buzzer_pin_name)};
    }

    public SpeakerShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public boolean refreshLed() {
        if (connectedPin != -1) {
            if (getApplication().isConnectedToBluetooth())
                isLedOn = getApplication().getConnectedDevice().digitalRead(connectedPin);
        } else
            isLedOn = false;
        if (isLedOn)
            playSound();
        else
            stopBuzzer();
        return isLedOn;
    }

    @Override
    public void onDigital(int portNumber, boolean portData) {
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
                        if (eventHandler != null)
                            eventHandler.onSpeakerChange(true);
                    break;
                case BUZZER_OFF:
                    // turn off bin
                    stopBuzzer();
                    if (eventHandler != null && isResumed)
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
    private boolean isPrepared = false;

    public synchronized void playSound() {
        uri = null;// getApplication().getBuzzerSound();
        if (mp == null) {
            if (uri == null) {
                mp = new MediaPlayer();
                final float volume = (float) (1 - (Math.log(MAX_VOLUME
                        - getBuzzerVolume()) / Math.log(MAX_VOLUME)));
                mp.setVolume(volume, volume);
                try {
                    AssetFileDescriptor descriptor = activity.getAssets().openFd("buzzer_sound.mp3");
                    mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    descriptor.close();
                    mp.prepareAsync();
                    mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            isPrepared = true;
                            mp.start();
                        }
                    });
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    Log.e("TAG", "speaker::setVolume::setDataSource", e);
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    Log.e("TAG", "speaker::setVolume::setDataSource", e);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("TAG", "speaker::setVolume::setDataSource", e);
                }
            } else {
                mp = new MediaPlayer();
                final float volume = (float) (1 - (Math.log(MAX_VOLUME
                        - getBuzzerVolume()) / Math.log(MAX_VOLUME)));
                mp.setVolume(volume, volume);
                if (uri != null)
                    try {
                        mp = MediaPlayer.create(activity, Uri.parse(uri));
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(
                                activity,
                                R.string.buzzer_an_error_occurred_cant_buzz_toast,
                                Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "speaker::setVolume::setDataSource", e);
                    } catch (SecurityException e) {
                        Toast.makeText(
                                activity,
                                R.string.buzzer_an_error_occurred_cant_buzz_toast,
                                Toast.LENGTH_SHORT).show();
                        // TODO Auto-generated catch block
                        Log.e("TAG", "speaker::setVolume::setDataSource", e);
                    } catch (IllegalStateException e) {
                        Toast.makeText(
                                activity,
                                R.string.buzzer_an_error_occurred_cant_buzz_toast,
                                Toast.LENGTH_SHORT).show();
                        // TODO Auto-generated catch block
                        Log.e("TAG", "speaker::setVolume::setDataSource", e);
                    }
            }
        }
        mp.setLooping(true);
        if (!mp.isPlaying() && isPrepared) {
            mp.start();
        }
    }

    public synchronized void stopBuzzer() {
        if (mp != null && mp.isPlaying()) {
            mp.setLooping(false);
            mp.stop();
            mp.release();
            mp = null;
            isPrepared = false;
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

    public int getBuzzerVolume() {
        return getApplication().getAppPreferences().getInt(VOLUME_PREF_KEY, 50);
    }

    public synchronized void setBuzzerVolume(int vol) {
        getApplication().getAppPreferences().edit()
                .putInt(VOLUME_PREF_KEY, vol).commit();
        final float volume = (float) (1 - (Math.log(MAX_VOLUME - vol) / Math
                .log(MAX_VOLUME)));
        if (mp != null) {
            try {
                mp.setVolume(volume, volume);
            } catch (IllegalStateException e) {

            }
        }
    }
}
