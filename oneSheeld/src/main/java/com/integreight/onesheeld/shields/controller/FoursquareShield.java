package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ApiObjects;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.Foursquare;
import com.integreight.onesheeld.shields.controller.utils.Foursquare.DialogListener;
import com.integreight.onesheeld.shields.controller.utils.FoursquareDialogError;
import com.integreight.onesheeld.shields.controller.utils.FoursquareError;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class FoursquareShield extends ControllerParent<FoursquareShield> {

    private FoursquareEventHandler eventHandler;
    private static final byte CHECKIN_METHOD_ID = (byte) 0x01;
    Foursquare foursquare;
    String redirectUrl = "http://www.1sheeld.com";
    String placeID = "";
    String message = "";

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    public FoursquareShield() {
        super();
    }

    @Override
    public ControllerParent<FoursquareShield> init(String tag) {
        // getShareprefrences
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);

        return super.init(tag);
    }

    public FoursquareShield(Activity activity, String tag) {
        super(activity, tag);
        // getShareprefrences
    }

    public boolean isFoursquareLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences
                .getBoolean("PREF_KEY_FOURSQUARE_LOGIN", false);
    }

    public void setFoursquareEventHandler(FoursquareEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public static interface FoursquareEventHandler {
        void onPlaceCheckin(String placeName);

        void setLastPlaceCheckin(String placeName);

        void onForsquareLoggedIn(String userName);

        void onForsquareLogout();

        void onForsquareError();

    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.FOURSQUARE_SHIELD.getId()) {
            if (isFoursquareLoggedInAlready())
                if (frame.getFunctionId() == CHECKIN_METHOD_ID) {
                    placeID = frame.getArgumentAsString(0);
                    message = frame.getArgumentAsString(1);
                    if (ConnectionDetector
                            .isConnectingToInternet(getApplication()
                                    .getApplicationContext())) {
                        ConnectFour connectFour = new ConnectFour();
                        connectFour.execute("");
                    } else
                        Toast.makeText(
                                getApplication().getApplicationContext(),
                                R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
                                Toast.LENGTH_SHORT).show();
                }
        }
    }

    private class ConnectFour extends AsyncTask<String, String, String> {

        String response = "";

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                String foursquare_token = mSharedPreferences.getString(
                        "PREF_FourSquare_OAUTH_TOKEN", null);
                String placeId = placeID;
                String messageId = URLEncoder.encode(message, "UTF-8");
                String checkinURLRequest = "https://api.foursquare.com/v2/checkins/add?venueId="
                        + placeId
                        + "&"
                        + "shout="
                        + messageId
                        + "&broadcast=public&oauth_token="
                        + foursquare_token
                        + "&v=20140201";
                Log.d("checkinURLRequest", checkinURLRequest);
                HttpPost post = new HttpPost(checkinURLRequest);

                HttpClient hc = new DefaultHttpClient();
                HttpResponse rp = hc.execute(post);
                HttpEntity mEntity = rp.getEntity();
                InputStream resp = mEntity.getContent();
                try {
                    response = getStringFromInputStream(resp);
                } catch (Exception e) {
                    response = getStringFromInputStream(resp);
                }

                if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Log.d("Response From Server ::", rp.toString());
                }
            } catch (Exception e) {
                Log.d("HTTP ERROR ::", e.toString());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            // parse checkin response !
            try {
                JSONObject json = new JSONObject(result);
                JSONObject response = json.getJSONObject("response");
                JSONObject checkins = response.getJSONObject("checkin");
                JSONObject venue = checkins.getJSONObject("venue");
                String placeName = venue.getString("name");
                if (eventHandler != null)
                    eventHandler.onPlaceCheckin(placeName);
                // save in share prefrences
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("PREF_FourSquare_LastPlace", placeName);
                // Commit the edits!
                editor.commit();

            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Exception of Parsing checkin response :: ", e.toString());
            }

        }

    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            Log.e("TAG", "Foursquare::getStringFromInputStream", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e("TAG", "Foursquare::getStringFromInputStream", e);
                }
            }
        }

        return sb.toString();

    }

    private class ParseUserFoursquareData extends
            AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String aa = null;

            try {

                aa = foursquare.request("users/self");
                // show and save prefrences user name , last place
                // checkin
                // jsonParser(aa);
                Log.d("Foursquare-Main", aa);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                Log.e("TAG", "Foursquare::ParseUserFoursquareData", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e("TAG", "Foursquare::ParseUserFoursquareData", e);
            }
            return aa;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject json = new JSONObject(result);
                JSONObject response = json.getJSONObject("response");
                JSONObject user = response.getJSONObject("user");
                String userName = user.getString("firstName");
                // Set user name UI
                if (eventHandler != null)
                    eventHandler.onForsquareLoggedIn(userName);
                JSONObject venue = user.getJSONObject("checkins")
                        .getJSONArray("items").getJSONObject(0)
                        .getJSONObject("venue");
                String placeName = venue.getString("name");
                if (eventHandler != null)
                    eventHandler.setLastPlaceCheckin(placeName);

                // save in share prefrences
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("PREF_FourSquare_UserName", userName);
                editor.putString("PREF_FourSquare_LastPlace", placeName);

                // Commit the edits!
                editor.commit();

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                Log.d("Exception of Parsing User login response :: ",
                        e.toString());

            }

        }

    }

    private class FoursquareAuthenDialogListener implements DialogListener {

        @Override
        public void onComplete(Bundle values) {
            new ParseUserFoursquareData().execute("");
        }

        @Override
        public void onFoursquareError(FoursquareError e) {
            Toast.makeText(getApplication().getApplicationContext(),
                    R.string.foursquare_foursquare_authorize_login_failed_toast, Toast.LENGTH_SHORT)
                    .show();

        }

        @Override
        public void onError(FoursquareDialogError e) {
            Toast.makeText(getApplication().getApplicationContext(),
                    R.string.foursquare_foursquare_authorize_login_failed_toast, Toast.LENGTH_SHORT)
                    .show();

        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplication().getApplicationContext(),
                    R.string.foursquare_foursquare_login_canceled_toast, Toast.LENGTH_SHORT).show();

        }

    }

    public void jsonParser(String result) {
        try {
            JSONObject json = new JSONObject(result);
            JSONObject response = json.getJSONObject("response");
            JSONObject user = response.getJSONObject("user");
            String userName = user.getString("firstName");
            // Set user name UI
            if (eventHandler != null)
                eventHandler.onForsquareLoggedIn(userName);
            JSONObject venue = user.getJSONObject("checkins")
                    .getJSONArray("items").getJSONObject(0)
                    .getJSONObject("venue");
            String placeName = venue.getString("name");
            if (eventHandler != null)
                eventHandler.onPlaceCheckin(placeName);

            // save in share prefrences
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("PREF_FourSquare_UserName", userName);
            editor.putString("PREF_FourSquare_LastPlace", placeName);

            // Commit the edits!
            editor.commit();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("TAG", "Foursquare::jsonParser", e);
        }

    }

    public void loginToFoursquare() {
        ProgressDialog prog = new ProgressDialog(activity);
        prog.setMessage(activity.getString(R.string.foursquare_please_wait));
        prog.setCancelable(false);
        prog.show();
        foursquare = new Foursquare(
                ApiObjects.foursquare.get("client_key"),
                ApiObjects.foursquare.get("client_secret"),
                redirectUrl);
        foursquare.authorize(getActivity(),
                new FoursquareAuthenDialogListener());
        prog.cancel();
    }

    @Override
    public void postConfigChange() {
        super.postConfigChange();
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
