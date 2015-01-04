package com.integreight.onesheeld.shields.fragments.sub;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;

public class BuzzerShieldSettings extends Fragment {
    public static BuzzerShieldSettings getInstance() {
        return new BuzzerShieldSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buzzer_sound_settings, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initView();
        super.onActivityCreated(savedInstanceState);
    }

    private void initView() {
        final RadioGroup group = (RadioGroup) getView().findViewById(
                R.id.ringtoneGroup);

        RingtoneManager manager = new RingtoneManager(getActivity());
        manager.setType(RingtoneManager.TYPE_RINGTONE);
        int pdng = (int) (5 * getResources().getDisplayMetrics().density + .5f);
        final String[] columns = {MediaStore.Images.Media.DATA,
                MediaStore.Audio.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;
        Cursor imagecursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
        int dataColumnIndex = imagecursor
                .getColumnIndex(MediaStore.Audio.Media.DATA);
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            final String uri = imagecursor.getString(dataColumnIndex);
            final String title = uri.substring(uri.lastIndexOf("/") + 1);
            RadioButton rb = new RadioButton(getActivity());
            rb.setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT));
            rb.setText(title);
            rb.setTypeface(((OneSheeldApplication) getActivity()
                    .getApplication()).appFont);
            rb.setGravity(Gravity.CENTER);
            rb.setPadding(pdng, pdng, pdng, pdng);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            rb.setTextColor(getResources().getColor(R.color.textColorOnDark));
            rb.setBackgroundResource(R.drawable.devices_list_item_selector);
            rb.setTag(uri);
            rb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((OneSheeldApplication) getActivity().getApplication())
                            .setBuzzerSound(uri);
                }
            });
            group.addView(rb);
        }
    }
}
