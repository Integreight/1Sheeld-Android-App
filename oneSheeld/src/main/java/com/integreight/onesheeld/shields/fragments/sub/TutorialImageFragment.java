package com.integreight.onesheeld.shields.fragments.sub;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.integreight.onesheeld.R;

public class TutorialImageFragment extends Fragment {
    final int[] tutImgs = new int[]{R.drawable.tutorial_1_screen,
            R.drawable.tutorial_2_screen, R.drawable.tutorial_3_screen,
            R.drawable.tutorial_4_screen, R.drawable.tutorial_5_screen,
            R.drawable.tutorial_6_screen, R.drawable.tutorial_7_screen};

    public static TutorialImageFragment newInstance(int indx) {
        TutorialImageFragment fragment = new TutorialImageFragment();
        Bundle b = new Bundle();
        b.putInt("indx", indx);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImageView iv = (ImageView) inflater.inflate(R.layout.tutorial_img,
                null, false);
        iv.setImageBitmap(null);
        iv.setBackgroundColor(Color.BLACK);
        iv.setImageResource(tutImgs[getArguments().getInt("indx")]);
        return iv;
    }
}
