package com.integreight.onesheeld.enums;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.AccelerometerShield;
import com.integreight.onesheeld.shields.controller.CameraShield;
import com.integreight.onesheeld.shields.controller.ClockShield;
import com.integreight.onesheeld.shields.controller.ColorDetectionShield;
import com.integreight.onesheeld.shields.controller.DataLoggerShield;
import com.integreight.onesheeld.shields.controller.EmailShield;
import com.integreight.onesheeld.shields.controller.EmptyShield;
import com.integreight.onesheeld.shields.controller.FacebookShield;
import com.integreight.onesheeld.shields.controller.FoursquareShield;
import com.integreight.onesheeld.shields.controller.GamepadShield;
import com.integreight.onesheeld.shields.controller.GpsShield;
import com.integreight.onesheeld.shields.controller.GravityShield;
import com.integreight.onesheeld.shields.controller.GyroscopeShield;
import com.integreight.onesheeld.shields.controller.InternetShield;
import com.integreight.onesheeld.shields.controller.KeyboardShield;
import com.integreight.onesheeld.shields.controller.KeypadShield;
import com.integreight.onesheeld.shields.controller.LcdShield;
import com.integreight.onesheeld.shields.controller.LedShield;
import com.integreight.onesheeld.shields.controller.LightShield;
import com.integreight.onesheeld.shields.controller.MagnetometerShield;
import com.integreight.onesheeld.shields.controller.MicShield;
import com.integreight.onesheeld.shields.controller.MusicShield;
import com.integreight.onesheeld.shields.controller.NotificationShield;
import com.integreight.onesheeld.shields.controller.OrientationShield;
import com.integreight.onesheeld.shields.controller.PatternShield;
import com.integreight.onesheeld.shields.controller.PhoneShield;
import com.integreight.onesheeld.shields.controller.PressureShield;
import com.integreight.onesheeld.shields.controller.ProximityShield;
import com.integreight.onesheeld.shields.controller.PushButtonShield;
import com.integreight.onesheeld.shields.controller.RemoteOneSheeldShield;
import com.integreight.onesheeld.shields.controller.SevenSegmentShield;
import com.integreight.onesheeld.shields.controller.SkypeShield;
import com.integreight.onesheeld.shields.controller.SliderShield;
import com.integreight.onesheeld.shields.controller.SmsShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.integreight.onesheeld.shields.controller.SpeechRecognitionShield;
import com.integreight.onesheeld.shields.controller.TaskerShield;
import com.integreight.onesheeld.shields.controller.TemperatureShield;
import com.integreight.onesheeld.shields.controller.TerminalShield;
import com.integreight.onesheeld.shields.controller.TextToSpeechShield;
import com.integreight.onesheeld.shields.controller.ToggleButtonShield;
import com.integreight.onesheeld.shields.controller.TwitterShield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum UIShield {
    LED_SHIELD((byte) 0x02, "LED", 0xff03d203,
            R.drawable.shields_list_led_symbol, false, LedShield.class), NOTIFICATION_SHIELD(
            (byte) 0x06, "Notification", 0xffd4d903,
            R.drawable.shields_list_notifications_symbol, false,
            NotificationShield.class), SEVENSEGMENT_SHIELD((byte) 0x07,
            "Seven Segment", 0xffe28203,
            R.drawable.shields_list_seven_segment_symbol, false,
            SevenSegmentShield.class), BUZZER_SHIELD((byte) 0x08, "Buzzer",
            0xffe93f03, R.drawable.shields_list_buzzer_symbol, false,
            SpeakerShield.class), MIC_SHIELD((byte) 0x18, "Mic", 0xff0362c0,
            R.drawable.shields_list_mic_symbol, false, MicShield.class, 1), KEYPAD_SHIELD(
            (byte) 0x09, "Keypad", 0xff03c0ae,
            R.drawable.shields_list_keypad_symbol, false, KeypadShield.class), SLIDER_SHIELD(
            (byte) 0x01, "Slider", 0xffc0034c,
            R.drawable.shields_list_slider_symbol, false, SliderShield.class), LCD_SHIELD(
            (byte) 0x17, "LCD", 0xff99bd03, R.drawable.shields_list_lcd_symbol,
            false, LcdShield.class, true), MAGNETOMETER_SHIELD((byte) 0x0A,
            "Magnetometer", 0xff40039f,
            R.drawable.shields_list_magnetometer_symbol, false,
            MagnetometerShield.class, 1), PUSHBUTTON_SHIELD((byte) 0x03,
            "Push Button", 0xffb97547,
            R.drawable.shields_list_push_button_symbol, false,
            PushButtonShield.class), TOGGLEBUTTON_SHIELD((byte) 0x04,
            "Toggle Button", 0xffc0039d,
            R.drawable.shields_list_push_button_symbol, false,
            ToggleButtonShield.class), ACCELEROMETER_SHIELD((byte) 0x0B,
            "Accelerometer", 0xff266a5d,
            R.drawable.shields_list_accelerometer_symbol, false,
            AccelerometerShield.class, 1), FACEBOOK_SHIELD((byte) 0x19,
            "Facebook", 0xff039dc0, R.drawable.shields_list_facebook_symbol,
            false, FacebookShield.class), TWITTER_SHIELD((byte) 0x1A,
            "Twitter", 0xffa14c4c, R.drawable.shields_list_twitter_symbol,
            false, TwitterShield.class), GAMEDPAD_SHIELD((byte) 0x0C,
            "Game Pad", 0xff658f08, R.drawable.shields_list_gamepad_symbol,
            false, GamepadShield.class), FOURSQUARE_SHIELD((byte) 0x1B,
            "Foursquare", 0xff061179,
            R.drawable.shields_list_foursquare_symbol, false,
            FoursquareShield.class), GPS_SHIELD((byte) 0x1C, "GPS", 0xffa10b07,
            R.drawable.shields_list_gps_symbol, false, GpsShield.class), SMS_SHIELD(
            (byte) 0x0D, "SMS", 0xffdb7f40, R.drawable.shields_list_sms_symbol,
            false, SmsShield.class, 1), MUSICPLAYER_SHIELD((byte) 0x1D,
            "Music Player", 0xffb950e9,
            R.drawable.shields_list_musicplayer_symbol, false,
            MusicShield.class), GYROSCOPE_SHIELD((byte) 0x0E, "Gyroscope",
            0xff4c84e9, R.drawable.shields_list_gyroscope_symbol, false,
            GyroscopeShield.class, 1), SKYPE_SHIELD((byte) 0x1F, "Skype",
            0xff08c473, R.drawable.shields_list_skype_symbol, false,
            SkypeShield.class), PROXIMITY_SHIELD((byte) 0x13, "Proximity",
            0xff543c8d, R.drawable.shields_list_proximity_symbol, false,
            ProximityShield.class, 1), GRAVITY_SHIELD((byte) 0x14, "Gravity",
            0xffd95342, R.drawable.shields_list_gravity_symbol, false,
            GravityShield.class, 1), ORIENTATION_SHIELD((byte) 0x0F,
            "Orientation", 0xff58844f,
            R.drawable.shields_list_orientation_symbol, false,
            OrientationShield.class, 1), LIGHT_SHIELD((byte) 0x10, "Light",
            0xff8b268d, R.drawable.shields_list_light_sensor_symbol, false,
            LightShield.class, 1), PRESSURE_SHIELD((byte) 0x11, "Pressure",
            0xff67584d, R.drawable.shields_list_pressure_symbol, false,
            PressureShield.class, 1), TEMPERATURE_SHIELD((byte) 0x12,
            "Temperature", 0xff999f45,
            R.drawable.shields_list_temperature_symbol, false,
            TemperatureShield.class, 1), CAMERA_SHIELD((byte) 0x15, "Camera",
            0xff6d0347, R.drawable.shields_list_camera_symbol, false,
            CameraShield.class, 1), PHONE_SHIELD((byte) 0x20, "Phone",
            0xffe9bd03, R.drawable.shields_list_phone_symbol, false,
            PhoneShield.class, 1), NFC((byte) 0x20, "NFC", 0xff127303,
            R.drawable.shields_list_nfc_symbol, false, EmptyShield.class, false), WIFI(
            (byte) 0x20, "WIFI", 0xff08bbb2,
            R.drawable.shields_list_wifi_symbol, false, EmptyShield.class,
            false), GSM((byte) 0x20, "GSM", 0xff5a0303,
            R.drawable.shields_list_gsm_symbol, false, EmptyShield.class, false), GLCD(
            (byte) 0x20, "GLCD", 0xff988564,
            R.drawable.shields_list_lcd_symbol, false, EmptyShield.class, false), EMAIL_SHIELD(
            (byte) 0x1E, "Email", 0xff114540,
            R.drawable.shields_list_email_symbol, false, EmailShield.class), CLOCK_SHIELD(
            (byte) 0x21, "Clock", 0xffc45527,
            R.drawable.shields_list_clock_symbol, false, ClockShield.class), KEYBOARD_SHIELD(
            (byte) 0x22, "Keyboard", 0xffde1f26,
            R.drawable.shields_list_keyboard_symbol, false,
            KeyboardShield.class), TEXT_TO_SPEECH_SHIELD((byte) 0x23,
            "Text To Speech", 0xffde1f26, R.drawable.shields_list_tts_symbol,
            false, TextToSpeechShield.class), SPEECH_RECOGNIZER_SHIELD(
            (byte) 0x24, "Voice Recognizer", 0xffde1f26,
            R.drawable.shields_list_voice_recognition_symbol, false,
            SpeechRecognitionShield.class, 1), DATA_LOGGER((byte) 0x25,
            "Data Logger", 0xffde1f26,
            R.drawable.shields_list_data_logger_symbol, false,
            DataLoggerShield.class), TERMINAL_SHIELD((byte) 0x26, "Terminal",
            0xffde1f26, R.drawable.shields_list_terminal_symbol, false,
            TerminalShield.class), TASKER_SHIELD((byte) 0x0, "Tasker",
            0xff0b4c8d, R.drawable.shields_list_flashlight_symbol, false,
            TaskerShield.class, false), REMOTEONESHEELD_SHIELD((byte) 0x28,
            "Remote 1Sheeld", 0xff0b4c8d,
            R.drawable.shields_list_flashlight_symbol, false,
            RemoteOneSheeldShield.class, false), PATTERN_SHIELD((byte) 0x27, "Pattern",
            0xffde1f26, R.drawable.shields_list_pattern_symbol, false,
            PatternShield.class), INTERNET_SHIELD((byte) 0x29, "Internet",
            0xffde1f26, R.drawable.shields_list_internet_symbol, false,
            InternetShield.class), COLOR_DETECTION_SHIELD((byte) 0x30, "Color Detector",
            0xffde1f26, R.drawable.shields_list_internet_symbol, false,
            ColorDetectionShield.class);
    public static int[] colors = new int[]{0xff03d203, 0xffd4d903,
            0xffe28203, 0xffe93f03, 0xff0362c0, 0xff03c0ae, 0xffc0034c,
            0xff99bd03, 0xff40039f, 0xffb97547, 0xffc0039d, 0xff266a5d,
            0xff039dc0, 0xffa14c4c, 0xff658f08, 0xff061179, 0xffa10b07,
            0xffdb7f40, 0xffb950e9, 0xff4c84e9, 0xff0b4c8d, 0xff08c473,
            0xff543c8d, 0xffd95342, 0xff58844f, 0xff8b268d, 0xff67584d,
            0xff999f45, 0xff6d0347, 0xffe9bd03, 0xff127303, 0xff08bbb2,
            0xff5a0303, 0xff988564, 0xff114540, 0xffc45527, 0xffde1f26,
            0xff142218, 0xffc9a302, 0xffa57378, 0xff3354af, 0xff282742,
            0xff381616};
    public byte id;
    public String name;
    public int itemBackgroundColor;
    public int symbolId;
    public boolean mainActivitySelection;
    public static UIShield shieldsActivitySelection;
    public static boolean isConnected = false;
    public boolean isReleasable = true;
    public int isInvalidatable = 0;
    public Class<? extends ControllerParent<?>> shieldType;
    public int position = 0;

    public static UIShield getShieldsActivitySelection() {
        return shieldsActivitySelection;
    }

    public String getName() {
        return name;
    }

    private UIShield(byte id, String name, int mainImageStripId, int symbolId,
                     boolean mainActivitySelection,
                     Class<? extends ControllerParent<?>> shieldType) {
        this.id = id;
        this.name = name;
        this.itemBackgroundColor = mainImageStripId;
        this.symbolId = symbolId;
        this.mainActivitySelection = mainActivitySelection;
        this.shieldType = shieldType;
    }

    private UIShield(byte id, String name, int mainImageStripId, int symbolId,
                     boolean mainActivitySelection,
                     Class<? extends ControllerParent<?>> shieldType, int isInvalidatable) {
        this.id = id;
        this.name = name;
        this.itemBackgroundColor = mainImageStripId;
        this.symbolId = symbolId;
        this.mainActivitySelection = mainActivitySelection;
        this.shieldType = shieldType;
        this.isInvalidatable = isInvalidatable;
    }

    private UIShield(byte id, String name, int mainImageStripId, int symbolId,
                     boolean mainActivitySelection,
                     Class<? extends ControllerParent<?>> shieldType,
                     boolean isReleasable) {
        this.id = id;
        this.name = name;
        this.itemBackgroundColor = mainImageStripId;
        this.symbolId = symbolId;
        this.mainActivitySelection = mainActivitySelection;
        this.shieldType = shieldType;
        this.isReleasable = isReleasable;
    }

    public byte getId() {
        return id;
    }

    public static void setConnected(boolean isConnected) {
        UIShield.isConnected = isConnected;
    }

    public static synchronized List<UIShield> valuesFiltered() {
        UIShield[] vals = values();
        List<UIShield> valsFiltered = new ArrayList<UIShield>();
        for (int i = 0; i < vals.length; i++) {
            UIShield cur = vals[i];
            if (cur.isReleasable) {
                cur.position = valsFiltered.size();
                valsFiltered.add(cur);
            }
        }
        Collections.sort(valsFiltered, new Comparator<UIShield>() {

            @Override
            public int compare(UIShield lhs, UIShield rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
        vals = null;
        final ArrayList<UIShield> ret = new ArrayList<UIShield>();
        for (UIShield uiShield : valsFiltered) {
            uiShield.itemBackgroundColor = colors[ret.size()];
            ret.add(uiShield);
        }
        valsFiltered.clear();
        valsFiltered = null;
        return ret;
    }

}
