package com.integreight.onesheeld.shields.controller.utils;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.shields.controller.NfcShield;

/**
 * Created by Mouso on 3/11/2015.
 */
public class NfcUtils extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            Intent intent = getIntent();
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                if (((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name()) != null) {
                    ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).handleIntent(intent);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Nfc Disabled", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        finish();
    }
}
