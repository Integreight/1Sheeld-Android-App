package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.provider.Settings;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mouso on 3/11/2015.
 */
public class NfcShield extends ControllerParent<NfcShield> {

    private static final byte SHIELD_ID = UIShield.NFC_SHIELD.getId();

    private static final byte RECORD_QUERY_DATA = 0x01;
    private static final byte RECORD_QUERY_PARSED_DATA = 0x03;
    private static final byte RECORD_QUERY_TYPE = 0x02;

    private static final byte NEW_TAG_FRAME = 0x01;
    private static final byte TAG_ERROR_FRAME = 0x02;
    private static final byte RECORD_QUERY_TYPE_FRAME = 0x03;
    private static final byte RECORD_QUERY_PARSED_DATA_FRAME = 0x04;
    private static final byte RECORD_QUERY_DATA_FRAME = 0x05;

    private static final int UNKNOWN_TYPE = 0;
    private static final int EMPTY_TYPE = 1;
    private static final int EXTERNAL_TYPE = 2;
    private static final int MIME_MEDIA_TYPE = 3;
    private static final int UNCHANGED_TYPE = 4;
    private static final int ABSOLUTE_URI_TYPE = 5;
    private static final int TEXT_TYPE = 6;
    private static final int URI_TYPE = 7;
    private static final int UNSUPPORTED_TYPE = 8;

    private static final byte TAG_NOT_SUPPORTED = 0x02;
    private static final byte RECORD_CAN_NOT_BE_PARSED = 0x01;
    private static final byte INDEX_OUT_OF_BOUNDS = 0x00;
    private static final byte NO_ENOUGH_BYTES = 0x03;
    private static final byte TAG_READING_ERROR = 0x04;
    private static final byte RECORD_NOT_FOUND = 0x05;

    private NFCEventHandler eventHandler;

    private Tag currentTag;
    private boolean isNdef_Flag = false;
    private boolean isTagSupported = false;
    private boolean isForeground = false;

    private static final String[] UriTypes = {"", "http://www.", "https://www.", "http://", "https://", "tel:", "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.", "ftps://", "sftp://", "smb://", "nfs://", "ftp://", "dav://", "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:", "sip:", "sips:", "tftp:", "btspp://", "btl2cap://", "btgoep://", "tcpobex://", "irdaobex://", "file://", "urn:epc:id:", "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:"};

    public NfcShield() {
    }

    public NfcShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public void setCurrentTag(Tag currentTag) {
        this.currentTag = currentTag;
    }

