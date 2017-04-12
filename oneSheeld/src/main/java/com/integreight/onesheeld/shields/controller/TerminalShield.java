package com.integreight.onesheeld.shields.controller;

import android.app.Activity;

import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.ArduinoConnectedPin;
import com.integreight.onesheeld.model.TerminalPrintedLine;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.fragments.TerminalFragment;
import com.integreight.onesheeld.utils.ArrayUtils;

import java.util.concurrent.CopyOnWriteArrayList;

public class TerminalShield extends ControllerParent<TerminalShield> {
    private static final byte WRITE = 0x01;
    private static final byte PRINT = 0x02;
    private static final byte DATA_IN = 0x01;
    private TerminalHandler eventHandler;
    public int[] encodingMths = new int[]{R.id.terminalString, R.id.asci,
            R.id.binary, R.id.hex};
    public static int selectedEnMth = 0;
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
        sendShieldFrame(sf, true);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
    }

    private boolean greaterThanThousand = false;

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.TERMINAL_SHIELD.getId()) {
            byte[] incomingByte = null;
            if (frame.getArguments() != null && frame.getArguments().size() > 0)
                incomingByte = frame.getArgument(0);
            if (incomingByte != null) {
                String date = terminalPrintedLines.size() == 0
                        || terminalPrintedLines
                        .get(terminalPrintedLines.size() - 1).isEndedWithNewLine ? TerminalFragment
                        .getTimeAsString() + " [RX] : "
                        : "";
                boolean isEndedWithNewLine = incomingByte.length > 0
                        && incomingByte[incomingByte.length - 1] == (byte) '\n';
                if (lastItemEndedWithNewLine) {
                    terminalPrintedLines.add(new TerminalPrintedLine(date,
                            incomingByte,
                            isEndedWithNewLine, true));
                    tempLines.add(new TerminalPrintedLine(date, incomingByte,
                            isEndedWithNewLine, true));
                } else if (terminalPrintedLines.size() > 0
                        && tempLines.size() > 0) {
                    if (!terminalPrintedLines.get(terminalPrintedLines.size() - 1).isEndedWithNewLine)
                        terminalPrintedLines.get(terminalPrintedLines.size() - 1).print = ArrayUtils.concat(terminalPrintedLines
                                .get(terminalPrintedLines.size() - 1).print, incomingByte);
                    else
                        terminalPrintedLines.get(terminalPrintedLines.size() - 1).print = terminalPrintedLines
                                .get(terminalPrintedLines.size() - 1).print;
                    tempLines.get(tempLines.size() - 1).print = terminalPrintedLines
                            .get(terminalPrintedLines.size() - 1).print;
                    if (isEndedWithNewLine)
                        terminalPrintedLines
                                .get(terminalPrintedLines.size() - 1).isEndedWithNewLine = true;
                }
                lastItemEndedWithNewLine = isEndedWithNewLine;
                greaterThanThousand = terminalPrintedLines.size() > 1000;
                if (greaterThanThousand) {
                    terminalPrintedLines.remove(0);
                    tempLines.remove(0);
                }
                switch (frame.getFunctionId()) {
                    case WRITE:
                        if (eventHandler != null) {
                            eventHandler.onPrint();
                        }
                        break;
                    case PRINT:
                        if (eventHandler != null) {
                            eventHandler.onPrint();
                        }
                        break;
                }
            }
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
        public void onPrint();
    }

}
