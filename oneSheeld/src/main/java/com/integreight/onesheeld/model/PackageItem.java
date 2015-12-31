package com.integreight.onesheeld.model;

public class PackageItem {
    public int id;
    public String name;
    public String packageName;
    public boolean isSelected = false;

    public PackageItem() {
    }

    public PackageItem(int id, String name, String path, String packageName) {
        super();
        this.id = id;
        this.name = name;
        this.packageName = name;
    }

    public PackageItem(String name, String path, String packageName) {
        super();
        this.name = name;
        this.packageName = packageName;
    }

}
