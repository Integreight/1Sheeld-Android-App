package com.integreight.onesheeld.shields.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.GlcdShield;
import com.integreight.onesheeld.shields.controller.utils.GlcdView;
import com.integreight.onesheeld.shields.fragments.sub.GlcdHelperView;

/**
 * Created by Mouso on 6/7/2015.
 */
public class GlcdFragment extends ShieldFragmentParent<GlcdFragment>{
    private GlcdView helperView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        helperView = new GlcdView(activity);
        return helperView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*helperView =(mView) v.findViewById(R.id.glcd_view);
        Button bn = (Button) v.findViewById(R.id.glcd_btn_1);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GlcdShield) getApplication().getRunningShields().get(getControllerTag())).doClear();
            }
        });*/
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }


        super.onStart();
    }

    private GlcdShield.GlcdEventHandler glcdEventHandler = new GlcdShield.GlcdEventHandler() {
        @Override
        public void setView(GlcdView glcdView) {
            if (canChangeUI() && uiHandler!= null) {
                helperView = glcdView;
                //Bitmap bb = ((GlcdView) (((GlcdShield) getApplication().getRunningShields().get(getControllerTag())).getGlcdView())).getDrawingCache();
                //helperView.setBitmap(bb);
                //helperView.invalidate();
            }
        }

        @Override
        public GlcdView getView() {
            return helperView;
        }

    };

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(), new GlcdShield(activity, getControllerTag()));
        }
    }

    @Override
    public void onResume() {
        ((GlcdShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(glcdEventHandler);
        super.onResume();
        //((GlcdShield) getApplication().getRunningShields().get(getControllerTag())).doClear();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


}
