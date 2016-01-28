package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.InternetRequestsExpandapleAdapter;
import com.integreight.onesheeld.model.InternetResponse;
import com.integreight.onesheeld.model.InternetUiRequest;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.InternetShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.integreight.onesheeld.shields.controller.utils.InternetResponsePopup;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;


import java.util.ArrayList;
import java.util.Enumeration;

public class InternetFragment extends ShieldFragmentParent<InternetFragment> {
    ExpandableListView requestsList;
    ArrayList<InternetUiRequest> requests;
    InternetResponsePopup popup;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.internet_shield_fragment_layout, container,
                false);

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        requestsList = (ExpandableListView) v.findViewById(R.id.requestsList);
    }

    @Override
    public void doOnStart() {
        ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).setUiCallback(new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeUI() && requestsList != null && requestsList.getExpandableListAdapter() != null && requestsList.getExpandableListAdapter() instanceof InternetRequestsExpandapleAdapter) {
                            requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
                            checkRequests();
                            ((InternetRequestsExpandapleAdapter) requestsList.getExpandableListAdapter()).updateRequests(requests);
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeUI() && requestsList != null && requestsList.getExpandableListAdapter() != null && requestsList.getExpandableListAdapter() instanceof InternetRequestsExpandapleAdapter) {
                            requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
                            checkRequests();
                            ((InternetRequestsExpandapleAdapter) requestsList.getExpandableListAdapter()).updateRequests(requests);
                        }
                    }
                });
            }

            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {

            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
        requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
        checkRequests();
        try {
            View footer = new View(getActivity());
            footer.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, getPixelFromDips(50)));
            requestsList.addFooterView(footer);
        } catch (Exception e) {
            CrashlyticsUtils.logException(e);
        }
        requestsList.setAdapter(new InternetRequestsExpandapleAdapter(activity, requests));
        requestsList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                if (requestsList.getExpandableListAdapter().getChildrenCount(i) == 6 && i2 == 1) {
                    InternetResponse response = requests.get(i).getResponse();
                    String headers = "";
                    Enumeration e = response.getHeaders().keys();
                    while (e.hasMoreElements()) {
                        String key = (String) e.nextElement();
                        headers += "      " + key + " : " + response.getHeaders().get(key) + "\n";
                    }
                    popup = new InternetResponsePopup(getActivity(), new String(response.getResponseBody()), activity.getString(R.string.internet_status_code)+": " + response.getStatusCode() + "\n\n" + activity.getString(R.string.internet_headers)+" : \n" + headers + "\n" + activity.getString(R.string.internet_response_body)+": \n" + new String(response.getResponseBody()));
                    popup.show();
                }
                return true;
            }
        });
        requestsList.setIndicatorBounds(getPixelFromDips(10), getPixelFromDips(40));
    }

    public int getPixelFromDips(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    private void checkRequests() {
        if (requests == null || requests.size() == 0) {
            getView().findViewById(R.id.noRequests).setVisibility(View.VISIBLE);
        } else
            getView().findViewById(R.id.noRequests).setVisibility(View.INVISIBLE);
    }

    @Override
    public void doOnStop() {
        if (popup != null && popup.isShowing())
            popup.cancel();
    }


    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new SpeakerShield(activity, getControllerTag()));
        }

    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }


}
