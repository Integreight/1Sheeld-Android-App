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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.plugin.BundleScrubber;
import com.integreight.onesheeld.plugin.Constants;
import com.integreight.onesheeld.plugin.PluginBundleManager;
import com.integreight.onesheeld.sdk.IncorrectPinException;
import com.integreight.onesheeld.utils.CrashlyticsUtils;

import java.util.Locale;

public final class QueryReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, final Intent intent) {
        OneSheeldApplication app = (OneSheeldApplication) context
                .getApplicationContext();
        if (!com.twofortyfouram.locale.Intent.ACTION_QUERY_CONDITION
                .equals(intent.getAction())
                || !app.isConnectedToBluetooth()) {
            if (Constants.IS_LOGGABLE) {
                Log.e(Constants.LOG_TAG,
                        String.format(
                                Locale.US,
                                "Received unexpected Intent action %s", intent.getAction())); //$NON-NLS-1$
            }
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent
                .getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

        if (PluginBundleManager.isConditionBundleValid(bundle)) {
            try {
                boolean conditionState = bundle
                        .getBoolean(PluginBundleManager.CONDITION_BUNDLE_EXTRA_OUTPUT);
                final int selectedPin = bundle
                        .getInt(PluginBundleManager.CONDITION_BUNDLE_EXTRA_PIN_NUMBER);
                boolean digitalReadStatus = false;
                if (app.isConnectedToBluetooth())
                    digitalReadStatus = app.getConnectedDevice().digitalRead(selectedPin);
                if (digitalReadStatus == conditionState
                        && digitalReadStatus != app.taskerPinsStatus
                        .get(selectedPin)) {
                    setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_SATISFIED);
                    app.taskerPinsStatus.put(selectedPin, digitalReadStatus);
                } else {
                    setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_UNSATISFIED);
                }
            }catch (IncorrectPinException ignored){
                CrashlyticsUtils.logException(ignored);
            }
        }
    }
}