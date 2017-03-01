package com.integreight.onesheeld.appFragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.popup.ArduinoConnectivityPopup;
import com.integreight.onesheeld.sdk.OneSheeldSdk;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.shields.controller.FaceDetectionShield;
import com.integreight.onesheeld.utils.ConnectingPinsView;
import com.integreight.onesheeld.utils.customviews.MultiDirectionSlidingDrawer;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.concurrent.CopyOnWriteArrayList;

public class ShieldsOperations extends Fragment {
    private View v;
    private static ShieldsOperations thisInstance;
    protected SelectedShieldsListFragment mFrag;
    private ShieldFragmentParent<?> mContent;
    private MainActivity activity;
    private CopyOnWriteArrayList<OnChangeListener> onChangeSlidingLockListeners = new CopyOnWriteArrayList<>();

    public static ShieldsOperations getInstance() {
        if (thisInstance == null) {
            thisInstance = new ShieldsOperations();
        }
        return thisInstance;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", mFrag.currentShield);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        mFrag.currentShield = savedInstanceState == null || savedInstanceState.get("position") == null ? 0
                : savedInstanceState.getInt("position");
        this.activity = (MainActivity) getActivity();
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_shields_operation, container,
                false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = (MainActivity) getActivity();
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(savedInstanceState);
//        else new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                initView(savedInstanceState);
//            }
//        },1000);
    }

    MultiDirectionSlidingDrawer pinsSlidingView;
    MultiDirectionSlidingDrawer settingsSlidingView;

    private void initView(Bundle savedInstanceState) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.pinsViewContainer,
                        ConnectingPinsView.getInstance()).commit();
        activity.enableMenu();
        ((CheckBox) getView().findViewById(R.id.isMenuOpening))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton arg0,
                                                 boolean arg1) {
                        if (arg1) {
                            activity.disableMenu();
                        } else
                            activity.enableMenu();
                        if (onChangeSlidingLockListeners != null && onChangeSlidingLockListeners.size() > 0) {
                            for (OnChangeListener onChangeListener : onChangeSlidingLockListeners) {
                                onChangeListener.onChange(arg1);
                            }
                        }
                    }
                });
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (activity != null && getView() != null && getView().findViewById(R.id.isMenuOpening) != null) {
                    activity.openMenu();
                    ((CheckBox) getView().findViewById(R.id.isMenuOpening))
                            .setChecked(false);
                }
            }
        }, 500);

        if (savedInstanceState == null) {
            FragmentTransaction t = activity.getSupportFragmentManager()
                    .beginTransaction();
            mFrag = SelectedShieldsListFragment.newInstance(activity);
            t.replace(R.id.selectedShieldsContainer, mFrag, "menuShieldsList");
            t.commit();
        } else {
            mFrag = (SelectedShieldsListFragment) activity
                    .getSupportFragmentManager().findFragmentByTag("menuShieldsList");
            SelectedShieldsListFragment.renewUiShieldAdapter(activity);
        }
        if (mContent == null) {
            mContent = mFrag.getShieldFragment(savedInstanceState == null || savedInstanceState.get("position") == null ?
                    (mFrag != null ? mFrag.currentShield : 0) : savedInstanceState.getInt("position"));
            try {
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        TextView shieldName = (OneSheeldTextView) activity
                                .findViewById(R.id.shieldName);
                        if (shieldName != null && mContent != null && mContent.shieldName != null) {
                            shieldName
                                    .setVisibility(mContent.shieldName
                                            .equalsIgnoreCase(UIShield.SEVENSEGMENT_SHIELD
                                                    .getName()) ? View.GONE
                                            : View.VISIBLE);
                            shieldName
                                    .setText(mContent.shieldName);
                        }
                    }
                });
            } catch (Exception e) {
            }
