package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
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

import com.integreight.firmatabluetooth.ShieldFrame;
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
                if (isToastable) {
                    activity.showToast(nfcAdapter == null ? "Device doesn't support NFC!" : "Please, Enable Your NFC");
                }
                selectionAction.onFailure();
            }
        } else {
            if (isToastable)
                activity.showToast("Device doesn't support NFC!");
            selectionAction.onFailure();
        }
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == SHIELD_ID) {
            if (frame.getArguments() != null && frame.getArguments().size() > 0) {
                int record, start, size;
                byte[] data;
                ShieldFrame sf;
                if (isNdef_Flag) {
                    switch (frame.getFunctionId()) {
                        case RECORD_QUERY_DATA:
                            record = frame.getArgumentAsInteger(1, 0);
                            start = frame.getArgumentAsInteger(2, 1);
                            size = frame.getArgumentAsInteger(1, 2);
                            data = readNdefRecordData(record, start, size, true);
                            if (data != null) {
                                sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_DATA_FRAME);
                                sf.addIntegerArgument(1, record);
                                sf.addArgument(data);
                                sendShieldFrame(sf, true);
                            }
                            break;
                        case RECORD_QUERY_PARSED_DATA:
                            record = frame.getArgumentAsInteger(1, 0);
                            data = readNdefRecordParsedData(record, 0, 255, true);
                            if (data != null) {
                                sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_PARSED_DATA_FRAME);
                                sf.addIntegerArgument(1, record);
                                sf.addArgument(data);
                                sendShieldFrame(sf, true);
                            }
                            break;
                        case RECORD_QUERY_TYPE:
                            record = frame.getArgumentAsInteger(1, 0);
                            start = frame.getArgumentAsInteger(2, 1);
                            size = frame.getArgumentAsInteger(1, 2);
                            data = readNdefRecordType(record, start, size, true);
                            if (data != null) {
                                sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_TYPE_FRAME);
                                sf.addIntegerArgument(1, record);
                                sf.addArgument(data);
                                sendShieldFrame(sf, true);
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    sendError(RECORD_NOT_FOUND);
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
                    if (getNdefMaxSize() > 0 && getTagId().length > 0) {
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
                                    if (getNdefMaxSize() > 0 && getTagId().length > 0) {
                                        isNdef_Flag = false;
                                        isTagSupported = true;
                                        int recordCount = getNdefRecordCount();
                                        if (recordCount > 0 && recordCount < 256) {
                                            isNdef_Flag = true;
                                            displayData();
                                            sendNewTagFrame();
                                        }else if (recordCount == 0){
                                            sendNewEmptyTagFrame();
                                        }else if(recordCount > 256){
                                            sendError(INDEX_OUT_OF_BOUNDS);
                                        }else {
                                            sendError(TAG_READING_ERROR);
                                        }
                                    } else {
                                        sendError(TAG_READING_ERROR);
                                    }
                                } else if (NdefFormatable.class.getName().equals(tech)) {
                                    if (getNdefMaxSize() > 0 && getTagId().length > 0) {
                                        isNdef_Flag = false;
                                        isTagSupported = true;
                                        sendNewEmptyTagFrame();
                                        displayData();
                                    } else {
                                        sendError(TAG_READING_ERROR);
                                    }
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
            if (isTagSupported && currentTag != null) {
                byte[] tagId = getTagId();
                int ndefMaxSize = getNdefMaxSize();
                int ndefUsedSize = getNdefUsedSize();
                ArrayList<ArrayList<String>> arrayListForDisplay = generateArrayListForDisplay();
                if (tagId.length > 0 && ndefMaxSize > 0 && ndefUsedSize > 0 && arrayListForDisplay != null)
                    eventHandler.ReadNdef(convertByteArrayToHexString(tagId), ndefMaxSize, ndefUsedSize, arrayListForDisplay);
            }
    }

    private void sendNewTagFrame() {
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {

                boolean dataReadingError = false;

                byte[] tagId = getTagId();
                int maxSize = getNdefMaxSize();
                int recordCount = getNdefRecordCount();
                int usedSize = getNdefUsedSize();
                byte[][] records = new byte[recordCount][5];

                for (int i = 0; i < recordCount; i++) {
                    int recordTypeCategory = getRecordTypeCategory(i);
                    int recordTypeLength = getNdefRecordTypeLength(i);
                    int recordDataLength = getNdefRecordDataLength(i);
                    if (recordTypeCategory < 0 || recordTypeLength < 0 || recordDataLength < 0){
                        dataReadingError = true;
                    }else {
                        records[i][0] = (byte) recordTypeCategory;
                        records[i][1] = (byte) recordTypeLength;
                        records[i][2] = (byte) (recordTypeLength >> 8);
                        records[i][3] = (byte) recordDataLength;
                        records[i][4] = (byte) (recordDataLength >> 8);
                    }
                }
                if (maxSize <= 0 || recordCount <= 0 || usedSize <= 0 || tagId.length <= 0 || records.length != recordCount){
                    dataReadingError = true;
                }else if(recordCount > 256){
                    sendError(INDEX_OUT_OF_BOUNDS);
                    return;
                }

                if (!dataReadingError) {
                    ShieldFrame sf = new ShieldFrame(SHIELD_ID, NEW_TAG_FRAME);

                    sf.addArgument(tagId);
                    sf.addIntegerArgument(2, maxSize);
                    sf.addIntegerArgument(1, recordCount);
                    sf.addIntegerArgument(2, usedSize);

                    for (int i = 0; i < recordCount; i++) {
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
                byte[] tagId = getTagId();
                if (tagId.length > 0) {
                    ShieldFrame sf = new ShieldFrame(SHIELD_ID, NEW_TAG_FRAME);
                    sf.addArgument(tagId);
                    sf.addIntegerArgument(2, 0);
                    sf.addIntegerArgument(1, 0);
                    sf.addIntegerArgument(2, 0);
                    sendShieldFrame(sf, true);
                }else {
                    sendError(TAG_READING_ERROR);
                    return;
                }
            }
        }
    }

    private byte[] getTagId() {
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                byte[] currentTagId = currentTag.getId();
                if (currentTagId != null) {
                    if (currentTagId.length == 0)
                        currentTagId = new byte[]{0x00};
                    return currentTagId;
                }
            }
        }
        return new byte[0];
    }

    private int getNdefUsedSize() {
        int size = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null && ndef.getCachedNdefMessage() != null)
                    size = ndef.getCachedNdefMessage().toByteArray().length;
                else
                    size = -1*TAG_READING_ERROR;
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                }
            }
        }
        return size;
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

    private int getNdefMaxSize() {
        int maxSize = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    maxSize = ndef.getMaxSize();
                    if (maxSize < 0){
                        return -1*TAG_READING_ERROR;
                    }
                }else {
                    return -1*TAG_READING_ERROR;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return maxSize;
    }

    private int getNdefRecordCount() {
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
                    //sendError(TAG_READING_ERROR);
                    return -1*TAG_READING_ERROR;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /*if (recordCount > 256) {
            sendError(INDEX_OUT_OF_BOUNDS);
            recordCount = 256;
        }*/
        return recordCount;
    }

    private int getRecordTypeCategory(int recordNumber) {
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
                            }
                        } else {
                            //sendError(RECORD_NOT_FOUND);
                            return -1*RECORD_NOT_FOUND;
                        }
                    else {
                        //sendError(RECORD_NOT_FOUND);
                        return -1*RECORD_NOT_FOUND;
                    }
                } else {
                    //sendError(TAG_READING_ERROR);
                    return -1*TAG_READING_ERROR;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return type;
    }

    private String getRecordTypeCategoryAsString(int recordNumber) {
        int typeInteger = getRecordTypeCategory(recordNumber);
        String typeString = "UNKNOWN_TYPE";
        switch (typeInteger) {
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
            default:
                typeString = null;
                break;
        }
        return typeString;
    }

    private int getNdefRecordTypeLength(int recordNumber) {
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
                            //sendError(RECORD_NOT_FOUND);
                            return -1*RECORD_NOT_FOUND;
                        }
                    else {
                        //sendError(RECORD_NOT_FOUND);
                        return -1*RECORD_NOT_FOUND;
                    }
                } else {
                    //sendError(TAG_READING_ERROR);
                    return -1*TAG_READING_ERROR;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return length;
    }

    private int getNdefRecordDataLength(int recordNumber) {
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
                            //sendError(RECORD_NOT_FOUND);
                            return -1*RECORD_NOT_FOUND;
                        }
                    else {
                        //sendError(RECORD_NOT_FOUND);
                        return -1*RECORD_NOT_FOUND;
                    }
                } else {
                    //sendError(TAG_READING_ERROR);
                    return -1*TAG_READING_ERROR;
                }
                try {
                    if (ndef != null) ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return length;
    }

    private byte[] readNdefRecordType(int recordNumber, int memoryIndex, int dataLength, boolean sendErrorFrames) {
        String dataString = "";
        if (dataLength > 255) dataLength = 255;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getType());
                        } else {
                            if (sendErrorFrames) sendError(RECORD_NOT_FOUND);
                            return null;
                        }
                    else {
                        if (sendErrorFrames) sendError(RECORD_NOT_FOUND);
                        return null;
                    }
                } else {
                    if (sendErrorFrames) sendError(TAG_READING_ERROR);
                    return null;
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
                return Arrays.copyOfRange(data, memoryIndex, memoryIndex + dataLength);
            } else {
                if (sendErrorFrames) sendError(NO_ENOUGH_BYTES);
                return data;
            }
        } else {
            if (sendErrorFrames) sendError(INDEX_OUT_OF_BOUNDS);
            return null;
        }
    }

    private byte[] readNdefRecordData(int recordNumber, int memoryIndex, int dataLength, boolean sendErrorFrames) {
        String dataString = "";
        if (dataLength > 255) dataLength = 255;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getPayload());
                        } else {
                            if (sendErrorFrames) sendError(RECORD_NOT_FOUND);
                            return null;
                        }
                    else {
                        if (sendErrorFrames) sendError(RECORD_NOT_FOUND);
                        return null;
                    }
                } else {
                    if (sendErrorFrames) sendError(TAG_READING_ERROR);
                    return null;
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
                return Arrays.copyOfRange(data, memoryIndex, memoryIndex + dataLength);
            } else {
                if (sendErrorFrames) sendError(NO_ENOUGH_BYTES);
                return data;
            }
        } else {
            if (sendErrorFrames) sendError(INDEX_OUT_OF_BOUNDS);
            return null;
        }
    }

    private byte[] readNdefRecordParsedData(int recordNumber, int memoryIndex, int dataLength, boolean sendErrorFrames) {
        String dataString = "";
        if (dataLength > 255) dataLength = 255;
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
                                        if (sendErrorFrames) sendError(RECORD_CAN_NOT_BE_PARSED);
                                        return null;
                                    }

                                    if (type == "Text")
                                        dataString = parseTextNdefRecord(record);
                                    else if (type == "Uri") {
                                        if (Integer.valueOf(record.getPayload()[0]) < UriTypes.length)
                                            dataString = UriTypes[record.getPayload()[0]] + new String(record.getPayload()).substring(1);
                                        else {
                                            if (sendErrorFrames) sendError(RECORD_CAN_NOT_BE_PARSED);
                                            return null;
                                        }
                                    } else {
                                        if (sendErrorFrames) sendError(RECORD_CAN_NOT_BE_PARSED);
                                        return null;
                                    }
                                } else {
                                    if (sendErrorFrames) sendError(RECORD_CAN_NOT_BE_PARSED);
                                    return null;
                                }
                            } catch (UnsupportedEncodingException e) {
                                if (sendErrorFrames) sendError(RECORD_CAN_NOT_BE_PARSED);
                                return null;
                                //e.printStackTrace();
                            }
                        } else {
                            if (sendErrorFrames) sendError(RECORD_NOT_FOUND);
                            return null;
                        }
                    else {
                        if (sendErrorFrames) sendError(RECORD_NOT_FOUND);
                        return null;
                    }
                } else {
                    if (sendErrorFrames) sendError(TAG_READING_ERROR);
                    return null;
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
            return Arrays.copyOfRange(data, memoryIndex, memoryIndex + data.length);
        } else {
            if (sendErrorFrames) sendError(INDEX_OUT_OF_BOUNDS);
            return null;
        }
    }

    private String parseTextNdefRecord(NdefRecord ndefRecord) throws UnsupportedEncodingException {
        byte[] payload = ndefRecord.getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;
        String plainData = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        return plainData;
    }

    private boolean getRecordParsableState(int recordNumber) {
        int recordTypeCategory = getRecordTypeCategory(recordNumber);
        if (recordTypeCategory > 0) {
            if (recordTypeCategory == URI_TYPE | recordTypeCategory == TEXT_TYPE)
                return true;
            return false;
        }else {
            return false;
        }
    }

    private void sendError(byte errorCode) {
        ShieldFrame sf = new ShieldFrame(SHIELD_ID, TAG_ERROR_FRAME);
        sf.addByteArgument(errorCode);
        sendShieldFrame(sf, true);
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

    private ArrayList<ArrayList<String>> generateArrayListForDisplay() {
        ArrayList<ArrayList<String>> parentArrayList = new ArrayList<ArrayList<String>>();
        ArrayList<String> childArrayList = new ArrayList<String>();
        int recordCount = getNdefRecordCount();
        if (recordCount >0 && recordCount < 256) {
            for (int childCount = 0; childCount < getNdefRecordCount(); childCount++) {
                String recordTypeCategory = getRecordTypeCategoryAsString(childCount);
                int ndefRecordTypeLength = getNdefRecordTypeLength(childCount);
                byte[] ndefRecordType = readNdefRecordType(childCount, 0, ndefRecordTypeLength, false);
                int ndefRecordDataLength = getNdefRecordDataLength(childCount);
                byte[] ndefRecordData = readNdefRecordData(childCount, 0, ndefRecordDataLength, false);
                boolean recordParsableState = getRecordParsableState(childCount);
                byte[] ndefRecordParsedData = null;
                if (recordParsableState) {
                    ndefRecordParsedData = readNdefRecordParsedData(childCount, 0, ndefRecordDataLength, false);
                }

                if (recordTypeCategory == null || ndefRecordTypeLength < 0 || ndefRecordType == null || ndefRecordDataLength < 0 || ndefRecordData == null || (recordParsableState && ndefRecordParsedData == null)) {
                    return null;
                }

                childArrayList.add("Type Category: " + recordTypeCategory);
                childArrayList.add("Type Size: " + String.valueOf(ndefRecordTypeLength));
                childArrayList.add("Type Raw:\n" + convertByteArrayToHexString(ndefRecordType));
                childArrayList.add("Type:\n" + parsedPrintedText(new String(ndefRecordType)));
                childArrayList.add("Data Size: " + String.valueOf(ndefRecordDataLength));
                childArrayList.add("Data Raw:\n" + convertByteArrayToHexString(ndefRecordData));
                childArrayList.add("Data:\n" + parsedPrintedText(new String(ndefRecordData)));
                if (recordParsableState && ndefRecordParsedData != null) {
                    childArrayList.add("Is Data Parsable: " + "true");
                    childArrayList.add("Parsed Data: " + parsedPrintedText(new String(ndefRecordParsedData)));
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
}
