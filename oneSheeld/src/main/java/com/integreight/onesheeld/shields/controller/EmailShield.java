package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.shields.controller.utils.GMailSender;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.SecurePreferences;

public class EmailShield extends ControllerParent<EmailShield> {

    private EmailEventHandler eventHandler;
    private static final byte SEND_METHOD_ID = (byte) 0x01;
    private static final byte SEND_WITH_ATTACHMENT = (byte) 0x02;
    private static SharedPreferences mSharedPreferences;
    private static final String PREF_EMAIL_SHIELD_USER_LOGIN = "user_login_status";
    private static final String PREF_EMAIL_SHIELD_GMAIL_ACCOUNT = "gmail_account";
    private static final String PREF_EMAIL_SHIELD_GMAIL_PASSWORD = "gmail_password";
    private String userEmail = "";
    private String password = "";
    private static String message_body = "";
    private static String message_reciption = "";
    private static String message_subject = "";
    private static String attachment_file_path = null;

    public EmailShield() {
        super();
    }

    @Override
    public ControllerParent<EmailShield> init(String tag) {
        mSharedPreferences = getApplication().getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
        return super.init(tag);
    }

    public EmailShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public void setEmailEventHandler(EmailEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public static interface EmailEventHandler {
        void onEmailsent(String email_send_to, String subject);

        void onSendingAuthError(String error);

        void onSuccess();

        void onEmailnotSent(String message_not_sent);

        void onLoginSuccess(String userName, String password);

        void startProgress();

        void stopProgress();
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.EMAIL_SHIELD.getId()) {
            if (frame.getFunctionId() == SEND_METHOD_ID || frame.getFunctionId() == SEND_WITH_ATTACHMENT) {
                if (isLoggedIn()) {
                    // retrieve user name and password from sharedPref...
                    setUserData();
                    // send Email
                    String email_send_to = frame.getArgumentAsString(0);
                    String subject = frame.getArgumentAsString(1);
                    String body = frame.getArgumentAsString(2);
                    if (eventHandler != null)
                        eventHandler.onEmailsent(email_send_to, subject);
                    // check Internet connection
                    if (ConnectionDetector
                            .isConnectingToInternet(getApplication()
                                    .getApplicationContext())) {
                        if (frame.getFunctionId() == SEND_METHOD_ID)
                            sendGmail(email_send_to, subject, body, null);
                        else if (frame.getFunctionId() == SEND_WITH_ATTACHMENT) {
                            byte sourceFolderId = frame.getArgument(3)[0];
                            String imgPath = null;
                            if (sourceFolderId == CameraUtils.FROM_ONESHEELD_FOLDER)
                                imgPath = CameraUtils
                                        .getLastCapturedImagePathFromOneSheeldFolder(activity);
                            else if (sourceFolderId == CameraUtils.FROM_CAMERA_FOLDER)
                                imgPath = CameraUtils
                                        .getLastCapturedImagePathFromCameraFolder(activity);
                            sendGmail(email_send_to, subject, body, imgPath);
                        }
                    } else
                        Toast.makeText(
                                getApplication().getApplicationContext(),
                                "Please check your Internet connection and try again.",
                                Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void sendGmail(String email_send_to, String subject, String body, String filePath) {
        message_body = body;
        message_reciption = email_send_to;
        message_subject = subject;
        attachment_file_path = filePath;
        new sendGmailinBackground().execute();

    }

    public class sendGmailinBackground extends AsyncTask<Void, Void, Integer> {
        GMailSender sender = new GMailSender(userEmail, password);
        int result;

        @Override
        protected Integer doInBackground(Void... params) {
            if (eventHandler != null)
                eventHandler.startProgress();
            try {
                result = (attachment_file_path == null) ? sender.sendMail(message_subject, message_body,
                        userEmail, message_reciption) : sender.sendMail(message_subject, message_body,
                        userEmail, message_reciption, attachment_file_path);
            } catch (Exception e) {
                Log.d("SendMail", e.getMessage());
                if (eventHandler != null)
                    eventHandler.stopProgress();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            switch (result) {
                case 0:
                    if (eventHandler != null)
                        eventHandler.onSuccess();
                    break;
                case 1:
                    if (eventHandler != null)
                        eventHandler.onEmailnotSent("Authentication Failed");
                    break;
                case 2:
                    if (eventHandler != null)
                        eventHandler
                                .onEmailnotSent("message could not be sent to the recipient ");
                    break;

                default:
                    break;
            }
            if (eventHandler != null)
                eventHandler.stopProgress();
        }

    }

    public void setUserData() {
        byte[] decryptedData = null;
        byte[] encryptedPassword_bytes = null;
        // password decryption
        this.userEmail = mSharedPreferences.getString(
                PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, "");
        // decrypt
        String encryptedPassword_str = mSharedPreferences.getString(
                PREF_EMAIL_SHIELD_GMAIL_PASSWORD, "");
        if (!encryptedPassword_str.equalsIgnoreCase("")) {
            encryptedPassword_bytes = Base64.decode(encryptedPassword_str,
                    Base64.DEFAULT);
        }

        try {
            byte[] key = SecurePreferences.generateKey();
            decryptedData = SecurePreferences.decrypt(key,
                    encryptedPassword_bytes);
            this.password = SecurePreferences
                    .convertByteArrayToString(decryptedData);

        } catch (Exception e) {
            // failed to decrypt password.
            Log.d("Email", "Email Sheeld" + "failed to decrypt password");
        }
    }

    private boolean isLoggedIn() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_EMAIL_SHIELD_USER_LOGIN,
                false);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }
}
