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

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.plugin.BundleScrubber;
import com.integreight.onesheeld.plugin.Constants;
import com.integreight.onesheeld.plugin.PluginBundleManager;

/**
 * This is the "query" BroadcastReceiver for a Locale Plug-in condition.
 * 
 * @see com.twofortyfouram.locale.Intent#ACTION_QUERY_CONDITION
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class QueryReceiver extends BroadcastReceiver {

	/**
	 * @param context
	 *            {@inheritDoc}.
	 * @param intent
	 *            the incoming
	 *            {@link com.twofortyfouram.locale.Intent#ACTION_QUERY_CONDITION}
	 *            Intent. This should always contain the
	 *            {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was
	 *            saved by {@link EditActivity} and later broadcast by Locale.
	 */
	public static int SELECTED_PIN = -1;
	public static boolean IS_HIGH = false;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		/*
		 * Always be strict on input parameters! A malicious third-party app
		 * could send a malformed Intent.
		 */
		OneSheeldApplication app = (OneSheeldApplication) context
				.getApplicationContext();
		if (!com.twofortyfouram.locale.Intent.ACTION_QUERY_CONDITION
				.equals(intent.getAction())
				|| app.getAppFirmata() == null
				|| (app.getAppFirmata() != null && !app.getAppFirmata()
						.isOpen())) {
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
			boolean conditionState = bundle
					.getBoolean(PluginBundleManager.CONDITION_BUNDLE_EXTRA_OUTPUT);
			final int selectedPin = bundle
					.getInt(PluginBundleManager.CONDITION_BUNDLE_EXTRA_PIN_NUMBER);
			boolean digitalReadStatus = app.getAppFirmata().digitalRead(
					selectedPin);
			if (digitalReadStatus == conditionState
					&& digitalReadStatus != app.taskerPinsStatus
							.get(selectedPin)) {
				setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_SATISFIED);
				app.taskerPinsStatus.put(selectedPin, digitalReadStatus);
				Toast.makeText(context,
						"Heeeeeee7   " + intent.getExtras().getString("Key"),
						Toast.LENGTH_SHORT).show();
			} else {
				setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_UNSATISFIED);
			}
		}
	}
}