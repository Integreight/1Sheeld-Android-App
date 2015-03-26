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
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Mouso on 3/11/2015.
 */
public class NfcShield extends ControllerParent<NfcShield>{

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

    private static final byte TAG_NOT_SUPPORTED = 0x00;
    private static final byte RECORD_CAN_NOT_BE_PARSED = 0x01;
    private static final byte INDEX_OUT_OF_BOUNDS = 0x02;
    private static final byte NO_ENOUGH_BYTES = 0x03;
    private static final byte TAG_READING_ERROR = 0x04;
    private static final byte RECORD_NOT_FOUND = 0x05;

    private NFCEventHandler eventHandler;

    private Tag currentTag;
    private boolean isNdef_Flag = false;
    private boolean isTagSupported = false;
    private boolean isForeground = false;

    private static final String[] UriTypes = {"","http://www.","https://www.","http://","https://","tel:","mailto:","ftp://anonymous:anonymous@","ftp://ftp.","ftps://","sftp://","smb://","nfs://","ftp://","dav://","news:","telnet://","imap:","rtsp://","urn:","pop:","sip:","sips:","tftp:","btspp://","btl2cap://","btgoep://","tcpobex://","irdaobex://","file://","urn:epc:id:","urn:epc:tag:","urn:epc:pat:","urn:epc:raw:","urn:epc:","urn:nfc:"};

    public NfcShield(){}

