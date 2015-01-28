package com.integreight.onesheeld.model;

import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by Saad on 1/28/15.
 */
public class InternetUiRequest extends InternetRequest{
    private ArrayList<Pair<String,String>> uiChildren;

    public ArrayList<Pair<String, String>> getUiChildren() {
        return uiChildren;
    }

    public void setUiChildren(ArrayList<Pair<String, String>> uiChildren) {
        this.uiChildren = uiChildren;
    }

}
