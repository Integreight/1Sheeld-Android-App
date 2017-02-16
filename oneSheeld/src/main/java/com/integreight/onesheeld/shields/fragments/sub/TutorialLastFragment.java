package com.integreight.onesheeld.shields.fragments.sub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.HitBuilders;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.sdk.OneSheeldSdk;

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
                        ((OneSheeldApplication)getActivity().getApplication())
                                .getTracker()
                                .send(new HitBuilders.EventBuilder()
                                        .setCategory("Last Tutorial Screen")
                                        .setAction("Have Board")
                                        .build());
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
                if (app.isConnectedToBluetooth())
                    OneSheeldSdk.getManager().disconnect(app.getConnectedDevice());
                getActivity().finish();
                ((OneSheeldApplication)getActivity().getApplication())
                        .getTracker()
                        .send(new HitBuilders.EventBuilder()
                                .setCategory("Last Tutorial Screen")
                                .setAction("Don't Have Board")
                                .build());
                Toast.makeText(getActivity(), R.string.tutorial_the_app_requires_1sheeld_board,
                        Toast.LENGTH_LONG).show();
                String url = app.isLocatedInTheUs()?"http://bit.ly/Buy1SheeldPlusAmazonFromApp":"http://bit.ly/Buy1SheeldFromApp";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
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
