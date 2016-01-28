package com.integreight.onesheeld.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.integreight.onesheeld.R;

import java.util.ArrayList;

/**
 * Created by Mouso on 4/23/2015.
 */
public class NfcNdefRecordsExpandableAdapter extends BaseExpandableListAdapter{

    private Context _context;
    private ArrayList<ArrayList<String>> _listNdefRecords;

    public NfcNdefRecordsExpandableAdapter(Context context,ArrayList<ArrayList<String>> listNdefRecords){
        this._context = context;
        this._listNdefRecords = listNdefRecords;
    }

    @Override
    public int getGroupCount() {
        return this._listNdefRecords.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listNdefRecords.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listNdefRecords.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listNdefRecords.get(groupPosition).get(childPosition);
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
        txtView.setText(_context.getString(R.string.nfc_record)+" "+groupPosition);
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
