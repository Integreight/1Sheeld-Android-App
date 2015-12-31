package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.PackageItem;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.NotificationObject;
import com.integreight.onesheeld.shields.controller.utils.NotificationReceiver;
import com.integreight.onesheeld.utils.database.NotificationPackageList;

import java.util.ArrayList;

public class NotificationShield extends ControllerParent<NotificationShield> {
    private NotificationEventHandler eventHandler;
    private String lastNotificationText;
    private SparseArray<NotificationObject> notificationObjectArrayList = new SparseArray<>();
    private ArrayList<PackageItem> packageItems = new ArrayList<PackageItem>();
    private static final byte ID = UIShield.NOTIFICATION_SHIELD.getId();
    private static boolean isServiceON = false;
    int tmpNotificationId = 0;
    NotificationObject tmpNotificationObject = null;

    private static final byte NOTIFY_PHONE_METHOD_ID = (byte) 0x01;
    private static final byte QUERY_DATA_METHOD_ID = (byte) 0x02;
    private static final byte DISMISS_METHOD_ID = (byte) 0x03;

    private static final byte QUERY_APP = 0X01;
    private static final byte QUERY_TITLE = 0X02;
    private static final byte QUERY_TEXT = 0X03;
    private static final byte QUERY_SUB_TEXT = 0X04;
    private static final byte QUERY_INFO_TEXT = 0X05;
    private static final byte QUERY_BIG_TEXT = 0X06;
    private static final byte QUERY_TEXT_LINES = 0X07;
    private static final byte QUERY_BIG_TITLE = 0X08;
    private static final byte QUERY_TICKER = 0X09;

    private static final byte ON_NEW_NOTIFICATION = 0x01;
    private static final byte ON_DATA_QUERY_REQUEST = 0x02;
    private static final byte ON_NEW_MESSAGE_NOTIFICATION = 0x03;
    private static final byte ON_NOTIFIACTION_DISMISSED = 0x04;
    private static final byte ON_ERROR = 0x05;

    private static final byte ERROR_NOTIFICATION_NOT_FOUND = 0x0A;
    private static final byte ERROR_DATA_TYPE_NOT_FOUND = 0x0B;
    private static final byte ERROR_TITLE = 0X02;
    private static final byte ERROR_TEXT = 0X03;
    private static final byte ERROR_SUB_TEXT = 0X04;
    private static final byte ERROR_INFO_TEXT = 0X05;
    private static final byte ERROR_BIG_TEXT = 0X06;
    private static final byte ERROR_TEXT_LINES = 0X07;
    private static final byte ERROR_BIG_TITLE = 0X08;
    private static final byte ERROR_TICKER = 0X09;

    private static final byte TYPE_FACEBOOK = 0x01;
    private static final byte TYPE_WHATSAPP = 0x02;
    private static final byte TYPE_GMAIL = 0x03;
    private static final byte TYPE_SLACK = 0x04;
    private static final byte TYPE_TELEGRAM = 0x05;
    private static final byte TYPE_HANGOUTS = 0x06;
    private static final byte TYPE_LINE = 0x07;



    public String getLastNotificationText() {
        return lastNotificationText;
    }

    public NotificationShield() {
        super();
    }

    public NotificationShield(Activity activity, String tag) {
        super(activity, tag);
         checkDenyList();
    }

    @Override
    public ControllerParent<NotificationShield> init(String tag) {
        checkDenyList();
        return super.init(tag);
    }

    public static byte getID() {
        return ID;
    }

