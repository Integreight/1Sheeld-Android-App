package com.integreight.onesheeld.shields.controller.utils;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import com.integreight.onesheeld.utils.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MicSoundMeter {
    // static final private double EMA_FILTER = 0.6;
    static final private double POWER_REFERENCE = 0.00002;
    private static MicSoundMeter thisInstance;
    boolean isCanceled = false;
    boolean isRecording = false;
    boolean initialStart = true;
    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    private File folder = null;

    private MicSoundMeter() {
        // TODO Auto-generated constructor stub
    }

    public static MicSoundMeter getInstance() {
        if (thisInstance == null)
            thisInstance = new MicSoundMeter();
        return thisInstance;
    }

    public boolean start(boolean record) {
        return start(record, null);
    }

    public boolean start(boolean record, String fileName) {
        if (isCanceled | initialStart) {
            initialStart = false;
            isCanceled = false;
            isRecording = false;
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mRecorder.setAudioEncodingBitRate(128);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
                mRecorder.setAudioSamplingRate(44100);
            else
                mRecorder.setAudioSamplingRate(8000);

            if (record) {
                File folder = new File(
                        Environment.getExternalStorageDirectory()
                                + "/OneSheeld");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                folder = new File(
                        Environment.getExternalStorageDirectory()
                                + "/OneSheeld/Mic");
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                mRecorder.setOutputFile(Environment
                        .getExternalStorageDirectory()
                        + "/OneSheeld/Mic/" + ((fileName != null) ? (fileName): ("Mic_" + String.valueOf(new Date().getTime()))) + ".mp3/");
            } else
                mRecorder.setOutputFile("/dev/null");

            try {
                isRecording = true;
                mRecorder.prepare();
                mRecorder.start();
                mEMA = 0.0;
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
                if (isRecording) {
                    isRecording = false;
                    isCanceled = true;
                    try {
                    mRecorder.stop();
                    mRecorder.reset();
                    mRecorder.release();
                    } catch (Exception e) {
                        Log.e("TAG", "stop MRecorder::Mic", e);
                    }
                    mRecorder = null;
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
