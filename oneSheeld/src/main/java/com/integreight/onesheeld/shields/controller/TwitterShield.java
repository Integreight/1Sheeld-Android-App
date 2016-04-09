package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ApiObjects;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.shields.controller.utils.ImageUtils;
import com.integreight.onesheeld.shields.controller.utils.TwitterAuthorization;
import com.integreight.onesheeld.shields.controller.utils.TwitterDialog;
import com.integreight.onesheeld.shields.controller.utils.TwitterDialogListener;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterShield extends ControllerParent<TwitterShield> {
    private TwitterEventHandler eventHandler;
    private String lastTweet;
    private static final byte UPDATE_STATUS_METHOD_ID = (byte) 0x01;
    private static final byte UPDATE_SEND_MESSAGE_METHOD_ID = (byte) 0x02;
    private static final byte UPLOAD_PHOTO_METHOD_ID = (byte) 0x03;
    private static final byte TRACK_KEYWORD_METHOD_ID = (byte) 0x04;
    private static final byte STOP_TRACKING_KEYWORD_METHOD_ID = (byte) 0x05;
    private static final byte GET_TWEET = (byte) 0x01;

    private boolean thereIsAConnectionRequest;
    private boolean isTwitterStreamConnecting;
    TwitterStream twitterStream;
    private List<String> trackedKeywords;

    public String getUsername() {
        return mSharedPreferences.getString(PREF_KEY_TWITTER_USERNAME, "");
    }

    // Preference Constants
    // static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    static final String PREF_KEY_TWITTER_USERNAME = "TwitterUsername";

    final String PREFS_NAME = "pref";
    TwitterFactory factory;
    Twitter twitter;
    RequestToken requestToken;
    private final String CALLBACKURL = "oob";
    String authUrl;

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;


    public String getLastTweet() {
        return lastTweet;
    }

    public TwitterShield() {
        super();
    }


    @Override
    public ControllerParent<TwitterShield> init(String tag) {
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
        trackedKeywords = new ArrayList<String>();
        thereIsAConnectionRequest = false;
        isTwitterStreamConnecting = false;
//		if(isTwitterLoggedInAlready())initTwitterListening();
        return super.init(tag);
    }

    public TwitterShield(Activity activity, String tag) {
        super(activity, tag);
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
        trackedKeywords = new ArrayList<String>();
        thereIsAConnectionRequest = false;
        isTwitterStreamConnecting = false;
    }

    public void setTwitterEventHandler(TwitterEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void tweet(final String tweet) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(ApiObjects.twitter.get("consumer_key"));
        cb.setOAuthConsumerSecret(ApiObjects.twitter.get("consumer_secret"));
        cb.setOAuthAccessToken(mSharedPreferences.getString(
                PREF_KEY_OAUTH_TOKEN, null));
        cb.setOAuthAccessTokenSecret(mSharedPreferences.getString(
                PREF_KEY_OAUTH_SECRET, null));
        factory = new TwitterFactory(cb.build());
        twitter = factory.getInstance();
        AccessToken accestoken = new AccessToken(mSharedPreferences.getString(
                PREF_KEY_OAUTH_TOKEN, null), mSharedPreferences.getString(
                PREF_KEY_OAUTH_SECRET, null));
        twitter.setOAuthAccessToken(accestoken);
        final StatusUpdate st = new StatusUpdate(tweet);
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    twitter.updateStatus(st);
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Toast.makeText(activity, R.string.twitter_tweet_posted_toast,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (TwitterException e) {
                    Log.e("TAG",
                            "TwitterShield::StatusUpdate::TwitterException", e);
                    if (eventHandler != null)
                        eventHandler.onTwitterError(e.getErrorMessage());
                }
                if (eventHandler != null)
                    eventHandler.stopProgress();

            }
        }).start();
    }

    public void sendDirectMessage(String userHandle, final String msg) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(ApiObjects.twitter.get("consumer_key"));
        cb.setOAuthConsumerSecret(ApiObjects.twitter.get("consumer_secret"));
        cb.setOAuthAccessToken(mSharedPreferences.getString(
                PREF_KEY_OAUTH_TOKEN, null));
        cb.setOAuthAccessTokenSecret(mSharedPreferences.getString(
                PREF_KEY_OAUTH_SECRET, null));
        factory = new TwitterFactory(cb.build());
        twitter = factory.getInstance();
        AccessToken accestoken = new AccessToken(mSharedPreferences.getString(
                PREF_KEY_OAUTH_TOKEN, null), mSharedPreferences.getString(
                PREF_KEY_OAUTH_SECRET, null));
        twitter.setOAuthAccessToken(accestoken);
        if (!userHandle.startsWith("@"))
            userHandle = "@" + userHandle;
        final String properUserHandle = userHandle;
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    twitter.sendDirectMessage(properUserHandle, msg);
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Toast.makeText(
                                    activity,
                                    activity.getString(R.string.twitter_message_sent_to_toast)+" " + properUserHandle + "!",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (TwitterException e) {
                    Log.e("TAG",
                            "TwitterShield::StatusUpdate::TwitterException", e);
                    if (eventHandler != null)
                        eventHandler.onTwitterError(e.getErrorMessage());
                }
                if (eventHandler != null)
                    eventHandler.stopProgress();

            }
        }).start();
    }

    public void login() {
        factory = new TwitterFactory();
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(ApiObjects.twitter.get("consumer_key"),
                ApiObjects.twitter.get("consumer_secret"));
        if (mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null) != null
                && mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null) != null) {
            AccessToken accestoken = new AccessToken(
                    mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, null),
                    mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, null));
            twitter.setOAuthAccessToken(accestoken);
        } else {
            new AsyncTask<Void, Void, Void>() {
                ProgressDialog prog;

                @Override
                protected void onPreExecute() {
                    prog = new ProgressDialog(activity);
                    prog.setMessage(activity.getString(R.string.twitter_please_wait));
                    prog.setCancelable(false);
                    prog.show();
                    super.onPreExecute();
                }

                ;

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        requestToken = twitter
                                .getOAuthRequestToken(CALLBACKURL);
                        authUrl = requestToken.getAuthenticationURL();
                    } catch (TwitterException e) {
                        Log.e("TAG",
                                "TwitterShield::requestToken::TwitterException",
                                e);
                        if (eventHandler != null)
                            eventHandler.onTwitterError(e.getErrorMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {

                    final TwitterDialogListener listener = new TwitterDialogListener() {
                        @Override
                        public void onComplete() {
                            Editor editor = mSharedPreferences
                                    .edit();
                            editor.putString(PREF_KEY_OAUTH_TOKEN,
                                    TwitterAuthorization.FETCHED_ACCESS_TOKEN);
                            editor.putString(PREF_KEY_OAUTH_SECRET,
                                    TwitterAuthorization.FETCHED_SECRET_TOKEN);
                            editor.putString(PREF_KEY_TWITTER_USERNAME,
                                    TwitterAuthorization.TWITTER_USER_NAME);
                            editor.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                            if (eventHandler != null)
                                eventHandler
                                        .onTwitterLoggedIn(TwitterAuthorization.TWITTER_USER_NAME);
                            // Commit the edits!
                            editor.commit();
//							initTwitterListening();

                        }

                        @Override
                        public void onError(String error) {
                            if (error != null && !error.isEmpty())
                                Toast.makeText(
                                        getApplication()
                                                .getApplicationContext(),
                                        error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(
                                    getApplication().getApplicationContext(),
                                    R.string.twitter_twitter_login_canceled_toast,
                                    Toast.LENGTH_SHORT).show();
                        }
                    };
                    new Handler().post(new Runnable() {

                        @Override
                        public void run() {
                            TwitterDialog mDialog = new TwitterDialog(
                                    getActivity(), authUrl, twitter,
                                    requestToken, listener);
                            mDialog.show();
                            if (prog != null && prog.isShowing()) {
                                try {
                                    prog.dismiss();
                                    prog.cancel();
                                } catch (Exception e) {
                                }
                            }
                        }
                    });

                    super.onPostExecute(result);
                }

            }.execute(null, null);

        }
    }

    public static interface TwitterEventHandler {
        void onRecieveTweet(String tweet);

        void onImageUploaded(String tweet);

        void onDirectMessageSent(String userHandle, String msg);

        void onTwitterLoggedIn(String userName);

        void onTwitterError(String error);

        void onNewTrackedKeyword(String word);

        void onNewTrackedKeywordRemoved(String word);

        void onNewTrackedTweetFound(String tweet);

        void startProgress();

        void stopProgress();
    }

    public boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    public void logoutFromTwitter() {
        stopListeningOnAKeyword();
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.remove(PREF_KEY_TWITTER_USERNAME);
        e.commit();
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.TWITTER_SHIELD.getId()) {

            if (isTwitterLoggedInAlready())

                if (ConnectionDetector.isConnectingToInternet(getApplication()
                        .getApplicationContext())) {
                    if (eventHandler != null)
                        eventHandler.startProgress();
                    if (frame.getFunctionId() == UPDATE_STATUS_METHOD_ID) {
                        lastTweet = frame.getArgumentAsString(0);
                        tweet(lastTweet);
                        if (eventHandler != null)
                            eventHandler.onRecieveTweet(lastTweet);
                    } else if (frame.getFunctionId() == UPLOAD_PHOTO_METHOD_ID) {
                        lastTweet = frame.getArgumentAsString(0);
                        byte sourceFolderId = frame.getArgument(1)[0];
                        String imgPath = null;
                        if (sourceFolderId == CameraUtils.FROM_ONESHEELD_FOLDER)
                            imgPath = CameraUtils
                                    .getLastCapturedImagePathFromOneSheeldFolder(activity,true);
                        else if (sourceFolderId == CameraUtils.FROM_CAMERA_FOLDER)
                            imgPath = CameraUtils
                                    .getLastCapturedImagePathFromCameraFolder(activity);
                        if (imgPath != null) {
                            uploadPhoto(imgPath, lastTweet);
                            if (eventHandler != null)
                                eventHandler.onImageUploaded(lastTweet);
                        }
                    } else if (frame.getFunctionId() == UPDATE_SEND_MESSAGE_METHOD_ID) {
                        String userHandle = frame.getArgumentAsString(0);
                        String msg = frame.getArgumentAsString(1);
                        sendDirectMessage(userHandle, msg);
                        if (eventHandler != null)
                            eventHandler.onDirectMessageSent(userHandle, msg);
                    } else if (frame.getFunctionId() == TRACK_KEYWORD_METHOD_ID) {
                        String keyword = frame.getArgumentAsString(0);
                        if (trackedKeywords.isEmpty() && twitterStream == null)
                            initTwitterListening();
                        if (!trackedKeywords.contains(keyword.toLowerCase())) {
                            trackedKeywords.add(keyword.toLowerCase());
                            FilterQuery query = new FilterQuery();
                            String[] keywordsStrings = new String[trackedKeywords.size()];
                            query.track(trackedKeywords.toArray(keywordsStrings));
                            twitterStream.cleanUp();
                            twitterStream.shutdown();
                            if (!isTwitterStreamConnecting && !thereIsAConnectionRequest) {
                                filterTwitterStream(query);
                            } else {
                                thereIsAConnectionRequest = true;
                            }
                        }
                        if (eventHandler != null)
                            eventHandler.onNewTrackedKeyword(keyword);
                    } else if (frame.getFunctionId() == STOP_TRACKING_KEYWORD_METHOD_ID) {
                        String keyword = frame.getArgumentAsString(0);
                        if (trackedKeywords.contains(keyword.toLowerCase())) {
                            trackedKeywords.remove(keyword.toLowerCase());
                            if (trackedKeywords.isEmpty() && twitterStream != null)
                                stopListeningOnAKeyword();
                            else if (twitterStream != null) {
                                FilterQuery query = new FilterQuery();
                                String[] keywordsStrings = new String[trackedKeywords.size()];
                                query.track(trackedKeywords.toArray(keywordsStrings));
                                twitterStream.cleanUp();
                                twitterStream.shutdown();
                                if (!isTwitterStreamConnecting && !thereIsAConnectionRequest) {
                                    filterTwitterStream(query);
                                } else {
                                    thereIsAConnectionRequest = true;
                                }
                            }
                        }
                        if (eventHandler != null)
                            eventHandler.onNewTrackedKeywordRemoved(keyword);
                    }

                } else
                    Toast.makeText(
                            getApplication().getApplicationContext(),
                            R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
                            Toast.LENGTH_SHORT).show();
        }

    }

    public void uploadPhoto(String filePath, String msg) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(ApiObjects.twitter.get("consumer_key"));
        cb.setOAuthConsumerSecret(ApiObjects.twitter.get("consumer_secret"));
        cb.setOAuthAccessToken(mSharedPreferences.getString(
                PREF_KEY_OAUTH_TOKEN, null));
        cb.setOAuthAccessTokenSecret(mSharedPreferences.getString(
                PREF_KEY_OAUTH_SECRET, null));
        factory = new TwitterFactory(cb.build());
        twitter = factory.getInstance();
        AccessToken accestoken = new AccessToken(mSharedPreferences.getString(
                PREF_KEY_OAUTH_TOKEN, null), mSharedPreferences.getString(
                PREF_KEY_OAUTH_SECRET, null));
        twitter.setOAuthAccessToken(accestoken);
        final Handler handler = new Handler(Looper.getMainLooper());
        new AsyncTask<String, Void, Status>() {
            Bitmap bitmap;
            Matrix matrix;
            ByteArrayOutputStream bos;
            byte[] bitmapdata;
            ByteArrayInputStream bs;

            @Override
            protected void onCancelled() {
                if (bitmap != null)
                    bitmap.recycle();
                bitmap = null;
                matrix = null;
                bos = null;
                bitmapdata = null;
                System.gc();
                super.onCancelled();
            }

            @Override
            protected twitter4j.Status doInBackground(String... params) {
                if (twitter != null)
                    try {
                        StatusUpdate status = new StatusUpdate(params[1]);
                        File f = new File(params[0]);
                        if (f.exists()) {
                            int rotate = ImageUtils
                                    .getCameraPhotoOrientation(params[0]);
                            bitmap = ImageUtils.decodeFile(new File(params[0]),
                                    1024);
                            matrix = new Matrix();
                            matrix.postRotate(rotate);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true);
                            bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                                    bos);
                            bitmapdata = bos.toByteArray();
                            bs = new ByteArrayInputStream(bitmapdata);
                            status.setMedia("Image", bs);
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    Toast.makeText(activity,
                                            R.string.twitter_uploading_the_image_toast,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                            return twitter.updateStatus(status);
                        } else if (eventHandler != null) {
                            System.err.println("File Not Found  " + params[0]);
                            eventHandler.onTwitterError(activity.getString(R.string.twitter_file_not_found)+"   "
                                    + params[0]);
                            eventHandler.stopProgress();
                        }

                    } catch (TwitterException e) {
                        if (eventHandler != null) {
                            eventHandler.stopProgress();
                            eventHandler.onTwitterError(e.getErrorMessage());
                        }
                        e.printStackTrace();
                    }
                return null;
            }

            @Override
            protected void onPostExecute(twitter4j.Status result) {
                if (bitmap != null)
                    bitmap.recycle();
                bitmap = null;
                matrix = null;
                bos = null;
                bitmapdata = null;
                if (eventHandler != null)
                    eventHandler.stopProgress();
                if (result != null)
                    Toast.makeText(activity,
                            R.string.twitter_image_uploaded_and_tweet_posted_toast,
                            Toast.LENGTH_LONG).show();
                System.gc();
                super.onPostExecute(result);
            }
        }.execute(filePath, msg);

    }

    private void filterTwitterStream(FilterQuery q) {
        isTwitterStreamConnecting = true;
        twitterStream.filter(q);
    }

    private void initTwitterListening() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(ApiObjects.twitter.get("consumer_key"));
        cb.setOAuthConsumerSecret(ApiObjects.twitter.get("consumer_secret"));
        cb.setOAuthAccessToken(mSharedPreferences.getString(
                PREF_KEY_OAUTH_TOKEN, null));
        cb.setOAuthAccessTokenSecret(mSharedPreferences.getString(
                PREF_KEY_OAUTH_SECRET, null));

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addConnectionLifeCycleListener(new ConnectionLifeCycleListener() {

            @Override
            public void onDisconnect() {
                // TODO Auto-generated method stub
                isTwitterStreamConnecting = false;

                if (thereIsAConnectionRequest) {
                    FilterQuery query = new FilterQuery();
                    String[] keywordsStrings = new String[trackedKeywords.size()];
                    query.track(trackedKeywords.toArray(keywordsStrings));
                    twitterStream.filter(query);
                    thereIsAConnectionRequest = false;
                }
            }

            @Override
            public void onConnect() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onCleanUp() {
                // TODO Auto-generated method stub
                isTwitterStreamConnecting = false;

                if (thereIsAConnectionRequest) {
                    FilterQuery query = new FilterQuery();
                    String[] keywordsStrings = new String[trackedKeywords.size()];
                    query.track(trackedKeywords.toArray(keywordsStrings));
                    twitterStream.filter(query);
                    thereIsAConnectionRequest = false;
                }
            }
        });

        StatusListener listener = new StatusListener() {
            public void onScrubGeo(long arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStallWarning(StallWarning arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStatus(twitter4j.Status arg0) {
                // TODO Auto-generated method stub
                Log.d("tweet", arg0.getText());
                ShieldFrame frame = new ShieldFrame(
                        UIShield.TWITTER_SHIELD.getId(), GET_TWEET);
                frame.addArgument(arg0.getUser().getName());
                frame.addArgument(arg0.getText());
                sendShieldFrame(frame, true);
            }

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTrackLimitationNotice(int arg0) {
                // TODO Auto-generated method stub

            }
        };
        twitterStream.addListener(listener);
    }

    private void stopListeningOnAKeyword() {
        if (twitterStream == null)
            return;
        twitterStream.clearListeners();
        twitterStream.cleanUp();
        twitterStream.shutdown();
        twitterStream = null;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        stopListeningOnAKeyword();
    }
    @Override
    public ControllerParent<TwitterShield> invalidate(
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
