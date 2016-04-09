package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.Log;

public class SkypeShield extends ControllerParent<SkypeShield> {

    private SkypeEventHandler eventHandler;
    private static final byte CALL_METHOD_ID = (byte) 0x01;
    private static final byte VIDEO_METHOD_ID = (byte) 0x02;
    private static final byte CHAT_METHOD_ID = (byte) 0x03;

    public SkypeShield() {
        super();
    }

    @Override
    public ControllerParent<SkypeShield> init(String tag) {
        return super.init(tag);
    }

    public SkypeShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public void setSkypeEventHandler(SkypeEventHandler eventHandler) {
        this.eventHandler = eventHandler;

    }

    public static interface SkypeEventHandler {
        void onCall(String user);

        void onVideoCall(String user);

        void onChat(String user);

        void onError(String error);

        void onSkypeClientNotInstalled(String popMessage);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        // TODO Auto-generated method stub
        if (frame.getShieldId() == UIShield.SKYPE_SHIELD.getId()) {
            String userId = frame.getArgumentAsString(0);
            Log.d("Skype_User_ID ", userId);
            if (ConnectionDetector.isConnectingToInternet(getApplication()
                    .getApplicationContext())) {
                switch (frame.getFunctionId()) {
                    case CALL_METHOD_ID:
                        callSkypeID(userId);
                        break;
                    case VIDEO_METHOD_ID:
                        videoCallSkypeID(userId);
                        break;
                    case CHAT_METHOD_ID:
                        chatSkypeID(userId);
                        break;

                    default:
                        break;
                }
            } else
                Toast.makeText(getApplication().getApplicationContext(),
                        R.string.general_toasts_please_check_your_internet_connection_and_try_again_toast,
                        Toast.LENGTH_SHORT).show();
        }

    }

    private void callSkypeID(String userId) {
        if (isSkypeClientInstalled(getActivity().getApplicationContext())) {
            Log.d("Skype Client Installed", "Yes");
            // Create the Intent from our Skype URI
            Uri skypeUri = Uri.parse("skype:" + userId + "?call");
            Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

            // Restrict the Intent to being handled by the Skype for
            // Android client only
            myIntent.setComponent(new ComponentName("com.skype.raider",
                    "com.skype.raider.Main"));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Initiate the Intent. It should never fail since we've
            // already established the
            // presence of its handler (although there is an extremely
            // minute window where that
            // handler can go away...)
            getActivity().startActivity(myIntent);
            if (eventHandler != null)
                eventHandler.onCall(userId);

        } else {
            Log.d("Skype Client Installed", "No");
            if (eventHandler != null)
                eventHandler
                        .onSkypeClientNotInstalled(activity.getString(R.string.skype_skype_app_is_not_installed));

        }

    }

    private void videoCallSkypeID(String userId) {
        if (isSkypeClientInstalled(getActivity().getApplicationContext())) {
            Log.d("Skype Client Installed", "Yes");
            // Create the Intent from our Skype URI
            Uri skypeUri = Uri.parse("skype:" + userId + "?call&video=true");
            Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

            // Restrict the Intent to being handled by the Skype for
            // Android client only
            myIntent.setComponent(new ComponentName("com.skype.raider",
                    "com.skype.raider.Main"));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Initiate the Intent. It should never fail since we've
            // already established the
            // presence of its handler (although there is an extremely
            // minute window where that
            // handler can go away...)
            getActivity().startActivity(myIntent);
            if (eventHandler != null)
                eventHandler.onVideoCall(userId);

        } else {
            Log.d("Skype Client Installed", "No");
            if (eventHandler != null)
                eventHandler
                        .onSkypeClientNotInstalled(activity.getString(R.string.skype_skype_app_is_not_installed));

        }

    }

    private void chatSkypeID(String userId) {
        if (isSkypeClientInstalled(getActivity().getApplicationContext())) {
            Log.d("Skype Client Installed", "Yes");
            // Create the Intent from our Skype URI
            Uri skypeUri = Uri.parse("skype:" + userId + "?chat");
            Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);

            // Restrict the Intent to being handled by the Skype for
            // Android client only
            myIntent.setComponent(new ComponentName("com.skype.raider",
                    "com.skype.raider.Main"));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Initiate the Intent. It should never fail since we've
            // already established the
            // presence of its handler (although there is an extremely
            // minute window where that
            // handler can go away...)
            getActivity().startActivity(myIntent);
            if (eventHandler != null)
                eventHandler.onChat(userId);

        } else {
            Log.d("Skype Client Installed", "No");
            if (eventHandler != null)
                eventHandler
                        .onSkypeClientNotInstalled(activity.getString(R.string.skype_skype_app_is_not_installed));

        }

    }

    public boolean isSkypeClientInstalled(Context myContext) {
        PackageManager myPackageMgr = myContext.getPackageManager();
        try {
            myPackageMgr.getPackageInfo("com.skype.raider",
                    PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            return (false);
        }
        return (true);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }
}
