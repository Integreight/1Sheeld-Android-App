package com.integreight.onesheeld.utils;

import java.util.Hashtable;

import android.util.SparseArray;

import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.Shield;

public class AppShields {
	private static AppShields thisInstance;
	private Hashtable<String, Shield> shieldsTable;
	private SparseArray<Shield> shieldsArray;

	private AppShields() {
		// TODO Auto-generated constructor stub
	}

	public static AppShields getInstance() {
		if (thisInstance == null) {
			thisInstance = new AppShields();
		}
		return thisInstance;
	};

	public void init() {
		initShields();
	}

	public Hashtable<String, Shield> getShieldsTable() {
		if (shieldsTable == null || shieldsTable.size() == 0)
			initShields();
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

	public void putShield(int position, Shield shield) {
		if (shieldsArray == null || shieldsArray.size() == 0)
			initShields();
		shieldsArray.put(position, shield);
	}

	public void putShield(String tag, Shield shield) {
		if (shieldsArray == null || shieldsArray.size() == 0)
			initShields();
		shieldsTable.put(tag, shield);
	}

	public Shield getShield(int position) {
		if (shieldsArray == null || shieldsArray.size() == 0)
			initShields();
		return shieldsArray.get(position);
	}

	private void initShields() {
		int i = 0;
		shieldsArray = new SparseArray<Shield>();
		shieldsTable = new Hashtable<String, Shield>();
		for (UIShield shield : UIShield.valuesFiltered()) {
			shieldsTable.put(shield.name(), new Shield(shield.getId(),
					shield.name, shield.getName(), shield.itemBackgroundColor,
					shield.symbolId, shield.mainActivitySelection,
					shield.shieldType, shield.isReleasable,
					shield.isInvalidatable));
			shieldsArray.put(i,
					new Shield(shield.getId(), shield.name, shield.getName(),
							shield.itemBackgroundColor, shield.symbolId,
							shield.mainActivitySelection, shield.shieldType,
							shield.isReleasable, shield.isInvalidatable));
			i++;
		}
	}
}
