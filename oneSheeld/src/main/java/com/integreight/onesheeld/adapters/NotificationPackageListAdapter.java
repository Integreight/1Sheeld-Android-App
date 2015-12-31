package com.integreight.onesheeld.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.PackageItem;
import com.integreight.onesheeld.shields.controller.NotificationShield;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;
import com.integreight.onesheeld.utils.database.NotificationPackageList;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationPackageListAdapter extends BaseAdapter {
    MainActivity activity;
    private CopyOnWriteArrayList<PackageItem> items;
    private LayoutInflater inflater;

    public NotificationPackageListAdapter(Activity a, ArrayList<PackageItem> items) {
        this.activity = (MainActivity) a;
        this.items = new CopyOnWriteArrayList<PackageItem>();
        for (PackageItem packageItem : items) {
            this.items.add(packageItem);
        }
        inflater = a.getLayoutInflater();
    }

    public int getCount() {
        return items.size();
    }

    public PackageItem getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        View row = convertView;
        Holder holder = null;
        if (row == null) {

            row = inflater.inflate(R.layout.notification_package_row, parent, false);

            holder = new Holder();
            holder.name = (OneSheeldTextView) row
                    .findViewById(R.id.packageItemName);
            holder.packageName = (OneSheeldTextView) row.findViewById(R.id.packageItemPackageName);
            holder.check = (CheckBox) row.findViewById(R.id.packageItemCheck);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }
        final Holder temp = holder;
        final PackageItem item = items.get(position);
        temp.name.setText(item.name);
        temp.packageName.setText(item.packageName);
        temp.check.setChecked(item.isSelected);
        temp.check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                temp.check.setChecked(!item.isSelected);
                items.get(position).isSelected = !item.isSelected;
            }
        });
        return row;
    }

    public void removeFromPlaylist() {
        NotificationPackageList db = new NotificationPackageList(activity);
        db.openToWrite();
        for (PackageItem item : items) {
            if (item.isSelected) {
                db.delete(item.id);
                items.remove(item);
            }
        }
        notifyDataSetChanged();
        db.close();
        ((NotificationShield) ((OneSheeldApplication) activity.getApplication()).getRunningShields().get(UIShield.NOTIFICATION_SHIELD.name())).checkDenyList();
    }

    public void updateList(ArrayList<PackageItem> items) {
        if (items != null) {
            this.items = new CopyOnWriteArrayList<PackageItem>();
            for (PackageItem packageItem : items) {
                this.items.add(packageItem);
            }
            notifyDataSetChanged();
        }
    }

    static class Holder {
        OneSheeldTextView name,packageName;
        CheckBox check;
    }

}
