package com.integreight.onesheeld.utils;

public class BitsUtils {

    public static byte setBit(byte b, int bit) {
        if (bit < 0 || bit >= 8) return b;
        return (byte) (b | (1 << bit));
    }

    public static byte resetBit(byte b, int bit) {
        if (bit < 0 || bit >= 8) return b;
        return (byte) (b & (~(1 << bit)));
    }

    public static boolean isBitSet(byte b, int bit) {
        if (bit < 0 || bit >= 8) return false;
        return (b & (1 << bit)) > 0;
    }

    public static boolean isBitSet(int b, int bit) {
        if (bit < 0 || bit >= 32) return false;
        return (b & (1 << bit)) > 0;
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
}
