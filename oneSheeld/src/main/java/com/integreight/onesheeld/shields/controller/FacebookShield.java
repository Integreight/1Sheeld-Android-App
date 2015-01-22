package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ApiObjects;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.shields.controller.utils.ImageUtils;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FacebookShield extends ControllerParent<FacebookShield> {
    private static FacebookEventHandler eventHandler;
    private String lastPost;
    private Fragment fragment;
    private static final byte UPDATE_STATUS_METHOD_ID = (byte) 0x01;
    private static final byte UPLOAD_PHOTO_METHOD_ID = (byte) 0x02;

    static final String PREF_KEY_FACEBOOK_USERNAME = "FacebookName";

    private static final List<String> PERMISSIONS = Arrays
            .asList("publish_actions");
    private boolean pendingPublishReauthorization = false;

    private static SharedPreferences mSharedPreferences;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();

    public String getLastPost() {
        return lastPost;
    }

    public FacebookShield() {
        super();
    }

    @Override
    public ControllerParent<FacebookShield> init(String tag) {
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        Session session = Session.getActiveSession();
        if (session == null) {
            if (session == null) {
                session = new Session.Builder(activity).setApplicationId(ApiObjects.facebook.get("app_id"))
                        .build();
            }
            Session.setActiveSession(session);
        }

        session.addCallback(statusCallback);
        return super.init(tag);
    }

    public FacebookShield(Activity activity, String tag, Fragment fragment,
                          Bundle savedInstanceState) {
        super(activity, tag);
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = new Session.Builder(activity)
                        .setApplicationId(ApiObjects.facebook.get("app_id"))
                        .build();
            }
            if (session == null) {
                session = new Session.Builder(activity)
                        .setApplicationId(ApiObjects.facebook.get("app_id"))
                        .build();
            }
            Session.setActiveSession(session);
        }

        session.addCallback(statusCallback);
        this.fragment = fragment;
    }

    public void setShieldFragment(Fragment fragment) {
        this.fragment = fragment;

    }

    public void setFacebookEventHandler(FacebookEventHandler eventHandler) {
        FacebookShield.eventHandler = eventHandler;

    }

    public static interface FacebookEventHandler {
        void onRecievePost(String post);

        void onFacebookLoggedIn();

        void onFacebookError(String error);

        void startProgress();

        void stopProgress();
    }

    public void loginToFacebook() {
        Session session = Session.getActiveSession();
        if (session == null) {
            session = new Session.Builder(activity).setApplicationId(ApiObjects.facebook.get("app_id"))
                    .build();
            Session.setActiveSession(session);
            loginToFacebook();
        } else if (!session.isOpened()) {
            session.openForRead(new Session.OpenRequest(fragment)
                    .setCallback(statusCallback));
        }

    }

    public void logoutFromFacebook() {

        if (Session.getActiveSession() != null
                && !Session.getActiveSession().isClosed()) {
            // Session.getActiveSession().close();
            Session.getActiveSession().closeAndClearTokenInformation();
        } else {
            Session ses = new Session.Builder(activity).setApplicationId(ApiObjects.facebook.get("app_id"))
                    .build();
            Session.setActiveSession(ses);
            ses.closeAndClearTokenInformation();
        }
        Session.setActiveSession(null);
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_FACEBOOK_USERNAME);
        e.commit();

    }

    public String getUsername() {
        return mSharedPreferences.getString(PREF_KEY_FACEBOOK_USERNAME, "");
    }

    public boolean isFacebookLoggedInAlready() {
        if (Session.getActiveSession() != null)
            return Session.getActiveSession().isOpened()
                    && getUsername().length() > 0;
        else
            return false;
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @SuppressWarnings("unused")
        @Override
        public void call(final Session session, SessionState state,
                         Exception exception) {
            if (exception != null && eventHandler != null) {
                exception.printStackTrace();
                logoutFromFacebook();
                eventHandler.onFacebookError(exception.getMessage());
            }
            if (session.isOpened()) {

                if (!pendingPublishReauthorization) {
                    if (!isSubsetOf(PERMISSIONS, session.getPermissions())) {
                        pendingPublishReauthorization = true;
                        Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
                                fragment, PERMISSIONS);
                        if (newPermissionsRequest != null)
                            session.requestNewPublishPermissions(newPermissionsRequest);
                        else {
                            if (eventHandler != null)
                                eventHandler
                                        .onFacebookError("Kindly, reset you facebook app or update it!");
                        }
                    }
                }

                if ((pendingPublishReauthorization && state
                        .equals(SessionState.OPENED_TOKEN_UPDATED))
                        || isSubsetOf(PERMISSIONS, session.getPermissions())) {
                    pendingPublishReauthorization = false;
                    Request.newMeRequest(session,
                            new Request.GraphUserCallback() {

                                // callback after Graph API response with user
                                // object
                                @Override
                                public void onCompleted(GraphUser user,
                                                        Response response) {
                                    if (user != null) {

                                        Editor e = mSharedPreferences.edit();
                                        e.putString(PREF_KEY_FACEBOOK_USERNAME,
                                                user.getName());
                                        e.commit();
                                        if (eventHandler != null)
                                            eventHandler.onFacebookLoggedIn();
                                    }

                                }
                            }).executeAsync();
                }
                // make request to the /me API

            }
        }
    }

    private boolean isSubsetOf(Collection<String> subset,
                               Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

    private void publishStory(final String message) {
        Session session = Session.getActiveSession();

        if (session != null) {

            Bundle postParams = new Bundle();
            postParams.putString("message", message);
            Request.Callback callback = new Request.Callback() {
                public void onCompleted(Response response) {
                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        if (eventHandler != null) {
                            Log.sysOut("$#$#$ " + error);
                            eventHandler.stopProgress();
                            eventHandler.onFacebookError(error
                                    .getErrorMessage());
                        }
                        return;
                    }
                    if (eventHandler != null) {
                        eventHandler.stopProgress();
                        eventHandler.onRecievePost(message);
                    }
                }
            };

            Request request = new Request(session, "me/feed", postParams,
                    HttpMethod.POST, callback);

            RequestAsyncTask task = new RequestAsyncTask(request);
            task.execute();
        }

    }

    public void uploadImage(final String path, final String msg) {
        new AsyncTask<Void, Void, Void>() {
            byte[] data = null;
            Bitmap bi;

            @Override
            protected Void doInBackground(Void... params) {
                int rotate = ImageUtils.getCameraPhotoOrientation(path);
                bi = ImageUtils.decodeFile(new File(path), 1024);
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                bi = Bitmap.createBitmap(bi, 0, 0, bi.getWidth(),
                        bi.getHeight(), matrix, true);
                return null;
            }

            @Override
            protected void onPostExecute(final Void result1) {
                Session session = Session.getActiveSession();

                if (session != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (bi != null) {
                        bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        bi.recycle();
                    }
                    data = baos.toByteArray();
                    Bundle postParams = new Bundle();
                    postParams.putString("message", msg);
                    postParams.putByteArray("source", data);
                    Request.Callback callback = new Request.Callback() {
                        public void onCompleted(Response response) {
                            FacebookRequestError error = response.getError();
                            if (error != null) {
                                if (eventHandler != null) {
                                    Log.sysOut("$#$#$ " + error);
                                    eventHandler.onFacebookError(error
                                            .getErrorMessage());
                                    eventHandler.stopProgress();
                                }
                                return;
                            }
                            data = null;
                            if (bi != null)
                                bi.recycle();
                            System.gc();
                            if (eventHandler != null) {
                                eventHandler.stopProgress();
                                Toast.makeText(activity, "Image Uploaded!",
                                        Toast.LENGTH_SHORT).show();
                                eventHandler.onRecievePost(msg);
                            }
                        }
                    };
                    Toast.makeText(activity, "Uploading your image!",
                            Toast.LENGTH_SHORT).show();
                    Request request = new Request(session, "me/photos",
                            postParams, HttpMethod.POST, callback);

                    RequestAsyncTask task = new RequestAsyncTask(request);
                    task.execute();
                } else {
                    if (bi != null)
                        bi.recycle();
                }
                super.onPostExecute(result1);
            }
        }.execute(null, null);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.FACEBOOK_SHIELD.getId()) {
            lastPost = frame.getArgumentAsString(0);
            if (isFacebookLoggedInAlready()) {
                if (ConnectionDetector.isConnectingToInternet(getApplication()
                        .getApplicationContext())) {
                    if (eventHandler != null)
                        eventHandler.startProgress();
                    if (frame.getFunctionId() == UPDATE_STATUS_METHOD_ID)
                        publishStory(lastPost);
                    else if (frame.getFunctionId() == UPLOAD_PHOTO_METHOD_ID) {
                        String imgPath = null;
                        byte sourceFolderId = frame.getArgument(1)[0];
                        if (sourceFolderId == CameraUtils.FROM_ONESHEELD_FOLDER)
                            imgPath = CameraUtils
                                    .getLastCapturedImagePathFromOneSheeldFolder(activity);
                        else if (sourceFolderId == CameraUtils.FROM_CAMERA_FOLDER)
                            imgPath = CameraUtils
                                    .getLastCapturedImagePathFromCameraFolder(activity);
                        if (imgPath != null) {
                            uploadImage(imgPath, lastPost);
                        }
                    }
                } else
                    Toast.makeText(
                            getApplication().getApplicationContext(),
                            "Please check your Internet connection and try again.",
                            Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void reset() {

    }
}
