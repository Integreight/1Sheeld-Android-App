package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.NfcShield;
import com.integreight.onesheeld.shields.controller.NfcShield.NFCEventHandler;


/**
 * Created by Mouso on 3/11/2015.
 */
public class NfcFragment extends ShieldFragmentParent<NfcFragment> {

    TextView ReadData;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.nfc_shield_fragment_view, container, false);
        return v;
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(nfcEventHandler);
        super.onStart();
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(), new NfcShield(activity, getControllerTag()));
        }
    }

    @Override
    public void onResume() {
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(nfcEventHandler);
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ReadData =(TextView) v.findViewById(R.id.NfcReadDisplayText);
        ImageView imageView = (ImageView) v.findViewById(R.id.NfcImageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).Display();
            }
        });

    }

    private NFCEventHandler nfcEventHandler = new NFCEventHandler() {
        @Override
        public void ReadNdef(byte[] data) {
            if (ReadData != null){
                ReadData.append("\n" + new String(data));
            }
        }
    };
}
