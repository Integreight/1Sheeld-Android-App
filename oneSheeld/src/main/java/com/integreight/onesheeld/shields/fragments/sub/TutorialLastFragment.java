package com.integreight.onesheeld.shields.fragments.sub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;

public class TutorialLastFragment extends Fragment {
    View v;

    public static TutorialLastFragment newInstance(int indx) {
        TutorialLastFragment fragment = new TutorialLastFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final OneSheeldApplication app = (OneSheeldApplication) getActivity()
                .getApplication();
        final ToggleButton cb = (ToggleButton) v.findViewById(R.id.showAgain);
        cb.setChecked(true);
        v.findViewById(R.id.goBtn).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        app.setShownTutAgain(cb.isChecked());
                        if (!cb.isChecked())
                            app.setTutShownTimes(0);
                        getActivity().finish();
                    }
                });
        v.findViewById(R.id.exploreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.setShownTutAgain(cb.isChecked());
                if (!cb.isChecked())
                    app.setTutShownTimes(0);
                app.setIsDemoMode(true);
                if (ArduinoConnectivityPopup.isOpened) {
                    ArduinoConnectivityPopup.isOpened = false;
                    ArduinoConnectivityPopup.thisInstance.cancel();
                }
                if (app.getAppFirmata().isOpen())
                    app.getAppFirmata().close();
                getActivity().finish();
            }
        });
        getView().findViewById(R.id.check).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        cb.setChecked(!cb.isChecked());
                    }
                });
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.tut_last_frag, container, false);
        return v;
    }
}
