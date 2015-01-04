package com.integreight.onesheeld.utils;

public class BitsUtils {

    public static byte setBit(byte b, int bit) {
        if (bit < 0 && bit >= 8) return b;
        return (byte) (b | (1 << bit));
    }

    public static byte resetBit(byte b, int bit) {
        if (bit < 0 && bit >= 8) return b;
        return (byte) (b & (~(1 << bit)));
    }

    public static boolean isBitSet(byte b, int bit) {
        if (bit < 0 && bit >= 8) return false;
        return (b & (1 << bit)) > 0;
    }
}
