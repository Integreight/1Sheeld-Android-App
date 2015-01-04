package com.integreight.onesheeld.shields.controller.utils;

public interface TwitterDialogListener {
    public abstract void onComplete();

    public abstract void onCancel();

    public abstract void onError(String error);
}
