package com.integreight.onesheeld.shields.controller.utils;

/**
 * Encapsulation of Foursquare.
 *
 * @author Mukesh Yadav
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.CookieSyncManager;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public class Foursquare {

    private static final String LOGIN = "oauth";
    public static final String API_END_POING_BASE_URL = "https://api.foursquare.com/v2/";
    public static String REDIRECT_URI;
    public static final String API_URL = "https://foursquare.com/oauth2/";
    // public static final String CANCEL_URI = "";
    public static final String TOKEN = "access_token";
    public static final String EXPIRES = "expires_in";
    public static final String SINGLE_SIGN_ON_DISABLED = "service_disabled";
    public static String AUTHENTICATE_URL = "https://foursquare.com/oauth2/authenticate";// +

    private String mClientId;
    private String mClientSecret;
    private String mAccessToken = null;

    private DialogListener mAuthDialogListener;
    private static SharedPreferences mSharedPreferences;

    public Foursquare(String clientId, String clientSecret, String redirectUrl) {
        if (clientId == null || clientSecret == null) {
            throw new IllegalArgumentException(
                    "You must specify your application ID when instantiating "
                            + "a Foursquare object. See README for details.");
        }
        mClientId = clientId;
        mClientSecret = clientSecret;
        REDIRECT_URI = redirectUrl;
    }

    public void authorize(Activity activity, final DialogListener listener) {
        mAuthDialogListener = listener;
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
        startDialogAuth(activity);
    }

    private void startDialogAuth(final Activity activity) {
        CookieSyncManager.createInstance(activity);
        Bundle params = new Bundle();
        dialog(activity, LOGIN, params, new DialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                CookieSyncManager.getInstance().sync();
                String _token = values.getString(TOKEN);
                setAccessToken(_token);
                // setAccessExpiresIn(values.getString(EXPIRES));
                if (isSessionValid()) {
                    Log.d("Foursquare-authorize",
                            "Login Success! access_token=" + getAccessToken());
                    mAuthDialogListener.onComplete(values);
                } else {
                    mAuthDialogListener.onFoursquareError(new FoursquareError(
                            activity.getString(R.string.foursquare_failed_to_receive_access_token)));
                }
            }

            public void onError(FoursquareDialogError error) {
                Log.d("Foursquare-authorize", "Login failed: " + error);
                mAuthDialogListener.onError(error);
            }

            public void onFoursquareError(FoursquareError error) {
                Log.d("Foursquare-authorize", "Login failed: " + error);
                mAuthDialogListener.onFoursquareError(error);
            }

            public void onCancel() {
                Log.d("Foursquare-authorize", "Login canceled");
                mAuthDialogListener.onCancel();
            }
        });
    }

    public void dialog(Context context, String action, Bundle parameters,
                       final DialogListener listener) {

        String endpoint = "";

        parameters.putString("client_id", mClientId);
        parameters.putString("display", "touch");
        if (action.equals(LOGIN)) {
            endpoint = AUTHENTICATE_URL;
            parameters.putString("client_secret", mClientSecret);
            parameters.putString("response_type", "token");
            parameters.putString("redirect_uri", REDIRECT_URI);
        }

        // if (isSessionValid()) {
        // parameters.putString(TOKEN, getAccessToken());
        // }
        String url = endpoint + "?" + FoursquareUtils.encodeUrl(parameters);
        new FoursquareDialog(context, url, listener).show();

    }

    public boolean isSessionValid() {
        if (getAccessToken() != null) {
            return true;
        }
        return false;
    }

    public void setAccessToken(String token) {
        mAccessToken = token;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("PREF_FourSquare_OAUTH_TOKEN", token);
        editor.putBoolean("PREF_KEY_FOURSQUARE_LOGIN", true);

        // Commit the edits!
        editor.commit();

        Log.d("Access Token::", token);
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public String request(String graphPath) throws MalformedURLException,
            IOException {
        return request(graphPath, new Bundle(), "GET");
    }

    public String request(String graphPath, Bundle parameters)
            throws MalformedURLException, IOException {
        return request(graphPath, parameters, "GET");
    }

    public String request(String graphPath, Bundle params, String httpMethod)
            throws FileNotFoundException, MalformedURLException, IOException {
        params.putString("format", "json");
        if (isSessionValid()) {
            String myToken = mSharedPreferences.getString("PREF_FourSquare_OAUTH_TOKEN", "");
            params.putString("oauth_token", myToken);
        }
        String url = API_END_POING_BASE_URL + graphPath;
        return FoursquareUtils.openUrl(url, httpMethod, params);
    }

    public static interface DialogListener {

        /**
         * Called when a dialog completes.
         * <p/>
         * Executed by the thread that initiated the dialog.
         *
         * @param values Key-value string pairs extracted from the response.
         */
        public void onComplete(Bundle values);

        /**
         * Called when a Foursquare responds to a dialog with an error.
         * <p/>
         * Executed by the thread that initiated the dialog.
         */
        public void onFoursquareError(FoursquareError e);

        /**
         * Called when a dialog has an error.
         * <p/>
         * Executed by the thread that initiated the dialog.
         */
        public void onError(FoursquareDialogError e);

        /**
         * Called when a dialog is canceled by the user.
         * <p/>
         * Executed by the thread that initiated the dialog.
         */
        public void onCancel();

    }
}
