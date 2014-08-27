package com.integreight.onesheeld.model;

public class TerminalPrintedLine {
	public String date;
	public String print;
	public boolean isEndedWithNewLine;

	public TerminalPrintedLine(String date, String print, boolean isEndedWithNewLine) {
		super();
		this.date = date;
		this.print = print;
		this.isEndedWithNewLine = isEndedWithNewLine;
	}
}
