package com.integreight.onesheeld.shields.controller.utils;

import android.media.MediaRecorder;
import android.os.Environment;

import com.integreight.onesheeld.utils.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MicSoundMeter {
    // static final private double EMA_FILTER = 0.6;
    static final private double POWER_REFERENCE = 0.00002;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    private File folder = null;
    private static MicSoundMeter thisInstance;
    boolean isCanceled = false;
    boolean isRecording = false;
    boolean initialStart = true;

    private MicSoundMeter() {
        // TODO Auto-generated constructor stub
    }

    public static MicSoundMeter getInstance() {
        if (thisInstance == null)
            thisInstance = new MicSoundMeter();
        return thisInstance;
    }

    public boolean start(boolean record){
        return start(record,null);
    }

    public boolean start(boolean record,String fileName) {
        if (isCanceled | initialStart) {
            initialStart = false;
            isCanceled = false;
            isRecording = false;
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            if (record) {
                folder = new File(
                        Environment.getExternalStorageDirectory()
                                + "/OneSheeld/Mic");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                mRecorder.setOutputFile(Environment
                        .getExternalStorageDirectory()
                        + "/OneSheeld/Mic/"+((fileName!=null)? fileName : ("Mic_" + String.valueOf(new Date().getTime()))) + ".amr/");
            }else
                mRecorder.setOutputFile("/dev/null");

            try {
                mRecorder.prepare();
                mRecorder.start();
                mEMA = 0.0;
                isRecording = true;
                return true;
            } catch (IllegalStateException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

        } else {
            Log.d("Mic", "Not Started");
            return false;
        }
    }

    public void stop() {
        if (mRecorder != null) {
            try {
                if (isRecording) {
                    mRecorder.stop();
                    mRecorder.reset();
                    mRecorder.release();
                    mRecorder = null;
                    isRecording = false;
                    isCanceled = true;
                }
            } catch (Exception e) {
                Log.e("TAG", "stop MRecorder::Mic", e);
            }
        }
    }

    public double getAmplitude() {
        if (mRecorder != null) {
            double maxAmp = 0;
            try {
                maxAmp = mRecorder.getMaxAmplitude();
            } catch (Exception e) {
            }
            return maxAmp;
        } else
            return 0;

    }

    // db= 20* log10(amplitude/baseline_amplitude);
    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        if (amp == 0)
            return 0;

        mEMA = (20.0 * Math.log10(amp / POWER_REFERENCE));
        return (mEMA - 100.0);
    }

}
