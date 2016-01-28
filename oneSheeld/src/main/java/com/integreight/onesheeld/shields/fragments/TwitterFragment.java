package com.integreight.onesheeld.shields.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.TwitterShield;
import com.integreight.onesheeld.shields.controller.TwitterShield.TwitterEventHandler;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

public class TwitterFragment extends ShieldFragmentParent<TwitterFragment> {

    LinearLayout lastTweetTextContainer;
    OneSheeldTextView userNameTextView;
    Button twitterLogin;
    Button twitterLogout;
    ProgressBar progress;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.twitter_shield_fragment_layout,
                container, false);

    }

    @Override
    public void doOnStart() {
        initializeFirmata();
        if ((getApplication().getRunningShields().get(
                getControllerTag())) != null
                && ((TwitterShield) getApplication().getRunningShields().get(
                getControllerTag())).isTwitterLoggedInAlready()) {
            buttonToLoggedIn();
        } else {
            buttonToLoggedOut();
        }
        twitterLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (ConnectionDetector.isConnectingToInternet(activity))
                    ((TwitterShield) getApplication().getRunningShields().get(
                            getControllerTag())).login();
                else
                    Toast.makeText(
                            getApplication().getApplicationContext(),
                            R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
                            Toast.LENGTH_SHORT).show();

            }
        });
        twitterLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                logoutFromTwitter();
            }
        });

    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        lastTweetTextContainer = (LinearLayout) v.findViewById(R.id.tweetsCont);
        userNameTextView = (OneSheeldTextView) v
                .findViewById(R.id.twitter_shield_username_textview);
        twitterLogin = (Button) v.findViewById(R.id.login);
        twitterLogout = (Button) v.findViewById(R.id.logout);
        progress = (ProgressBar) v.findViewById(R.id.progress);
    }

    private TwitterEventHandler twitterEventHandler = new TwitterEventHandler() {

        @Override
        public void onRecieveTweet(final String tweet) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        OneSheeldTextView tweetItem = (OneSheeldTextView) activity
                                .getLayoutInflater().inflate(
                                        R.layout.tweet_item,
                                        lastTweetTextContainer, false);
                        tweetItem.setText(tweet);
                        lastTweetTextContainer.addView(tweetItem);
                        ((ScrollView) lastTweetTextContainer.getParent())
                                .invalidate();
                    }
                });
            }
        }

        @Override
        public void onTwitterLoggedIn(final String userName) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        userNameTextView.setText(activity.getString(R.string.twitter_logged_in_as)+": @" + userName);
                        buttonToLoggedIn();
                    }
                });
            }
        }

        @Override
        public void onTwitterError(final String error) {
            // TODO Auto-generated method stub

            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(activity, error, Toast.LENGTH_SHORT)
                                .show();

                    }
                });
            }

        }

        @Override
        public void startProgress() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (progress != null && canChangeUI()) {
                        progress.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @Override
        public void stopProgress() {
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (progress != null && canChangeUI()) {
                        progress.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public void onImageUploaded(final String tweet) {
            // TODO Auto-generated method stub
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        OneSheeldTextView tweetItem = (OneSheeldTextView) activity
                                .getLayoutInflater().inflate(
                                        R.layout.tweet_item,
                                        lastTweetTextContainer, false);
                        tweetItem.setText(tweet);
                        lastTweetTextContainer.addView(tweetItem);
                        ((ScrollView) lastTweetTextContainer.getParent())
                                .invalidate();
                    }
                }
            });
        }

        @Override
        public void onDirectMessageSent(final String userHandle,
                                        final String msg) {
            // TODO Auto-generated method stub
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        OneSheeldTextView tweetItem = (OneSheeldTextView) activity
                                .getLayoutInflater().inflate(
                                        R.layout.tweet_item,
                                        lastTweetTextContainer, false);
                        tweetItem.setText(activity.getString(R.string.twitter_to)+": " + userHandle + ", "+activity.getString(R.string.twitter_message)+": "
                                + msg);
                        lastTweetTextContainer.addView(tweetItem);
                        ((ScrollView) lastTweetTextContainer.getParent())
                                .invalidate();
                    }
                }
            });
        }

        @Override
        public void onNewTrackedKeyword(final String word) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(activity, activity.getString(R.string.twitter_twitter_tracks_new_keyword_toast)+": " + word, Toast.LENGTH_SHORT)
                                .show();

                    }
                });
            }
        }

        @Override
        public void onNewTrackedKeywordRemoved(final String word) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(activity, activity.getString(R.string.twitter_twitter_stopped_tracking_keyword_toast)+": " + word, Toast.LENGTH_SHORT)
                                .show();

                    }
                });
            }
        }

        @Override
        public void onNewTrackedTweetFound(final String tweet) {
            // TODO Auto-generated method stub
            if (canChangeUI()) {
                uiHandler.removeCallbacksAndMessages(null);
                uiHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(activity, activity.getString(R.string.twitter_tracked_tweet_found_toast)+": " + tweet, Toast.LENGTH_SHORT)
                                .show();

                    }
                });
            }
        }

    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null)
            getApplication().getRunningShields().put(getControllerTag(),
                    new TwitterShield(activity, getControllerTag()));
        ((TwitterShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setTwitterEventHandler(twitterEventHandler);
        checkLogin();
    }

    private void checkLogin() {
        if ((getApplication().getRunningShields().get(
                getControllerTag())) != null
                && ((TwitterShield) getApplication().getRunningShields().get(
                getControllerTag())).isTwitterLoggedInAlready()) {
            userNameTextView.setVisibility(View.VISIBLE);
            userNameTextView.setText(activity.getString(R.string.twitter_logged_in_as)+": @"
                    + ((TwitterShield) getApplication().getRunningShields()
                    .get(getControllerTag())).getUsername());
            twitterLogout.setVisibility(View.VISIBLE);
            twitterLogin.setVisibility(View.INVISIBLE);
            lastTweetTextContainer.setVisibility(View.VISIBLE);
            lastTweetTextContainer.removeAllViews();
            if(getView()!=null)
            getView().invalidate();
        } else {
            userNameTextView.setVisibility(View.INVISIBLE);
            userNameTextView.setText("");
            twitterLogout.setVisibility(View.INVISIBLE);
            twitterLogin.setVisibility(View.VISIBLE);
            lastTweetTextContainer.setVisibility(View.INVISIBLE);
            lastTweetTextContainer.removeAllViews();
            if(getView()!=null)
                getView().invalidate();
        }
    }

    private void logoutFromTwitter() {
        // Clear the shared preferences
        ((TwitterShield) getApplication().getRunningShields().get(
                getControllerTag())).logoutFromTwitter();
        buttonToLoggedOut();
    }

    private void buttonToLoggedOut() {
        if (twitterLogout != null) {
            twitterLogout.setVisibility(View.INVISIBLE);
        }
        if (twitterLogin != null) {
            twitterLogin.setVisibility(View.VISIBLE);
        }
        if (userNameTextView != null) {
            userNameTextView.setVisibility(View.INVISIBLE);
        }
        if (lastTweetTextContainer != null) {
            lastTweetTextContainer.setVisibility(View.INVISIBLE);
            lastTweetTextContainer.removeAllViews();
        }
        if(getView()!=null)
            getView().invalidate();
    }

    private void buttonToLoggedIn() {
        if (twitterLogout != null) {
            twitterLogout.setVisibility(View.VISIBLE);
        }
        if (twitterLogin != null) {
            twitterLogin.setVisibility(View.INVISIBLE);
        }
        if (userNameTextView != null) {
            userNameTextView.setVisibility(View.VISIBLE);
        }
        if (lastTweetTextContainer != null) {
            lastTweetTextContainer.setVisibility(View.VISIBLE);
            lastTweetTextContainer.removeAllViews();
        }
        if(getView()!=null)
            getView().invalidate();
    }

    @Override
    public void doOnServiceConnected() {
    }

}
