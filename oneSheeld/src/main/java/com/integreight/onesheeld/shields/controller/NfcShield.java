package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
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

    private static final byte RECORD_QUERY_DATA = 0x11;
    private static final byte RECORD_QUERY_PARSED_DATA = 0x12;
    private static final byte RECORD_QUERY_TYPE = 0x14;

    private NFCEventHandler eventHandler;
    //private ShieldFrame sf;

    private Tag currentTag;
    private boolean isNdef_Flag = false;

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
        //return super.invalidate(selectionAction,registerNFCListener(isToastable));
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
        if (frame.getShieldId() == UIShield.NFC_SHIELD.getId()) {
            if (frame.getArguments() != null && frame.getArguments().size() > 0){
                int record,start,size;
                String data;
                ShieldFrame sf;
                switch (frame.getFunctionId()) {
                    case RECORD_QUERY_DATA:
                        record = frame.getArgumentAsInteger(1,0);
                        start = frame.getArgumentAsInteger(2,1);
                        size = frame.getArgumentAsInteger(1,2);
                        data = readNdefRecordData(record,start,size);
                        sf = new ShieldFrame((byte) 0x16, (byte) 0x00, (byte) 0x12);
                        sf.addIntegerArgument(1,false,record);
                        sf.addStringArgument(data);
                        sendShieldFrame(sf);
                        break;
                    case RECORD_QUERY_PARSED_DATA:
                        record = frame.getArgumentAsInteger(1,0);
                        data = readNdefRecordParsedData(record,0,256);
                        sf = new ShieldFrame((byte) 0x16, (byte) 0x00, (byte) 0x12);
                        sf.addIntegerArgument(1,false,record);
                        sf.addStringArgument(data);
                        sendShieldFrame(sf);
                        break;
                    case RECORD_QUERY_TYPE:
                        record = frame.getArgumentAsInteger(1,0);
                        start = frame.getArgumentAsInteger(2,1);
                        size = frame.getArgumentAsInteger(1,2);
                        data = readNdefRecordType(record,start,size);
                        sf = new ShieldFrame((byte) 0x16, (byte) 0x00, (byte) 0x12);
                        sf.addIntegerArgument(1,false,record);
                        sf.addStringArgument(data);
                        sendShieldFrame(sf);
                        break;
                    default:
                        break;
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
        stopForegroundDispatch(activity);
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
        isNdef_Flag = false;
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        currentTag = tag;
        resetTechnologyFlags();
        switch (action) {
            case NfcAdapter.ACTION_NDEF_DISCOVERED:
                setCurrentTag(tag);
                isNdef_Flag = true;
                break;
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                setCurrentTag(tag);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    String[] techList = tag.getTechList();
                    for (String tech : techList) {
                        if (Ndef.class.getName().equals(tech)) {
                            isNdef_Flag = true;
                            Display();
                            sendNewTagFrame();
                        }
                    }
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
            eventHandler.ReadNdef(readNdefRecordData(i, 0, 64).getBytes());
            Toast.makeText(getActivity().getApplicationContext(),new String(readNdefRecordData(i, 0, 64)),Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNewTagFrame(){
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                ShieldFrame sf = new ShieldFrame((byte) 0x16, (byte) 0x00, (byte) 0x01);
                sf.addArgument(currentTag.getId());
                sf.addIntegerArgument(2, false, getNdefMaxSize());
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
                sendShieldFrame(sf);
            }
        }
    }

    private int getNdefUsedSize(){
        int size = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                size = ndef.getCachedNdefMessage().toByteArray().length;
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
            if (nfcAdapter != null)
                nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
        }
    }

    public void stopForegroundDispatch(final Activity Mactivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            if (nfcAdapter != null)
                nfcAdapter.disableForegroundDispatch(activity);
        }

    }

    private int getNdefMaxSize(){
        int maxSize = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                maxSize = ndef.getMaxSize();
            }
        }
        return maxSize;
    }

    private int getNdefRecordCount(){
        int recordCount = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                recordCount = ndef.getCachedNdefMessage().getRecords().length;
            }
        }
        return recordCount;
    }

    private int getNdefRecordType(int recordNumber){
        int type = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                int tnfType = record.getTnf();
                switch (tnfType) {
                    case NdefRecord.TNF_EMPTY:
                        type = 1;
                        break;
                    case NdefRecord.TNF_WELL_KNOWN:
                        if (Arrays.equals(record.getType(), NdefRecord.RTD_URI)){
                            type = 7;
                        }else if(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)){
                            type = 7;
                        }else{
                            type = 9;
                        }
                        break;
                    case NdefRecord.TNF_ABSOLUTE_URI:
                        type = 3;
                        break;
                    case NdefRecord.TNF_EXTERNAL_TYPE:
                        type = 4;
                        break;
                    case NdefRecord.TNF_MIME_MEDIA:
                        type = 5;
                        break;
                    case NdefRecord.TNF_UNCHANGED:
                        type = 6;
                        break;
                    case NdefRecord.TNF_UNKNOWN:
                        type = 7;
                        break;
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
                NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                return record.getType().length;
            }
        }
        return length;
    }

    private int getNdefRecordDataLength(int recordNumber){
        int length = 0;
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                NdefRecord record = ndef.getCachedNdefMessage().getRecords()[recordNumber];
                return record.getPayload().length;
            }
        }
        return length;
    }

    private String readNdefRecordType(int recordNumber,int memoryIndex,int dataLength) {
        String dataString = "";
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getType());
            }
        }
        if(dataLength < dataString.length()-memoryIndex) {
            return dataString.substring(memoryIndex, memoryIndex + dataLength);
        }else{
            return dataString.substring(memoryIndex);
        }
    }

    private String readNdefRecordData(int recordNumber,int memoryIndex,int dataLength) {
        String dataString = "";
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
                dataString = new String(ndef.getCachedNdefMessage().getRecords()[recordNumber].getPayload());
            }
        }
        if(dataLength < dataString.length()-memoryIndex) {
            return dataString.substring(memoryIndex, memoryIndex + dataLength);
        }else{
            return dataString.substring(memoryIndex);
        }
    }

    private String readNdefRecordParsedData(int recordNumber,int memoryIndex,int dataLength) {
        String dataString = "";
        if (currentTag != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                Ndef ndef = Ndef.get(currentTag);
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
                        else
                            return null;

                        if (type == "Text")
                            dataString = parseTextNdefRecord(record);
                        else if (type == "Uri"){
                            dataString = UriTypes[record.getPayload()[0]] + new String(record.getPayload()).substring(1);
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            }
        }
        if(dataLength < dataString.length()-memoryIndex) {
            return dataString.substring(memoryIndex, memoryIndex + dataLength);
        }else{
            return dataString.substring(memoryIndex);
        }
    }

    private String parseTextNdefRecord(NdefRecord ndefRecord) throws UnsupportedEncodingException {
        byte[] payload = ndefRecord.getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-6";
        int languageCodeLength = payload[0] & 0063;
        String plainData = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        return plainData;
    }

}
