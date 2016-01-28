package com.integreight.onesheeld.shields.fragments;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
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

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.EmailShield;
import com.integreight.onesheeld.shields.controller.EmailShield.EmailEventHandler;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.SecurePreferences;

import java.util.Arrays;

public class EmailFragment extends ShieldFragmentParent<EmailFragment> {

    TextView sendTo, subject, userName;
    Button login_bt, logout_bt;
    ProgressBar progress;
    private String userEmail;
    private Boolean isLoggedIn = false;
    private static SharedPreferences mSharedPreferences;
    private static final String PREF_EMAIL_SHIELD_USER_LOGIN = "user_login_status";
    private static final String PREF_EMAIL_SHIELD_GMAIL_ACCOUNT = "gmail_account";

    private static final String[] SCOPES = {GmailScopes.GMAIL_COMPOSE};
    GoogleAccountCredential mCredential;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;

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
        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, null));

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
                if (ConnectionDetector.isConnectingToInternet(activity)) {
                    // show dialog of registration then call add account method
                    if (mCredential.getSelectedAccountName() == null){
                        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    }
                }else
                    Toast.makeText(
                            getApplication().getApplicationContext(),
                            "Please check your Internet connection and try again.",
                            Toast.LENGTH_SHORT).show();
            }
        });

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
        // if user logged in run controller else ask for login
        userEmail = mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, "");
        isLoggedIn = mSharedPreferences.getBoolean(PREF_EMAIL_SHIELD_USER_LOGIN,false);
        if (mCredential.getSelectedAccountName() != null && userEmail != null && !userEmail.equals("") && isLoggedIn == true) { // this replaces isGmailLoggedInAlready method
            addAccount(userEmail);
        } else {
            login_bt.setVisibility(View.VISIBLE);
        }
    }

    private EmailEventHandler emailEventHandler = new EmailEventHandler() {

        @Override
        public void onSendingAuthError(String error,Intent intent,int requestCode) {
            if (canChangeUI()) {
                if (error != null && !error.equals(""))
                Toast.makeText(getApplication(), error, Toast.LENGTH_LONG).show();
                if (intent != null && requestCode != 0)
                    startActivityForResult(intent,requestCode);
            }

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
                        Toast.LENGTH_SHORT).show();
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

        @Override
        public GoogleAccountCredential getCredential() {
            return mCredential;
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

    private void addAccount(String accountName) {
        Log.d("account name ", accountName);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, accountName);
        editor.commit();
        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, null));

        ((EmailShield) getApplication().getRunningShields().get(getControllerTag())).setEmailEventHandler(emailEventHandler);
        if (canChangeUI()) {
            login_bt.setVisibility(View.INVISIBLE);
            logout_bt.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            userName.setText(accountName);
        }
    }

    private void logoutGmailAccount() {
        mCredential.setSelectedAccountName(null);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT);
        editor.remove(PREF_EMAIL_SHIELD_USER_LOGIN);
        editor.commit();
        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, null));

        login_bt.setVisibility(View.VISIBLE);
        logout_bt.setVisibility(View.INVISIBLE);
        userName.setVisibility(View.INVISIBLE);
        subject.setText("");
        sendTo.setText("");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == activity.RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, accountName);
                        editor.putBoolean(PREF_EMAIL_SHIELD_USER_LOGIN, true);
                        editor.apply();
                        addAccount(accountName);
                        ((EmailShield) getApplication().getRunningShields().get(getControllerTag())).sendTestRequest(mCredential);
                        Toast.makeText(activity.getApplicationContext(), accountName, Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == activity.RESULT_CANCELED) {
                    Toast.makeText(activity.getApplicationContext(), "Account unspecified.", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (requestCode != activity.RESULT_OK && mCredential.getSelectedAccountName() == null)
                    startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                break;
        }
        // if user logged in run controller else ask for login
        if (mCredential.getSelectedAccountName() != null) { // this replaces isGmailLoggedInAlready method
            userEmail = mSharedPreferences.getString(
                    PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, "");
            ((EmailShield) getApplication().getRunningShields().get(getControllerTag())).setEmailEventHandler(emailEventHandler);
            login_bt.setVisibility(View.INVISIBLE);
            logout_bt.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            userName.setText(userEmail);
        } else {
            login_bt.setVisibility(View.VISIBLE);
            logout_bt.setVisibility(View.INVISIBLE);
            userName.setVisibility(View.INVISIBLE);
        }
    }
}