//            activity.setTitle(mFrag.getUIShield(0).name + " " + activity.getString(R.string.shield));
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.shieldsContainerFrame, mContent,
                            mContent.getControllerTag()).commit();
        }
        pinsSlidingView = (MultiDirectionSlidingDrawer) getView().findViewById(
                R.id.pinsViewSlidingView);
        settingsSlidingView = (MultiDirectionSlidingDrawer) getView()
                .findViewById(R.id.settingsSlidingView);
        getView().findViewById(R.id.pinsFixedHandler).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        pinsSlidingView.animateOpen();
                    }
                });
        pinsSlidingView
                .setOnDrawerOpenListener(new MultiDirectionSlidingDrawer.OnDrawerOpenListener() {

                    @Override
                    public void onDrawerOpened() {
                        if (getView() != null && settingsSlidingView != null)
                            if (settingsSlidingView.isOpened())
                                settingsSlidingView.animateOpen();
                        activity.disableMenu();
                    }
                });
        pinsSlidingView
                .setOnDrawerCloseListener(new MultiDirectionSlidingDrawer.OnDrawerCloseListener() {

                    @Override
                    public void onDrawerClosed() {
                        if (getView() != null && settingsSlidingView != null && getView().findViewById(R.id.isMenuOpening) != null)
                            if (!settingsSlidingView.isOpened()
                                    && !((CheckBox) getView().findViewById(
                                    R.id.isMenuOpening)).isChecked())
                                activity.enableMenu();
                    }
                });
        pinsSlidingView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return pinsSlidingView.isOpened();
            }
        });
        getView().findViewById(R.id.settingsFixedHandler).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (getView() != null && settingsSlidingView != null)
                            settingsSlidingView.animateOpen();
                    }
                });
        settingsSlidingView
                .setOnDrawerOpenListener(new MultiDirectionSlidingDrawer.OnDrawerOpenListener() {

                    @Override
                    public void onDrawerOpened() {
                        if (getView() != null && pinsSlidingView != null && pinsSlidingView.isOpened()) {
                            pinsSlidingView.animateOpen();
                        }
                        activity.disableMenu();
                    }
                });
        settingsSlidingView
                .setOnDrawerCloseListener(new MultiDirectionSlidingDrawer.OnDrawerCloseListener() {

                    @Override
                    public void onDrawerClosed() {
                        if (getView() != null && pinsSlidingView != null && getView().findViewById(
                                R.id.isMenuOpening) != null)
                            if (!pinsSlidingView.isOpened()
                                    && !((CheckBox) getView().findViewById(
                                    R.id.isMenuOpening)).isChecked())
                                activity.enableMenu();
                    }
                });
        settingsSlidingView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return settingsSlidingView.isOpened();
            }
        });
        ((ToggleButton) getView().findViewById(R.id.shieldStatus))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (MainActivity.currentShieldTag != null)
                            ((OneSheeldApplication) activity.getApplication())
                                    .getRunningShields().get(
                                    MainActivity.currentShieldTag).isInteractive = isChecked;
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.shields_operation, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mContent = null;
        super.onDestroy();
    }

    private OneSheeldApplication getApplication() {
        return (OneSheeldApplication) activity.getApplication();
    }

    @Override
    public void onResume() {
        activity.getOnConnectionLostHandler().canInvokeOnCloseConnection = false;
        if (!getApplication().isConnectedToBluetooth()) {
            activity.getOnConnectionLostHandler().connectionLost = true;
        }
        activity.getOnConnectionLostHandler().sendEmptyMessage(0);
        activity.closeMenu();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (activity != null
                        && activity.findViewById(R.id.getAvailableDevices) != null)
                    activity.findViewById(R.id.getAvailableDevices)
                            .setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
//                                    if (!((OneSheeldApplication) activity.getApplication()).getIsDemoMode()) {
                                    if (activity.getThisApplication().getRunningShields().get(UIShield.CAMERA_SHIELD.name()) != null)
                                        try {
                                            ((CameraShield) activity.getThisApplication().getRunningShields().get(UIShield.CAMERA_SHIELD.name())).hidePreview();
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    if (activity.getThisApplication().getRunningShields().get(UIShield.COLOR_DETECTION_SHIELD.name()) != null)
                                        try {
                                            ((ColorDetectionShield) activity.getThisApplication().getRunningShields().get(UIShield.COLOR_DETECTION_SHIELD.name())).hidePreview();
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    if (activity.getThisApplication().getRunningShields().get(UIShield.FACE_DETECTION.name()) != null)
                                        try {
                                            ((FaceDetectionShield) activity.getThisApplication().getRunningShields().get(UIShield.FACE_DETECTION.name())).hidePreview();
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    activity.closeMenu();
                                    if (activity.getSupportFragmentManager()
                                            .getBackStackEntryCount() > 1) {
                                        activity.getSupportFragmentManager()
                                                .popBackStack();
                                        activity.getSupportFragmentManager()
                                                .executePendingTransactions();
                                    }
                                    OneSheeldSdk.getManager().disconnectAll();
                                    if (!ArduinoConnectivityPopup.isOpened) {
                                        ArduinoConnectivityPopup.isOpened = true;
                                        new ArduinoConnectivityPopup(activity)
                                                .show();
                                    }
                                }
                            });
            }
        }, 500);
        if (((OneSheeldApplication) activity.getApplication()).getIsDemoMode()
                && !getApplication().isConnectedToBluetooth())
            ((ViewGroup) activity.findViewById(R.id.getAvailableDevices)).getChildAt(1).setBackgroundResource(R.drawable.scan_button);
        else
            ((ViewGroup) activity.findViewById(R.id.getAvailableDevices)).getChildAt(1).setBackgroundResource(R.drawable.bluetooth_disconnect_button);
        ((ViewGroup) activity.findViewById(R.id.cancelConnection))
                .getChildAt(1).setBackgroundResource(R.drawable.back_button);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                activity.findViewById(R.id.cancelConnection)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                boolean isMenuOpened = (activity.appSlidingMenu != null && activity.appSlidingMenu
                                        .isOpen())
                                        || (settingsSlidingView != null && settingsSlidingView
                                        .isOpened())
                                        || (pinsSlidingView != null && pinsSlidingView
                                        .isOpened());
                                activity.onBackPressed();
                                if (!isMenuOpened)
                                    activity.findViewById(R.id.cancelConnection)
                                            .setOnClickListener(
                                                    new View.OnClickListener() {

                                                        @Override
                                                        public void onClick(
                                                                View v) {
                                                            // TODO
                                                            // Auto-generated
                                                            // method stub

                                                        }
                                                    });
                            }
                        });
            }
        }, 500);
        super.onResume();
    }

    public void addOnSlidingLocksListener(OnChangeListener listener) {
        if (onChangeSlidingLockListeners == null)
            onChangeSlidingLockListeners = new CopyOnWriteArrayList<>();
        if (!onChangeSlidingLockListeners.contains(listener))
            onChangeSlidingLockListeners.add(listener);
    }

    public static interface OnChangeListener {
        public void onChange(boolean isChecked);
    }
}
