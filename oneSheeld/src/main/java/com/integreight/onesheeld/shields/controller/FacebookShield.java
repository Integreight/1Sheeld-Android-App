package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ApiObjects;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.shields.controller.utils.ImageUtils;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class FacebookShield extends ControllerParent<FacebookShield> {
    private static FacebookEventHandler eventHandler;
    private String lastPost;
    private Fragment fragment;

    private static final byte UPDATE_STATUS_METHOD_ID = (byte) 0x01;
    private static final byte UPLOAD_PHOTO_METHOD_ID = (byte) 0x02;

    private static final String PERMISSION = "publish_actions";
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    public String getLastPost() {
        return lastPost;
    }

    public FacebookShield() {
        super();
    }

    private void initFBSdk() {
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.setApplicationId(ApiObjects.facebook.get("app_id"));
            FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        }
        callbackManager = CallbackManager.Factory.create();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (eventHandler != null) {
                    if (currentProfile != null)
                        eventHandler.onFacebookLoggedIn();
                }
            }
        };
    }

    @Override
    public ControllerParent<FacebookShield> init(String tag) {
        initFBSdk();
        return super.init(tag);
    }

    public FacebookShield(Activity activity, String tag, Fragment fragment,
                          Bundle savedInstanceState) {
        super(activity, tag);
        initFBSdk();
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
        ArrayList<String> perms = new ArrayList<>();
        perms.add("publish_actions");
        LoginManager.getInstance().logInWithPublishPermissions(fragment, perms);
    }

    public void logoutFromFacebook() {
        LoginManager.getInstance().logOut();
    }

    public String getUsername() {
        Profile profile = Profile.getCurrentProfile();
        return profile != null ? profile.getName() : "";
    }

    public boolean isFacebookLoggedInAlready() {
        return AccessToken.getCurrentAccessToken() != null;
    }


    private void publishStory(final String message) {

        if (AccessToken.getCurrentAccessToken() != null) {

            Bundle postParams = new Bundle();
            postParams.putString("message", message);
            GraphRequest.Callback callback = new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    FacebookRequestError error = graphResponse.getError();
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

            GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "feed", postParams,
                    HttpMethod.POST, callback);

            request.executeAsync();
        } else if (eventHandler != null) eventHandler.onFacebookError(activity.getString(R.string.facebook_you_must_login_first_toast));

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

                if (AccessToken.getCurrentAccessToken() != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (bi != null) {
                        bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        bi.recycle();
                    }
                    data = baos.toByteArray();
                    Bundle postParams = new Bundle();
                    postParams.putString("message", msg);
                    postParams.putByteArray("source", data);
                    GraphRequest.Callback callback = new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
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
                                Toast.makeText(activity, R.string.facebook_image_uploaded,
                                        Toast.LENGTH_SHORT).show();
                                eventHandler.onRecievePost(msg);
                            }
                        }
                    };
                    Toast.makeText(activity, R.string.facebook_uploading_your_image,
                            Toast.LENGTH_SHORT).show();
                    GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "me/photos",
                            postParams, HttpMethod.POST, callback);

                    request.executeAsync();
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
                                    .getLastCapturedImagePathFromOneSheeldFolder(activity,true);
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
                            R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
                            Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void preConfigChange() {
        super.preConfigChange();
    }

    @Override
    public void postConfigChange() {
        super.postConfigChange();
    }

    @Override
    public void reset() {
        profileTracker.stopTracking();
    }

    @Override
    public ControllerParent<FacebookShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        if(Build.VERSION.SDK_INT >=16)
        addRequiredPremission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkForPermissions())
            this.selectionAction.onSuccess();
        else
            this.selectionAction.onFailure();
        return super.invalidate(selectionAction, isToastable);
    }
}
