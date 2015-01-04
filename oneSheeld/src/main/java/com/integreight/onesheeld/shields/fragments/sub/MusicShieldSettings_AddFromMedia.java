package com.integreight.onesheeld.shields.fragments.sub;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.MediaAudioFilesListAdapter;
import com.integreight.onesheeld.model.PlaylistItem;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;

import java.util.ArrayList;

public class MusicShieldSettings_AddFromMedia extends Fragment {
    public static MusicShieldSettings_AddFromMedia getInstance() {
        return new MusicShieldSettings_AddFromMedia();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_shield_settings_add_from_media,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initView();
        super.onActivityCreated(savedInstanceState);
    }

    ListView playlist;

    private void initView() {
        playlist = (ListView) getView().findViewById(R.id.playList);
        final OneSheeldButton addFromMedia = (OneSheeldButton) getView()
                .findViewById(R.id.addFromMedia);

        final String[] columns = {MediaStore.Images.Media.DATA,
                MediaStore.Audio.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor imagecursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
        ArrayList<PlaylistItem> items = new ArrayList<PlaylistItem>();
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA);
            PlaylistItem item = new PlaylistItem();
            item.path = imagecursor.getString(dataColumnIndex);
            item.name = item.path.substring(item.path.lastIndexOf("/") + 1);
            items.add(item);
        }
        playlist.setAdapter(new MediaAudioFilesListAdapter(getActivity(), items));
        addFromMedia.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MediaAudioFilesListAdapter) playlist.getAdapter())
                        .addToPlayList();
                ((MainActivity) getActivity())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settingsViewContainer,
                                MusicShieldSettings.getInstance()).commit();
            }
        });

    }
}
