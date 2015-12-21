package com.integreight.onesheeld.shields.fragments.sub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.NotificationPackageListAdapter;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.database.NotificationPackageList;

public class NotificationShieldSettings extends Fragment {
    public static NotificationShieldSettings getInstance() {
        return new NotificationShieldSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_shield_settings, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initView();
        super.onActivityCreated(savedInstanceState);
    }

    private void initView() {
        final ListView playlist = (ListView) getView().findViewById(
                R.id.packageList);
        final OneSheeldButton addFromSDcard = (OneSheeldButton) getView()
                .findViewById(R.id.addToPackageList);
        final OneSheeldButton removeFromPlaylist = (OneSheeldButton) getView()
                .findViewById(R.id.removeFromPackageList);
        NotificationPackageList db = new NotificationPackageList(getActivity());
        db.openToWrite();
        playlist.setAdapter(new NotificationPackageListAdapter(getActivity(), db
                .getPlaylist()));
        db.close();
        removeFromPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((NotificationPackageListAdapter) playlist.getAdapter())
                        .removeFromPlaylist();
            }
        });
        addFromSDcard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settingsViewContainer,
                                NotificationShieldSettings_AddFromDevice.getInstance())
                        .commit();
            }
        });

    }

}
