package com.integreight.onesheeld.utils;

public class BitsUtils {

	public static byte setBit(byte b,int bit){
		if (bit>8)return b;
		return (byte)(b|(b<<bit));
	}
	
	public static byte resetBit(byte b,int bit){
		if (bit>8)return b;
		return (byte)(b&(~(b<<bit)));
	}
}
