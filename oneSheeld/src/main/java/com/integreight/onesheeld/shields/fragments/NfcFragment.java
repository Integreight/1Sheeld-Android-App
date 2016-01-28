package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.NfcNdefRecordsExpandableAdapter;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.NfcShield;
import com.integreight.onesheeld.shields.controller.NfcShield.NFCEventHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.ArrayList;


/**
 * Created by Mouso on 3/11/2015.
 */
public class NfcFragment extends ShieldFragmentParent<NfcFragment> {

    ExpandableListView nfcRecords;
    OneSheeldTextView cardDetails, noCard;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.nfc_shield_fragment_view, container, false);
    }

    @Override
    public void doOnViewCreated(View v, Bundle savedInstanceState) {
        nfcRecords = (ExpandableListView) v.findViewById(R.id.nfc_Records_list);
        cardDetails = new OneSheeldTextView(activity);
        cardDetails.setTextColor(getResources().getColor(R.color.textColorOnDark));
        cardDetails.setTextSize(15);
        int tenDP = (int) (10 * getResources().getDisplayMetrics().density + .5f);
        cardDetails.setPadding(0, tenDP, 0, tenDP);
        nfcRecords.addHeaderView(cardDetails, null, false);
        noCard = (OneSheeldTextView) v.findViewById(R.id.nfc_no_card);

        nfcRecords.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return true;
            }
        });
    }

    @Override
    public void doOnStart() {
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).setEventHandler(nfcEventHandler);
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(), new NfcShield(activity, getControllerTag()));
        }
    }

    @Override
    public void doOnResume() {
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).showSettingsDialogIfNfcIsNotEnabled();
        ((NfcShield) getApplication().getRunningShields().get(getControllerTag())).displayData();
    }

    private NFCEventHandler nfcEventHandler = new NFCEventHandler() {
        @Override
        public void ReadNdef(final String id, final int maxSize, final int usedSize, final ArrayList<ArrayList<String>> data) {
            //handle data display
            if (canChangeUI() && uiHandler != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeUI() && noCard != null && cardDetails != null) {
                            noCard.setVisibility(View.GONE);
                            cardDetails.setText(activity.getString(R.string.nfc_device_tag_id) + " :     \t" + id);
                            cardDetails.append("\n");
                            cardDetails.append(activity.getString(R.string.nfc_device_max_size) + " :\t" + String.valueOf(maxSize) + " " + activity.getString(R.string.nfc_device_bytes));
                            cardDetails.append("\n");
                            cardDetails.append(activity.getString(R.string.nfc_device_used_size)+" : " + String.valueOf(usedSize) + " " + activity.getString(R.string.nfc_device_bytes));
                            cardDetails.append("\n");
                            cardDetails.append(activity.getString(R.string.nfc_device_number_of_records) + " : " + String.valueOf(data.size()) + " " + activity.getString(R.string.nfc_device_records));

                            NfcNdefRecordsExpandableAdapter nfcNdefRecordsExpandableAdapter = new NfcNdefRecordsExpandableAdapter(activity, data);
                            nfcRecords.setAdapter(nfcNdefRecordsExpandableAdapter);
                        }
                    }
                });

        }
    };
}
