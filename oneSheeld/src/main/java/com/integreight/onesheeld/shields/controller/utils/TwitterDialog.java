package com.integreight.onesheeld.shields.controller.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.Log;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Display Foursquare authentication dialog.
 *
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 */
@SuppressLint("SetJavaScriptEnabled")
public class TwitterDialog extends Dialog {

    static final float[] DIMENSIONS_LANDSCAPE = {460, 260};
    static final float[] DIMENSIONS_PORTRAIT = {280, 420};
    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;

    private String mUrl;
    private TwitterDialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;
    private ImageView mCrossImage;
    private String currentURl = null;
    Twitter twitter;
    RequestToken requestToken;

    private static final String TAG = "Foursquare-WebView";

    public TwitterDialog(Context context, String url, Twitter twitter,
                         RequestToken requestToken, TwitterDialogListener listener) {
        super(context);
        this.requestToken = requestToken;
        this.twitter = twitter;
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSpinner = new ProgressDialog(getContext());

        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSpinner.setMessage(getContext().getString(R.string.twitter_loading)+"...");

        mContent = new LinearLayout(getContext());

        mContent.setOrientation(LinearLayout.VERTICAL);

        setUpTitle();
        setUpWebView();
        createCrossImage();
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
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float[] dimensions = (screenWidth < screenHeight) ? DIMENSIONS_PORTRAIT
                : DIMENSIONS_LANDSCAPE;

        mContent.addView(mCrossImage, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addContentView(mContent, new FrameLayout.LayoutParams(
                (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
                * scale + 0.5f)));

        CookieSyncManager.createInstance(getContext());

        CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.removeAllCookie();
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Drawable icon =
        // getContext().getResources().getDrawable(R.drawable.t);

        mTitle = new TextView(getContext());

        mTitle.setText(getContext().getString(R.string.twitter_shield_name));
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(0xFF4099FF);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
        // mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null,
        // null);

        mContent.addView(mTitle);
    }

    private void createCrossImage() {
        mCrossImage = new ImageView(getContext());
        // Dismiss the dialog when user click on the 'x'
        mCrossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancel();
                TwitterDialog.this.dismiss();
            }
        });
        // Drawable crossDrawable = getContext().getResources().getDrawable(
        // R.drawable.add_contacts);
        // mCrossImage.setImageDrawable(crossDrawable);
        /*
		 * 'x' should not be visible while webview is loading make it visible
		 * only after webview has fully loaded
		 */
        mCrossImage.setVisibility(View.INVISIBLE);
    }

    private void setUpWebView() {
        mWebView = new WebView(getContext());

        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new TwitterWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        mContent.addView(mWebView);
    }

    private class TwitterWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(TAG, "Page error: " + description);

            super.onReceivedError(view, errorCode, description, failingUrl);

            mListener.onError(description);

            Log.d("check twitter", "here 3");
            TwitterDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            mSpinner.dismiss();

            Log.d(TAG, "onPageFinished URL: " + url);

            currentURl = url;
            mWebView.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        }

    }

    class MyJavaScriptInterface {
        @SuppressWarnings("unused")
        @JavascriptInterface
        public void showHTML(String html) throws TwitterException {
            // System.out.println("html web page " + html);

            String startImageUrl = "<img src=\"";
            String endImageUrl = "\" alt=\"";
            String extractedImage;
            int imageStartPosition = html.indexOf(startImageUrl)
                    + startImageUrl.length();
            int imageEndPosition = html.indexOf(endImageUrl);
            if (imageEndPosition != -1 && html.indexOf(startImageUrl) != -1) {
                extractedImage = html.substring(imageStartPosition,
                        imageEndPosition);
                // System.out.println("IAMGE EXTRACTEDDDD "+extractedImage);
                TwitterAuthorization.FETCHED_IMAGE_URL = extractedImage
                        .replace("mini", "normal");

            }

            String pinCode = null;
            String name = null;
            String endSearchList = "</code></kbd>";
            String startSearchList = "<kbd aria-labelledby=\"code-desc\"><code>";

            String nameTagStart = "<span class=\"name\">";
            String nameTagEnd = "</span>";

            int endPosition = html.indexOf(endSearchList);
            int startPosition = html.indexOf(startSearchList)
                    + startSearchList.length();

            int nameStartPostion = html.indexOf(nameTagStart)
                    + nameTagStart.length();
            int nameEndPosition = html.indexOf(nameTagEnd);

            // System.out.println("name tag start  "+nameStartPostion);
            // System.out.println("name tag end  "+nameEndPosition);

            if (nameEndPosition != -1 && html.indexOf(nameTagStart) != -1) {
                name = html.substring(nameStartPostion, nameEndPosition);
                TwitterAuthorization.TWITTER_USER_NAME = name;
                // System.out.println("name view  "+name);
            }

            if (endPosition != -1 && html.indexOf(startSearchList) != -1) {
                pinCode = html.substring(startPosition, endPosition);
                // System.out.println("pin code  "+pinCode);
                AccessToken accessToken = twitter.getOAuthAccessToken(
                        requestToken, pinCode);
                String token = accessToken.getToken(), secret = accessToken
                        .getTokenSecret();

                TwitterAuthorization.FETCHED_ACCESS_TOKEN = accessToken
                        .getToken();
                TwitterAuthorization.FETCHED_SECRET_TOKEN = accessToken
                        .getTokenSecret();
                // System.out.println("token  "+token+" SECRET "+secret);

                mListener.onComplete();
                Log.d("check twitter", "here 2");
                TwitterDialog.this.dismiss();

            } else if (currentURl
                    .equals("https://api.twitter.com/oauth/authenticate")
                    && pinCode == null) {
            }
        }
    }
}
