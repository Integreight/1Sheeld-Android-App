package com.integreight.onesheeld.shieldsfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.integreight.onesheeld.R;

public class KeypadFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
            // Inflate the layout for this fragment

            return inflater.inflate(R.layout.keypad_shield_fragment_layout, container, false);
        }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub

    	super.onActivityCreated(savedInstanceState);
    }
    

	
}
