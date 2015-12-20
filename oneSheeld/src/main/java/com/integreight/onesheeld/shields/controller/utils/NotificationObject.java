package com.integreight.onesheeld.shields.controller.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Moustafa Nasr on 12/16/2015.
 */
public class NotificationObject {
    private String packageName="";
    private int notificationId=0;
    private long time=0;
    private String ticker="";
    private String text="";
    private String subText="";
    private String infoText="";
    private String bigText="";
    private ArrayList<String> textLines = new ArrayList<String>();
    private String title="";
    private String bigTitle="";
    private String tag = null;

    private JSONObject jsonObject;
    // Json Keys
    private static final String NOTIFICATION_PACKAGE = "NOTIFICATION_PACKAGE";
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final String NOTIFICATION_TAG = "NOTIFICATION_TAG";
    private static final String NOTIFICATION_TICKER = "NOTIFICATION_TICKER";
    private static final String NOTIFICATION_TIME = "NOTIFICATION_TIME";
    private static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";
    private static final String NOTIFICATION_BIG_TITLE = "NOTIFICATION_BIG_TITLE";
    private static final String NOTIFICATION_TEXT = "NOTIFICATION_TEXT";
    private static final String NOTIFICATION_BIG_TEXT = "NOTIFICATION_BIG_TEXT";
    private static final String NOTIFICATION_TEXT_LINES = "NOTIFICATION_TEXT_LINES";
    private static final String NOTIFICATION_SUB_TEXT = "NOTIFICATION_SUB_TEXT";
    private static final String NOTIFICATION_INFO_TEXT = "NOTIFICATION_INFO_TEXT";

    public NotificationObject(String packageName,int notificationId,long time){
        this.packageName = packageName;
        this.notificationId = notificationId;
        this.time = time;
    }

    public NotificationObject(String jsonString){
        fromJsonString(jsonString);
    }

    public void setBigText(String bigText) {
        this.bigText = bigText;
    }

    public void setBigTitle(String bigTitle) {
        this.bigTitle = bigTitle;
    }

    public void setTextLines(ArrayList<String> textLines) {
        this.textLines = textLines;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public long getTime() {
        return time;
    }

    public String getBigText() {
        return bigText;
    }

    public String getBigTitle() {
        return bigTitle;
    }

    public ArrayList<String> getTextLines() {
        return textLines;
    }

    public String getInfoText() {
        return infoText;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSubText() {
        return subText;
    }

    public String getText() {
        return text;
    }

    public String getTicker() {
        return ticker;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return packageName+":"+String.valueOf(notificationId);
    }

    public String toJsonString(){
        jsonObject = new JSONObject();
        try {
            jsonObject.put(NOTIFICATION_PACKAGE,(packageName != null)? packageName:"");
            jsonObject.put(NOTIFICATION_ID,notificationId);
            jsonObject.put(NOTIFICATION_TIME,time);
            if (tag != null)
                jsonObject.put(NOTIFICATION_TAG,tag);
            jsonObject.put(NOTIFICATION_TICKER,(ticker != null)? ticker:"");
            jsonObject.put(NOTIFICATION_TITLE,(title != null)? title:"");
            jsonObject.put(NOTIFICATION_BIG_TITLE,(bigTitle != null)? bigTitle:"");
            jsonObject.put(NOTIFICATION_TEXT,(text != null)? text:"");
            jsonObject.put(NOTIFICATION_BIG_TEXT,(bigText != null)? bigText:"");
            JSONArray jArray = new JSONArray();
            for (int lineCounter = 0;lineCounter < textLines.size(); lineCounter++) {
                jArray.put(textLines.get(lineCounter));
            }
            jsonObject.put(NOTIFICATION_TEXT_LINES,jArray);
            jsonObject.put(NOTIFICATION_SUB_TEXT,(subText != null)? subText:"");
            jsonObject.put(NOTIFICATION_INFO_TEXT,(infoText != null)? infoText:"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonString = jsonObject.toString();
        jsonObject = null;
        return jsonString;
    }

    public void fromJsonString(String jsonString){
        try {
            jsonObject = new JSONObject(jsonString);
            setPackageName(jsonObject.getString(NOTIFICATION_PACKAGE));
            setNotificationId(jsonObject.getInt(NOTIFICATION_ID));
            if (jsonObject.has(NOTIFICATION_TAG))
                setTag(jsonObject.getString(NOTIFICATION_TAG));
            else
                setTag(null);
            setTime(jsonObject.getLong(NOTIFICATION_TIME));
            setTicker(jsonObject.getString(NOTIFICATION_TICKER));
            setTitle(jsonObject.getString(NOTIFICATION_TITLE));
            setBigTitle(jsonObject.getString(NOTIFICATION_BIG_TITLE));
            setText(jsonObject.getString(NOTIFICATION_TEXT));
            setBigText(jsonObject.getString(NOTIFICATION_BIG_TEXT));
            ArrayList<String> array = new ArrayList<>();
            JSONArray jArray = jsonObject.getJSONArray(NOTIFICATION_TEXT_LINES);
            for (int lineCounter = 0;lineCounter < jArray.length(); lineCounter++) {
                array.add(jArray.getString(lineCounter));
            }
            setTextLines(array);
            setSubText(jsonObject.getString(NOTIFICATION_SUB_TEXT));
            setInfoText(jsonObject.getString(NOTIFICATION_INFO_TEXT));
            jsonObject = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (NotificationObject.class.equals(o.getClass())){
//            if (this.packageName.equals(((NotificationObject) o).getPackageName()) && this.notificationId == ((NotificationObject) o).getNotificationId())
            if (this.toString().equals(o.toString()))
                return true;
        }
        return false;
    }
}
