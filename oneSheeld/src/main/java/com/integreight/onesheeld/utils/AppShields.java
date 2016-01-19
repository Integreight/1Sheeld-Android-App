package com.integreight.onesheeld.utils;

import android.util.SparseArray;

import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.Shield;

import java.util.ArrayList;
import java.util.Hashtable;

public class AppShields {
    private static AppShields thisInstance;
    private Hashtable<String, Shield> shieldsTable;
    private Hashtable<String, String> shieldsTags;
    private SparseArray<Shield> shieldsArray;
    private String rememberedShields;

    private AppShields() {
        // TODO Auto-generated constructor stub
    }

    public static AppShields getInstance() {
        if (thisInstance == null) {
            thisInstance = new AppShields();
        }
        return thisInstance;
    }

    public void init(String selectedCach) {
        this.rememberedShields = selectedCach;
        initShields();
    }

    public Hashtable<String, Shield> getShieldsTable() {
        if (shieldsTable == null || shieldsTable.size() == 0) {
            initShields();
        }
        return shieldsTable;
    }

    public SparseArray<Shield> getShieldsArray() {
        if (shieldsArray == null || shieldsArray.size() == 0)
            initShields();
        return shieldsArray;
    }

    public Shield getShield(String tag) {
        if (shieldsArray == null || shieldsArray.size() == 0)
            initShields();
        return shieldsTable.get(tag);
    }
//
//    public Shield getShieldByName(String name) {
//        if (nameAndTagTable == null || nameAndTagTable.size() == 0 || shieldsArray == null || shieldsArray.size() == 0)
//            initShields();
//        return shieldsTable.get(nameAndTagTable.get(name));
//    }

    public void putShield(int position, Shield shield) {
        if (shieldsArray == null || shieldsArray.size() == 0)
            initShields();
        shieldsArray.put(position, shield);
        shieldsTable.put(shield.tag, shield);
    }

    public void putShield(String tag, Shield shield) {
        if (shieldsArray == null || shieldsArray.size() == 0)
            initShields();
        shieldsTable.put(tag, shield);
        shieldsArray.put(shield.position, shield);
    }

    public Shield getShield(int position) {
        if (shieldsArray == null || shieldsArray.size() == 0)
            initShields();
        return shieldsArray.get(position);
    }

    public ArrayList<Byte> getRememberedShields() {
        if (rememberedShields == null || rememberedShields.length() == 0)
            return new ArrayList<>();
        String[] arrString = rememberedShields.split(",");
        ArrayList<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < arrString.length; i++) {
            bytes.add(Byte.parseByte(arrString[i]));
        }
        return bytes;
    }

    public String getSelectedShields() {
        String selected = "";
        for (int i = 0; i < shieldsArray.size(); i++) {
            Shield shield = shieldsArray.get(i);
            selected += (shield.mainActivitySelection ? shield.id + "," : "");
        }
        return selected.trim().length() == 0 ? selected : selected.substring(0, selected.length() - 1);
    }

    private void initShields() {
        int i = 0;
        shieldsArray = new SparseArray();
        shieldsTable = new Hashtable();
        shieldsTags = new Hashtable();
        ArrayList<Byte> remembered = getRememberedShields();
        for (UIShield shield : UIShield.valuesFiltered()) {
            shieldsTable.put(shield.name(), new Shield(shield.getId(), i,
                    shield.name(), shield.getName(), shield.getItemBackgroundColor(),
                    shield.getSymbolId(), remembered.contains(shield.getId()) ? true : shield.isMainActivitySelection(),
                    shield.getShieldType(), shield.getShieldFragment(), shield.isReleasable(),
                    shield.getIsInvalidatable()));
            shieldsArray.put(i,
                    new Shield(shield.getId(), i, shield.name(), shield.getName(),
                            shield.getItemBackgroundColor(), shield.getSymbolId(),
                            remembered.contains(shield.getId()) ? true : shield.isMainActivitySelection(), shield.getShieldType(), shield.getShieldFragment(),
                            shield.isReleasable(), shield.getIsInvalidatable()));
            shieldsTags.put(shield.getShieldType().getName(), shield.name());
            shieldsTags.put(shield.getShieldFragment().getName(), shield.name());
            i++;
        }
    }

    public synchronized String getShieldTag(String key) {
        if (shieldsTags == null || shieldsTags.size() == 0)
            initShields();
        return shieldsTags.get(key);
    }
}
