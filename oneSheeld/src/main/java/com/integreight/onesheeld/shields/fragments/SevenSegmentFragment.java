package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.sdk.OneSheeldDevice;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.SevenSegmentShield;
import com.integreight.onesheeld.shields.controller.SevenSegmentShield.SevenSegmentsEventHandler;
import com.integreight.onesheeld.utils.ConnectingPinsView;
import com.integreight.onesheeld.utils.ConnectingPinsView.OnPinSelectionListener;

import java.util.Hashtable;

public class SevenSegmentFragment extends
        ShieldFragmentParent<SevenSegmentFragment> {
    ImageView aSegment;
    ImageView bSegment;
    ImageView cSegment;
    ImageView dSegment;
    ImageView eSegment;
    ImageView fSegment;
    ImageView gSegment;
    ImageView dotSegment;
    LinearLayout colorChooseCont;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.sevensegment_shield_fragment_view,
                container, false);

    }

    @Override
    public void doOnViewCreated(View v, Bundle savedInstanceState) {
        aSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_a_segment_imageview);
        bSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_b_segment_imageview);
        cSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_c_segment_imageview);
        dSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_d_segment_imageview);
        eSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_e_segment_imageview);
        fSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_f_segment_imageview);
        gSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_g_segment_imageview);
        dotSegment = (ImageView) v
                .findViewById(R.id.sevensegment_shield_dot_segment_imageview);
        colorChooseCont = (LinearLayout) v
                .findViewById(R.id.colorsContainer);
    }

    @Override
    public void doOnStart() {
        if (getControllerTag() != null)
            ((SevenSegmentShield) getApplication().getRunningShields().get(
                    getControllerTag()))
                    .setSevenSegmentsEventHandler(sevenSegmentsEventHandler);
        refreshSegments(((SevenSegmentShield) getApplication()
                .getRunningShields().get(getControllerTag())).refreshSegments());
        for (int i = 0; i < colorChooseCont.getChildCount(); i++) {
            final int x = i;
            colorChooseCont.getChildAt(x).setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            colorIndx = x;
                            if (getApplication().getRunningShields().get(
                                    getControllerTag()) != null)
                                refreshSegments(((SevenSegmentShield) getApplication()
                                        .getRunningShields().get(
                                                getControllerTag()))
                                        .refreshSegments());
                        }
                    });
        }
        ConnectingPinsView.getInstance().reset(
                getApplication().getRunningShields().get(getControllerTag()),
                new OnPinSelectionListener() {

                    @Override
                    public void onSelect(ArduinoPin pin) {
                        if (pin != null) {
                            (getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag()))
                                    .setConnected(new ArduinoConnectedPin(
                                            pin.microHardwarePin,
                                            OneSheeldDevice.INPUT));
                            refreshSegments(((SevenSegmentShield) getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag())).refreshSegments());
                        }

                    }

                    @Override
                    public void onUnSelect(ArduinoPin pin) {
                        refreshSegments(((SevenSegmentShield) getApplication()
                                .getRunningShields().get(getControllerTag()))
                                .refreshSegments());
                    }
                });

    }

    private SevenSegmentsEventHandler sevenSegmentsEventHandler = new SevenSegmentsEventHandler() {

        @Override
        public void onSegmentsChange(
                final Hashtable<String, Boolean> segmentsStatus) {
            // TODO Auto-generated method stub
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        refreshSegments(segmentsStatus);
                    }
                }
            });

        }
    };
    private int colorIndx = 0;
    private int[] vertical = new int[]{
            R.drawable.seven_segments_vertical_segment_red,
            R.drawable.seven_segments_vertical_segment_green,
            R.drawable.seven_segments_vertical_segment_yellow,
            R.drawable.seven_segments_vertical_segment_blue};
    private int[] hor = new int[]{
            R.drawable.seven_segments_horizontal_segment_red,
            R.drawable.seven_segments_horizontal_segment_green,
            R.drawable.seven_segments_horizontal_segment_yellow,
            R.drawable.seven_segments_horizontal_segment_blue};
    private int[] dot = new int[]{R.drawable.seven_segments_dot_red,
            R.drawable.seven_segments_dot_green,
            R.drawable.seven_segments_dot_yellow,
            R.drawable.seven_segments_dot_blue};

    private void refreshSegments(Hashtable<String, Boolean> segmentsStatus) {
        if (segmentsStatus.get("  A  ") != null && segmentsStatus.get("  A  ")) {
            aSegment.setImageResource(hor[colorIndx]);
        } else {
            aSegment.setImageResource(R.drawable.seven_segments_horizontal_segment_gray);
        }

        if (segmentsStatus.get("  B  ") != null && segmentsStatus.get("  B  ")) {
            bSegment.setImageResource(vertical[colorIndx]);
        } else {
            bSegment.setImageResource(R.drawable.seven_segments_vertical_segment_gray);
        }

        if (segmentsStatus.get("  C  ") != null && segmentsStatus.get("  C  ")) {
            cSegment.setImageResource(vertical[colorIndx]);
        } else {
            cSegment.setImageResource(R.drawable.seven_segments_vertical_segment_gray);
        }

        if (segmentsStatus.get("  D  ") != null && segmentsStatus.get("  D  ")) {
            dSegment.setImageResource(hor[colorIndx]);
        } else {
            dSegment.setImageResource(R.drawable.seven_segments_horizontal_segment_gray);
        }

        if (segmentsStatus.get("  E  ") != null && segmentsStatus.get("  E  ")) {
            eSegment.setImageResource(vertical[colorIndx]);
        } else {
            eSegment.setImageResource(R.drawable.seven_segments_vertical_segment_gray);
        }

        if (segmentsStatus.get("  F  ") != null && segmentsStatus.get("  F  ")) {
            fSegment.setImageResource(vertical[colorIndx]);
        } else {
            fSegment.setImageResource(R.drawable.seven_segments_vertical_segment_gray);
        }

        if (segmentsStatus.get("  G  ") != null && segmentsStatus.get("  G  ")) {
            gSegment.setImageResource(hor[colorIndx]);
        } else {
            gSegment.setImageResource(R.drawable.seven_segments_horizontal_segment_gray);
        }

        if (segmentsStatus.get(" DOT ") != null && segmentsStatus.get(" DOT ")) {
            dotSegment.setImageResource(dot[colorIndx]);
        } else {
            dotSegment.setImageResource(R.drawable.seven_segments_dot_gray);
        }

    }

    private void initializeFirmata() {
        if ((getApplication().getRunningShields().get(getControllerTag())) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new SevenSegmentShield(activity, getControllerTag()));

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

}