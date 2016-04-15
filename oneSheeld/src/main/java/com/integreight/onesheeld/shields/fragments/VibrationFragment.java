package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.VibrationShield;

/**
 * Author: Mostafa Mahmoud
 * Email: mostafa_mahmoud@protonmail.com
 * Created on: 11/22/15
 */
public class VibrationFragment extends ShieldFragmentParent<VibrationFragment>{
    private Button vibrationStopButton;
    Animation shake;
    TextView vibrationTextTextView;
    ImageView vibrationLogo;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        return inflater.inflate(R.layout.vibration_shield_fragment_layout,container,false);
    }

    @Override
    public void doOnViewCreated(View view, Bundle savedInstanceStat) {
        vibrationStopButton = (Button) view.findViewById(R.id.vibration_stop_button);
        vibrationTextTextView = (TextView) view.findViewById(R.id.vibration_shield_text_textview);
        vibrationLogo = (ImageView) view.findViewById(R.id.vibration_shield_logo_imageview);
    }

    @Override
    public void doOnActivityCreated(Bundle savedInstanceStat) {
        shake = AnimationUtils.loadAnimation(activity, R.anim.shake);
    }

    @Override
    public void doOnStart() {
        VibrationShield.VibrationShieldListener vibrationShieldListener = new VibrationShield.VibrationShieldListener() {
            @Override
            public void onStart() {
                if (canChangeUI()) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            vibrationLogo.startAnimation(shake);
                            vibrationTextTextView.setText("");
                            vibrationStopButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onPause() {
                if (canChangeUI()) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            vibrationLogo.clearAnimation();
                            vibrationTextTextView.setText(R.string.vibration_paused);
                            vibrationStopButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onStop() {
                if (canChangeUI()) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            vibrationLogo.clearAnimation();
                            vibrationTextTextView.setText("");
                            vibrationStopButton.setVisibility(View.INVISIBLE);
                            uiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    vibrationTextTextView.setText("");
                                }
                            },2000);
                        }
                    });
                }
            }
        };
        ((VibrationShield)getApplication().getRunningShields().get(getControllerTag()))
                .setVibrationShieldListener(vibrationShieldListener);
        vibrationStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VibrationShield) getApplication().getRunningShields().get(getControllerTag()))
                        .stop();
            }
        });
    }

    @Override
    public void doOnResume() {
        invalidateController();
        if(canChangeUI()){
            if(getApplication().getIsDemoMode()
                    && !getApplication().isConnectedToBluetooth()){
                vibrationLogo.startAnimation(shake);
                vibrationTextTextView.setText(R.string.vibration_ready);
                uiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        vibrationLogo.clearAnimation();
                    }
                },1000);
            }
            else if(((VibrationShield) getApplication().getRunningShields().get(getControllerTag()))
                    .isPaused() || ((VibrationShield) getApplication().getRunningShields().get(getControllerTag()))
                    .isVibrating()){
                vibrationStopButton.setVisibility(View.VISIBLE);
                if(((VibrationShield) getApplication().getRunningShields().get(getControllerTag()))
                        .isVibrating()) {
                    vibrationLogo.startAnimation(shake);
                    vibrationTextTextView.setText("");
                }
                else if(((VibrationShield) getApplication().getRunningShields().get(getControllerTag()))
                        .isPaused()){
                    vibrationTextTextView.setText(R.string.vibration_paused);
                }
            }
        }
    }

    @Override
    public void doOnServiceConnected() {
        invalidateController();
    }

    private void invalidateController() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new VibrationShield(activity, getControllerTag()));
        }
    }
}
