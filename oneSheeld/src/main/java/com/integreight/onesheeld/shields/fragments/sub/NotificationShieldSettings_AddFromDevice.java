package com.integreight.onesheeld.shields.fragments.sub;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.PackagesListAdapter;
import com.integreight.onesheeld.model.PackageItem;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationShieldSettings_AddFromDevice extends Fragment {
    public static NotificationShieldSettings_AddFromDevice getInstance() {
        return new NotificationShieldSettings_AddFromDevice();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_shield_settings_add_package,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initView();
        super.onActivityCreated(savedInstanceState);
    }

    ListView playlist;

    private void initView() {
        playlist = (ListView) getView().findViewById(R.id.packageList2);
        final OneSheeldButton addFromMedia = (OneSheeldButton) getView()
                .findViewById(R.id.addToDenyList);

        final PackageManager pm = getActivity().getPackageManager();
        //get a list of installed apps.
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        ArrayList<PackageItem> items = new ArrayList<PackageItem>();
        for (PackageInfo packageInfo : packages) {
            PackageItem item = new PackageItem();
            item.packageName = packageInfo.packageName;
            item.name = (String) packageInfo.applicationInfo.loadLabel(pm);
            items.add(item);
        }
        Collections.sort(items, new Comparator<PackageItem>() {
            @Override
            public int compare(PackageItem lhs, PackageItem rhs) {
                return lhs.name.compareToIgnoreCase(rhs.name);
            }
        });
        playlist.setAdapter(new PackagesListAdapter(getActivity(), items));
        addFromMedia.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((PackagesListAdapter) playlist.getAdapter())
                        .addToPlayList();
                ((MainActivity) getActivity())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settingsViewContainer,
                                NotificationShieldSettings.getInstance()).commit();
            }
        });

    }
}
