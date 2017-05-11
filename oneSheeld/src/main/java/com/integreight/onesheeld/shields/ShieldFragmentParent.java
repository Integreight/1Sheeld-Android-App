package com.integreight.onesheeld.shields;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.HitBuilders;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.Shield;
import com.integreight.onesheeld.utils.AppShields;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;

/**
 * @param <T> Child frag class is the super class for all shields
 * @author Ahmed Saad
 */
@SuppressWarnings("unchecked")
public abstract class ShieldFragmentParent<T extends ShieldFragmentParent<?>>
        extends Fragment {
    private String controllerTag = "";// unique key for the shield
    public boolean hasSettings = false;// a flag to check if the shield has
    // Settings sliding drawer or not like
    // music player shield
    public Handler uiHandler = new Handler(); // handler to do UI changes
    public String shieldName = ""; // to be setten on the top of the Shield
    // screen
    public MainActivity activity; // MainActivity Instance to be used in all
    // shields

    public ShieldFragmentParent() {
        controllerTag = AppShields.getInstance().getShieldTag(((T) (this)).getClass().getName());
    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = (MainActivity) activity;
        super.onAttach(activity);
        ((T) this).doOnAttach();
    }

    @Override
    public void onDetach() {
        // activity = null;
        ((T) this).doOnDetach();
        super.onDetach();
    }

    public MainActivity getAppActivity() {
        return (MainActivity) getActivity();
    }

    public OneSheeldApplication getApplication() {
        return activity.getThisApplication();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        activity = getAppActivity();
        controllerTag = AppShields.getInstance().getShieldTag(((T) (this)).getClass().getName());
        super.onActivityCreated(savedInstanceState);
        ((T) this).doOnActivityCreated(savedInstanceState);
    }

    public boolean reInitController() {
        ControllerParent<?> type = null;
        Shield shield = AppShields.getInstance().getShield(getControllerTag());
        if (shield != null) {
            try {
                type = shield.shieldType.newInstance();
            } catch (java.lang.InstantiationException e) {
                // TODO Auto-generated catch block
                Log.e("TAG", "Exception", e);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                Log.e("TAG", "Exception", e);
            }
            if (type != null) {
                type.setActivity(getAppActivity()).init(shield.tag);
                getApplication().getRunningShields().get(getControllerTag())
                        .setHasForgroundView(true);
                return true;
            }
        } else {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(this).commit();
            onDestroy();
            return false;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if (getApplication().getAppFirmata().isOpen() == false && !getApplication().getIsDemoMode()) return;
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        ((T) this).doOnViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d("S", "start");
        super.onStart();
        activity = getAppActivity();
        uiHandler = new Handler();
        /*
         * If the Shield lost it's controller instance within the application,
		 * then starts to re-init it
		 */

            if (getApplication().getRunningShields().get(getControllerTag()) != null && activity != null && activity.findViewById(R.id.settingsFixedHandler) != null)
                getApplication().getRunningShields().get(getControllerTag())
                        .setHasForgroundView(true);
            else {
//            if (!reInitController())
                return;
            }
        // View or hide Setting sliding drawer handler button
        activity.findViewById(R.id.settingsFixedHandler).setVisibility(
                hasSettings ? View.VISIBLE : View.GONE);
        // View or hide Pins sliding drawer handler button
        activity.findViewById(R.id.pinsFixedHandler)
                .setVisibility(
                        getApplication().getRunningShields().get(
                                getControllerTag()) == null
                                || getApplication().getRunningShields().get(
                                getControllerTag()).requiredPinsIndex == -1 ? View.GONE
                                : View.VISIBLE);
        ((T) this).doOnStart();
    }

    @Override
    public void onStop() {
        // stop shield controller form affecting the UI
        ((T) this).doOnStop();
        if (getApplication().getRunningShields().get(getControllerTag()) != null)
            getApplication().getRunningShields().get(getControllerTag())
                    .setHasForgroundView(false);
        uiHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    public void doOnServiceConnected() {

    }

    /**
     * @return the Shield notNull controller tag
     * @desc we have 4 backups of the shield tag: (Local variable here,
     * hashtable saved on the application subclass depends on shield
     * fragment name, Shield fragment tag and a fragment tag), So we
     * iterate and ignore null backups and reset them all
     */
    public String getControllerTag() {
        if (controllerTag == null || controllerTag.trim().length() == 0) {
            String tagFromApp = AppShields.getInstance().getShieldTag(((T) (this)).getClass().getName());
            controllerTag = (tagFromApp != null ? tagFromApp
                    : controllerTag != null ? controllerTag
                    : (getArguments() != null && getArguments().getString(
                    "tag") != null) ? getArguments().getString(
                    "tag") : getTag());
            if (controllerTag == null)
                CrashlyticsUtils
                        .log("ControllerTag = null" + ((T) (this)) != null ? ((T) (this))
                                .getClass().getName() : "");
            Log.test("TAG", controllerTag + "  Tag from app:  " + tagFromApp
                    + "  Frag Tag:  " + getTag() + "  Arg:  "
                    + getArguments().getString("tag"));
        }
        getArguments().putString("tag", controllerTag);
        return controllerTag;
    }

    public void setControllerTag(String controllerTag) {
        this.controllerTag = controllerTag;
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (getApplication().getAppFirmata().isOpen() == false && !getApplication().getIsDemoMode()) return;
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        if (activity == null || activity.findViewById(R.id.shieldStatus) == null)
            return;
        MainActivity.currentShieldTag = getControllerTag();
        // restore the staus of shield interaction toggle button
        if (getApplication().getRunningShields().get(getControllerTag()) != null)
            ((ToggleButton) activity.findViewById(R.id.shieldStatus))
                    .setChecked(getApplication().getRunningShields().get(
                            getControllerTag()).isInteractive);
        // Google analytics tracking
        getApplication().getTracker().setScreenName(getControllerTag());
        getApplication().getTracker().send(
                new HitBuilders.ScreenViewBuilder().build());
        // Logging current view for crashlytics
        CrashlyticsUtils.setString("Current View", getTag());
        ((T) this).doOnResume();
    }

    /**
     * @return a flag to check if the fragment read to hold UI changes
     */
    public boolean canChangeUI() {
        if (uiHandler == null)
            uiHandler = new Handler();
        return (getActivity() != null
                && getControllerTag() != null
                && getApplication().getRunningShields().get(getControllerTag()) != null && getApplication()
                .getRunningShields().get(getControllerTag())
                .isHasForgroundView() && getView() != null);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        activity = getAppActivity();
    }

    @Override
    public void onDestroy() {
        ((T) this).doOnDestroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((T) this).doOnPause();
    }
    public void doOnStart() {
    }

    public void doOnDestroy() {
    }

    public void doOnResume() {
    }

    public void doOnStop() {
    }

    public void doOnAttach() {
    }

    public void doOnDetach() {
    }

    public void doOnActivityCreated(Bundle savedInstanceStat) {
    }

    public void doOnViewCreated(View view, Bundle savedInstanceStat) {
    }
    public void doOnPause() {}
}
