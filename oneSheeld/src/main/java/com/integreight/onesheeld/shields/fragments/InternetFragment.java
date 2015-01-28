package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.InternetRequestsExpandapleAdapter;
import com.integreight.onesheeld.model.InternetUiRequest;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.InternetShield;
import com.integreight.onesheeld.shields.controller.SpeakerShield;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

public class InternetFragment extends ShieldFragmentParent<InternetFragment> {
    ExpandableListView requestsList;
    ArrayList<InternetUiRequest> requests;

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
                if (canChangeUI() && requestsList.getExpandableListAdapter() != null && requestsList.getExpandableListAdapter() instanceof InternetRequestsExpandapleAdapter) {
                    requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
                    checkRequests();
                    ((InternetRequestsExpandapleAdapter) requestsList.getExpandableListAdapter()).updateRequests(requests);
                }
                super.onStart();
            }

            @Override
            public void onFinish() {
                if (canChangeUI() && requestsList.getExpandableListAdapter() != null && requestsList.getExpandableListAdapter() instanceof InternetRequestsExpandapleAdapter) {
                    requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
                    checkRequests();
                    ((InternetRequestsExpandapleAdapter) requestsList.getExpandableListAdapter()).updateRequests(requests);
                }
                super.onFinish();
            }
        });
        requests = ((InternetShield) getApplication().getRunningShields().get(getControllerTag())).getUiRequests();
        checkRequests();
        requestsList.setAdapter(new InternetRequestsExpandapleAdapter(activity, requests));
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
