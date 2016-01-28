package com.integreight.onesheeld.shields.controller.utils;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
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
            String action = intent.getAction();
            if (action != null) {
                if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                    if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                        if (((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name()) != null) {
                            ((NfcShield) ((OneSheeldApplication) getApplication()).getRunningShields().get(UIShield.NFC_SHIELD.name())).handleIntent(intent);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.nfc_nfc_disabled_toast, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            } else
                startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        finish();
    }
}
