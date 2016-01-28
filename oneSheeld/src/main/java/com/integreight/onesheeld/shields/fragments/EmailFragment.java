package com.integreight.onesheeld.shields.fragments;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;

public class EmailFragment extends ShieldFragmentParent<EmailFragment> {

    TextView sendTo, subject, userName;
    Button login_bt, logout_bt;
    ProgressBar progress;
    private String userEmail;
    private Boolean isLoggedIn = false;


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
                    if (((EmailShield) getApplication().getRunningShields().get(
                            getControllerTag())).getCredential().getSelectedAccountName() == null){
                        startActivityForResult(((EmailShield) getApplication().getRunningShields().get(
                                getControllerTag())).getCredential().newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    }
                }else
                    Toast.makeText(
                            getApplication().getApplicationContext(),
                            R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
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
        userEmail =  ((EmailShield) getApplication().getRunningShields().get(
                getControllerTag())).getUserEmail();
        isLoggedIn = ((EmailShield) getApplication().getRunningShields().get(
                getControllerTag())).isLoggedIn();
        if (((EmailShield) getApplication().getRunningShields().get(
                getControllerTag())).getCredential().getSelectedAccountName() != null && userEmail != null && !userEmail.equals("") && isLoggedIn) { // this replaces isGmailLoggedInAlready method
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
                Toast.makeText(getApplication(), R.string.email_email_sent_successfully_toast,
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

//        @Override
//        public GoogleAccountCredential getCredential() {
//            return mCredential;
//        }
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

    @Override
    public void doOnResume() {
        super.doOnResume();
                if (progress != null && canChangeUI() && ((EmailShield) getApplication().getRunningShields().get(
                        getControllerTag())).isSending()) {
                    progress.setVisibility(View.VISIBLE);
                }
    }

    private void addAccount(String accountName) {
        Log.d("account name ", accountName);
        ((EmailShield) getApplication().getRunningShields().get(getControllerTag())).setCredential(accountName);

        ((EmailShield) getApplication().getRunningShields().get(getControllerTag())).setEmailEventHandler(emailEventHandler);
        if (canChangeUI()) {
            login_bt.setVisibility(View.INVISIBLE);
            logout_bt.setVisibility(View.VISIBLE);
            userName.setVisibility(View.VISIBLE);
            userName.setText(accountName);
        }
    }

    private void logoutGmailAccount() {
        ((EmailShield) getApplication().getRunningShields().get(
                getControllerTag())).getCredential().setSelectedAccountName(null);
        ((EmailShield) getApplication().getRunningShields().get(
                getControllerTag())).logout();
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
                        ((EmailShield) getApplication().getRunningShields().get(
                                getControllerTag())).getCredential().setSelectedAccountName(accountName);
//                        ((EmailShield) getApplication().getRunningShields().get(
//                                getControllerTag())).setAccountName(accountName);
                        ((EmailShield) getApplication().getRunningShields().get(
                                getControllerTag())).setLoggedIn();
                        addAccount(accountName);
                        ((EmailShield) getApplication().getRunningShields().get(getControllerTag())).sendTestRequest(((EmailShield) getApplication().getRunningShields().get(
                                getControllerTag())).getCredential());
                        Toast.makeText(activity.getApplicationContext(), accountName, Toast.LENGTH_SHORT).show();
                    }
                } else if (resultCode == activity.RESULT_CANCELED) {
                    Toast.makeText(activity.getApplicationContext(), R.string.email_you_didnt_specify_an_account_to_use_toast, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (requestCode != activity.RESULT_OK && ((EmailShield) getApplication().getRunningShields().get(
                        getControllerTag())).getCredential().getSelectedAccountName() == null)
                    startActivityForResult(((EmailShield) getApplication().getRunningShields().get(
                            getControllerTag())).getCredential().newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                break;
        }
        // if user logged in run controller else ask for login
        if (((EmailShield) getApplication().getRunningShields().get(
                getControllerTag())).getCredential().getSelectedAccountName() != null) { // this replaces isGmailLoggedInAlready method
            userEmail = ((EmailShield) getApplication().getRunningShields().get(
                    getControllerTag())).getUserEmail();
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
