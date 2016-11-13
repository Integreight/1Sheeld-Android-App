package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.integreight.onesheeld.BuildConfig;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataLoggerShield extends ControllerParent<DataLoggerShield> {
    private static final byte START_LOGGING = 0x01;
    private static final byte STOP_LOGGING = 0x02;
    private static final byte ADD_FLOAT = 0x03;
    private static final byte ADD_STRING = 0x04;
    private static final byte LOG = 0x05;
    private boolean isStarted = false;
    public CopyOnWriteArrayList<String> headerList = new CopyOnWriteArrayList<>();
    ArrayList<Map<String, String>> dataSet = new ArrayList<>();
    String fileName = null;
    String fullFileName = null;
    String filePath = null;
    String header[];
    Map<String, String> rowData = new HashMap<>();
    public static final int READ_FOR_LOGGING = 0, LOGGING = 1,
            STOPPED_LOGGING = 2;
    public int currentStatus = READ_FOR_LOGGING;
    private DataLoggerListener eventHandler;


    public boolean isLoggingStarted() {
        return isStarted;
    }

    public DataLoggerShield() {
        super();
    }

    public DataLoggerShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        super.setConnected(pins);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public ControllerParent<DataLoggerShield> invalidate(SelectionAction selectionAction, boolean isToastable) {
        this.selectionAction = selectionAction;
        addRequiredPremission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= 16)
            addRequiredPremission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkForPermissions()) {
            if (selectionAction != null)
                selectionAction.onSuccess();
        } else {
            if (selectionAction != null)
                selectionAction.onFailure();
        }
        return super.invalidate(selectionAction, isToastable);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.DATA_LOGGER.getId()) {
            switch (frame.getFunctionId()) {
                case START_LOGGING:
                    if (frame.getArguments().size() > 0)
                        fileName = frame.getArgumentAsString(0);
                    else
                        fileName = null;
                    headerList = new CopyOnWriteArrayList<>();
                    dataSet = new ArrayList<>();
                    rowData = new HashMap<>();
                    currentStatus = LOGGING;
                    isStarted = true;
                    Log.d("HeaderSize", "Start");
                    if (eventHandler != null) {
                        eventHandler.onStartLogging();
                    }
                    break;
                case STOP_LOGGING:
                    saveData();
                    break;
                case ADD_STRING:
                    if (isStarted) {
                        currentStatus = LOGGING;
                        String key = frame.getArgumentAsString(0);
                        String value = frame.getArgumentAsString(1);
                        if (!headerList.contains(key))
                            headerList.add(key);
                        rowData.put(key, value);
                        if (eventHandler != null) {
                            eventHandler.onAdd(key, value);
                        }
                    }
                    break;
                case ADD_FLOAT:
                    if (isStarted) {
                        currentStatus = LOGGING;
                        String keyFloat = frame.getArgumentAsString(0);
                        String valueFloat = frame.getArgumentAsFloat(1) + "";
                        Log.d("HeaderSize", "Add    : " + keyFloat + "   " + valueFloat);
                        if (!headerList.contains(keyFloat))
                            headerList.add(keyFloat);
                        rowData.put(keyFloat, valueFloat);
                        if (eventHandler != null) {
                            eventHandler.onAdd(keyFloat, valueFloat);
                        }
                    }
                    break;
                case LOG:
                    if (isStarted) {
                        currentStatus = LOGGING;
                        if (eventHandler != null) {
                            eventHandler.onLog(new HashMap<>(rowData));
                        }
                        if (!headerList.contains("Time")) headerList.add("Time");
                        rowData.put("Time",
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date())
                                        .toString());
                        //rowData.remove("Time");
                        dataSet.add(new HashMap<>(rowData));
                        rowData = new HashMap<>();
                        Log.d("HeaderSize", "Log:  " + headerList.size() + "   **");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void saveData() {
        if (isStarted) {
            isStarted = false;
            ICsvMapWriter mapWriter = null;
            try {
                currentStatus = STOPPED_LOGGING;
                if (eventHandler != null) {
                    eventHandler.onStopLogging(dataSet);
                }
                File folder = new File(
                        Environment.getExternalStorageDirectory()
                                + "/OneSheeld");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                folder = new File(
                        Environment.getExternalStorageDirectory()
                                + "/OneSheeld/DataLogger");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                fullFileName = (fileName == null
                        || fileName.length() == 0 ? new Date()
                        .getTime() + ""
                        : fileName + " - " + new Date()
                        .getTime()) + ".csv";
                filePath = Environment
                        .getExternalStorageDirectory()
                        + "/OneSheeld/DataLogger/"
                        + fullFileName;
                mapWriter = new CsvMapWriter(
                        new FileWriter(filePath),
                        CsvPreference.STANDARD_PREFERENCE);
                final CellProcessor[] processors = new CellProcessor[headerList
                        .size()];
                for (int i = 0; i < processors.length; i++) {
                    processors[i] = new Optional();
                }

                // write the header
                header = new String[headerList.size()];
                Log.d("HeaderSize", "Stop:   " + headerList.size() + "");
                int i = 0;
                for (final String headerItem : headerList) {
                    header[i] = headerItem;
                    i++;
                }
                if (header.length > 0) {
                    mapWriter.writeHeader(header);

                    // write the customer MapsaqxzheaderList
                    for (Map<String, String> value : dataSet) {
                        mapWriter.write(value, header, processors);
                    }
                }
//                Toast.makeText(activity,"Data Logged Successfully.",Toast.LENGTH_SHORT).show();
                showNotification(activity.getString(R.string.data_logger_data_logged_successfully_notification) + ((fullFileName == null && fullFileName.length() <= 0) ? "." : " " + activity.getString(R.string.data_logger_to) + " " + fullFileName));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (mapWriter != null) {
                    try {
                        mapWriter.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // reset();
                }
            }
        }
    }

    protected void showNotification(String notificationText) {
        // TODO Auto-generated method stub
        NotificationCompat.Builder build = new NotificationCompat.Builder(
                activity);
        build.setSmallIcon(OneSheeldApplication.getNotificationIcon());
        build.setContentTitle(activity.getString(R.string.data_logger_shield_name));
        build.setContentText(notificationText);
        build.setTicker(notificationText);
        build.setWhen(System.currentTimeMillis());
        build.setAutoCancel(true);
        Toast.makeText(activity, notificationText, Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) activity
                .getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeFileType = mimeTypeMap.getMimeTypeFromExtension("csv");
        if(Build.VERSION.SDK_INT>=24) {
            Uri fileURI = FileProvider.getUriForFile(activity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    new File(filePath == null || filePath.length() == 0 ? "" : filePath));
            notificationIntent.setDataAndType(fileURI, mimeFileType);
        }
        else{
            notificationIntent.setDataAndType(Uri.fromFile(new File(filePath == null
                    || filePath.length() == 0 ? "" : filePath)), mimeFileType);
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent intent = PendingIntent.getActivity(activity, 0,
                notificationIntent, 0);
        build.setContentIntent(intent);
        Notification notification = build.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) new Date().getTime(), notification);
    }

    @Override
    public void reset() {
        saveData();
        if (dataSet != null)
            dataSet.clear();
        dataSet = null;
        header = null;
        if (headerList != null)
            headerList.clear();
        headerList = null;
    }

    public void setEventHandler(DataLoggerListener eventHandler) {
        this.eventHandler = eventHandler;
    }

    public static interface DataLoggerListener {
        public void onStartLogging();

        public void onStopLogging(ArrayList<Map<String, String>> loggedValues);

        public void onReadyForLogging();

        public void onAdd(String key, String value);

        public void onLog(Map<String, String> rowData);
    }

}
