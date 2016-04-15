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

package com.integreight.onesheeld.plugin.action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.plugin.BundleScrubber;
import com.integreight.onesheeld.plugin.PluginBundleManager;
import com.integreight.onesheeld.sdk.OneSheeldDevice;

/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 *
 * @see com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class FireReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        OneSheeldApplication app = (OneSheeldApplication) context
                .getApplicationContext();
        if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent
                .getAction()) ||
                !app.isConnectedToBluetooth()) {
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent
                .getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

        if (PluginBundleManager.isActionBundleValid(bundle)) {
            if (app.isConnectedToBluetooth()) {
                app.getConnectedDevice().pinMode(
                        bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_PIN_NUMBER),
                        OneSheeldDevice.OUTPUT);
                app.getConnectedDevice().digitalWrite(
                        bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_PIN_NUMBER),
                        bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_OUTPUT));
            }
        }
    }
}