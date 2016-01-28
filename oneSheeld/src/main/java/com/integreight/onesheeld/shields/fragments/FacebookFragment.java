package com.integreight.onesheeld.shields.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.FacebookShield;
import com.integreight.onesheeld.shields.controller.FacebookShield.FacebookEventHandler;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class FacebookFragment extends ShieldFragmentParent<FacebookFragment> implements View.OnClickListener {

    LinearLayout lastPostTextCont;
    TextView userNameTextView;
    Button facebookLogin;
    Button facebookLogout;
    Bundle savedInstanceState;
    ProgressBar progress;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        this.savedInstanceState = savedInstanceState;
        return inflater.inflate(R.layout.facebook_shield_fragment_layout,
                container, false);

    }

    @Override
    public void doOnStart() {
        initializeFirmata();
        checkLogin();
        facebookLogin.setOnClickListener(this);
        facebookLogout.setOnClickListener(this);
    }

    @Override
    public void doOnResume() {
        facebookLogin.setOnClickListener(this);
        facebookLogout.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (((FacebookShield) getApplication().getRunningShields().get(
                getControllerTag())).getCallbackManager() != null)
            ((FacebookShield) getApplication().getRunningShields().get(
                    getControllerTag())).getCallbackManager().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        lastPostTextCont = (LinearLayout) v.findViewById(R.id.postsCont);
        userNameTextView = (TextView) v
                .findViewById(R.id.facebook_shield_username_textview);
        facebookLogin = (Button) v.findViewById(R.id.login);
        facebookLogout = (Button) v.findViewById(R.id.logout);
        progress = (ProgressBar) v.findViewById(R.id.progress);
    }

    private FacebookEventHandler facebookEventHandler = new FacebookEventHandler() {

        @Override
        public void onRecievePost(final String post) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        OneSheeldTextView posty = (OneSheeldTextView) activity
                                .getLayoutInflater().inflate(
                                        R.layout.facebook_post_item,
                                        lastPostTextCont, false);
                        posty.setText(post);
                        lastPostTextCont.addView(posty);
                        ((ScrollView) lastPostTextCont.getParent())
                                .invalidate();
                        Toast.makeText(activity, R.string.facebook_posted_on_your_wall_toast,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onFacebookLoggedIn() {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        buttonToLoggedIn();
                    }
                });
            }
        }

        @Override
        public void onFacebookError(final String error) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(activity, error, Toast.LENGTH_SHORT)
                                .show();
                        // buttonToLoggedIn();
                        // getAppActivity()
                        // .setSupportProgressBarIndeterminateVisibility(
                        // false);
                    }
                });
            }
        }

        @Override
        public void startProgress() {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (progress != null && canChangeUI()) {
                        progress.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @Override
        public void stopProgress() {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (progress != null && canChangeUI()) {
                        progress.setVisibility(View.GONE);
                    }
                }
            });
        }

    };

    private void initializeFirmata() {
        if ((getApplication().getRunningShields().get(getControllerTag())) == null)
            getApplication().getRunningShields().put(
                    getControllerTag(),
                    new FacebookShield(activity, getControllerTag(), this,
                            savedInstanceState));
        ((FacebookShield) getApplication().getRunningShields().get(
                getControllerTag())).setShieldFragment(this);
        ((FacebookShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setFacebookEventHandler(facebookEventHandler);
        checkLogin();
    }

    private void checkLogin() {
        if ((getApplication().getRunningShields().get(getControllerTag())) != null
                && ((FacebookShield) getApplication().getRunningShields().get(
                getControllerTag())).isFacebookLoggedInAlready()) {
            buttonToLoggedIn();
        } else {
            buttonToLoggedOut();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        return super.onOptionsItemSelected(item);
    }

    private void logoutFromFacebook() {
        ((FacebookShield) getApplication().getRunningShields().get(
                getControllerTag())).logoutFromFacebook();
        buttonToLoggedOut();
    }

    private void loginToFacebook() {
        if (ConnectionDetector.isConnectingToInternet(activity))

            ((FacebookShield) getApplication().getRunningShields().get(
                    getControllerTag())).loginToFacebook();
        else
            Toast.makeText(getApplication().getApplicationContext(),
                    R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
                    Toast.LENGTH_SHORT).show();
        // getAppActivity().setSupportProgressBarIndeterminateVisibility(true);
    }

    private void buttonToLoggedOut() {
        if (facebookLogout != null)
            facebookLogout.setVisibility(View.INVISIBLE);
        if (facebookLogin != null)
            facebookLogin.setVisibility(View.VISIBLE);
        if (userNameTextView != null)
            userNameTextView.setVisibility(View.INVISIBLE);
        if (lastPostTextCont != null) {
            lastPostTextCont.removeAllViews();
            lastPostTextCont.setVisibility(View.INVISIBLE);
        }
    }

    private void buttonToLoggedIn() {
        if (facebookLogin != null)
            facebookLogin.setVisibility(View.INVISIBLE);
        if (facebookLogout != null)
            facebookLogout.setVisibility(View.VISIBLE);
        if (userNameTextView != null)
            userNameTextView.setVisibility(View.VISIBLE);
        if (lastPostTextCont != null) {
            lastPostTextCont.removeAllViews();
            lastPostTextCont.setVisibility(View.VISIBLE);
        }
        userNameTextView.setText(activity.getString(R.string.facebook_logged_in_as)+": "
                + ((FacebookShield) getApplication().getRunningShields().get(
                getControllerTag())).getUsername());
    }

    @Override
    public void doOnServiceConnected() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:
                loginToFacebook();
                break;
            case R.id.logout:
                logoutFromFacebook();
                break;
            default:
                break;
        }
    }
}
