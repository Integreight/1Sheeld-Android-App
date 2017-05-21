package com.integreight.onesheeld.appFragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.SelectedShieldsListAdapter;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.Shield;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.fragments.AccelerometerFragment;
import com.integreight.onesheeld.shields.fragments.BuzzerFragment;
import com.integreight.onesheeld.shields.fragments.CameraFragment;
import com.integreight.onesheeld.shields.fragments.ClockFragment;
import com.integreight.onesheeld.shields.fragments.ColorDetectionFragment;
import com.integreight.onesheeld.shields.fragments.DataLoggerFragment;
import com.integreight.onesheeld.shields.fragments.EmailFragment;
import com.integreight.onesheeld.shields.fragments.EmptyShieldFragment;
import com.integreight.onesheeld.shields.fragments.FaceDetectionFragment;
import com.integreight.onesheeld.shields.fragments.FacebookFragment;
import com.integreight.onesheeld.shields.fragments.FoursquareFragment;
import com.integreight.onesheeld.shields.fragments.GamepadFragment;
import com.integreight.onesheeld.shields.fragments.GlcdFragment;
import com.integreight.onesheeld.shields.fragments.GpsFragment;
import com.integreight.onesheeld.shields.fragments.GravityFragment;
import com.integreight.onesheeld.shields.fragments.GyroscopeFragment;
import com.integreight.onesheeld.shields.fragments.InternetFragment;
import com.integreight.onesheeld.shields.fragments.KeyboardFragment;
import com.integreight.onesheeld.shields.fragments.KeypadFragment;
import com.integreight.onesheeld.shields.fragments.LcdFragment;
import com.integreight.onesheeld.shields.fragments.LedFragment;
import com.integreight.onesheeld.shields.fragments.LightFragment;
import com.integreight.onesheeld.shields.fragments.MagnetometerFragment;
import com.integreight.onesheeld.shields.fragments.MicFragment;
import com.integreight.onesheeld.shields.fragments.MusicPlayerFragment;
import com.integreight.onesheeld.shields.fragments.NfcFragment;
import com.integreight.onesheeld.shields.fragments.NotificationFragment;
import com.integreight.onesheeld.shields.fragments.OrientationFragment;
import com.integreight.onesheeld.shields.fragments.PatternFragment;
import com.integreight.onesheeld.shields.fragments.PhoneFragment;
import com.integreight.onesheeld.shields.fragments.PressureFragment;
import com.integreight.onesheeld.shields.fragments.ProximityFragment;
import com.integreight.onesheeld.shields.fragments.PushButtonFragment;
import com.integreight.onesheeld.shields.fragments.SevenSegmentFragment;
import com.integreight.onesheeld.shields.fragments.SkypeFragment;
import com.integreight.onesheeld.shields.fragments.SliderFragment;
import com.integreight.onesheeld.shields.fragments.SmsFragment;
import com.integreight.onesheeld.shields.fragments.SpeechRecognitionFragment;
import com.integreight.onesheeld.shields.fragments.TemperatureFragment;
import com.integreight.onesheeld.shields.fragments.TerminalFragment;
import com.integreight.onesheeld.shields.fragments.TextToSpeechFragment;
import com.integreight.onesheeld.shields.fragments.ToggleButtonFragment;
import com.integreight.onesheeld.shields.fragments.TwitterFragment;
import com.integreight.onesheeld.shields.fragments.VibrationFragment;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.customviews.AppSlidingLeftMenu;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.HashMap;
import java.util.Map;

public class SelectedShieldsListFragment extends ListFragment {
    private static SelectedShieldsListAdapter UIShieldAdapter;
    Map<String, ShieldFragmentParent<?>> creadtedShields = new HashMap<>();
    private MainActivity activity;
    public int currentShield = 0;

    public static SelectedShieldsListFragment newInstance(Activity activity) {
        UIShieldAdapter = new SelectedShieldsListAdapter(activity);
        return new SelectedShieldsListFragment();
    }

