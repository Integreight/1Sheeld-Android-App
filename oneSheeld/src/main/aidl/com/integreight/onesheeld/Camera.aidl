// Camera.aidl
package com.integreight.onesheeld;

// Declare any non-default types here with import statements

interface Camera {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void add(String flash, boolean isFront, int quality);
}