    @Override
    public ControllerParent<NfcShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        registerNFCListener(isToastable);
        return super.invalidate(selectionAction, isToastable);
    }

    public void registerNFCListener(boolean isToastable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                setupForegroundDispatch();
                selectionAction.onSuccess();
            } else {
                if(nfcAdapter == null){
                    if (isToastable) {
                        activity.showToast(R.string.nfc_device_doesnt_support_nfc);
                    }
                }
                else {
                    showSettingsDialogIfNfcIsNotEnabled();
                }
                selectionAction.onFailure();
            }
        } else {
            if (isToastable)
                activity.showToast(R.string.nfc_device_doesnt_support_nfc);
            selectionAction.onFailure();
        }
    }

    public void showSettingsDialogIfNfcIsNotEnabled(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
                AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
                alertbox.setMessage(R.string.nfc_we_need_you_to_enable_nfc_for_this_shield_to_work);
                alertbox.setPositiveButton(R.string.nfc_validation_dialog_ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            getActivity().startActivity(intent);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            getActivity().startActivity(intent);
                        }
                    }
                });
                alertbox.setNegativeButton(R.string.nfc_validation_dialog_later_button, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        activity.showToast(activity.getString(R.string.nfc_please_enable_nfc_to_be_able_to_use_this_shield));
                    }
                });
                alertbox.show();
            }
        }
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == SHIELD_ID) {
            if (frame.getArguments() != null && frame.getArguments().size() > 0) {
                int record, start, size;
                DataReply data;
                ShieldFrame sf;
                if (isNdef_Flag) {
                    switch (frame.getFunctionId()) {
                        case RECORD_QUERY_DATA:
                            record = frame.getArgumentAsInteger(0);
                            start = frame.getArgumentAsInteger(1);
                            size = frame.getArgumentAsInteger(2);
                            data = readNdefRecordData(record, start, size,255);
                            if (!data.hasError() || data.getError() == NO_ENOUGH_BYTES) {
                                sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_DATA_FRAME);
                                sf.addArgument(1, record);
                                sf.addArgument(data.getBytesData());
                                sendShieldFrame(sf, true);
                            }
                            if (data.hasError()){
                                sendError(data.getError(),data.getError() == NO_ENOUGH_BYTES);
                            }
                            break;
                        case RECORD_QUERY_PARSED_DATA:
                            record = frame.getArgumentAsInteger(0);
                            data = readNdefRecordParsedData(record, 0, 255,255);
                            if (!data.hasError() || data.getError() == NO_ENOUGH_BYTES) {
                                sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_PARSED_DATA_FRAME);
                                sf.addArgument(1, record);
                                sf.addArgument(data.getBytesData());
                                sendShieldFrame(sf, true);
                            }
                            if (data.hasError() && data.getError() != NO_ENOUGH_BYTES){
                                sendError(data.getError());
                            }
                            break;
                        case RECORD_QUERY_TYPE:
                            record = frame.getArgumentAsInteger(0);
                            start = frame.getArgumentAsInteger(1);
                            size = frame.getArgumentAsInteger(2);
                            data = readNdefRecordType(record, start, size,255);
                            if (!data.hasError() || data.getError() == NO_ENOUGH_BYTES) {
                                sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_TYPE_FRAME);
                                sf.addArgument(1, record);
                                sf.addArgument(data.getBytesData());
                                sendShieldFrame(sf, true);
                            }
                            if (data.hasError()){
                                sendError(data.getError(),data.getError() == NO_ENOUGH_BYTES);
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    sendError(TAG_READING_ERROR);
                }
            }
        }
    }

    @Override
    public void reset() {
        if (isForeground)
            stopForegroundDispatch();
        else {
            PackageManager packageManager = activity.getApplicationContext().getPackageManager();
            packageManager.setComponentEnabledSetting(new ComponentName("com.integreight.onesheeld", "com.integreight.onesheeld.NFCUtils-alias"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public NFCEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(NFCEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public static interface NFCEventHandler {
        void ReadNdef(String id, int maxSize, int usedSize, ArrayList<ArrayList<String>> data);
    }

    private void resetTechnologyFlags() {
        isTagSupported = false;
        isNdef_Flag = false;
    }


    public void setupForegroundDispatch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
            final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

            String[][] techList = new String[3][1];
            techList[0][0] = "android.nfc.tech.NdefFormatable";
            techList[1][0] = "android.nfc.tech.NfcA";
            techList[2][0] = "android.nfc.tech.Ndef";

            IntentFilter[] filters = new IntentFilter[2];
            filters[0] = new IntentFilter();
            filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
            filters[0].addCategory(Intent.CATEGORY_DEFAULT);
            filters[1] = new IntentFilter();
            filters[1].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filters[1].addCategory(Intent.CATEGORY_DEFAULT);
            if (nfcAdapter != null) {
                if (!isForeground) {
                    nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
                    isForeground = true;
                }
            }
        }
    }

    public void stopForegroundDispatch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            if (nfcAdapter != null) {
                if (isForeground) {
                    nfcAdapter.disableForegroundDispatch(activity);
                    isForeground = false;
                }
            }
        }

    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            sendError(TAG_READING_ERROR);
        }else {
            resetTechnologyFlags();
            switch (action) {
                case NfcAdapter.ACTION_NDEF_DISCOVERED:
                    setCurrentTag(tag);
                    if (!getNdefMaxSize().hasError() && !getTagId().hasError()) {
                        isTagSupported = true;
                        isNdef_Flag = true;
                        displayData();
                        sendNewTagFrame();
                    } else {
                        sendError(TAG_READING_ERROR);
                    }
                    break;
                case NfcAdapter.ACTION_TECH_DISCOVERED:
                    setCurrentTag(tag);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                        String[] techList = tag.getTechList();
                        for (String tech : techList) {
                            if (!isTagSupported) {
                                if (Ndef.class.getName().equals(tech)) {
                                    DataReply maxSize = getNdefMaxSize();
                                    if (!maxSize.hasError() && !getTagId().hasError()) {
                                        isNdef_Flag = false;
                                        isTagSupported = true;
                                        DataReply recordCount = getNdefRecordCount();
                                        if (recordCount.getIntegerData() > 0 && recordCount.getIntegerData() < 256) {
                                            isNdef_Flag = true;
                                            displayData();
                                            sendNewTagFrame();
                                        }else if (recordCount.getIntegerData() == 0){
                                            sendNewEmptyTagFrame();
                                        }else if(recordCount.getError() != 0){
                                            sendError(recordCount.getError());
                                        }
                                    } else if (maxSize.hasError()){
                                        sendError(maxSize.getError());
                                    }
                                } else if (NdefFormatable.class.getName().equals(tech)) {
                                    /*isNdef_Flag = false;
                                    isTagSupported = true;
                                    if (!getNdefMaxSize().hasError()&& !getTagId().hasError()) {
                                        sendNewEmptyTagFrame();
                                        displayData();
                                    } else {
                                        sendError(TAG_READING_ERROR);
                                    }*/
                                }
                            } else
                                break;
                        }
                        if (!isTagSupported)
                            sendError(TAG_NOT_SUPPORTED);
                    }
                    break;
                case NfcAdapter.ACTION_TAG_DISCOVERED:
                    setCurrentTag(tag);
                    break;
            }
        }
    }

    public void displayData() {
        if (eventHandler != null)
            if (isNdef_Flag && currentTag != null) {
                DataReply tagId = getTagId();
                DataReply ndefMaxSize = getNdefMaxSize();
                DataReply ndefUsedSize = getNdefUsedSize();
                ArrayList<ArrayList<String>> arrayListForDisplay = generateArrayListForDisplay();
                if (!tagId.hasError() && !ndefMaxSize.hasError()&& !ndefUsedSize.hasError() && arrayListForDisplay != null)
                    eventHandler.ReadNdef(convertByteArrayToHexString(tagId.getBytesData()), ndefMaxSize.getIntegerData(), ndefUsedSize.getIntegerData(), arrayListForDisplay);
            }
    }

    private void sendNewTagFrame() {
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {

                boolean dataReadingError = false;

                DataReply tagId = getTagId();
                DataReply maxSize = getNdefMaxSize();
                DataReply recordCount = getNdefRecordCount();
                DataReply usedSize = getNdefUsedSize();
                byte[][] records = new byte[recordCount.getIntegerData()][5];

                for (int i = 0; i < recordCount.getIntegerData(); i++) {
                    DataReply recordTypeCategory = getRecordTypeCategory(i);
                    DataReply recordTypeLength = getNdefRecordTypeLength(i);
                    DataReply recordDataLength = getNdefRecordDataLength(i);
                    if (recordTypeCategory.hasError() || recordTypeLength.hasError() || recordDataLength.hasError()){
                        dataReadingError = true;
                    }else {
                        records[i][0] = (byte) recordTypeCategory.getIntegerData();
                        records[i][1] = (byte) recordTypeLength.getIntegerData();
                        records[i][2] = (byte) (recordTypeLength.getIntegerData() >> 8);
                        records[i][3] = (byte) recordDataLength.getIntegerData();
                        records[i][4] = (byte) (recordDataLength.getIntegerData() >> 8);
                    }
                }
                if (maxSize.hasError() || (recordCount.hasError() && recordCount.getError() != INDEX_OUT_OF_BOUNDS) || usedSize.hasError() || tagId.hasError() || records.length != recordCount.getIntegerData()){
                    dataReadingError = true;
                }


                if (!dataReadingError) {
                    ShieldFrame sf = new ShieldFrame(SHIELD_ID, NEW_TAG_FRAME);

                    sf.addArgument(tagId.getBytesData());
                    sf.addArgument(2, maxSize.getIntegerData());
                    sf.addArgument(1, recordCount.getIntegerData());
                    sf.addArgument(2, usedSize.getIntegerData());

                    for (int i = 0; i < recordCount.getIntegerData(); i++) {
                        sf.addArgument(records[i]);
                    }
                    sendShieldFrame(sf, true);
                }else{
                    sendError(TAG_READING_ERROR);
                }
            }
        }
    }

    private void sendNewEmptyTagFrame() {
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                DataReply tagId = getTagId();
                DataReply maxSize = getNdefMaxSize();
                if (!tagId.hasError() && !maxSize.hasError()) {
                    ShieldFrame sf = new ShieldFrame(SHIELD_ID, NEW_TAG_FRAME);
                    sf.addArgument(tagId.getBytesData());
                    sf.addArgument(2, maxSize.getIntegerData());
                    sf.addArgument(1, 0);
                    sf.addArgument(2, 0);
                    sendShieldFrame(sf, true);
                }else {
                    sendError(TAG_READING_ERROR);
                    return;
                }
            }
        }
    }

    private DataReply getTagId() {
        DataReply dataReply = new DataReply();
        byte[] currentTagId = new byte[0];
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                currentTagId = currentTag.getId();
                if (currentTagId != null) {
                    if (currentTagId.length == 0)
                        currentTagId = new byte[]{0x00};
                }else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
            }
        }else {
            dataReply.setError(TAG_READING_ERROR);
        }
        dataReply.setBytesData(currentTagId);
        return dataReply;
    }

    private DataReply getNdefUsedSize() {
        DataReply dataReply = new DataReply();
        int size = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null && ndef.getCachedNdefMessage() != null)
                    size = ndef.getCachedNdefMessage().toByteArray().length;
                else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                }
            }
        }
        dataReply.setIntegerData(size);
        return dataReply;
    }

    private DataReply getNdefMaxSize() {
        DataReply dataReply = new DataReply();
        int maxSize = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    maxSize = ndef.getMaxSize();
                    if (maxSize <= 0){
                        dataReply.setError(TAG_READING_ERROR);
                        return dataReply;
                    }
                }else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataReply.setIntegerData(maxSize);
        return dataReply;
    }

    private DataReply getNdefRecordCount() {
        DataReply dataReply = new DataReply();
        int recordCount = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);

                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        recordCount = ndef.getCachedNdefMessage().getRecords().length;
                    else
                        recordCount = 0;
                } else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (recordCount > 256) {
            dataReply.setError(INDEX_OUT_OF_BOUNDS);
            recordCount = 256;
        }
        dataReply.setIntegerData(recordCount);
        return dataReply;
    }

    private DataReply getRecordTypeCategory(int recordNumber) {
        DataReply dataReply = new DataReply();
        int type = UNKNOWN_TYPE;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                            int tnfType = record.getTnf();
                            switch (tnfType) {
                                case NdefRecord.TNF_EMPTY:
                                    type = EMPTY_TYPE;
                                    break;
                                case NdefRecord.TNF_WELL_KNOWN:
                                    if (Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                                        type = URI_TYPE;
                                    } else if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                                        type = TEXT_TYPE;
                                    } else {
                                        type = UNSUPPORTED_TYPE;
                                    }
                                    break;
                                case NdefRecord.TNF_ABSOLUTE_URI:
                                    type = ABSOLUTE_URI_TYPE;
                                    break;
                                case NdefRecord.TNF_EXTERNAL_TYPE:
                                    type = EXTERNAL_TYPE;
                                    break;
                                case NdefRecord.TNF_MIME_MEDIA:
                                    type = MIME_MEDIA_TYPE;
                                    break;
                                case NdefRecord.TNF_UNCHANGED:
                                    type = UNCHANGED_TYPE;
                                    break;
                                case NdefRecord.TNF_UNKNOWN:
                                    type = UNKNOWN_TYPE;
                                    break;
                                default:
                                    type = UNKNOWN_TYPE;
                                    break;
                            }
                        } else {
                            dataReply.setError(RECORD_NOT_FOUND);
                            return dataReply;
                        }
                    else {
                        dataReply.setError(RECORD_NOT_FOUND);
                        return dataReply;
                    }
                } else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataReply.setIntegerData(type);
        return dataReply;
    }

    private String getRecordTypeCategoryAsString(int recordNumber) {
        DataReply typeInteger = getRecordTypeCategory(recordNumber);
        String typeString = "UNKNOWN_TYPE";
        if (!typeInteger.hasError())
            switch (typeInteger.getIntegerData()) {
                case UNKNOWN_TYPE:
                    typeString = "UNKNOWN_TYPE";
                    break;
                case EMPTY_TYPE:
                    typeString = "EMPTY_TYPE";
                    break;
                case EXTERNAL_TYPE:
                    typeString = "EXTERNAL_TYPE";
                    break;
                case MIME_MEDIA_TYPE:
                    typeString = "MIME_MEDIA_TYPE";
                    break;
                case UNCHANGED_TYPE:
                    typeString = "UNCHANGED_TYPE";
                    break;
                case ABSOLUTE_URI_TYPE:
                    typeString = "ABSOLUTE_URI_TYPE";
                    break;
                case TEXT_TYPE:
                    typeString = "TEXT_TYPE";
                    break;
                case URI_TYPE:
                    typeString = "URI_TYPE";
                    break;
                case UNSUPPORTED_TYPE:
                    typeString = "UNSUPPORTED_TYPE";
                    break;
            }

        return typeString;
    }

    private DataReply getNdefRecordTypeLength(int recordNumber) {
        DataReply dataReply = new DataReply();
        int length = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                            length = record.getType().length;
                        } else {
                            dataReply.setError(RECORD_NOT_FOUND);
                            return dataReply;
                        }
                    else {
                        dataReply.setError(RECORD_NOT_FOUND);
                        return dataReply;
                    }
                } else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataReply.setIntegerData(length);
        return dataReply;
    }

    private DataReply getNdefRecordDataLength(int recordNumber) {
        DataReply dataReply = new DataReply();
        int length = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                            length = record.getPayload().length;
                        } else {
                            dataReply.setError(RECORD_NOT_FOUND);
                            return dataReply;
                        }
                    else {
                        dataReply.setError(RECORD_NOT_FOUND);
                        return dataReply;
                    }
                } else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        dataReply.setIntegerData(length);
        return dataReply;
    }

    private DataReply readNdefRecordType(int recordNumber, int memoryIndex, int dataLength,int maxDataLength) {
        DataReply dataReply = new DataReply();
        String dataString = "";
        if (dataLength > maxDataLength) dataLength = maxDataLength;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getType());
                        } else {
                            dataReply.setError(RECORD_NOT_FOUND);
                            return dataReply;
                        }
                    else {
                        dataReply.setError(RECORD_NOT_FOUND);
                        return dataReply;
                    }
                } else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] data = dataString.getBytes();
        if (memoryIndex < data.length && data.length > 0 && dataLength > 0) {
            if (dataLength <= data.length - memoryIndex) {
                dataReply.setBytesData(Arrays.copyOfRange(data, memoryIndex, memoryIndex + dataLength));
                return dataReply;
            } else {
                dataReply.setError(NO_ENOUGH_BYTES);
                dataReply.setBytesData(Arrays.copyOfRange(data, memoryIndex, data.length));
                return dataReply;
            }
        } else {
            dataReply.setError(INDEX_OUT_OF_BOUNDS);
            return dataReply;
        }
    }

    private DataReply readNdefRecordData(int recordNumber, int memoryIndex, int dataLength, int maxDataLength) {
        DataReply dataReply = new DataReply();
        String dataString = "";
        if (dataLength > maxDataLength) dataLength = maxDataLength;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getPayload());
                        } else {
                            dataReply.setError(RECORD_NOT_FOUND);
                            return dataReply;
                        }
                    else {
                        dataReply.setError(RECORD_NOT_FOUND);
                        return dataReply;
                    }
                } else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] data = dataString.getBytes();
        if (memoryIndex < data.length && data.length > 0 && dataLength > 0) {
            if (dataLength <= data.length - memoryIndex) {
                dataReply.setBytesData(Arrays.copyOfRange(data, memoryIndex, memoryIndex + dataLength));
                return dataReply;
            } else {
                dataReply.setError(NO_ENOUGH_BYTES);
                dataReply.setBytesData(Arrays.copyOfRange(data, memoryIndex, data.length));
                return dataReply;
            }
        } else {
            dataReply.setError(INDEX_OUT_OF_BOUNDS);
            return dataReply;
        }
    }

    private DataReply readNdefRecordParsedData(int recordNumber, int memoryIndex, int dataLength, int maxDataLength) {
        DataReply dataReply = new DataReply();
        String dataString = "";
        if (dataLength > maxDataLength) dataLength = maxDataLength;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            NdefMessage message = ndef.getCachedNdefMessage();
                            try {
                                NdefRecord record = message.getRecords()[recordNumber];
                                short tnf = record.getTnf();

                                if (tnf == NdefRecord.TNF_WELL_KNOWN) {
                                    String type;
                                    if (Arrays.equals(record.getType(), NdefRecord.RTD_URI))
                                        type = "Uri";
                                    else if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
                                        type = "Text";
                                    else {
                                        dataReply.setError(RECORD_CAN_NOT_BE_PARSED);
                                        return dataReply;
                                    }

                                    if (type == "Text")
                                        dataString = parseTextNdefRecord(record);
                                    else if (type == "Uri") {
                                        if (Integer.valueOf(record.getPayload()[0]) < UriTypes.length)
                                            dataString = UriTypes[record.getPayload()[0]] + new String(record.getPayload()).substring(1);
                                        else {
                                            dataReply.setError(RECORD_CAN_NOT_BE_PARSED);
                                            return dataReply;
                                        }
                                    } else {
                                        dataReply.setError(RECORD_CAN_NOT_BE_PARSED);
                                        return dataReply;
                                    }
                                } else {
                                    dataReply.setError(RECORD_CAN_NOT_BE_PARSED);
                                    return dataReply;
                                }
                            } catch (UnsupportedEncodingException e) {
                                dataReply.setError(RECORD_CAN_NOT_BE_PARSED);
                                return dataReply;
                                //e.printStackTrace();
                            }
                        } else {
                            dataReply.setError(RECORD_NOT_FOUND);
                            return dataReply;
                        }
                    else {
                        dataReply.setError(RECORD_NOT_FOUND);
                        return dataReply;
                    }
                } else {
                    dataReply.setError(TAG_READING_ERROR);
                    return dataReply;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] data = dataString.getBytes();
        if (memoryIndex < data.length && data.length > 0 && dataLength > 0) {
            if (dataLength <= data.length - memoryIndex) {
                dataReply.setBytesData(Arrays.copyOfRange(data, memoryIndex, memoryIndex + dataLength));
                return dataReply;
            } else {
                dataReply.setError(NO_ENOUGH_BYTES);
                dataReply.setBytesData(Arrays.copyOfRange(data, memoryIndex, data.length));
                return dataReply;
            }
        } else {
            dataReply.setError(INDEX_OUT_OF_BOUNDS);
            return dataReply;
        }
    }

    private boolean getRecordParsableState(int recordNumber) {
        DataReply recordTypeCategory = getRecordTypeCategory(recordNumber);
        if (!recordTypeCategory.hasError()) {
            if (recordTypeCategory.getIntegerData() == URI_TYPE | recordTypeCategory.getIntegerData() == TEXT_TYPE)
                return true;
            return false;
        }else {
            return false;
        }
    }

    private String parseTextNdefRecord(NdefRecord ndefRecord) throws UnsupportedEncodingException {
        byte[] payload = ndefRecord.getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8":"UTF-16";
        int languageCodeLength = payload[0] & 0063;
        String plainData = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        return plainData;
    }

    private String convertByteArrayToHexString(byte[] message) {
        StringBuilder hexMessage = new StringBuilder();
        for (byte b : message) {
            hexMessage.append("0x" + String.format("%02X ", b));
        }
        return new String(hexMessage);
    }

    private String parsedPrintedText(String text) {
        //replace all unprintable chars with printable one
        for (int i = 0; i < 32; i++) {
            text = text.replace((char) i, '\ufffd');
        }
        return text;
    }

    private void sendError(byte errorCode,boolean isQueued) {
        ShieldFrame sf = new ShieldFrame(SHIELD_ID, TAG_ERROR_FRAME);
        sf.addArgument(errorCode);
        if(isQueued)
            queueShieldFrame(sf);
        else
            sendShieldFrame(sf, true);
    }

    private void sendError(byte errorCode) {
        sendError(errorCode,false);
    }
    private ArrayList<ArrayList<String>> generateArrayListForDisplay() {
        ArrayList<ArrayList<String>> parentArrayList = new ArrayList<ArrayList<String>>();
        ArrayList<String> childArrayList = new ArrayList<String>();
        DataReply recordCount = getNdefRecordCount();
        if (recordCount.getIntegerData() > 0 && (!recordCount.hasError() || recordCount.getError() == INDEX_OUT_OF_BOUNDS) ) {
            for (int childCount = 0; childCount < recordCount.getIntegerData(); childCount++) {
                String recordTypeCategory = getRecordTypeCategoryAsString(childCount);
                DataReply ndefRecordTypeLength = getNdefRecordTypeLength(childCount);
                DataReply ndefRecordType = readNdefRecordType(childCount, 0, ndefRecordTypeLength.getIntegerData(),ndefRecordTypeLength.getIntegerData());
                DataReply ndefRecordDataLength = getNdefRecordDataLength(childCount);
                DataReply ndefRecordData = readNdefRecordData(childCount, 0, ndefRecordDataLength.getIntegerData(),ndefRecordDataLength.getIntegerData());

                boolean recordParsableState = getRecordParsableState(childCount);
                DataReply ndefRecordParsedData = null;
                if (recordParsableState) {
                    ndefRecordParsedData = readNdefRecordParsedData(childCount, 0, ndefRecordDataLength.getIntegerData(),ndefRecordDataLength.getIntegerData());
                }

                if (recordTypeCategory == null || ndefRecordTypeLength.hasError() || (ndefRecordType.hasError() && ndefRecordType.getError() != NO_ENOUGH_BYTES) || ndefRecordDataLength.hasError() || (ndefRecordData.hasError() && ndefRecordData.getError() != NO_ENOUGH_BYTES) || (recordParsableState && ndefRecordParsedData.hasError() && ndefRecordParsedData.getError() != NO_ENOUGH_BYTES)) {
                    return null;
                }

                childArrayList.add("Type Category: " + recordTypeCategory);
                childArrayList.add("Type Size: " + String.valueOf(ndefRecordTypeLength.getIntegerData()));
                childArrayList.add("Type Raw:\n" + convertByteArrayToHexString(ndefRecordType.getBytesData()));
                childArrayList.add("Type:\n" + parsedPrintedText(new String(ndefRecordType.getBytesData())));
                childArrayList.add("Data Size: " + String.valueOf(ndefRecordDataLength.getIntegerData()));
                childArrayList.add("Data Raw:\n" + convertByteArrayToHexString(ndefRecordData.getBytesData()));
                childArrayList.add("Data:\n" + parsedPrintedText(new String(ndefRecordData.getBytesData())));
                if (recordParsableState && ndefRecordParsedData != null) {
                    childArrayList.add("Is Data Parsable: " + "true");
                    childArrayList.add("Parsed Data: " + parsedPrintedText(new String(ndefRecordParsedData.getBytesData())));
                } else
                    childArrayList.add("Is Data Parsable: " + "false");

                parentArrayList.add(childArrayList);
                childArrayList = new ArrayList<String>();
            }
            return parentArrayList;
        }else {
            return null;
        }
    }


    private class DataReply {
        private byte error = 0;
        private byte[] bytesData = new byte[0];
        private int integerData = 0;

        public boolean hasError(){
            if (error == 0)
                return false;
            return true;
        }

        public void setError(byte error) {
            this.error = error;
        }

        public void setIntegerData(int integerData) {
            this.integerData = integerData;
        }

        public void setBytesData(byte[] bytesData) {
            this.bytesData = bytesData;
        }

        public byte getError() {
            return error;
        }

        public int getIntegerData() {
            return integerData;
        }

        public byte[] getBytesData() {
            return bytesData;
        }
    }

}