    public static void renewUiShieldAdapter(Activity activity) {
        if (UIShieldAdapter != null)
            UIShieldAdapter.setActivity(activity);
        else {
            UIShieldAdapter = new SelectedShieldsListAdapter(activity);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        activity = (MainActivity) getActivity();
        if (UIShieldAdapter != null)
            UIShieldAdapter.setActivity(activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            this.activity = (MainActivity) context;
            renewUiShieldAdapter(activity);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.selected_shields_list, null);
    }

    public ShieldFragmentParent<?> getShieldFragment(int position) {
        Shield uiShield = UIShieldAdapter.getItem(position);
        if (creadtedShields.containsKey(uiShield.tag))
            return creadtedShields.get(uiShield.tag);
        else {
            try {
                if (uiShield.shieldFragment != null)
                    return addToCreatedListAndReturn(uiShield, uiShield.shieldFragment.newInstance());
                else {
                    if (activity != null)
                        activity.getThisApplication()
                                .getTracker()
                                .send(new HitBuilders.EventBuilder()
                                        .setCategory("Extreme Cases")
                                        .setAction(
                                                "Initialize fragments without reflection")
                                        .build());
                    return generateShieldFragment(uiShield);
                }
            } catch (java.lang.InstantiationException e) {
                CrashlyticsUtils.logException(e);
                return generateShieldFragment(uiShield);
            } catch (IllegalAccessException e) {
                CrashlyticsUtils.logException(e);
                return generateShieldFragment(uiShield);
            }
        }
    }

    private ShieldFragmentParent<?> generateShieldFragment(Shield uiShield) {
        if (uiShield.id == UIShield.VIBRATION_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new VibrationFragment());
        if (uiShield.id == UIShield.LED_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new LedFragment());
        if (uiShield.id == UIShield.ACCELEROMETER_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new AccelerometerFragment());
        if (uiShield.id == UIShield.FACEBOOK_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new FacebookFragment());
        if (uiShield.id == UIShield.KEYPAD_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new KeypadFragment());
        if (uiShield.id == UIShield.LCD_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new LcdFragment());
        if (uiShield.id == UIShield.MAGNETOMETER_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new MagnetometerFragment());
        if (uiShield.id == UIShield.PUSHBUTTON_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new PushButtonFragment());
        if (uiShield.id == UIShield.SEVENSEGMENT_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new SevenSegmentFragment());
        if (uiShield.id == UIShield.SLIDER_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new SliderFragment());
        if (uiShield.id == UIShield.BUZZER_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new BuzzerFragment());
        if (uiShield.id == UIShield.TOGGLEBUTTON_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new ToggleButtonFragment());
        if (uiShield.id == UIShield.TWITTER_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new TwitterFragment());
        if (uiShield.id == UIShield.NOTIFICATION_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new NotificationFragment());
        if (uiShield.id == UIShield.GAMEDPAD_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new GamepadFragment());
        if (uiShield.id == UIShield.FOURSQUARE_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new FoursquareFragment());
        if (uiShield.id == UIShield.GPS_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new GpsFragment());
        if (uiShield.id == UIShield.SMS_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new SmsFragment());
        if (uiShield.id == UIShield.MUSICPLAYER_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new MusicPlayerFragment());
        if (uiShield.id == UIShield.GYROSCOPE_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new GyroscopeFragment());
        if (uiShield.id == UIShield.MIC_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new MicFragment());
        if (uiShield.id == UIShield.SKYPE_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new SkypeFragment());
        if (uiShield.id == UIShield.PROXIMITY_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new ProximityFragment());
        if (uiShield.id == UIShield.GRAVITY_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new GravityFragment());
        if (uiShield.id == UIShield.ORIENTATION_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new OrientationFragment());
        if (uiShield.id == UIShield.LIGHT_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new LightFragment());
        if (uiShield.id == UIShield.PRESSURE_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new PressureFragment());
        if (uiShield.id == UIShield.TEMPERATURE_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new TemperatureFragment());
        if (uiShield.id == UIShield.CAMERA_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new CameraFragment());
        if (uiShield.id == UIShield.PHONE_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new PhoneFragment());
        if (uiShield.id == UIShield.EMAIL_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new EmailFragment());
        if (uiShield.id == UIShield.CLOCK_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new ClockFragment());
        if (uiShield.id == UIShield.KEYBOARD_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new KeyboardFragment());
        if (uiShield.id == UIShield.SPEECH_RECOGNIZER_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new SpeechRecognitionFragment());
        if (uiShield.id == UIShield.TEXT_TO_SPEECH_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield,
                    new TextToSpeechFragment());
        if (uiShield.id == UIShield.DATA_LOGGER.getId())
            return addToCreatedListAndReturn(uiShield, new DataLoggerFragment());
        if (uiShield.id == UIShield.TERMINAL_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new TerminalFragment());
        if (uiShield.id == UIShield.PATTERN_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new PatternFragment());
        if (uiShield.id == UIShield.INTERNET_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new InternetFragment());
        if (uiShield.id == UIShield.NFC_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new NfcFragment());
        if (uiShield.id == UIShield.GLCD_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new GlcdFragment());
        if (uiShield.id == UIShield.COLOR_DETECTION_SHIELD.getId())
            return addToCreatedListAndReturn(uiShield, new ColorDetectionFragment());
        if (uiShield.id == UIShield.FACE_DETECTION.getId())
            return addToCreatedListAndReturn(uiShield, new FaceDetectionFragment());
        else {
            return new EmptyShieldFragment();
        }
    }

    private ShieldFragmentParent<?> addToCreatedListAndReturn(Shield uiShield,
                                                              ShieldFragmentParent<?> fragment) {
        fragment.setControllerTag(uiShield.tag);
        Bundle b = new Bundle();
        b.putString("tag", uiShield.tag);
        fragment.setArguments(b);
        fragment.shieldName = uiShield.name;
        creadtedShields.put(uiShield.tag, fragment);
        return fragment;
    }

    public Shield getUIShield(int position) {
        return UIShieldAdapter.getItem(position);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = (MainActivity) getActivity();
        if (UIShieldAdapter != null)
            UIShieldAdapter.setActivity(activity);
        setListAdapter(UIShieldAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        getListView().setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !((AppSlidingLeftMenu) activity
                        .findViewById(R.id.sliding_pane_layout)).isOpen();
            }
        });
        setRetainInstance(true);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        ShieldFragmentParent<?> newContent = getShieldFragment(position);
        if (newContent != null) {
            currentShield = position;
            switchFragment(newContent, UIShieldAdapter.getItem(position));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", currentShield);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        currentShield = savedInstanceState == null || savedInstanceState.get("position") == null ? 0
                : savedInstanceState.getInt("position");
        this.activity = (MainActivity) getActivity();
        if (UIShieldAdapter != null)
            UIShieldAdapter.setActivity(activity);
        super.onViewStateRestored(savedInstanceState);
    }

    TextView shieldName;

    private void switchFragment(final ShieldFragmentParent<?> fragment,
                                final Shield uiShield) {
        getListView().post(new Runnable() {
            @Override
            public void run() {
                activity.replaceCurrentFragment(R.id.shieldsContainerFrame,
                        fragment, uiShield.tag, false, false);
                try {
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            shieldName = (OneSheeldTextView) activity
                                    .findViewById(R.id.shieldName);
                            shieldName.setVisibility(fragment.shieldName
                                    .equalsIgnoreCase(UIShield.SEVENSEGMENT_SHIELD
                                            .getName()) ? View.GONE
                                    : View.VISIBLE);
                            shieldName.setText(fragment.shieldName);
                        }
                    });
                } catch (Exception e) {
                    CrashlyticsUtils.logException(e);
                }
                activity.closeMenu();
            }
        });
    }
}
