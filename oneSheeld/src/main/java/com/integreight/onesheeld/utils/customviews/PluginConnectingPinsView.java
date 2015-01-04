package com.integreight.onesheeld.utils.customviews;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.shields.observer.OnChildFocusListener;
import com.integreight.onesheeld.utils.customviews.PluginPinsColumnContainer.PinData;

public class PluginConnectingPinsView extends Fragment {
    private static PluginConnectingPinsView thisInstance;
    private View view;
    private String selectedPinName = "";

    public static PluginConnectingPinsView getInstance() {
        if (thisInstance == null) {
            thisInstance = new PluginConnectingPinsView();
        }
        return thisInstance;
    }

    public void recycle() {
        thisInstance = null;
    }

    boolean isInflated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.plugin_connecting_pins_layout,
                    container, false);
            isInflated = true;
        } else
            isInflated = false;
        if (((ViewGroup) view.getParent()) != null)
            ((ViewGroup) view.getParent()).removeAllViews();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private Handler resettingHandler = new Handler();

    public void reset(final OnPinSelectionListener listner,
                      final int currentIndx) {
        final TextView show = (TextView) view.findViewById(R.id.show);
        selectedPinName = "";
        show.setText("");
        show.setVisibility(View.INVISIBLE);
        final ImageView cursor = ((ImageView) view.findViewById(R.id.cursor));
        cursor.setVisibility(View.INVISIBLE);
        final PluginPinsColumnContainer thisPinsContainer = ((PluginPinsColumnContainer) view
                .findViewById(R.id.cont));
        if (resettingHandler == null)
            resettingHandler = new Handler();
        resettingHandler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                thisPinsContainer.setup(new OnChildFocusListener() {

                    @Override
                    public void focusOnThisChild(int childIndex, String tag) {
                        show.setVisibility(View.VISIBLE);
                        show.setText(tag.startsWith("_") ? tag.substring(1)
                                : tag);
                    }

                    @Override
                    public void selectThisChild(int childIndex, String tag) {
                        show.setVisibility(tag.length() > 0 ? View.VISIBLE
                                : View.INVISIBLE);
                        show.setText(tag.startsWith("_") ? tag.substring(1)
                                : tag);
                        if (childIndex != -1) {
                            listner.onSelect(ArduinoPin.valueOf(tag));
                        } else {
                            listner.onSelect(null);
                        }
                    }
                }, cursor, new onGetPinsView() {

                    @Override
                    public void onPinsDrawn() {
                        if (selectedPinName.length() > 0) {
                            PinData data = thisPinsContainer
                                    .getDataOfTag(selectedPinName);
                            if (data.rect != null && data.index != -1) {
                                thisPinsContainer.setCursorTo(data);
                                show.setVisibility(View.VISIBLE);
                                show.setText(data.tag.startsWith("_") ? data.tag
                                        .substring(1) : data.tag);
                            } else {
                                show.setText("");
                                show.setVisibility(View.INVISIBLE);
                                cursor.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            show.setText("");
                            show.setVisibility(View.INVISIBLE);
                            cursor.setVisibility(View.INVISIBLE);
                        }
                    }
                }, currentIndx);
                LinearLayout pinsContainer = (LinearLayout) view
                        .findViewById(R.id.requiredPinsContainer);
                pinsContainer.removeAllViews();

            }
        });
    }

    public static interface OnPinSelectionListener {
        public void onSelect(ArduinoPin pin);

        public void onUnSelect(ArduinoPin pin);
    }

    public static interface onGetPinsView {
        public void onPinsDrawn();
    }
}
