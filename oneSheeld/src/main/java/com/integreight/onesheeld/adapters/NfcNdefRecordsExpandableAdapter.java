package com.integreight.onesheeld.adapters;

import android.content.Context;
import android.nfc.NdefRecord;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.NfcShield;
import com.integreight.onesheeld.utils.Log;

import java.util.ArrayList;

/**
 * Created by Mouso on 4/23/2015.
 */
public class NfcNdefRecordsExpandableAdapter extends BaseExpandableListAdapter{

    private Context _context;
    private ArrayList<NdefRecord> _listNdefRecords;

    public NfcNdefRecordsExpandableAdapter(Context context,ArrayList<NdefRecord> listNdefRecords){
        this._context = context;
        this._listNdefRecords = listNdefRecords;
    }

    @Override
    public int getGroupCount() {
        return this._listNdefRecords.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
            boolean isParsable = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getRecordParsableState(groupPosition);
            if(isParsable) {
                return 9;
            }else {
                return 8;
            }
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listNdefRecords.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        switch (childPosition){
            case 0:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    String type = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getRecordTypeCategoryAsString(groupPosition);
                    return "Type Category: "+type;
                }else{
                    return "";
                }
            case 1:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    return "Type Size: "+String.valueOf(((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getNdefRecordTypeLength(groupPosition));
                }else{
                    return "";
                }
            case 2:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    int dataLength = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getNdefRecordTypeLength(groupPosition);
                    byte[] typeBytes =  ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).readNdefRecordType(groupPosition, 0, dataLength);
                    String typeHexString = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).convertByteArrayToHexString(typeBytes);
                    return "Type Raw: "+typeHexString;
                }else{
                    return "";
                }
            case 3:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    int dataLength = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getNdefRecordTypeLength(groupPosition);
                    String data = new String(((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).readNdefRecordType(groupPosition, 0, dataLength));
                    String printableData = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).parsedPrintedText(data);
                    return "Type:"+printableData;
                }else{
                    return "";
                }
            case 4:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    return "Data Size:"+String.valueOf(((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getNdefRecordDataLength(groupPosition));
                }else{
                    return "";
                }
            case 5:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    int dataLength = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getNdefRecordDataLength(groupPosition);
                    byte[] data =  ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).readNdefRecordData(groupPosition, 0, dataLength);
                    String DataHexString = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).convertByteArrayToHexString(data);
                    return "Data Raw: "+DataHexString;
                }else{
                    return "";
                }
            case 6:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    int dataLength = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getNdefRecordDataLength(groupPosition);
                    String data = new String(((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).readNdefRecordData(groupPosition, 0, dataLength));
                    String printableData = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).parsedPrintedText(data);
                    return "Data: " + printableData;
                }else{
                    return "";
                }
            case 7:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    boolean isParcelable = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getRecordParsableState(groupPosition);
                    if (isParcelable)
                        return "Is Data Parsable: "+"true";
                    else
                        return "Is Data Parsable: "+"false";
                }else{
                    return "";
                }
            case 8:
                if (((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name()))  != null) {
                    int dataLength = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).getNdefRecordDataLength(groupPosition);
                    String data = new String(((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).readNdefRecordParsedData(groupPosition, 0, 255));
                    String printableData = ((NfcShield) ((OneSheeldApplication) this._context.getApplicationContext()).getRunningShields().get(UIShield.NFC_SHIELD.name())).parsedPrintedText(data);
                    return "Parsed Data: "+ printableData;
                }else{
                    return "";
                }
            default:
                return "";
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.nfc_record_list_header,null);
        }
        TextView txtView = (TextView) convertView.findViewById(R.id.nfc_txt);
        txtView.setText("Record "+groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String recordData = (String) getChild(groupPosition,childPosition);
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.nfc_record_details,null);
        }
        TextView txtView = (TextView) convertView.findViewById(R.id.nfc_txt);
        txtView.setText(recordData);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
