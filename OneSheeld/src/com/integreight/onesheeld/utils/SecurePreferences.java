package com.integreight.onesheeld.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SecurePreferences {
	public static byte[] encrypt(byte[] raw, byte[] clear) {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		byte[] encrypted = null;

		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

			encrypted = cipher.doFinal(clear);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
		}
		return encrypted;
	}

	public static byte[] decrypt(byte[] raw, byte[] encrypted) {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		byte[] decrypted = null;
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			decrypted = cipher.doFinal(encrypted);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
		}

		return decrypted;
	}

	public static byte[] convertStirngToByteArray(String s) {
		byte[] byteArray = null;
		if (s != null) {
			if (s.length() > 0) {
				try {
					byteArray = s.getBytes();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return byteArray;
	}

	public static String convertByteArrayToString(byte[] byteArray) {
		String s = null;
		if (byteArray != null) {
			if (byteArray.length > 0) {
				try {
					s = new String(byteArray, "UTF-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return s;
	}
}