package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.model.TerminalPrintedLine;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.fragments.TerminalFragment;
import com.integreight.onesheeld.utils.Log;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CopyOnWriteArrayList;

public class TerminalShield extends ControllerParent<TerminalShield> {
    private static final byte WRITE = 0x01;
    private static final byte PRINT = 0x02;
    private static final byte DATA_IN = 0x01;
    private TerminalHandler eventHandler;
    public int[] encodingMths = new int[]{R.id.terminalString, R.id.asci,
            R.id.binary, R.id.hex};
    public int selectedEnMth = 0;
    public CopyOnWriteArrayList<TerminalPrintedLine> terminalPrintedLines = new CopyOnWriteArrayList<>();
    public boolean lastItemEndedWithNewLine = true;
    public CopyOnWriteArrayList<TerminalPrintedLine> tempLines = new CopyOnWriteArrayList<TerminalPrintedLine>();
    public boolean isTimeOn = true, isAutoScrolling = true;

    public TerminalShield() {
        super();
    }

    public TerminalShield(Activity activity, String tag) {
        super(activity, tag);
    }

    @Override
    public ControllerParent<TerminalShield> init(String tag) {
        terminalPrintedLines = new CopyOnWriteArrayList<TerminalPrintedLine>();
        lastItemEndedWithNewLine = true;
        return super.init(tag);
    }

    @Override
    public void setConnected(ArduinoConnectedPin... pins) {
        super.setConnected(pins);
    }

    private ShieldFrame sf;

    public void input(String input) {
        sf = new ShieldFrame(UIShield.TERMINAL_SHIELD.getId(), DATA_IN);
        sf.addArgument(input);
        sendShieldFrame(sf,true);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
    }

    private boolean greaterThanThousand = false;

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.TERMINAL_SHIELD.getId()) {
            String outputTxt = null;
            if (frame.getArguments() != null && frame.getArguments().size() > 0)
                outputTxt = frame.getArgumentAsString(0);
            if (outputTxt != null) {
                String date = terminalPrintedLines.size() == 0
                        || terminalPrintedLines
                        .get(terminalPrintedLines.size() - 1).isEndedWithNewLine ? TerminalFragment
                        .getTimeAsString() + " [RX] : "
                        : "";
                boolean isEndedWithNewLine = outputTxt.length() > 0
                        && outputTxt.charAt(outputTxt.length() - 1) == '\n';
                if (lastItemEndedWithNewLine) {
                    terminalPrintedLines.add(new TerminalPrintedLine(date,
                            outputTxt.substring(0,
                                    isEndedWithNewLine ? outputTxt.length() - 1
                                            : outputTxt.length()),
                            isEndedWithNewLine, true));
                    tempLines.add(new TerminalPrintedLine(date,
                            getEncodedString(outputTxt.substring(0,
                                    isEndedWithNewLine ? outputTxt.length() - 1
                                            : outputTxt.length())),
                            isEndedWithNewLine, true));
                } else if (terminalPrintedLines.size() > 0
                        && tempLines.size() > 0) {
                    terminalPrintedLines.get(terminalPrintedLines.size() - 1).print = terminalPrintedLines
                            .get(terminalPrintedLines.size() - 1).print
                            + outputTxt.substring(0,
                            isEndedWithNewLine ? outputTxt.length() - 1
                                    : outputTxt.length());
                    tempLines.get(tempLines.size() - 1).print = getEncodedString(terminalPrintedLines
                            .get(terminalPrintedLines.size() - 1).print);
                    if (isEndedWithNewLine)
                        terminalPrintedLines
                                .get(terminalPrintedLines.size() - 1).isEndedWithNewLine = true;
                }
                lastItemEndedWithNewLine = isEndedWithNewLine;
                greaterThanThousand = terminalPrintedLines.size() > 1000;
                if (greaterThanThousand) {
                    // for (int i = 0; i < 1; i++) {
                    terminalPrintedLines.remove(0);
                    tempLines.remove(0);
                    // }
                }
                switch (frame.getFunctionId()) {
                    case WRITE:
                        if (eventHandler != null) {
                            eventHandler.onPrint(outputTxt, greaterThanThousand);
                        }
                        break;
                    case PRINT:
                        if (eventHandler != null) {
                            eventHandler.onPrint(outputTxt, greaterThanThousand);
                        }
                        break;

                    default:
                        break;
                }
            }

//            Log.d("internetLog", "Terminal " + outputTxt);
        }
    }

    @Override
    public void reset() {
        if (terminalPrintedLines != null)
            terminalPrintedLines.clear();
        terminalPrintedLines = null;
    }

    public TerminalHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(TerminalHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public interface TerminalHandler {
        public void onPrint(String output, final boolean clearBeforeWriting);
    }

    public String getEncodedString(String toBeEncoded) {
        String out = "";
        switch (selectedEnMth) {
            case 0:
                out = toBeEncoded;
                break;
            case 1:
                try {
                    byte[] en = toBeEncoded.getBytes("US-ASCII");
                    for (byte b : en) {
                        out += b;
                    }
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case 2:
                byte[] bytes = toBeEncoded.getBytes();
                StringBuilder binary = new StringBuilder();
                for (byte b : bytes) {
                    int val = b;
                    for (int i = 0; i < 8; i++) {
                        binary.append((val & 128) == 0 ? 0 : 1);
                        val <<= 1;
                    }
                    binary.append(' ');
                }
                out = binary.toString();
                break;
            case 3:
                byte[] byts = toBeEncoded.getBytes();
                for (byte b : byts) {
                    if ((Integer.toHexString(b).length() < 2))
                        out += "0" + Integer.toHexString(b) + " ";
                    else if ((Integer.toHexString(b).length() == 2))
                        out += Integer.toHexString(b) + " ";
                    else {
                        String temp = Integer.toHexString(b);
                        temp = temp.substring(temp.length() - 2);
                        out += temp + " ";
                    }
                }
                break;
            default:
                break;
        }
        return out;
    }

}
