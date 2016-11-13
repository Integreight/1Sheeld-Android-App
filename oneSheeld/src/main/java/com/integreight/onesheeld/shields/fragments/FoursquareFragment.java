package com.integreight.onesheeld.shields.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.FoursquareShield;
import com.integreight.onesheeld.shields.controller.FoursquareShield.FoursquareEventHandler;
import com.integreight.onesheeld.shields.controller.utils.FoursquareUtils;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;

public class FoursquareFragment extends
        ShieldFragmentParent<FoursquareFragment> {

    TextView userName;
    TextView lastCheckin;
    Button login, logout;
    private static SharedPreferences mSharedPreferences;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.foursquare_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        mSharedPreferences = getApplication().getSharedPreferences(
                "com.integreight.onesheeld", Context.MODE_PRIVATE);
        login = (Button) v.findViewById(R.id.foursquare_shiled_login_bt);
        logout = (Button) v.findViewById(R.id.foursquare_shiled_logout_bt);
        userName = (TextView) v
                .findViewById(R.id.foursquare_shield_username_textview);
        lastCheckin = (TextView) v
                .findViewById(R.id.foursquare_shield_last_checkin_textview);

        Log.d("Foursquare Sheeld::OnActivityCreated()", "");
        // if user logged in set data
        initializeFirmata();
        if (((FoursquareShield) getApplication().getRunningShields().get(
                getControllerTag())).isFoursquareLoggedInAlready()) {
            userName.setVisibility(View.VISIBLE);
            lastCheckin.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
            login.setVisibility(View.INVISIBLE);

            String name = mSharedPreferences.getString(
                    "PREF_FourSquare_UserName", "");
            String lastcheck = mSharedPreferences.getString(
                    "PREF_FourSquare_LastPlace", "");

            userName.setText(name);
            lastCheckin.setText(lastcheck);
        } else {
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
        }
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // start login to foursquare
                try{
                    if (ConnectionDetector.isConnectingToInternet(activity))
                        ((FoursquareShield) getApplication().getRunningShields()
                                .get(getControllerTag())).loginToFoursquare();
                    else
                        Toast.makeText(
                                getApplication().getApplicationContext(),
                                R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
                                Toast.LENGTH_SHORT).show();
                }catch (WindowManager.BadTokenException ignored){
                    CrashlyticsUtils.logException(ignored);
                }
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                removeFoursquareData();
                login.setVisibility(View.VISIBLE);
                logout.setVisibility(View.INVISIBLE);
            }
        });
    }

    FoursquareEventHandler foursquareEventHandler = new FoursquareEventHandler() {

        @Override
        public void onPlaceCheckin(final String placeName) {
            // TODO Auto-generated method stub
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI())
                        lastCheckin.setText(placeName);
                }
            });
        }

        @Override
        public void onForsquareLoggedIn(final String user) {
            // TODO Auto-generated method stub
            userName.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        login.setVisibility(View.INVISIBLE);
                        logout.setVisibility(View.VISIBLE);
                        userName.setVisibility(View.VISIBLE);
                        userName.setText(user);
                    }
                }
            });
        }

        @Override
        public void onForsquareLogout() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onForsquareError() {
            // TODO Auto-generated method stub

        }

        @Override
        public void setLastPlaceCheckin(final String placeName) {
            lastCheckin.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        lastCheckin.setVisibility(View.VISIBLE);
                        lastCheckin.setText(placeName);
                    }
                }
            });
        }

    };

    @Override
    public void doOnStart() {
        ((FoursquareShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setFoursquareEventHandler(foursquareEventHandler);
    }

    private void initializeFirmata() {

        if (getApplication().getRunningShields().get(getControllerTag()) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new FoursquareShield(activity, getControllerTag()));
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void removeFoursquareData() {
        Editor e = mSharedPreferences.edit();
        e.remove("PREF_FourSquare_UserName");
        e.remove("PREF_FourSquare_LastPlace");
        e.remove("PREF_KEY_FOURSQUARE_LOGIN");
        e.remove("PREF_FourSquare_OAUTH_TOKEN");
        e.commit();
        FoursquareUtils.clearCookies(getApplication());
        login.setVisibility(View.VISIBLE);
        logout.setVisibility(View.INVISIBLE);
        userName.setVisibility(View.INVISIBLE);
        lastCheckin.setVisibility(View.INVISIBLE);
    }

    @Override
    public void doOnResume() {
        // if user logged in set data
        if (((FoursquareShield) getApplication().getRunningShields().get(
                getControllerTag())).isFoursquareLoggedInAlready()) {
            userName.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
            login.setVisibility(View.INVISIBLE);

            final String name = mSharedPreferences.getString(
                    "PREF_FourSquare_UserName", "");
            final String lastcheck = mSharedPreferences.getString(
                    "PREF_FourSquare_LastPlace", "");

            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        userName.setText(name);
                        lastCheckin.setText(lastcheck);
                    }
                });
            }

        } else {
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
        }
    }
}
