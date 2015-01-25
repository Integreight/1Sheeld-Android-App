/*
 *   Copyright 2012 Hai Bison
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.integreight.onesheeld.utils.customviews.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Resources' utilities.
 * 
 * @author Hai Bison
 *
 */
public class Resources {

    /**
     * This is singleton class.
     */
    private Resources() {
    }// Resources()

    /**
     * Convenient method for {@link android.content.Context#getTheme()} and
     * {@link Resources.Theme#resolveAttribute(int, android.util.TypedValue, boolean)}.
     *
     * @param context
     *            the context.
     * @param resId
     *            The resource identifier of the desired theme attribute.
     * @return the resource ID that {@link android.util.TypedValue#resourceId} points to, or
     *         {@code 0} if not found.
     */
    public static int resolveAttribute(Context context, int resId) {
        return resolveAttribute(context, resId, 0);
    }// resolveAttribute()

    /**
     * Convenient method for {@link android.content.Context#getTheme()} and
     * {@link Resources.Theme#resolveAttribute(int, android.util.TypedValue, boolean)}.
     *
     * @param context
     *            the context.
     * @param resId
     *            The resource identifier of the desired theme attribute.
     * @param defaultValue
     *            the default value if cannot resolve {@code resId}.
     * @return the resource ID that {@link android.util.TypedValue#resourceId} points to, or
     *         {@code defaultValue} if not found.
     */
    public static int resolveAttribute(Context context, int resId,
            int defaultValue) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(resId, typedValue, true))
            return typedValue.resourceId;
        return defaultValue;
    }// resolveAttribute()

}
