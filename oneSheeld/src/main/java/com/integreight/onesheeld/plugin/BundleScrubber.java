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

import android.content.Intent;
import android.os.Bundle;

public final class BundleScrubber {
    public static boolean scrub(final Intent intent) {
        if (null == intent) {
            return false;
        }

        return scrub(intent.getExtras());
    }

    public static boolean scrub(final Bundle bundle) {
        if (null == bundle) {
            return false;
        }
        try {
            // if a private serializable exists, this will throw an exception
            bundle.containsKey(null);
        } catch (final Exception e) {
            bundle.clear();
            return true;
        }

        return false;
    }
}