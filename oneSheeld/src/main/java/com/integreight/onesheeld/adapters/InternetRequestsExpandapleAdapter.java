package com.integreight.onesheeld.adapters;

/**
 * Created by Saad on 1/28/15.
 */

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.InternetRequest;
import com.integreight.onesheeld.model.InternetResponse;
import com.integreight.onesheeld.model.InternetUiRequest;

import java.util.ArrayList;

public class InternetRequestsExpandapleAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<InternetUiRequest> _listDataHeader; // header titles
    // child data in format of header title, child title

    public InternetRequestsExpandapleAdapter(Context context, ArrayList<InternetUiRequest> listDataHeader) {
        this._context = context;
        this._listDataHeader = listDataHeader;
    }

    public void updateRequests(ArrayList<InternetUiRequest> listDataHeader) {
        this._listDataHeader = listDataHeader;
        notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return (this._listDataHeader.get(groupPosition))
                .getUiChildren().get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Pair<String, String> child = (Pair<String, String>) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.internet_request_child_item, null);
        }

        ((TextView) convertView
                .findViewById(R.id.key)).setText(child.first);
        ((TextView) convertView
                .findViewById(R.id.value)).setText(child.second);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (this._listDataHeader.get(groupPosition)).getUiChildren()
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        InternetUiRequest request = (InternetUiRequest) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.internet_request_parent_item, null);
        }

        ((TextView) convertView
                .findViewById(R.id.requestID)).setText(_context.getString(R.string.internet_request) + " : " + request.getId());
        ProgressBar prog =
                (ProgressBar) convertView
                        .findViewById(R.id.executingRequest);
        ImageView status =
                (ImageView) convertView
                        .findViewById(R.id.requestStatus);
        if (!request.isCancelled() || request.getStatus() == InternetRequest.REQUEST_STATUS.EXECUTED) {
            if (request.getStatus() == InternetRequest.REQUEST_STATUS.IN_QUEUE) {
                prog.setVisibility(View.INVISIBLE);
                status.setImageBitmap(null);
                status.setBackgroundResource(R.drawable.internet_shield_yellow);
                status.setVisibility(View.VISIBLE);
            } else if (request.getStatus() == InternetRequest.REQUEST_STATUS.SENT || request.getStatus() == InternetRequest.REQUEST_STATUS.CALLED) {
                prog.setVisibility(View.VISIBLE);
                status.setImageBitmap(null);
                status.setVisibility(View.INVISIBLE);
            } else {
                prog.setVisibility(View.INVISIBLE);
                status.setBackgroundResource(request.getStatus() == InternetRequest.REQUEST_STATUS.EXECUTED && request.getResponse() != null && request.getResponse().getStatus() == InternetResponse.RESPONSE_STATUS.SUCCESSFUL ? R.drawable.internet_shield_green : R.drawable.internet_shield_red);
                status.setVisibility(View.VISIBLE);
            }
        } else {
            prog.setVisibility(View.INVISIBLE);
            status.setImageBitmap(null);
            status.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
