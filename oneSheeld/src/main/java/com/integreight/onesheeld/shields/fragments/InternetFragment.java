package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.InternetRequestsExpandapleAdapter;
import com.integreight.onesheeld.model.InternetResponse;
import com.integreight.onesheeld.model.InternetUiRequest;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.InternetShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.integreight.onesheeld.shields.controller.utils.InternetResponsePopup;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Enumeration;

public class InternetFragment extends ShieldFragmentParent<InternetFragment> {
    ExpandableListView requestsList;
    ArrayList<InternetUiRequest> requests;
    InternetResponsePopup popup;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.internet_shield_fragment_layout, container,
                false);
        return v;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestsList = (ExpandableListView) getView().findViewById(R.id.requestsList);
    }

    @Override
    public void onStart() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            if (!reInitController())
                return;
        }
        ((ViewGroup) getView()).getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).addRequest();
            }
        });
        ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).setUiCallback(new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeUI() && requestsList.getExpandableListAdapter() != null && requestsList.getExpandableListAdapter() instanceof InternetRequestsExpandapleAdapter) {
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
                        if (canChangeUI() && requestsList.getExpandableListAdapter() != null && requestsList.getExpandableListAdapter() instanceof InternetRequestsExpandapleAdapter) {
                            requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
                            checkRequests();
                            ((InternetRequestsExpandapleAdapter) requestsList.getExpandableListAdapter()).updateRequests(requests);
                        }
                    }
                });
            }

            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
        requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
        checkRequests();
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
                    popup = new InternetResponsePopup(getActivity(), new String(response.getResponseBody()), "Status code: " + response.getStatusCode() + "\n\n" + "Headers : \n" + headers + "\n" + "Response Body: \n" + new String(response.getResponseBody()));
                    popup.show();
                }
                return true;
            }
        });
        super.onStart();
    }

    private void checkRequests() {
        if (requests == null || requests.size() == 0) {
            getView().findViewById(R.id.noRequests).setVisibility(View.VISIBLE);
        } else
            getView().findViewById(R.id.noRequests).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop() {
        if (popup != null && popup.isShowing())
            popup.cancel();
        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

    @Override
    public void onResume() {
        super.onResume();
    }

}
