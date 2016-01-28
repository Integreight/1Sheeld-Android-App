package com.integreight.onesheeld.shields.controller.utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.integreight.onesheeld.R;

/**
 * Created by Saad on 1/29/15.
 */
public class InternetResponsePopup extends Dialog {
    Activity activity;
    WebView webView;
    ScrollView scroll;
    String response;
    String webResponse;

    public InternetResponsePopup(Activity activity, String webResponse, String value) {
        super(activity, android.R.style.Theme_Translucent_NoTitleBar);
        this.activity = activity;
        this.response = value;
        this.webResponse = webResponse;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internet_popup_view);
        scroll = (ScrollView) findViewById(R.id.responseScroll);
        webView = (WebView) findViewById(R.id.responseWeb);
        webView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isShowing()) {
                    ((TextView) findViewById(R.id.redirecting)).setText(activity.getString(R.string.internet_redirecting_to)+" " + url);
                    findViewById(R.id.redirecting).setVisibility(View.VISIBLE);
                    webView.loadUrl(url);
                    return true;
                }
                return false; //Allow WebView to load url
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                findViewById(R.id.redirecting).setVisibility(View.INVISIBLE);
                super.onPageFinished(view, url);
            }
        });
        ((TextView) scroll.getChildAt(0)).setText(response);
        ((ToggleButton) findViewById(R.id.responseToggle)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    scroll.setVisibility(View.INVISIBLE);
                    ((ViewGroup) webView.getParent()).setVisibility(View.VISIBLE);
                    String mime = "text/html";
                    String encoding = "utf-8";
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadDataWithBaseURL(null, webResponse, mime, encoding, null);
                } else {
                    scroll.setVisibility(View.VISIBLE);
                    ((ViewGroup) webView.getParent()).setVisibility(View.INVISIBLE);
                    ((TextView) scroll.getChildAt(0)).setText(response);
                }
            }
        });
        findViewById(R.id.cancelPopup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    @Override
    public void setOnCancelListener(OnCancelListener listener) {
        webView.destroy();
        super.setOnCancelListener(listener);
    }
}
