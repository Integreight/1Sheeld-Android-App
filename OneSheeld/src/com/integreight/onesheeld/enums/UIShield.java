package com.integreight.onesheeld.enums;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.FacebookShield;
import com.integreight.onesheeld.shields.controller.GamepadShield;
import com.integreight.onesheeld.shields.controller.KeypadShield;
import com.integreight.onesheeld.shields.controller.LcdShield;
import com.integreight.onesheeld.shields.controller.LedShield;
import com.integreight.onesheeld.shields.controller.NotificationShield;
import com.integreight.onesheeld.shields.controller.SevenSegmentShield;
import com.integreight.onesheeld.shields.controller.SliderShield;
import com.integreight.onesheeld.shields.controller.SmsShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.integreight.onesheeld.shields.controller.ToggleButtonShield;
import com.integreight.onesheeld.shields.controller.TwitterShield;
import com.integreight.onesheeld.shields.fragments.BuzzerFragment;
import com.integreight.onesheeld.utils.ControllerParent;

public enum UIShield {

	LED_SHIELD(1, "LED", R.drawable.shields_activity_strip_13,
			R.drawable.shields_activity_strip_13_bw,
			R.drawable.shields_activity_small_strip_13,
			R.drawable.shields_activity_led_symbol, false, LedShield.class), NOTIFICATION_SHIELD(
			2, "Notification", R.drawable.shields_activity_strip_1,
			R.drawable.shields_activity_strip_1_bw,
			R.drawable.shields_activity_small_strip_1,
			R.drawable.shields_activity_vibration_symbol, false,
			NotificationShield.class), SEVENSEGMENT_SHIELD(3, "Seven Segment",
			R.drawable.shields_activity_strip_19,
			R.drawable.shields_activity_strip_19_bw,
			R.drawable.shields_activity_small_strip_19,
			R.drawable.shields_activity_seven_segment_symbol, false,
			SevenSegmentShield.class), BUZZER_SHIELD(4, "Buzzer",
			R.drawable.shields_activity_strip_9,
			R.drawable.shields_activity_strip_9_bw,
			R.drawable.shields_activity_small_strip_9,
			R.drawable.shields_activity_buzzer_symbol, false,
			SpeakerShield.class), MIC_SHIELD(5, "Mic",
			R.drawable.shields_activity_strip_15,
			R.drawable.shields_activity_strip_15_bw,
			R.drawable.shields_activity_small_strip_15,
			R.drawable.shields_activity_mic_symbol, false, SpeakerShield.class), KEYPAD_SHIELD(
			6, "Keypad", R.drawable.shields_activity_strip_2,
			R.drawable.shields_activity_strip_2_bw,
			R.drawable.shields_activity_small_strip_2,
			R.drawable.shields_activity_keypad_symbol, false,
			KeypadShield.class), SLIDER_SHIELD(7, "Sliders",
			R.drawable.shields_activity_strip_3,
			R.drawable.shields_activity_strip_3_bw,
			R.drawable.shields_activity_small_strip_3,
			R.drawable.shields_activity_sliders_symbol, false,
			SliderShield.class), LCD_SHIELD(8, "LCD",
			R.drawable.shields_activity_strip_4,
			R.drawable.shields_activity_strip_4_bw,
			R.drawable.shields_activity_small_strip_4,
			R.drawable.shields_activity_lcd_symbol, false, LcdShield.class), MAGNETOMETER_SHIELD(
			9, "Magnetometer", R.drawable.shields_activity_strip_16,
			R.drawable.shields_activity_strip_16_bw,
			R.drawable.shields_activity_small_strip_16,
			R.drawable.shields_activity_magnetometer_symbol, false), PUSHBUTTON_SHIELD(
			10, "Push Button", R.drawable.shields_activity_strip_12,
			R.drawable.shields_activity_strip_12_bw,
			R.drawable.shields_activity_small_strip_12,
			R.drawable.shields_activity_push_button_symbol, false), TOGGLEBUTTON_SHIELD(
			11, "On/Off Button", R.drawable.shields_activity_strip_6,
			R.drawable.shields_activity_strip_6_bw,
			R.drawable.shields_activity_small_strip_6,
			R.drawable.shields_activity_push_button_symbol, false,
			ToggleButtonShield.class), ACCELEROMETER_SHIELD(12,
			"Accelerometer", R.drawable.shields_activity_strip_21,
			R.drawable.shields_activity_strip_21_bw,
			R.drawable.shields_activity_small_strip_21,
			R.drawable.shields_activity_accelerometer_symbol, false), FACEBOOK_SHIELD(
			13, "Facebook", R.drawable.shields_activity_strip_7,
			R.drawable.shields_activity_strip_7_bw,
			R.drawable.shields_activity_small_strip_7,
			R.drawable.shields_activity_facebook_symbol, false,
			FacebookShield.class), TWITTER_SHIELD(14, "Twitter",
			R.drawable.shields_activity_strip_17,
			R.drawable.shields_activity_strip_17_bw,
			R.drawable.shields_activity_small_strip_17,
			R.drawable.shields_activity_twitter_symbol, false,
			TwitterShield.class), GAMEDPAD_SHIELD(15, "Game Pad",
			R.drawable.shields_activity_strip_10,
			R.drawable.shields_activity_strip_10_bw,
			R.drawable.shields_activity_small_strip_10,
			R.drawable.shields_activity_gamepad_symbol, false,
			GamepadShield.class), INTERNET_SHIELD(16, "Internet",
			R.drawable.shields_activity_strip_5,
			R.drawable.shields_activity_strip_5_bw,
			R.drawable.shields_activity_small_strip_5,
			R.drawable.shields_activity_internet_symbol, false), FOURSQUARE_SHIELD(
			17, "Foursquare", R.drawable.shields_activity_strip_8,
			R.drawable.shields_activity_strip_8_bw,
			R.drawable.shields_activity_small_strip_8,
			R.drawable.shields_activity_foursquare_symbol, false), GPS_SHIELD(
			18, "GPS", R.drawable.shields_activity_strip_18,
			R.drawable.shields_activity_strip_18_bw,
			R.drawable.shields_activity_small_strip_18,
			R.drawable.shields_activity_gps_symbol, false), SMS_SHIELD(19,
			"SMS", R.drawable.shields_activity_strip_20,
			R.drawable.shields_activity_strip_20_bw,
			R.drawable.shields_activity_small_strip_20,
			R.drawable.shields_activity_sms_symbol, false, SmsShield.class), MUSICPLAYER_SHIELD(
			20, "Music Player", R.drawable.shields_activity_strip_11,
			R.drawable.shields_activity_strip_11_bw,
			R.drawable.shields_activity_small_strip_11,
			R.drawable.shields_activity_musicplayer_symbol, false), GYROSCOPE_SHIELD(
			21, "Gyroscope", R.drawable.shields_activity_strip_14,
			R.drawable.shields_activity_strip_14_bw,
			R.drawable.shields_activity_small_strip_14,
			R.drawable.shields_activity_gyroscope_symbol, false), FLASHLIGHT_SHIELD(
			22, "Flashlight", R.drawable.shields_activity_strip_22,
			R.drawable.shields_activity_strip_22_bw,
			R.drawable.shields_activity_small_strip_22,
			R.drawable.shields_activity_flashlight_symbol, false);

