package com.integreight.onesheeld.shields.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.EmailShield;
import com.integreight.onesheeld.shields.controller.EmailShield.EmailEventHandler;
import com.integreight.onesheeld.shields.controller.utils.GmailSinginPopup;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.SecurePreferences;

public class EmailFragment extends ShieldFragmentParent<EmailFragment> {

    TextView sendTo, subject, userName;
    Button login_bt, logout_bt;
    private static SharedPreferences mSharedPreferences;
    private static final String PREF_EMAIL_SHIELD_USER_LOGIN = "user_login_status";
    private static final String PREF_EMAIL_SHIELD_GMAIL_ACCOUNT = "gmail_account";
    private static final String PREF_EMAIL_SHIELD_GMAIL_PASSWORD = "gmail_password";
    private String userEmail;
    ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.email_shield_fragment_layout, container,
                false);
    }

    @Override
    public void doOnViewCreated(View v, @Nullable Bundle savedInstanceState) {
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);

        sendTo = (TextView) v.findViewById(R.id.gmail_shield_sendto_textview);
        userName = (TextView) v
                .findViewById(R.id.gmail_shield_username_textview);
        subject = (TextView) v.findViewById(R.id.gmail_shield_subject_textview);
        login_bt = (Button) v.findViewById(R.id.login_gmail_bt);
        logout_bt = (Button) v.findViewById(R.id.logout_gmail_bt);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        login_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ConnectionDetector.isConnectingToInternet(activity))
                    // show dialog of registration then call add account method
                    new GmailSinginPopup(activity, emailEventHandler).show();
                else
                    Toast.makeText(
                            getApplication().getApplicationContext(),
                            "Please check your Internet connection and try again.",
                            Toast.LENGTH_SHORT).show();
            }
        });

        // if user logged in run controller else ask for login
        if (isGmailLoggedInAlready()) {
            userEmail = mSharedPreferences.getString(
                    PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, "");

            ((EmailShield) getApplication().getRunningShields().get(
                    getControllerTag()))
                    .setEmailEventHandler(emailEventHandler);
            login_bt.setVisibility(View.INVISIBLE);
            logout_bt.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            userName.setText(userEmail);
        } else {
            login_bt.setVisibility(View.VISIBLE);
        }

        logout_bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutGmailAccount();
            }
        });
    }
    @Override
    public void doOnStart() {
        ((EmailShield) getApplication().getRunningShields().get(
                getControllerTag()))
                .setEmailEventHandler(emailEventHandler);
    }

    private boolean isGmailLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_EMAIL_SHIELD_USER_LOGIN,
                false);
    }

    private EmailEventHandler emailEventHandler = new EmailEventHandler() {

        @Override
        public void onSendingAuthError(String error) {
            if (canChangeUI())
                Toast.makeText(getApplication(), error, Toast.LENGTH_LONG)
                        .show();

        }

        @Override
        public void onEmailsent(final String email_send_to,
                                final String subject_text) {
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (canChangeUI()) {
                        sendTo.setText(email_send_to);
                        subject.setText(subject_text);
                    }
                }
            });
        }

        @Override
        public void onSuccess() {
            if (canChangeUI())
                Toast.makeText(getApplication(), "Email sent Successful",
                        Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoginSuccess(final String userName, final String password) {
            uiHandler.removeCallbacksAndMessages(null);
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    addAccount(userName, password);
                }
            });
        }

        @Override
        public void onEmailnotSent(String message_not_sent) {

            if (canChangeUI()) {
                Toast.makeText(getApplication(), message_not_sent,
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void startProgress() {
            // TODO Auto-generated method stub
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
            // TODO Auto-generated method stub
            uiHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (progress != null && canChangeUI()) {
                        progress.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    private void initializeFirmata() {
        if (getApplication().getRunningShields().get(getControllerTag()) == null) {
            getApplication().getRunningShields().put(getControllerTag(),
                    new EmailShield(activity, getControllerTag()));
            ((EmailShield) getApplication().getRunningShields().get(
                    getControllerTag()))
                    .setEmailEventHandler(emailEventHandler);
        }
    }

    @Override
    public void doOnServiceConnected() {
        initializeFirmata();
    }

    private void addAccount(String accountName, String password) {
        Log.d("account name ", accountName);
        // save in share perefrences
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, accountName);
        // encrypt password before saved in share perefrence...
        byte[] b = SecurePreferences.convertStirngToByteArray(password);
        // encrypt
        try {
            byte[] key = SecurePreferences.generateKey();
            byte[] encryptedPassword = SecurePreferences.encrypt(key, b);
            String encryptedPassword_str = Base64.encodeToString(
                    encryptedPassword, Base64.DEFAULT);
            editor.putString(PREF_EMAIL_SHIELD_GMAIL_PASSWORD,
                    encryptedPassword_str);
            editor.putBoolean(PREF_EMAIL_SHIELD_USER_LOGIN, true);
            // Commit the edits!
            editor.commit();
            ((EmailShield) getApplication().getRunningShields().get(
                    getControllerTag()))
                    .setEmailEventHandler(emailEventHandler);
            if (canChangeUI()) {
                login_bt.setVisibility(View.INVISIBLE);
                logout_bt.setVisibility(View.VISIBLE);
                userName.setVisibility(View.VISIBLE);
                userName.setText(accountName);
            }

        } catch (Exception e) {
            Log.d("Email", "EmaiFragment:: filed in password encryption");
        }

    }

    private void logoutGmailAccount() {
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT);
        e.remove(PREF_EMAIL_SHIELD_GMAIL_PASSWORD);
        e.remove(PREF_EMAIL_SHIELD_USER_LOGIN);
        e.commit();
        login_bt.setVisibility(View.VISIBLE);
        logout_bt.setVisibility(View.INVISIBLE);
        userName.setVisibility(View.INVISIBLE);
        subject.setText("");
        sendTo.setText("");

    }

}
