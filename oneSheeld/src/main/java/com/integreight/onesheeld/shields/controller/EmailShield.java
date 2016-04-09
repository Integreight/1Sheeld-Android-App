package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.CrashlyticsUtils;
import com.integreight.onesheeld.utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailShield extends ControllerParent<EmailShield> {

    private EmailEventHandler eventHandler;
    private static final byte SEND_METHOD_ID = (byte) 0x01;
    private static final byte SEND_WITH_ATTACHMENT = (byte) 0x02;
    private static SharedPreferences mSharedPreferences;
    private static final String PREF_EMAIL_SHIELD_USER_LOGIN = "user_login_status";
    private static final String PREF_EMAIL_SHIELD_GMAIL_ACCOUNT = "gmail_account";
    private static final int PREF_EMAIL_SHIELD_REQUEST_AUTHORIZATION = 1001;
    private static int ORDER_GET_LABELS = 0;
    private static int ORDER_SEND_EMAIL = 1;
    private String userEmail = "";
    private static String message_body = "";
    private static String message_reciption = "";
    private static String message_subject = "";
    private static String attachment_file_path = null;
    private static DataSource source;
    private static BodyPart messageFilePart,messageBodyPart;
    private volatile boolean isSending = false;
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {GmailScopes.GMAIL_COMPOSE};
    private Boolean isLoggedIn = false;

    private Queue<EmailShield.EmailRequest> emailsQueue = new ConcurrentLinkedQueue<>();

    /////
    public EmailShield() {
        super();
    }

    @Override
    public ControllerParent<EmailShield> init(String tag) {
        mSharedPreferences = activity.getApplicationContext()
                .getSharedPreferences("com.integreight.onesheeld",
                        Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, null));
        return super.init(tag);
    }

    public EmailShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public void setEmailEventHandler(EmailEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public boolean isSending(){
        return isSending;
    }
    public void setCredential(String accountName){
        setAccountName(accountName);
        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, null));
    }

    public void logout(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT);
        editor.remove(PREF_EMAIL_SHIELD_USER_LOGIN);
        editor.commit();
        mCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, null));
    }

    public GoogleAccountCredential getCredential(){
        return mCredential;
    }

    public String getUserEmail(){
        return mSharedPreferences.getString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, "");
    }

    public static interface EmailEventHandler {
        void onEmailsent(String email_send_to, String subject);

        void onSendingAuthError(String error,Intent intent,int requestCode);

        void onSuccess();

        void onEmailnotSent(String message_not_sent);

        void startProgress();

        void stopProgress();

//        GoogleAccountCredential getCredential();
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
                    // check Internet connection
                    if (ConnectionDetector
                            .isConnectingToInternet(getApplication()
                                    .getApplicationContext())) {
                        if (frame.getFunctionId() == SEND_METHOD_ID) {
//                            sendGmail(email_send_to, subject, body, null);
                            EmailRequest request=new EmailRequest(email_send_to,subject,body,null);
                            if (emailsQueue == null)
                                emailsQueue = new ConcurrentLinkedQueue<>();
                            emailsQueue.add(request);
                            checkQueue();
                        }
                        else if (frame.getFunctionId() == SEND_WITH_ATTACHMENT) {
                            byte sourceFolderId = frame.getArgument(3)[0];
                            String imgPath = null;
                            if (sourceFolderId == CameraUtils.FROM_ONESHEELD_FOLDER)
                                imgPath = CameraUtils
                                        .getLastCapturedImagePathFromOneSheeldFolder(activity,true);
                            else if (sourceFolderId == CameraUtils.FROM_CAMERA_FOLDER)
                                imgPath = CameraUtils
                                        .getLastCapturedImagePathFromCameraFolder(activity);
                            EmailRequest request=new EmailRequest(email_send_to,subject,body,imgPath);
                            if (emailsQueue == null)
                                emailsQueue = new ConcurrentLinkedQueue<>();
                            emailsQueue.add(request);
                            checkQueue();
//                            sendGmail(email_send_to, subject, body, imgPath);
                        }
                    } else
                        Toast.makeText(
                                getApplication().getApplicationContext(),
                                R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
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
        new sendGmailinBackground(mCredential,ORDER_SEND_EMAIL).execute();
    }

    public void sendTestRequest(GoogleAccountCredential credential){
        if (ConnectionDetector.isConnectingToInternet(getApplication().getApplicationContext())) {
            new sendGmailinBackground(credential, ORDER_GET_LABELS).execute();
        }
    }

    public class sendGmailinBackground extends AsyncTask<Void, Void, String> {
        private com.google.api.services.gmail.Gmail mService = null;
        private Exception mLastError = null;
        private int order = 0;

        public sendGmailinBackground(GoogleAccountCredential credential,int order){
            isSending=true;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getActivity().getString(R.string.app_name))
                    .build();
            this.order = order;
            Log.d("Email","Sending Created!");
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d("Email","Sending Started!");
            if (eventHandler != null)
                eventHandler.startProgress();
            try {
                return sendEmail();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                if (eventHandler != null)
                    eventHandler.stopProgress();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (result != null){
                if (eventHandler != null)
                    eventHandler.onSuccess();
            }
            attachment_file_path = null;
            if (eventHandler != null)
                eventHandler.stopProgress();
            isSending=false;
            Log.d("Email","Sending Ended!");
            if (emailsQueue != null && !emailsQueue.isEmpty())
                emailsQueue.poll();
            checkQueue();
        }

        @Override
        protected void onCancelled(String s) {
            //super.onCancelled();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    //showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                    if (eventHandler != null && order == ORDER_SEND_EMAIL) {
                        Log.d("Email","The following google play service error occurred:\n" + ((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                        eventHandler.onEmailnotSent(activity.getString(R.string.email_email_couldnt_be_sent_toast));
                    }
                    if(order>0)CrashlyticsUtils.logException(mLastError);
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    if (eventHandler != null)
                        eventHandler.onSendingAuthError(mLastError.getMessage(),((UserRecoverableAuthIOException) mLastError).getIntent(),PREF_EMAIL_SHIELD_REQUEST_AUTHORIZATION);
                } else {
                    if (eventHandler != null && order == ORDER_SEND_EMAIL) {
                        Log.d("Email", "The following error occurred:\n" + mLastError.getMessage());
                        eventHandler.onEmailnotSent(activity.getString(R.string.email_email_couldnt_be_sent_toast));
                    }
                    if(order>0)CrashlyticsUtils.logException(mLastError);
                }
            } else {
                if (eventHandler != null)
                    eventHandler.onEmailnotSent(activity.getString(R.string.email_email_couldnt_be_sent_toast));
            }
            if (eventHandler != null)
                eventHandler.stopProgress();
            isSending=false;
            Log.d("Email","Sending Canceled!");
            checkQueue();
        }
        File attachedFile;
        private String sendEmail() throws IOException {
            try {
                if (attachment_file_path != null)
                    attachedFile = new File(attachment_file_path);
                if (order > 0) {
                    if (attachment_file_path == null) {
                        mService.users().messages().send("me", createMessageWithEmail(createEmail(message_reciption, userEmail, message_subject, message_body))).execute();
                    }else {
                        if (attachedFile.exists())
                            mService.users().messages().send("me", createMessageWithEmail(createEmailWithAttachment(message_reciption, userEmail, message_subject, message_body, attachment_file_path))).execute();
                        else {
                            mService.users().messages().send("me", createMessageWithEmail(createEmail(message_reciption, userEmail, message_subject, message_body))).execute();
//                            cancel(true);
//                            if (eventHandler != null)
//                                eventHandler.stopProgress();
                            return "";
                        }
                    }
                }else
                    mService.users().labels().list("me").execute();
            } catch (MessagingException e) {
                e.printStackTrace();
                return null;
            }
            return "";
        }
    }

    private void setUserData() {
        this.userEmail = mSharedPreferences.getString(
                PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, "");
    }

    public void setAccountName(String accountName){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_EMAIL_SHIELD_GMAIL_ACCOUNT, accountName);
        editor.commit();
    }

    public void setLoggedIn(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(PREF_EMAIL_SHIELD_USER_LOGIN, true);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return mSharedPreferences.getBoolean(PREF_EMAIL_SHIELD_USER_LOGIN,false);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        emailsQueue = new ConcurrentLinkedQueue<>();
        isSending=false;
    }

    public static MimeMessage createEmail(String to, String from, String subject,
                                          String bodyText) throws MessagingException{
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);
        email.setFrom(fAddress);
        email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    public static MimeMessage createEmailWithAttachment(String to, String from, String subject,
                                          String bodyText,String filePath) throws MessagingException{
        File file = new File(filePath);
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        Multipart multipart = new MimeMultipart();
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);
        email.setFrom(fAddress);
        email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
        email.setSubject(subject);
        if (file.exists()) {
            source = new FileDataSource(filePath);
            messageFilePart = new MimeBodyPart();
            messageBodyPart = new MimeBodyPart();
            try {
                messageBodyPart.setText(bodyText);
                messageFilePart.setDataHandler(new DataHandler(source));
                messageFilePart.setFileName(file.getName());

                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(messageFilePart);
                email.setContent(multipart);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }else
            email.setText(bodyText);
        return email;
    }

    public static Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        email.writeTo(bytes);
        String encodedEmail = Base64.encodeToString(bytes.toByteArray(), Base64.URL_SAFE);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    @Override
    public ControllerParent<EmailShield> invalidate(
            com.integreight.onesheeld.shields.ControllerParent.SelectionAction selectionAction,
            boolean isToastable) {
        this.selectionAction = selectionAction;
        addRequiredPremission(Manifest.permission.GET_ACCOUNTS);
        if(Build.VERSION.SDK_INT >=16)
        addRequiredPremission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkForPermissions())
            this.selectionAction.onSuccess();
        else
            this.selectionAction.onFailure();
        return super.invalidate(selectionAction, isToastable);

    }

    private synchronized void checkQueue() {
        if (emailsQueue != null && !emailsQueue.isEmpty() && !isSending) {
            EmailRequest request = emailsQueue.peek();
            if(!request.canRetry()) request = emailsQueue.poll();
            request.decrementRetries();
            if (eventHandler != null)
                eventHandler.onEmailsent(request.getAddress(), request.getSubject());
            isSending=true;
            if (request.getImagePath()!=null) {
                    sendGmail(request.getAddress(), request.getSubject(), request.getBody(), request.getImagePath());
                } else {
                    sendGmail(request.getAddress(), request.getSubject(), request.getBody(), null);
                }
            Log.d("Email","Sending Requested!");
        }
    }

    public class EmailRequest{
        private String address;
        private String subject;
        private String body;
        private String imagePath;
        private int retries;

        public EmailRequest(String address, String subject, String body, String imagePath) {
            this.address=address;
            this.subject=subject;
            this.body=body;
            this.imagePath=imagePath;
            retries=3;
        }

        public boolean canRetry() {
            return retries>0;
        }

        public void decrementRetries() {
            if(retries>0)retries--;
        }

        public String getAddress() {
            return address;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public String getImagePath() {
            return imagePath;
        }
    }
}
