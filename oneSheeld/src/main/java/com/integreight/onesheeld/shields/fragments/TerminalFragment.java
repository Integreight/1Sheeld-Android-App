package com.integreight.onesheeld.shields.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.adapters.TerminalLinesAdapter;
import com.integreight.onesheeld.model.TerminalPrintedLine;
import com.integreight.onesheeld.shields.ShieldFragmentParent;
import com.integreight.onesheeld.shields.controller.TerminalShield;
import com.integreight.onesheeld.shields.controller.TerminalShield.TerminalHandler;
import com.integreight.onesheeld.utils.customviews.OneSheeldButton;
import com.integreight.onesheeld.utils.customviews.OneSheeldEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TerminalFragment extends ShieldFragmentParent<TerminalFragment> {
    private ListView output;
    private OneSheeldEditText inputField;
    private OneSheeldButton send,copyAll;
    private boolean endedWithNewLine = false;
    private TerminalLinesAdapter outputAdapter;
    private ToggleButton timeToggle, autoScrollingToggle;

    public static String getTimeAsString() {
        return DateFormat.format("MM/dd/yyyy hh:mm:ss", new java.util.Date())
                .toString();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.terminal_shield_fragment_layout,
                container, false);
    }

    @Override
    public void doOnViewCreated(final View v, @Nullable Bundle savedInstanceState) {
        output = (ListView) v.findViewById(R.id.terminalOutput);
        outputAdapter = new TerminalLinesAdapter(activity,
                new ArrayList<TerminalPrintedLine>());
        output.setAdapter(outputAdapter);
        outputAdapter = (TerminalLinesAdapter) output.getAdapter();
        inputField = (OneSheeldEditText) v.findViewById(R.id.terminalInput);
        send = (OneSheeldButton) v.findViewById(R.id.send);
        timeToggle = (ToggleButton) v.findViewById(R.id.toggleTime);
        autoScrollingToggle = (ToggleButton) v
                .findViewById(R.id.toggleAutoScrolling);
        copyAll = (OneSheeldButton) v.findViewById(R.id.copy_all);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            copyAll.setVisibility(View.INVISIBLE);
        }
        timeToggle.setChecked(((TerminalShield) getApplication()
                .getRunningShields().get(getControllerTag())).isTimeOn);
        autoScrollingToggle.setChecked(((TerminalShield) getApplication()
                .getRunningShields().get(getControllerTag())).isAutoScrolling);
        timeToggle.setTypeface(getApplication().appFont);
        autoScrollingToggle.setTypeface(getApplication().appFont);
        timeToggle
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        ((TerminalShield) getApplication().getRunningShields()
                                .get(getControllerTag())).isTimeOn = isChecked;
                        outputAdapter.isTimeOn = isChecked;
                        if (!TerminalLinesAdapter.isTextSelected)
                            outputAdapter.notifyDataSetChanged();
                    }
                });
        autoScrollingToggle
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        ((TerminalShield) getApplication().getRunningShields()
                                .get(getControllerTag())).isAutoScrolling = isChecked;
                    }
                });
        v.findViewById(R.id.clearTerminal).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        outputAdapter
                                .updateLines(new ArrayList<TerminalPrintedLine>());
                        ((TerminalShield) getApplication().getRunningShields()
                                .get(getControllerTag())).lastItemEndedWithNewLine = true;
                        ((TerminalShield) getApplication().getRunningShields()
                                .get(getControllerTag())).tempLines = new CopyOnWriteArrayList<TerminalPrintedLine>();
                        ((TerminalShield) getApplication().getRunningShields()
                                .get(getControllerTag())).terminalPrintedLines = new CopyOnWriteArrayList<TerminalPrintedLine>();
                    }
                });
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ((TerminalShield) getApplication().getRunningShields().get(
                        getControllerTag())).input(inputField.getText()
                        .toString());
                ((TerminalShield) getApplication().getRunningShields().get(
                        getControllerTag())).terminalPrintedLines
                        .add(new TerminalPrintedLine((!endedWithNewLine ? "\n"
                                : "") + getTimeAsString() + " [TX] " + ": ",
                                inputField.getText().toString().getBytes(), true, false));
                ((TerminalShield) getApplication().getRunningShields().get(
                        getControllerTag())).tempLines
                        .add(new TerminalPrintedLine((!endedWithNewLine ? "\n"
                                : "") + getTimeAsString() + " [TX] " + ": ",
                                inputField.getText().toString().getBytes(), true, false));
                ((TerminalShield) getApplication().getRunningShields().get(
                        getControllerTag())).lastItemEndedWithNewLine = true;
                outputAdapter
                        .updateLines(((TerminalShield) getApplication()
                                .getRunningShields().get(getControllerTag())).tempLines);
                if (((TerminalShield) getApplication().getRunningShields().get(
                        getControllerTag())).tempLines.size() > 0
                        && ((TerminalShield) getApplication()
                        .getRunningShields().get(getControllerTag())).isAutoScrolling)
                    output.setSelection(((TerminalShield) getApplication()
                            .getRunningShields().get(getControllerTag())).tempLines
                            .size() - 1);
                endedWithNewLine = true;
                inputField.setText("");
                InputMethodManager imm = (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputField.getWindowToken(), 0);
            }
        });
        copyAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputAdapter.copyAll();
            }
        });

        int i = 0;
        for (final int id : ((TerminalShield) getApplication()
                .getRunningShields().get(getControllerTag())).encodingMths) {
            final int x = i;
            v.findViewById(id).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View vi) {
                    if (TerminalShield.selectedEnMth != x) {
                        v.findViewById(
                                ((TerminalShield) getApplication()
                                        .getRunningShields().get(
                                                getControllerTag())).encodingMths[TerminalShield.selectedEnMth])
                                .setBackgroundColor(
                                        getResources()
                                                .getColor(
                                                        R.color.arduino_conn_resetAll_bg));
                        v.findViewById(
                                ((TerminalShield) getApplication()
                                        .getRunningShields().get(
                                                getControllerTag())).encodingMths[x])
                                .setBackgroundColor(
                                        getResources().getColor(
                                                R.color.arduinoPinsSelector));
                        TerminalShield.selectedEnMth = x;
                        ((TerminalShield) getApplication().getRunningShields()
                                .get(getControllerTag())).tempLines = new CopyOnWriteArrayList<TerminalPrintedLine>();
                        for (TerminalPrintedLine line : ((TerminalShield) getApplication()
                                .getRunningShields().get(getControllerTag())).terminalPrintedLines) {
                            ((TerminalShield) getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag())).tempLines
                                    .add(new TerminalPrintedLine(
                                            line.date,
                                            line.print,
                                            line.isEndedWithNewLine, line
                                            .isRx()));
                        }
                        outputAdapter
                                .updateLines(((TerminalShield) getApplication()
                                        .getRunningShields().get(
                                                getControllerTag())).tempLines);
                        if (((TerminalShield) getApplication()
                                .getRunningShields().get(getControllerTag())).tempLines
                                .size() > 0
                                && ((TerminalShield) getApplication()
                                .getRunningShields().get(
                                        getControllerTag())).isAutoScrolling)
                            output.setSelection(((TerminalShield) getApplication()
                                    .getRunningShields()
                                    .get(getControllerTag())).tempLines.size() - 1);
                    }
                }
            });
            i++;
        }
    }

    @Override
    public void doOnStart() {
        if (getView() != null) {
            getView().findViewById(
                    ((TerminalShield) getApplication().getRunningShields().get(
                            getControllerTag())).encodingMths[0])
                    .setBackgroundColor(
                            getResources().getColor(
                                    R.color.arduino_conn_resetAll_bg));
            getView().findViewById(
                    ((TerminalShield) getApplication().getRunningShields().get(
                            getControllerTag())).encodingMths[TerminalShield.selectedEnMth])
                    .setBackgroundColor(
                            getResources().getColor(R.color.arduinoPinsSelector));
        }
        ((TerminalShield) getApplication().getRunningShields().get(
                getControllerTag())).setEventHandler(terminalHandler);
        if (((TerminalShield) getApplication().getRunningShields().get(
                getControllerTag())).terminalPrintedLines == null)
            ((TerminalShield) getApplication().getRunningShields().get(
                    getControllerTag())).terminalPrintedLines = new CopyOnWriteArrayList<>();
        ((TerminalShield) getApplication().getRunningShields().get(
                getControllerTag())).tempLines = new CopyOnWriteArrayList<>();
        for (TerminalPrintedLine line : ((TerminalShield) getApplication()
                .getRunningShields().get(getControllerTag())).terminalPrintedLines) {
            ((TerminalShield) getApplication().getRunningShields().get(
                    getControllerTag())).tempLines.add(new TerminalPrintedLine(
                    line.date, line.print,
                    line.isEndedWithNewLine, line.isRx()));
        }
        outputAdapter.updateLines(((TerminalShield) getApplication()
                .getRunningShields().get(getControllerTag())).tempLines);
        if (((TerminalShield) getApplication().getRunningShields().get(
                getControllerTag())).tempLines.size() > 0
                && ((TerminalShield) getApplication().getRunningShields().get(
                getControllerTag())).isAutoScrolling)
            output.setSelection(((TerminalShield) getApplication()
                    .getRunningShields().get(getControllerTag())).tempLines
                    .size() - 1);
        outputAdapter.isTimeOn = ((TerminalShield) getApplication().getRunningShields().get(getControllerTag())).isTimeOn;
        if (!TerminalLinesAdapter.isTextSelected)
            outputAdapter.notifyDataSetChanged();
    }

    @Override
    public void doOnPause() {
        getAppActivity().hideSoftKeyboard();
    }

    TerminalHandler terminalHandler = new TerminalHandler() {
        @Override
        public void onPrint() {
            if (canChangeUI())
                uiHandler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        if (output != null && canChangeUI()) {
                            outputAdapter
                                    .updateLines((List<TerminalPrintedLine>) ((TerminalShield) getApplication()
                                            .getRunningShields().get(
                                                    getControllerTag())).tempLines
                                            .clone());
                            if (((TerminalShield) getApplication()
                                    .getRunningShields().get(getControllerTag())).tempLines
                                    .size() > 0
                                    && ((TerminalShield) getApplication()
                                    .getRunningShields().get(
                                            getControllerTag())).isAutoScrolling)
                                output.setSelection(((TerminalShield) getApplication()
                                        .getRunningShields()
                                        .get(getControllerTag())).tempLines.size() - 1);
                        }
                    }
                });
        }
    };

}
