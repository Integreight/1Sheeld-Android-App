package com.integreight.onesheeld.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
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
            Log.e("TAG", "Exception", e);
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
                    Log.e("TAG", "Exception", e);
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
                    Log.e("TAG", "Exception", e);
                }
            }
        }
        return s;
    }

    public static byte[] generateKey() {
        byte[] keyStart = convertStirngToByteArray("password_key");
        KeyGenerator kgen;
        byte[] key = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
            //SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            sr.setSeed(keyStart);
            kgen.init(128, sr);
            SecretKey skey = kgen.generateKey();
            key = skey.getEncoded();
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            Log.e("TAG", "Exception", e);
        }
        return key;
    }
}