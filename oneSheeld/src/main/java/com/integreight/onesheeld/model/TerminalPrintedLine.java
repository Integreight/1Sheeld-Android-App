package com.integreight.onesheeld.model;

public class TerminalPrintedLine {
    public String date;
    public byte[] print;
    public boolean isEndedWithNewLine;
    private boolean isRx;

    public TerminalPrintedLine(String date, byte[] print, boolean isEndedWithNewLine, boolean isRx) {
        super();
        this.date = date;
        this.print = print;
        this.isEndedWithNewLine = isEndedWithNewLine;
        this.isRx = isRx;
    }

    public boolean isRx() {
        return isRx;
    }

    public boolean isTx() {
        return !isRx;
    }


}
