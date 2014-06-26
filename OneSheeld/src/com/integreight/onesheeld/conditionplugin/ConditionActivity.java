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
package com.integreight.onesheeld.conditionplugin;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.NoSuchElementException;

import com.integreight.onesheeld.R;

/**
 * This is the "Edit" activity for a Locale Plug-in.
 * <p>
 * This Activity can be started in one of two states:
 * <ul>
 * <li>New plug-in instance: The Activity's Intent will not contain
 * {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE}.</li>
 * <li>Old plug-in instance: The Activity's Intent will contain
 * {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} from a previously saved
 * plug-in instance that the user is editing.</li>
 * </ul>
 * 
 * @see com.twofortyfouram.locale.Intent#ACTION_EDIT_CONDITION
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class ConditionActivity extends AbstractPluginActivity {
	/**
	 * ListView shown in the Activity.
	 */
	private ListView mList = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BundleScrubber.scrub(getIntent());

		final Bundle localeBundle = getIntent().getBundleExtra(
				com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
		BundleScrubber.scrub(localeBundle);

		setContentView(R.layout.plugin_condition_activity);

		mList = ((ListView) findViewById(android.R.id.list));
		mList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice,
				android.R.id.text1, getResources().getStringArray(
						R.array.display_states)));

		if (null == savedInstanceState) {
			if (PluginBundleManager.isBundleValid(localeBundle)) {
				final boolean isDisplayOn = localeBundle
						.getBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE);
				final int position = getPositionForIdInArray(
						getApplicationContext(), R.array.display_states,
						isDisplayOn ? R.string.list_on : R.string.list_off);
				mList.setItemChecked(position, true);
			}
		}
	}

	@Override
	public void finish() {
		if (!isCanceled()) {
			if (AdapterView.INVALID_POSITION != mList.getCheckedItemPosition()) {
				final int selectedResourceId = getResourceIdForPositionInArray(
						getApplicationContext(), R.array.display_states,
						mList.getCheckedItemPosition());

				final boolean isDisplayOn;
				if (R.string.list_on == selectedResourceId) {
					isDisplayOn = true;
				} else if (R.string.list_off == selectedResourceId) {
					isDisplayOn = false;
				} else {
					throw new AssertionError();
				}

				final Intent resultIntent = new Intent();

				/*
				 * This extra is the data to ourselves: either for the Activity
				 * or the BroadcastReceiver. Note that anything placed in this
				 * Bundle must be available to Locale's class loader. So storing
				 * String, int, and other standard objects will work just fine.
				 * Parcelable objects are not acceptable, unless they also
				 * implement Serializable. Serializable objects must be standard
				 * Android platform objects (A Serializable class private to
				 * this plug-in's APK cannot be stored in the Bundle, as
				 * Locale's classloader will not recognize it).
				 */
				final Bundle resultBundle = PluginBundleManager.generateBundle(
						getApplicationContext(), isDisplayOn);
				resultIntent.putExtra(
						com.twofortyfouram.locale.Intent.EXTRA_BUNDLE,
						resultBundle);

				/*
				 * The blurb is concise status text to be displayed in the
				 * host's UI.
				 */
				resultIntent.putExtra(
						com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
						generateBlurb(getApplicationContext(), isDisplayOn));

				setResult(RESULT_OK, resultIntent);
			}
		}

		super.finish();
	}

	/**
	 * @param context
	 *            Application context.
	 * @param isDisplayOn
	 *            True if the plug-in detects when the display is on.
	 * @return A blurb for the plug-in.
	 */
	/* package */static String generateBlurb(final Context context,
			final boolean isDisplayOn) {
		if (isDisplayOn) {
			return context.getString(R.string.blurb_on);
		}

		return context.getString(R.string.blurb_off);
	}

	/**
	 * Gets the position of an element in a typed array
	 * 
	 * @param context
	 *            Application context. Cannot be null.
	 * @param arrayId
	 *            resource ID of the array.
	 * @param elementId
	 *            resource ID of the element in the array.
	 * @return position of the {@code elementId} in the array.
	 * @throws NoSuchElementException
	 *             if {@code elementId} is not in the array.
	 */
	/* package */static int getPositionForIdInArray(final Context context,
			final int arrayId, final int elementId) {
		if (Constants.IS_PARAMETER_CHECKING_ENABLED) {
			if (null == context) {
				throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
			}
		}

		TypedArray array = null;
		try {
			array = context.getResources().obtainTypedArray(arrayId);
			for (int x = 0; x < array.length(); x++) {
				if (array.getResourceId(x, 0) == elementId) {
					return x;
				}
			}
		} finally {
			if (null != array) {
				array.recycle();
				array = null;
			}
		}

		throw new NoSuchElementException();
	}

	/**
	 * Gets the position of an element in a typed array.
	 * 
	 * @param context
	 *            Application context. Cannot be null.
	 * @param arrayId
	 *            resource ID of the array.
	 * @param position
	 *            position in the array to retrieve.
	 * @return resource id of element in {@code position}.
	 * @throws IndexOutOfBoundsException
	 *             if {@code position} is not in the array.
	 */
	/* package */static int getResourceIdForPositionInArray(
			final Context context, final int arrayId, final int position) {
		if (Constants.IS_PARAMETER_CHECKING_ENABLED) {
			if (null == context) {
				throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
			}
		}

		TypedArray stateArray = null;
		try {
			stateArray = context.getResources().obtainTypedArray(arrayId);
			final int selectedResourceId = stateArray
					.getResourceId(position, 0);

			if (0 == selectedResourceId) {
				throw new IndexOutOfBoundsException();
			}

			return selectedResourceId;
		} finally {
			if (null != stateArray) {
				stateArray.recycle();
				stateArray = null;
			}
		}
	}
}