	private int id;
	private String name;
	private int mainImageStripId;
	private int mainBWImageStripId;
	private int smallImageStripId;
	private int symbolId;
	private boolean mainActivitySelection;
	private static UIShield shieldsActivitySelection;
	private static boolean isConnected = false;
	private Class<? extends ControllerParent<?>> shieldType;

	public int getSymbolId() {
		return symbolId;
	}

	// public int getMainBWImageStripId() {
	// return mainBWImageStripId;
	// }
	public Boolean isMainActivitySelection() {
		return mainActivitySelection;
	}

	public static UIShield getShieldsActivitySelection() {
		return shieldsActivitySelection;
	}

	public static void setShieldsActivitySelection(UIShield selection) {
		shieldsActivitySelection = selection;
	}

	public void setMainActivitySelection(boolean toggleStatus) {
		this.mainActivitySelection = toggleStatus;
	}

	public int getMainImageStripId() {
		if (isConnected)
			return mainImageStripId;
		else
			return mainBWImageStripId;
	}

	public int getSmallImageStripId() {
		return smallImageStripId;
	}

	public String getName() {
		return name;
	}

	public Class<? extends ControllerParent<?>> getShieldType() {
		return shieldType;
	}

	public void setShieldType(Class<? extends ControllerParent<?>> shieldType) {
		this.shieldType = shieldType;
	}

	private UIShield(int id, String name, int mainImageStripId,
			int mainBWImageStripId, int smallImageStripId, int symbolId,
			boolean mainActivitySelection) {
		this.id = id;
		this.name = name;
		this.mainImageStripId = mainImageStripId;
		this.mainBWImageStripId = mainBWImageStripId;
		this.smallImageStripId = smallImageStripId;
		this.symbolId = symbolId;
		this.mainActivitySelection = mainActivitySelection;
	}

	private UIShield(int id, String name, int mainImageStripId,
			int mainBWImageStripId, int smallImageStripId, int symbolId,
			boolean mainActivitySelection,
			Class<? extends ControllerParent<?>> shieldType) {
		this.id = id;
		this.name = name;
		this.mainImageStripId = mainImageStripId;
		this.mainBWImageStripId = mainBWImageStripId;
		this.smallImageStripId = smallImageStripId;
		this.symbolId = symbolId;
		this.mainActivitySelection = mainActivitySelection;
		this.shieldType = shieldType;
	}

	public int getId() {
		return id;
	}

	public static UIShield getItem(int id) {
		switch (id) {
		case 1:
			return LED_SHIELD;
		case 2:
			return NOTIFICATION_SHIELD;
		case 3:
			return SEVENSEGMENT_SHIELD;
		case 4:
			return BUZZER_SHIELD;
		case 5:
			return MIC_SHIELD;
		case 6:
			return KEYPAD_SHIELD;
		case 7:
			return SLIDER_SHIELD;
		case 8:
			return LCD_SHIELD;
		case 9:
			return MAGNETOMETER_SHIELD;
		case 10:
			return PUSHBUTTON_SHIELD;
		case 11:
			return TOGGLEBUTTON_SHIELD;
		case 12:
			return ACCELEROMETER_SHIELD;
		case 13:
			return FACEBOOK_SHIELD;
		case 14:
			return TWITTER_SHIELD;
		case 15:
			return GAMEDPAD_SHIELD;
		case 16:
			return INTERNET_SHIELD;
		case 17:
			return FOURSQUARE_SHIELD;
		case 18:
			return GPS_SHIELD;
		case 19:
			return SMS_SHIELD;
		case 20:
			return MUSICPLAYER_SHIELD;
		case 21:
			return GYROSCOPE_SHIELD;
		case 22:
			return FLASHLIGHT_SHIELD;
		}
		return null;
	}

	public static boolean isConnected() {
		return isConnected;
	}

	public static void setConnected(boolean isConnected) {
		UIShield.isConnected = isConnected;
	}

}
