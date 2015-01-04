package com.integreight.onesheeld.shields.fragments.sub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.MusicPlayListAdapter;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.database.MusicPlaylist;

public class MusicShieldSettings extends Fragment {
    public static MusicShieldSettings getInstance() {
        return new MusicShieldSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_shield_settings, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initView();
        super.onActivityCreated(savedInstanceState);
    }

    private void initView() {
        final ListView playlist = (ListView) getView().findViewById(
                R.id.playList);
        final OneSheeldButton addFromSDcard = (OneSheeldButton) getView()
                .findViewById(R.id.addFromSDcard);
        final OneSheeldButton removeFromPlaylist = (OneSheeldButton) getView()
                .findViewById(R.id.removeFromPlayList);
        MusicPlaylist db = new MusicPlaylist(getActivity());
        db.openToWrite();
        playlist.setAdapter(new MusicPlayListAdapter(getActivity(), db
                .getPlaylist()));
        db.close();
        removeFromPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MusicPlayListAdapter) playlist.getAdapter())
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
                                MusicShieldSettings_AddFromMedia.getInstance())
                        .commit();
            }
        });

    }
}
