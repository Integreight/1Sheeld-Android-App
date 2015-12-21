package com.integreight.onesheeld.model;

public class PackageItem {
    public int id;
    public String name;
    public boolean isSelected = false;

    public PackageItem() {
    }

    public PackageItem(int id, String name, String path) {
        super();
        this.id = id;
        this.name = name;
    }

    public PackageItem(String name, String path) {
        super();
        this.name = name;
    }

}
