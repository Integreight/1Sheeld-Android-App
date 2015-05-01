package com.integreight.onesheeld.popup;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.integreight.firmatabluetooth.Jodem;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.integreight.onesheeld.utils.HttpRequest;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.customviews.CircularProgressBar;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class FirmwareUpdatingPopup extends Dialog {
    public static boolean isOpened = false;
    private OneSheeldButton upgradeBtn;
    private ProgressBar progress;
    CircularProgressBar downloadingProgress;
    private OneSheeldTextView statusText;
    private OneSheeldTextView progressTxt;
    private MainActivity activity;
    private RelativeLayout transactionSlogan;
    Jodem jodem;
    private Handler uIHandler = new Handler();
    private boolean isFailed = false;

    public FirmwareUpdatingPopup(final MainActivity activity) {
        super(activity, android.R.style.Theme_Translucent_NoTitleBar);
        this.activity = activity;
        final Handler handler = new Handler();
        jodem = new Jodem(activity.getThisApplication().getAppFirmata()
                .getBTService(), new Jodem.JodemEventHandler() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    Log.e("TAG", "Exception", e);
                }
                isFailed = false;
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        FirmwareUpdatingPopup.this.setCancelable(true);
                        statusText.setText("Done Successfully!");
                        setUpgrade();
                    }
                });
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        changeSlogan("Upgrade Firmware", COLOR.BLUE);
                    }
                }, 1500);

                activity.getThisApplication()
                        .getTracker()
                        .send(new HitBuilders.EventBuilder()
                                .setCategory("Firmware")
                                .setAction("Firmware installed successfully")
                                .build());

            }

            @Override
            public void onProgress(final int totalBytes, final int sendBytes,
                                   int errorCount) {
                // TODO Auto-generated method stub
                Log.d("bootloader", "total:" + totalBytes + " sent:"
                        + sendBytes + " error:" + errorCount);
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        int status = (int) ((float) sendBytes / totalBytes * 100);
                        changeSlogan("Installing...", COLOR.BLUE);
                        downloadingProgress.setProgress(status);
                        progressTxt.setText(status + "%");
                    }
                });

            }

            @Override
            public void onError(final String error) {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        FirmwareUpdatingPopup.this.setCancelable(true);
                        changeSlogan("An error occurred!", COLOR.RED);
                        isFailed = true;
                        setUpgrade();
                        activity.getThisApplication()
                                .getTracker()
                                .send(new HitBuilders.EventBuilder()
                                        .setCategory("Firmware")
                                        .setAction(
                                                "Firmware installation failed")
                                        .build());
                    }
                });
            }

            @Override
            public void onTimout() {
                // TODO Auto-generated method stub
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        FirmwareUpdatingPopup.this.setCancelable(true);
                        changeSlogan("1Sheeld not responding!", COLOR.RED);
                        isFailed = true;
                        setUpgrade();
                    }
                });

            }
        });
        ((OneSheeldApplication) activity.getApplication()).getTracker()
                .setScreenName("Firmware Upgrade");
        ((OneSheeldApplication) activity.getApplication()).getTracker().send(
                new HitBuilders.ScreenViewBuilder().build());
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#CC000000"));
        }
        setContentView(R.layout.updating_firmware_view);
        isOpened = true;
        upgradeBtn = (OneSheeldButton) findViewById(R.id.update);
        progress = (ProgressBar) findViewById(R.id.progressUpdating);
        downloadingProgress = (CircularProgressBar) findViewById(R.id.progressDownloading);
        progressTxt = (OneSheeldTextView) findViewById(R.id.progressTxt);
        statusText = (OneSheeldTextView) findViewById(R.id.updateStatusText);
        transactionSlogan = (RelativeLayout) findViewById(R.id.transactionSloganUpdating);
        setUpgrade();
        changeSlogan(
                activity.getResources().getString(R.string.upgradeFirmata),
                COLOR.BLUE);
        setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                isOpened = false;
                activity.getThisApplication().getAppFirmata()
                        .returnAppToNormal();
                jodem.stop();
            }
        });
        activity.getThisApplication().getAppFirmata().enableBootloaderMode();
        super.onCreate(savedInstanceState);
    }

    private void showInstallationProgress() {
        upgradeBtn.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
        downloadingProgress.setVisibility(View.INVISIBLE);
        progressTxt.setVisibility(View.INVISIBLE);
    }

    private void showDownloadingProgress() {
        upgradeBtn.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);
        downloadingProgress.setProgress(0);
        downloadingProgress.setVisibility(View.VISIBLE);
        progressTxt.setText("");
        progressTxt.setVisibility(View.VISIBLE);
    }

    private void reloadData() {

        HttpRequest.getInstance().get(
                OneSheeldApplication.FIRMWARE_UPGRADING_URL,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onFinish() {
                        // TODO Auto-generated method stub
                        super.onFinish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseBody, Throwable e) {
                        ((OneSheeldApplication) activity.getApplication())
                                .setMajorVersion(-1);
                        ((OneSheeldApplication) activity.getApplication())
                                .setMinorVersion(-1);
                        super.onFailure(statusCode, headers, responseBody, e);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            System.err.println(response);
                            ((OneSheeldApplication) activity.getApplication())
                                    .setMajorVersion(Integer.parseInt(response
                                            .getString("major")));
                            ((OneSheeldApplication) activity.getApplication())
                                    .setMinorVersion(Integer.parseInt(response
                                            .getString("minor")));
                            ((OneSheeldApplication) activity.getApplication())
                                    .setVersionWebResult(response.toString());
                            downloadFirmware();
                        } catch (NumberFormatException e) {
                            // TODO Auto-generated catch block
                            Log.e("TAG", "Exception", e);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Log.e("TAG", "Exception", e);
                        }
                        super.onSuccess(statusCode, headers, response);
                    }
                });
    }

    private byte[] binaryFile;

    private void downloadFirmware() {
        changeSlogan("Downloading...", COLOR.BLUE);
        if (activity.getThisApplication().getVersionWebResult() != null) {
            showInstallationProgress();
            try {
                HttpRequest.getInstance().get(
                        new JSONObject(activity.getThisApplication()
                                .getVersionWebResult()).get("url")
                                .toString(),
                        new BinaryHttpResponseHandler(new String[]{
                                "application/octet-stream", "text/plain"}) {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                                FirmwareUpdatingPopup.this.setCancelable(false);
                                activity.getThisApplication().getAppFirmata()
                                        .prepareAppForSendingFirmware();
                                activity.getThisApplication().getAppFirmata()
                                        .resetMicro();
                                binaryFile = binaryData;
                                showDownloadingProgress();
                                jodem.send(binaryData, 4);
                                if (!isFailed)
                                    changeSlogan("Installing...", COLOR.BLUE);
                                else
                                    changeSlogan("Please, Press reset!",
                                            COLOR.BLUE);
                            }

                            @Override
                            public void onFailure(int statusCode,
                                                  Header[] headers, byte[] binaryData,
                                                  Throwable error) {
                                changeSlogan("Error Downloading!", COLOR.RED);
                                setUpgrade();
                                Log.d("bootloader", statusCode + "");
                                activity.getThisApplication()
                                        .getTracker()
                                        .send(new HitBuilders.EventBuilder()
                                                .setCategory("Firmware")
                                                .setAction(
                                                        "Firmware download failed")
                                                .build());
                            }

                            @Override
                            public void onProgress(int bytesWritten,
                                                   int totalSize) {
                                super.onProgress(bytesWritten, totalSize);
                            }
                        });
            } catch (JSONException e) {
                Log.e("TAG", "Exception", e);
            }
        } else
            reloadData();
    }

    private void setUpgrade() {
        upgradeBtn.setText("Upgrade");
        upgradeBtn.setVisibility(View.VISIBLE);
        progress.setVisibility(View.INVISIBLE);
        progressTxt.setVisibility(View.INVISIBLE);
        downloadingProgress.setVisibility(View.INVISIBLE);
        upgradeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ConnectionDetector.isConnectingToInternet(activity)) {
                    if (binaryFile == null)
                        downloadFirmware();
                    else {
                        FirmwareUpdatingPopup.this.setCancelable(false);
                        activity.getThisApplication().getAppFirmata()
                                .prepareAppForSendingFirmware();
                        activity.getThisApplication().getAppFirmata()
                                .resetMicro();
                        showDownloadingProgress();
                        jodem.send(binaryFile, 4);
                        if (!isFailed)
                            changeSlogan("Installing...", COLOR.BLUE);
                        else
                            changeSlogan("Please, Press reset!", COLOR.BLUE);

                    }
                } else {
                    changeSlogan("No Internet Connection", COLOR.RED);
                }
            }
        });
    }

    private void changeSlogan(final String text, final int color) {
        uIHandler.post(new Runnable() {

            @Override
            public void run() {
                statusText.setText(text);
                transactionSlogan.setBackgroundColor(color);
            }
        });
    }

    private final static class COLOR {
        public final static int RED = 0xff9B1201;
        public final static int YELLOW = 0xffE79401;
        public final static int BLUE = 0xff0094C1;
        public final static int ORANGE = 0xffE74D01;
    }
}