    public NfcShield(Activity activity,String tag){super(activity,tag);}

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
            if (nfcAdapter != null) {
                setupForegroundDispatch(activity);
                selectionAction.onSuccess();
            } else {
                if (isToastable)
                    activity.showToast("Device doesn't support NFC!");
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
            if (frame.getArguments() != null && frame.getArguments().size() > 0){
                int record,start,size;
                String data;
                ShieldFrame sf;
                if(isNdef_Flag) {
                    switch (frame.getFunctionId()) {
                        case RECORD_QUERY_DATA:
                            record = frame.getArgumentAsInteger(1, 0);
                            start = frame.getArgumentAsInteger(2, 1);
                            size = frame.getArgumentAsInteger(1, 2);
                            data = readNdefRecordData(record, start, size);
                            sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_DATA_FRAME);
                            sf.addIntegerArgument(1, false, record);
                            sf.addStringArgument(data);
                            sendShieldFrame(sf, true);
                            break;
                        case RECORD_QUERY_PARSED_DATA:
                            record = frame.getArgumentAsInteger(1, 0);
                            data = readNdefRecordParsedData(record, 0, 256);
                            sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_PARSED_DATA_FRAME);
                            sf.addIntegerArgument(1, false, record);
                            sf.addStringArgument(data);
                            sendShieldFrame(sf, true);
                            break;
                        case RECORD_QUERY_TYPE:
                            record = frame.getArgumentAsInteger(1, 0);
                            start = frame.getArgumentAsInteger(2, 1);
                            size = frame.getArgumentAsInteger(1, 2);
                            data = readNdefRecordType(record, start, size);
                            sf = new ShieldFrame(SHIELD_ID, RECORD_QUERY_TYPE_FRAME);
                            sf.addIntegerArgument(1, false, record);
                            sf.addStringArgument(data);
                            sendShieldFrame(sf, true);
                            break;
                        default:
                            break;
                    }
                }else{
                    sendError(RECORD_NOT_FOUND);
                }
                //if (eventHandler != null) {
                    //eventHandler.onNFCRx(outputTxt);
                //}
            }
        }
    }

    @Override
    public void reset() { }
    @Override
    public void resetThis() {
        if (isForeground)
            stopForegroundDispatch(activity);
        else {
            PackageManager packageManager = activity.getPackageManager();
            packageManager.setComponentEnabledSetting(new ComponentName("com.integreight.onesheeld", "com.integreight.onesheeld.NFCUtils-alias"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, activity.BIND_NOT_FOREGROUND);
        }
        super.resetThis();
    }

    public NFCEventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(NFCEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
    public static interface NFCEventHandler {
        void ReadNdef(byte[] data);
    }

    public void resetTechnologyFlags() {
        isTagSupported = false;
        isNdef_Flag = false;
    }

    public void handleIntent(Intent intent) {
        Toast.makeText(activity.getApplicationContext(),"Tag Recived.",Toast.LENGTH_SHORT).show();
        String action = intent.getAction();
        if(action==null)return;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null)
            sendError(TAG_READING_ERROR);

        currentTag = tag;
        resetTechnologyFlags();
        switch (action) {
            case NfcAdapter.ACTION_NDEF_DISCOVERED:
                setCurrentTag(tag);
                isTagSupported = true;
                isNdef_Flag = true;
                Display();
                sendNewTagFrame();
                break;
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                setCurrentTag(tag);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    String[] techList = tag.getTechList();
                    for (String tech : techList) {
                        if (!isTagSupported) {
                            if (Ndef.class.getName().equals(tech)) {
                                isNdef_Flag = false;
                                isTagSupported = true;
                                //Display();
                                if (getNdefRecordCount() == 0)
                                    sendNewEmptyTagFrame();
                                else {
                                    isNdef_Flag = true;
                                    sendNewTagFrame();
                                }
                            } else if (NdefFormatable.class.getName().equals(tech)) {
                                isNdef_Flag = false;
                                isTagSupported = true;
                                //Display();
                                sendNewEmptyTagFrame();
                            }
                        }else
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

    public void Display(){
        int recordCount = getNdefRecordCount();
        for(int i=0;i<recordCount;i++){
            if (eventHandler != null)
                eventHandler.ReadNdef(readNdefRecordData(i, 0, 64).getBytes());
            //Toast.makeText(getActivity().getApplicationContext(),new String(readNdefRecordData(i, 0, 64)),Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNewTagFrame(){
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                ShieldFrame sf = new ShieldFrame(SHIELD_ID,NEW_TAG_FRAME);
                byte[] currentTagId = currentTag.getId();
                if(currentTagId.length == 0)
                    currentTagId = new byte[]{0x00};

                sf.addArgument(currentTagId);
                sf.addIntegerArgument(2, false, getNdefMaxSize());
                sf.addIntegerArgument(1,false,getNdefRecordCount());
                sf.addIntegerArgument(2, false, getNdefUsedSize());

                int recordCount = getNdefRecordCount();
                for (int i = 0; i < recordCount; i++) {
                    byte[] recordByte = {0, 0, 0, 0, 0};
                    recordByte[0] = (byte) getNdefRecordType(i);
                    recordByte[1] = (byte) getNdefRecordTypeLength(i);
                    recordByte[2] = (byte) (getNdefRecordTypeLength(i) >> 8);
                    recordByte[3] = (byte) getNdefRecordDataLength(i);
                    recordByte[4] = (byte) (getNdefRecordDataLength(i) >> 8);
                    sf.addArgument(recordByte);
                }
                sendShieldFrame(sf,true);
            }
        }
    }

    private void sendNewEmptyTagFrame(){
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                ShieldFrame sf = new ShieldFrame(SHIELD_ID,NEW_TAG_FRAME);
                byte[] currentTagId = currentTag.getId();
                if(currentTagId.length == 0)
                    currentTagId = new byte[]{0x00};

                sf.addArgument(currentTagId);
                sf.addIntegerArgument(2, false, 0);
                sf.addIntegerArgument(1,false,0);
                sf.addIntegerArgument(2, false, 0);
                sendShieldFrame(sf,true);
            }
        }
    }

    private int getNdefUsedSize(){
        int size = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if(ndef.getCachedNdefMessage() != null)
                    size = ndef.getCachedNdefMessage().toByteArray().length;
                else
                    size = 0;
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return size;
    }

    public void setupForegroundDispatch(final Activity Mactivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1){
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

    public void stopForegroundDispatch(final Activity Mactivity) {
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

    private int getNdefMaxSize(){
        int maxSize = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    maxSize = ndef.getMaxSize();
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return maxSize;
    }

    private int getNdefRecordCount(){
        int recordCount = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);

                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        recordCount = ndef.getCachedNdefMessage().getRecords().length;
                    else
                        recordCount = 0;
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(recordCount > 256){
            sendError(INDEX_OUT_OF_BOUNDS);
            recordCount = 256;
        }
        return recordCount;
    }

    private int getNdefRecordType(int recordNumber){
        int type = UNKNOWN_TYPE;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if(ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
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
                        }else
                            sendError(RECORD_NOT_FOUND);
                    else
                        sendError(RECORD_NOT_FOUND);
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return type;
    }

    private int getNdefRecordTypeLength(int recordNumber){
        int length = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if(ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                            return record.getType().length;
                        }else
                            sendError(RECORD_NOT_FOUND);
                    else
                        sendError(RECORD_NOT_FOUND);
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return length;
    }

    private int getNdefRecordDataLength(int recordNumber){
        int length = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if(ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                            return record.getPayload().length;
                        }else
                            sendError(RECORD_NOT_FOUND);
                    else
                        sendError(RECORD_NOT_FOUND);
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return length;
    }

    private String readNdefRecordType(int recordNumber,int memoryIndex,int dataLength) {
        String dataString = "";
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if(ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getType());
                        }else
                            sendError(RECORD_NOT_FOUND);
                    else
                        sendError(RECORD_NOT_FOUND);
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (memoryIndex < dataString.length()) {
            if (dataLength < dataString.length() - memoryIndex) {
                return dataString.substring(memoryIndex, memoryIndex + dataLength);
            } else {
                sendError(NO_ENOUGH_BYTES);
                return dataString.substring(memoryIndex);
            }
        }else {
            sendError(INDEX_OUT_OF_BOUNDS);
            return "";
        }
    }

    private String readNdefRecordData(int recordNumber,int memoryIndex,int dataLength) {
        String dataString = "";
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getPayload());
                        }else
                            sendError(RECORD_NOT_FOUND);
                    else
                        sendError(RECORD_NOT_FOUND);
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (memoryIndex < dataString.length()) {
            if(dataLength < dataString.length()-memoryIndex) {
                return dataString.substring(memoryIndex, memoryIndex + dataLength);
            }else{
                sendError(NO_ENOUGH_BYTES);
                return dataString.substring(memoryIndex);
            }
        }else {
            sendError(INDEX_OUT_OF_BOUNDS);
            return "";
        }
    }

    private String readNdefRecordParsedData(int recordNumber,int memoryIndex,int dataLength) {
        String dataString = "";
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef != null) {
                    if (ndef.getCachedNdefMessage() != null)
                        if (ndef.getCachedNdefMessage().getRecords().length > recordNumber) {
                            NdefMessage message = ndef.getCachedNdefMessage();
                            try{
                                NdefRecord record = message.getRecords()[recordNumber];
                                short tnf = record.getTnf();

                                if (tnf == NdefRecord.TNF_WELL_KNOWN) {
                                    String type;
                                    if (Arrays.equals(record.getType(), NdefRecord.RTD_URI))
                                        type = "Uri";
                                    else if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
                                        type = "Text";
                                    else {
                                        sendError(RECORD_CAN_NOT_BE_PARSED);
                                        return "";
                                    }

                                    if (type == "Text")
                                        dataString = parseTextNdefRecord(record);
                                    else if (type == "Uri"){
                                        if(Integer.valueOf(record.getPayload()[0]) < UriTypes.length)
                                            dataString = UriTypes[record.getPayload()[0]] + new String(record.getPayload()).substring(1);
                                        else
                                            sendError(RECORD_CAN_NOT_BE_PARSED);
                                    }else{
                                        sendError(RECORD_CAN_NOT_BE_PARSED);
                                    }
                                }
                            } catch (UnsupportedEncodingException e) {
                                sendError(RECORD_CAN_NOT_BE_PARSED);
                                //e.printStackTrace();
                            }
                        }else
                            sendError(RECORD_NOT_FOUND);
                    else
                        sendError(RECORD_NOT_FOUND);
                }else
                    sendError(TAG_READING_ERROR);
                try {
                    ndef.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (memoryIndex < dataString.length()) {
            if(dataLength < dataString.length()-memoryIndex) {
                return dataString.substring(memoryIndex, memoryIndex + dataLength);
            }else{
                sendError(NO_ENOUGH_BYTES);
                return dataString.substring(memoryIndex);
            }
        }else {
            sendError(INDEX_OUT_OF_BOUNDS);
            return "";
        }
    }

    private String parseTextNdefRecord(NdefRecord ndefRecord) throws UnsupportedEncodingException {
        byte[] payload = ndefRecord.getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-6";
        int languageCodeLength = payload[0] & 0063;
        String plainData = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        return plainData;
    }

    private void sendError(byte errorCode){
        ShieldFrame sf = new ShieldFrame(SHIELD_ID,TAG_ERROR_FRAME);
        sf.addByteArgument(errorCode);
    }
}
