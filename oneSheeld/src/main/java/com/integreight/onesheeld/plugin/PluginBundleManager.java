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

package com.integreight.onesheeld.plugin;

import android.content.Context;
import android.os.Bundle;

public final class PluginBundleManager {

    public static final String BUNDLE_EXTRA_PIN_NUMBER = "com.integreight.onesheeld.extra.PIN_NUMBER"; //$NON-NLS-1$
    public static final String BUNDLE_EXTRA_OUTPUT = "com.integreight.onesheeld.extra.OUTPUT"; //$NON-NLS-1$
    public static final String CONDITION_BUNDLE_EXTRA_PIN_NUMBER = "com.integreight.condition.extra.PIN_NUMBER"; //$NON-NLS-1$
    public static final String CONDITION_BUNDLE_EXTRA_OUTPUT = "com.integreight.condition.extra.OUTPUT"; //$NON-NLS-1$

    public static boolean isActionBundleValid(final Bundle bundle) {
        if (null == bundle) {
            return false;
        }

        if (!bundle.containsKey(BUNDLE_EXTRA_PIN_NUMBER)
                || !bundle.containsKey(BUNDLE_EXTRA_OUTPUT)) {
            return false;
        }

        if (2 != bundle.keySet().size()) {
            return false;
        }

        return true;
    }

    public static boolean isConditionBundleValid(final Bundle bundle) {
        if (null == bundle) {
            return false;
        }

        if (!bundle.containsKey(CONDITION_BUNDLE_EXTRA_PIN_NUMBER)
                || !bundle.containsKey(CONDITION_BUNDLE_EXTRA_OUTPUT)) {
            return false;
        }

        if (2 != bundle.keySet().size()) {
            return false;
        }

        return true;
    }

    public static Bundle generateActionBundle(final Context context,
                                              final int pin, final boolean output) {
        Bundle result = new Bundle();
        result.putInt(BUNDLE_EXTRA_PIN_NUMBER, pin);
        result.putBoolean(BUNDLE_EXTRA_OUTPUT, output);

        return result;
    }

    public static Bundle generateConditionBundle(final Context context,
                                                 final int pin, final boolean output) {
        Bundle result = new Bundle();
        result.putInt(CONDITION_BUNDLE_EXTRA_PIN_NUMBER, pin);
        result.putBoolean(CONDITION_BUNDLE_EXTRA_OUTPUT, output);

        return result;
    }

    private PluginBundleManager() {
        throw new UnsupportedOperationException(
                "This class is non-instantiable"); //$NON-NLS-1$
    }
}