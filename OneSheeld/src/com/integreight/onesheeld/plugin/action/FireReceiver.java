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
import android.widget.Toast;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.plugin.BundleScrubber;

/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 * 
 * @see com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class FireReceiver extends BroadcastReceiver {

	/**
	 * @param context
	 *            {@inheritDoc}.
	 * @param intent
	 *            the incoming
	 *            {@link com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING}
	 *            Intent. This should contain the
	 *            {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was
	 *            saved by {@link EditActivity} and later broadcast by Locale.
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		/*
		 * Always be strict on input parameters! A malicious third-party app
		 * could send a malformed Intent.
		 */

		if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent
				.getAction())) {
			return;
		}

		BundleScrubber.scrub(intent);

		final Bundle bundle = intent
				.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		BundleScrubber.scrub(bundle);

		if (PluginBundleManager.isBundleValid(bundle)) {
			OneSheeldApplication app = (OneSheeldApplication) context
					.getApplicationContext();
			System.out
					.println(app.getAppFirmata()
							+ "    "
							+ bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_PIN_NUMBER)
							+ "    "
							+ bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_OUTPUT)
							+ "    " + app.getAppFirmata().isOpen());
			if (app.getAppFirmata() != null) {
				app.getAppFirmata()
						.pinMode(
								bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_PIN_NUMBER),
								ArduinoFirmata.OUTPUT);
				app.getAppFirmata()
						.digitalWrite(
								bundle.getInt(PluginBundleManager.BUNDLE_EXTRA_PIN_NUMBER),
								bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_OUTPUT));
			}
			Toast.makeText(context, "MESSAGE", Toast.LENGTH_LONG).show();
		}
	}
}