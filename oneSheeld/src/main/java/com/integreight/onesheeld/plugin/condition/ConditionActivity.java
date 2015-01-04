/*
 * Copyright 2013 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.integreight.onesheeld.plugin.condition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.ArduinoPin;
import com.integreight.onesheeld.plugin.AbstractPluginActivity;
import com.integreight.onesheeld.plugin.BundleScrubber;
import com.integreight.onesheeld.plugin.PluginBundleManager;
import com.integreight.onesheeld.utils.customviews.PluginConnectingPinsView;

public final class ConditionActivity extends AbstractPluginActivity {
    private String[] output = {"High", "Low"};
    Spinner outputSpinner;
    int selectedPin = -1;

    @Override
    protected void onResume() {
        ((PluginConnectingPinsView) getSupportFragmentManager()
                .findFragmentByTag("Pins")).reset(
                new PluginConnectingPinsView.OnPinSelectionListener() {

                    @Override
                    public void onUnSelect(ArduinoPin pin) {
                        selectedPin = -1;
                    }

                    @Override
                    public void onSelect(ArduinoPin pin) {
                        if (pin != null)
                            selectedPin = pin.microHardwarePin;
                        else
                            selectedPin = -1;
                    }
                }, selectedPin);
        super.onResume();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BundleScrubber.scrub(getIntent());

        final Bundle localeBundle = getIntent().getBundleExtra(
                com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(localeBundle);

        setContentView(R.layout.plugin_action_activity);
        outputSpinner = (Spinner) findViewById(R.id.output_spinner);
        ArrayAdapter<String> outputArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, output);
        outputSpinner.setAdapter(outputArrayAdapter);

        if (null == savedInstanceState) {
            if (PluginBundleManager.isConditionBundleValid(localeBundle)) {
                final boolean output = localeBundle
                        .getBoolean(PluginBundleManager.CONDITION_BUNDLE_EXTRA_OUTPUT);
                selectedPin = localeBundle
                        .getInt(PluginBundleManager.CONDITION_BUNDLE_EXTRA_PIN_NUMBER);
                if (output)
                    outputSpinner.setSelection(0);
                else
                    outputSpinner.setSelection(1);
            }
        }
        PluginConnectingPinsView pluginPinsView = PluginConnectingPinsView
                .getInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.pluginPinsFrame, pluginPinsView, "Pins").commit();
    }

    @Override
    public void finish() {
        if (!isCanceled()) {
            final boolean output = (outputSpinner).getSelectedItem().toString()
                    .toLowerCase().equals("high") ? true : false;
            final Intent resultIntent = new Intent();
            final Bundle resultBundle = PluginBundleManager
                    .generateConditionBundle(getApplicationContext(),
                            selectedPin, output);
            resultIntent
                    .putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE,
                            resultBundle);
            final String blurb = selectedPin >= 0 ? (generateBlurb(
                    getApplicationContext(), "Pin " + selectedPin + " set to "
                            + (output ? "High" : "Low"))) : generateBlurb(
                    getApplicationContext(), "No Pins Selected");
            ((OneSheeldApplication) getApplication()).getAppPreferences()
                    .edit().putInt("PluginActionPin", selectedPin).commit();
            resultIntent.putExtra(
                    com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);

            setResult(RESULT_OK, resultIntent);
        }

        super.finish();
    }

    static String generateBlurb(final Context context,
                                final String message) {
        final int maxBlurbLength = context.getResources().getInteger(
                R.integer.twofortyfouram_locale_maximum_blurb_length);

        if (message.length() > maxBlurbLength) {
            return message.substring(0, maxBlurbLength);
        }

        return message;
    }
}