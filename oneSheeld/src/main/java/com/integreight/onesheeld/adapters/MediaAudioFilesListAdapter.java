package com.integreight.onesheeld.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.integreight.onesheeld.BuildConfig;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.PlaylistItem;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;
import com.integreight.onesheeld.utils.database.MusicPlaylist;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class MediaAudioFilesListAdapter extends BaseAdapter {
    MainActivity activity;
    private CopyOnWriteArrayList<PlaylistItem> items;
    private LayoutInflater inflater;

    public MediaAudioFilesListAdapter(Activity a, ArrayList<PlaylistItem> items) {
        this.activity = (MainActivity) a;
        this.items = new CopyOnWriteArrayList<PlaylistItem>();
        for (PlaylistItem playlistItem : items) {
            this.items.add(playlistItem);
        }
        inflater = a.getLayoutInflater();
    }

    public int getCount() {
        return items.size();
    }

    public PlaylistItem getItem(int position) {
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

            row = inflater.inflate(R.layout.music_row, parent, false);

            holder = new Holder();
            holder.name = (OneSheeldTextView) row
                    .findViewById(R.id.musicItemName);
            holder.check = (CheckBox) row.findViewById(R.id.musicItemCheck);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }
        final Holder temp = holder;
        final PlaylistItem item = items.get(position);
        temp.name.setText(item.name);
        temp.name.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT >= 24) {
                    Uri fileURI = FileProvider.getUriForFile(activity,
                            BuildConfig.APPLICATION_ID + ".provider",
                            new File(item.path));
                    intent.setDataAndType(fileURI,
                            "audio/*");
                }
                else{
                    intent.setDataAndType(Uri.parse("file://" + item.path),
                            "audio/*");
                }
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(intent);
            }
        });
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

    public void addToPlayList() {
        MusicPlaylist db = new MusicPlaylist(activity);
        db.openToWrite();
        for (PlaylistItem item : items) {
            if (item.isSelected) {
                db.insert(item);
            }
        }
        notifyDataSetChanged();
        db.close();
    }

    public void updateList(ArrayList<PlaylistItem> items) {
        if (items != null) {
            this.items = new CopyOnWriteArrayList<PlaylistItem>();
            for (PlaylistItem playlistItem : items) {
                this.items.add(playlistItem);
            }
            notifyDataSetChanged();
        }
    }

    static class Holder {
        OneSheeldTextView name;
        CheckBox check;
    }

}
