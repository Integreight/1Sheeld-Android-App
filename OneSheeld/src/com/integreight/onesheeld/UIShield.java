package com.integreight.onesheeld;



public enum UIShield {
	
	LED_SHIELD(1, "LED",R.drawable.shields_activity_yellow_strip,R.drawable.shields_activity_led_symbol, false),
	VIBRATION_SHIELD(2, "Vibration",R.drawable.shields_activity_orange_strip,R.drawable.shields_activity_vibration_symbol, false),
	SEVENSEGMENT_SHIELD(3, "Seven Segments",R.drawable.shields_activity_red_strip,R.drawable.shields_activity_seven_segment_symbol, false),
	SPEAKER_SHIELD(4, "Speaker",R.drawable.shields_activity_rose_strip,R.drawable.shields_activity_speaker_symbol, false),
	MIC_SHIELD(5, "Mic",R.drawable.shields_activity_dark_blue_strip,R.drawable.shields_activity_mic_symbol ,false),
	KEYPAD_SHIELD(6, "Keypad",R.drawable.shields_activity_light_blue_strip,R.drawable.shields_activity_keypad_symbol ,false),
	SLIDER_SHIELD(7, "Sliders",R.drawable.shields_activity_blue_strip,R.drawable.shields_activity_sliders_symbol ,false),
	LCD_SHIELD(8, "LCD",R.drawable.shields_activity_cyan_strip,R.drawable.shields_activity_lcd_symbol ,false),
	MAGNETOMETER_SHIELD(9, "Magnetometer",R.drawable.shields_activity_light_sea_green_strip,R.drawable.shields_activity_magnetometer_symbol ,false),
	PUSHBUTTON_SHIELD(10, "Push Button",R.drawable.shields_activity_green_strip,R.drawable.shields_activity_push_button_symbol ,false),
	TOGGLEBUTTON_SHIELD(11, "Toggle Button",R.drawable.shields_activity_dark_sea_green_strip,R.drawable.shields_activity_push_button_symbol ,false),
	ACCELEROMETER_SHIELD(12, "Accelerometer",R.drawable.shields_activity_gray_strip,R.drawable.shields_activity_accelerometer_symbol ,false),
	FACEBOOK_SHIELD(13, "Facebook",R.drawable.shields_activity_gray_strip,R.drawable.shields_activity_accelerometer_symbol ,false),
	TWITTER_SHIELD(14, "Twitter",R.drawable.shields_activity_gray_strip,R.drawable.shields_activity_accelerometer_symbol ,false);
	
	private int id;
	private String name;
	private int imageId;
	private int iconId;
	private boolean toggleStatus;
	
	public int getIconId() {
		return iconId;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	public Boolean getToggleStatus() {
		return toggleStatus;
	}
	public void setToggleStatus(boolean toggleStatus) {
		this.toggleStatus = toggleStatus;
	}
	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	private UIShield(int id, String name ,int imageId ,int iconId ,boolean toggleStatus) {
		this.id = id;
		this.name = name;
		this.imageId = imageId;
		this.toggleStatus = toggleStatus;
		this.iconId = iconId;
	
	
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	
	public static UIShield getItem(int id){
		switch (id) {
		case 1:    return LED_SHIELD;
		case 2:    return VIBRATION_SHIELD;
		case 3:    return SEVENSEGMENT_SHIELD;
		case 4:    return SPEAKER_SHIELD;
		case 5:    return MIC_SHIELD;
		case 6:    return KEYPAD_SHIELD;
		case 7:    return SLIDER_SHIELD;
		case 8:    return LCD_SHIELD;
		case 9:    return MAGNETOMETER_SHIELD;
		case 10:    return PUSHBUTTON_SHIELD;
		case 11:    return TOGGLEBUTTON_SHIELD;
		case 12:    return ACCELEROMETER_SHIELD;  
		case 13:    return FACEBOOK_SHIELD;
		case 14:    return TWITTER_SHIELD;  
		}
		return null;
	}

	
	

	
}