    protected void showNotification(String notificationText) {
        // TODO Auto-generated method stub
        NotificationCompat.Builder build = new NotificationCompat.Builder(
                activity);
        build.setSmallIcon(R.drawable.white_ee_icon);
        build.setContentTitle(notificationText);
        build.setContentText("Notification received from 1Sheeld!");
        build.setTicker(notificationText);
        build.setWhen(System.currentTimeMillis());
        Toast.makeText(activity, notificationText, Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) activity
                .getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        Intent notificationIntent = new Intent(activity, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                notificationIntent, 0);
        build.setContentIntent(intent);
        Notification notification = build.build();
        NotificationManager notificationManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, notification);
    }

    public static boolean isServiceON() {
        return isServiceON;
    }

    public Boolean startNotificationReceiver(){
        if (!isServiceON) {
            ContentResolver contentResolver = activity.getContentResolver();
            String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
            String packageName = activity.getPackageName();
            if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                // in this situation we know that the user has not granted the app the Notification access permission
                activity.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                return false;
            } else {
                activity.startService(new Intent(activity, NotificationReceiver.class));
                LocalBroadcastManager.getInstance(activity).registerReceiver(onNotice, new IntentFilter("NotificationDetailsMessage"));
                LocalBroadcastManager.getInstance(activity).registerReceiver(onRemoval, new IntentFilter("NotificationRemovalMessage"));
                isServiceON = true;
                return true;
            }
        }else {
            return true;
        }
    }

    public void stopNotificationReceiver(){
        if (isServiceON) {
            activity.stopService(new Intent(activity, NotificationReceiver.class));
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(onNotice);
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(onRemoval);
            isServiceON = false;
        }
    }

    boolean allowed = false;
    public static final String EXTRAS = "extras",JSON_EXTRAS = "jsonExtras";
    private int keyCounter = 1;
    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationObject currentNotification = new NotificationObject(intent.getStringExtra(JSON_EXTRAS));
            int currentKey = getNotificationKey(currentNotification.getPackageName(),currentNotification.getNotificationId(),currentNotification.getTime());

            if (currentNotification.getPackageName().equals("com.integreight.onesheeld")) {
                return;
            }
            allowed = false;
            for (PackageItem item: packageItems){
                if (currentNotification.getPackageName().equals(item.name)) {
                    allowed = true;
                }
            }
            if (!allowed)
                return;

            if (currentKey == 0) {
                notificationObjectArrayList.put(keyCounter, currentNotification);
                currentKey = keyCounter;
                keyCounter++;
            }else{
                if (notificationObjectArrayList.get(currentKey).compareTo(currentNotification)) {
                    notificationObjectArrayList.get(currentKey).fromJsonString(intent.getStringExtra(JSON_EXTRAS));
                    return;
                }else {
                    notificationObjectArrayList.get(currentKey).fromJsonString(intent.getStringExtra(JSON_EXTRAS));
                }
            }

            if (eventHandler != null ){

                ShieldFrame sf1 = new ShieldFrame(getID(),ON_NEW_NOTIFICATION);
                sf1.addIntegerArgument(2, currentKey);
                sf1.addStringArgument(currentNotification.getPackageName());
                sf1.addIntegerArgument(4, (int) currentNotification.getTime());
                byte tickerFlag = (byte) ((currentNotification.getTicker().equals(""))? 0x00:0x01);
                byte textFlag = (byte) ((currentNotification.getText().equals(""))? 0x00:(0x01 << 1));
                byte subTextFlag = (byte) ((currentNotification.getSubText().equals(""))? 0x00:(0x01 << 2));
                byte infoTextFlag = (byte) ((currentNotification.getInfoText().equals(""))? 0x00:(0x01 << 3));
                byte bigTextFlag = (byte) ((currentNotification.getBigText().equals(""))? 0x00:(0x01 << 4));
                byte textLinesFlag = (byte) ((currentNotification.getTextLines().equals(""))? 0x00:(0x01 << 5));
                byte titleFlag = (byte) ((currentNotification.getTitle().equals(""))? 0x00:(0x01 << 6));
                byte bigTitleFlag = (byte) ((currentNotification.getBigTitle().equals(""))? 0x00:(0x01 << 7));
                sf1.addByteArgument((byte) (tickerFlag | textFlag | subTextFlag | infoTextFlag | bigTextFlag | textLinesFlag | titleFlag | bigTitleFlag));
                sendShieldFrame(sf1,true);

                ShieldFrame sf2 = new ShieldFrame(getID(),ON_NEW_MESSAGE_NOTIFICATION);
                sf2.addIntegerArgument(2, currentKey);

                if (currentNotification.getPackageName().equals("com.facebook.orca")) {
                    if (currentNotification.getTextLines().size() == 0 && !currentNotification.getTitle().equals("")) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getText());
                        sf2.addByteArgument(TYPE_FACEBOOK);
                        sf2.addStringArgument(currentNotification.getTitle());
                        sf2.addStringArgument(currentNotification.getText());
                        queueShieldFrame(sf2);
                    }else if (!currentNotification.getTitle().equals("")) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getTextLines().get(currentNotification.getTextLines().size() - 1));
                        sf2.addByteArgument(TYPE_FACEBOOK);
                        sf2.addStringArgument(currentNotification.getTitle());
                        sf2.addStringArgument(currentNotification.getTextLines().get(currentNotification.getTextLines().size() - 1));
                        queueShieldFrame(sf2);
                    }
                }else if (currentNotification.getPackageName().equals("com.whatsapp")) {
                    if (currentNotification.getTextLines().size() == 0 && !currentNotification.getText().equals("")) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getText());
                        sf2.addByteArgument(TYPE_WHATSAPP);
                        sf2.addStringArgument(currentNotification.getTitle());
                        sf2.addStringArgument(currentNotification.getText());
                        sf2.addIntegerArgument(1, currentNotification.getText().length());
                        queueShieldFrame(sf2);
                    }
                }else if (currentNotification.getPackageName().equals("com.google.android.gm")) {
                    if (!currentNotification.getBigText().equals("")) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getBigText());
                        sf2.addByteArgument(TYPE_GMAIL);
                        sf2.addStringArgument(currentNotification.getTitle());
                        sf2.addStringArgument(currentNotification.getBigText());
                        queueShieldFrame(sf2);
                    }else if(currentNotification.getTextLines().size() > 0) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getTextLines().get(currentNotification.getTextLines().size() - 1));
                        sf2.addByteArgument(TYPE_GMAIL);
                        sf2.addIntegerArgument(2, tmpNotificationId);
                        sf2.addStringArgument(currentNotification.getTitle());
                        sf2.addStringArgument(currentNotification.getTextLines().get(currentNotification.getTextLines().size() - 1));
                        queueShieldFrame(sf2);
                    }
                }else if (currentNotification.getPackageName().equals("com.google.android.talk")) {
                    if (currentNotification.getTicker().contains(":")) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTicker().split(":")[0] + currentNotification.getTitle());
                        sf2.addByteArgument(TYPE_HANGOUTS);
                        sf2.addStringArgument(currentNotification.getTicker().split(":")[0]);
                        sf2.addStringArgument(currentNotification.getTitle());
                        queueShieldFrame(sf2);
                    }
                }else if (currentNotification.getPackageName().equals("com.slack")) {
                    eventHandler.onNotifiactionArrived(currentNotification.getTitle()+" :"+currentNotification.getText());
                    sf2.addByteArgument(TYPE_SLACK);
                    sf2.addStringArgument(currentNotification.getTitle());
                    sf2.addStringArgument(currentNotification.getText());
                    queueShieldFrame(sf2);
                }else if(currentNotification.getPackageName().equals("jp.naver.line.android")) {
                    eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getText());
                    sf2.addByteArgument(TYPE_LINE);
                    sf2.addStringArgument(currentNotification.getTitle());
                    sf2.addStringArgument(currentNotification.getText());
                    queueShieldFrame(sf2);
                }else if (currentNotification.getPackageName().equals("org.telegram.messenger")) {
                    if (currentNotification.getTextLines().size() == 0 && !currentNotification.getText().equals("")) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getText());
                        sf2.addByteArgument(TYPE_TELEGRAM);
                        sf2.addStringArgument(currentNotification.getTitle());
                        sf2.addStringArgument(currentNotification.getText());
                        queueShieldFrame(sf2);
                    }else if (currentNotification.getTextLines().size() > 0) {
                        eventHandler.onNotifiactionArrived(currentNotification.getTitle() + " :" + currentNotification.getTextLines().get(0));
                        sf2.addByteArgument(TYPE_TELEGRAM);
                        sf2.addStringArgument(currentNotification.getTitle());
                        sf2.addStringArgument(currentNotification.getTextLines().get(0));
                        queueShieldFrame(sf2);
                    }
                }else if (!currentNotification.getTitle().equals(""))
                    eventHandler.onNotifiactionArrived(currentNotification.getTitle());
                else if (!currentNotification.getTicker().equals(""))
                    eventHandler.onNotifiactionArrived(currentNotification.getTicker());
                else
                    eventHandler.onNotifiactionArrived(currentNotification.getPackageName());
            }
        }
    };

    BroadcastReceiver onRemoval = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationObject currentNotification = new NotificationObject(intent.getStringExtra(JSON_EXTRAS));
            removeNotification(currentNotification);
        }
    };

    private synchronized void removeNotification(NotificationObject currentNotification){
        for (int notificationsCounter=0;notificationsCounter<notificationObjectArrayList.size();notificationsCounter++){
            if (currentNotification.equals(notificationObjectArrayList.get(notificationObjectArrayList.keyAt(notificationsCounter)))){
                ShieldFrame sf = new ShieldFrame(getID(),ON_NOTIFIACTION_DISMISSED);
                sf.addIntegerArgument(2, notificationObjectArrayList.keyAt(notificationsCounter));
                notificationObjectArrayList.remove(notificationsCounter);
                sendShieldFrame(sf,true);
                break;
            }
        }
    }

    public void setNotificationEventHandler(
            NotificationEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private NotificationObject getNotification(String packageNmae,int notificationId){
        NotificationObject currentNotification = new NotificationObject(packageNmae,notificationId,00);
        for (int notificationsCounter=0;notificationsCounter<notificationObjectArrayList.size();notificationsCounter++){
            if (currentNotification.equals(notificationObjectArrayList.get(notificationsCounter))){
                return notificationObjectArrayList.get(notificationsCounter);
            }
        }
        return null;
    }

    private int getNotificationKey(String packageName,int notificationId,long notificationTime){
        NotificationObject currentNotification = new NotificationObject(packageName,notificationId,notificationTime);
        if (notificationObjectArrayList.size() > 0) {
            if (notificationTime > 0) {
                for (int notificationsCounter = 0; notificationsCounter < notificationObjectArrayList.size(); notificationsCounter++) {
                    if (currentNotification.compareTo(notificationObjectArrayList.get(notificationsCounter))) {
                        return notificationObjectArrayList.keyAt(notificationsCounter);
                    }
                }
            }else{
                for (int notificationsCounter = 0; notificationsCounter < notificationObjectArrayList.size(); notificationsCounter++) {
                    if (currentNotification.equals(notificationObjectArrayList.get(notificationsCounter))) {
                        return notificationObjectArrayList.keyAt(notificationsCounter);
                    }
                }
            }
        }
        return 0;
    }

    private void dismissNotification(NotificationObject currentNotification){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            NotificationReceiver.getThisInstance().cancelNotification(currentNotification.getPackageName(), currentNotification.getTag(), currentNotification.getNotificationId());
            do {
                tmpNotificationId = getNotificationKey(currentNotification.getPackageName(), currentNotification.getNotificationId(), 0);
                notificationObjectArrayList.remove(tmpNotificationId);
            }while (tmpNotificationId != 0);
            ShieldFrame sf = new ShieldFrame(getID(),ON_NOTIFIACTION_DISMISSED);
            sf.addIntegerArgument(2, tmpNotificationId);
        }
    }

    public void checkDenyList() {
//        if (packageItems == null || (packageItems != null && packageItems.size() == 0)) {
            NotificationPackageList db = new NotificationPackageList(activity);
            db.openToWrite();
            packageItems = db.getPlaylist();
            db.close();
            //init();
//        }
    }

    public interface NotificationEventHandler {
        void onNotificationReceive(String notificationText);
        void onNotifiactionArrived(String notificationText);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.NOTIFICATION_SHIELD.getId()) {
            String notificationText = frame.getArgumentAsString(0);
            lastNotificationText = notificationText;
            switch (frame.getFunctionId()){
                case NOTIFY_PHONE_METHOD_ID:
                    showNotification(notificationText);
                    if (eventHandler != null)
                        eventHandler.onNotificationReceive(notificationText);
                    break;
                case QUERY_DATA_METHOD_ID:
                    tmpNotificationId = frame.getArgumentAsInteger(0);
                    tmpNotificationObject = notificationObjectArrayList.get(tmpNotificationId, null);
                    if (tmpNotificationObject != null) {
                        if (frame.getArguments().size()>=2) {
                            ShieldFrame sf = new ShieldFrame(getID(), ON_DATA_QUERY_REQUEST);
                            switch (frame.getArgument(1)[0]) {
                                case QUERY_APP:
                                    sf.addIntegerArgument(2, tmpNotificationId);
                                    sf.addByteArgument(QUERY_APP);
                                    sf.addStringArgument(tmpNotificationObject.getPackageName());
                                    sf.addIntegerArgument(1,tmpNotificationObject.getPackageName().length());
                                    sendShieldFrame(sf,true);
                                    break;
                                case QUERY_TITLE:
                                    if (!tmpNotificationObject.getTitle().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_TITLE);
                                        sf.addStringArgument(tmpNotificationObject.getTitle());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getTitle().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId,ERROR_TITLE);
                                    }
                                    break;
                                case QUERY_TEXT:
                                    if (!tmpNotificationObject.getText().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_TEXT);
                                        sf.addStringArgument(tmpNotificationObject.getText());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getText().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId, ERROR_TEXT);
                                    }
                                    break;
                                case QUERY_SUB_TEXT:
                                    if (!tmpNotificationObject.getSubText().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_SUB_TEXT);
                                        sf.addStringArgument(tmpNotificationObject.getSubText());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getSubText().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId, ERROR_SUB_TEXT);
                                    }
                                    break;
                                case QUERY_INFO_TEXT:
                                    if (!tmpNotificationObject.getInfoText().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_INFO_TEXT);
                                        sf.addStringArgument(tmpNotificationObject.getInfoText());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getInfoText().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId, ERROR_INFO_TEXT);
                                    }
                                    break;
                                case QUERY_BIG_TEXT:
                                    if (!tmpNotificationObject.getBigText().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_BIG_TEXT);
                                        sf.addStringArgument(tmpNotificationObject.getBigText());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getBigText().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId, ERROR_BIG_TEXT);
                                    }
                                    break;
                                case QUERY_TEXT_LINES:
                                    if (!tmpNotificationObject.getPackageName().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_TEXT_LINES);
                                        sf.addStringArgument(tmpNotificationObject.getTextLinesAsString());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getTextLinesAsString().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId, ERROR_TEXT_LINES);
                                    }
                                    break;
                                case QUERY_BIG_TITLE:
                                    if (!tmpNotificationObject.getBigTitle().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_BIG_TITLE);
                                        sf.addStringArgument(tmpNotificationObject.getBigTitle());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getBigTitle().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId, ERROR_BIG_TITLE);
                                    }
                                    break;
                                case QUERY_TICKER:
                                    if (!tmpNotificationObject.getTicker().equals("")) {
                                        sf.addIntegerArgument(2, tmpNotificationId);
                                        sf.addByteArgument(QUERY_TICKER);
                                        sf.addStringArgument(tmpNotificationObject.getTicker());
                                        sf.addIntegerArgument(1,tmpNotificationObject.getTicker().length());
                                        sendShieldFrame(sf,true);
                                    }else {
                                        sendError(tmpNotificationId, ERROR_TICKER);
                                    }
                                    break;
                                default:
                                    sendError(tmpNotificationId, ERROR_DATA_TYPE_NOT_FOUND);
                                    break;
                            }
                        }
                    }else {
                        sendError(0, ERROR_NOTIFICATION_NOT_FOUND);
                    }
                    break;
                case DISMISS_METHOD_ID:
                    tmpNotificationId = frame.getArgumentAsInteger(0);
                    tmpNotificationObject = notificationObjectArrayList.get(tmpNotificationId, null);
                    if (tmpNotificationObject != null)
                        dismissNotification(tmpNotificationObject);
                    break;
            }
        }
    }

    private void sendError(int notificationId ,byte errorType){
        ShieldFrame sf = new ShieldFrame(getID(),ON_ERROR);
        sf.addIntegerArgument(2,notificationId);
        sf.addByteArgument(errorType);
        sendShieldFrame(sf,true);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        stopNotificationReceiver();
    }

}
