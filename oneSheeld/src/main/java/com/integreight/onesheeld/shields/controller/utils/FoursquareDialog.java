package com.integreight.onesheeld.shields.controller.utils;

/**
 * Encapsulation of Foursquare Dialog.
 *
 * @author Mukesh Yadav
 */

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.controller.utils.Foursquare.DialogListener;
import com.integreight.onesheeld.utils.Log;

public class FoursquareDialog extends Dialog {

    static final int FB_BLUE = 0xFF0cbadf;
    static final float[] DIMENSIONS_LANDSCAPE = {460, 260};
    static final float[] DIMENSIONS_PORTRAIT = {280, 420};
    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;
    static final String DISPLAY_STRING = "touch";
    static final String FB_ICON = "icon.png";

    private String mUrl;
    private DialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;

    public FoursquareDialog(Context context, String url, DialogListener listener) {
        super(context);
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage(getContext().getString(R.string.foursquare_loading)+"...");

        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int screenHeight = 0, screenWidth = 0;
        try {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindow().getWindowManager().getDefaultDisplay()
                    .getMetrics(displaymetrics);
            screenHeight = displaymetrics.heightPixels;
            screenWidth = displaymetrics.widthPixels;
        } catch (Exception ignored) {
        }
        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17)
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(
                        getWindow().getWindowManager().getDefaultDisplay(),
                        realSize);
                screenHeight = realSize.y;
                screenWidth = realSize.x;
            } catch (Exception ignored) {
            }
        float[] dimensions = (screenWidth < screenHeight) ? DIMENSIONS_PORTRAIT
                : DIMENSIONS_LANDSCAPE;
        addContentView(mContent, new FrameLayout.LayoutParams(
                (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
                * scale + 0.5f)));
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mTitle = new TextView(getContext());
        mTitle.setText("Foursquare");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(FB_BLUE);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
        mContent.addView(mTitle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new FoursquareDialog.FoursquareWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mContent.addView(mWebView);
    }

    private class FoursquareWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("Foursquare-WebView", "Redirect URL: " + url);
            if (url.startsWith(Foursquare.REDIRECT_URI)) {
                Bundle values = FoursquareUtils.parseUrl(url);
                mListener.onComplete(values);
                FoursquareDialog.this.dismiss();
                return true;
            } else if (url.contains(DISPLAY_STRING)) {
                return false;
            }
            getContext().startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(new FoursquareDialogError(description, errorCode,
                    failingUrl));
            FoursquareDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("Foursquare-WebView", "Webview loading URL: " + url);
            if (checkRedirectUrl(url)) {
                FoursquareDialog.this.dismiss();
            } else {
                super.onPageStarted(view, url, favicon);
                mSpinner.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            mSpinner.dismiss();

        }

        private boolean checkRedirectUrl(String url) {
            if (url.startsWith(Foursquare.REDIRECT_URI)) {
                Bundle values = FoursquareUtils.parseUrl(url);
                mListener.onComplete(values);

                FoursquareDialog.this.dismiss();
                return true;
            }
            return false;
        }

    }
}
