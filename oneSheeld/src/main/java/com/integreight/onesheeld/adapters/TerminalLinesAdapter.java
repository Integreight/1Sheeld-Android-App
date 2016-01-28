package com.integreight.onesheeld.adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.TerminalPrintedLine;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.List;

public class TerminalLinesAdapter extends BaseAdapter {
    Activity activity;
    List<TerminalPrintedLine> lines;
    LayoutInflater inflater;
    public boolean isTimeOn = true;
    public static boolean isTextSelected = false;
    private static String copyLine = "";
    private Handler uiHandler;
    private Runnable copyRunnable;
    ClipboardManager clipboardManager = null;
    ClipData clipData = null;

    public TerminalLinesAdapter(Activity a, List<TerminalPrintedLine> lines) {
        this.activity = a;
        this.lines = lines;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        uiHandler = new Handler();
        copyRunnable = new Runnable() {
            @Override
            public void run() {
                clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (!copyLine.equals("")) {
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("OneSheeldClipboard", copyLine));
                        Toast.makeText(activity.getApplicationContext(), R.string.terminal_line_copied_toast, Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(activity.getApplicationContext(), R.string.terminal_couldnt_copy_empty_line_toast, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public int getCount() {
        return lines.size();
    }

    public TerminalPrintedLine getItem(int position) {
        return lines.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void updateLines(List<TerminalPrintedLine> lines) {
        this.lines = lines;
        if (!isTextSelected)
            notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        View row = convertView;
        Holder holder = null;
        if (row == null) {

            row = inflater.inflate(R.layout.terminal_printed_line, parent,
                    false);

            holder = new Holder();
            holder.contScrollView = row.findViewById(R.id.terminal_line_scroll_view);
            holder.contTextContainer = row.findViewById(R.id.terminal_line_text_container);
            holder.dateX = (OneSheeldTextView) row.findViewById(R.id.dateX);
            holder.output = (OneSheeldTextView) row
                    .findViewById(R.id.terminalOutput);
            holder.seperator = (TextView) row.findViewById(R.id.seperator);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }
        final TerminalPrintedLine line = lines.get(position);
        if (isTimeOn) {
            holder.dateX.setVisibility(View.VISIBLE);
            holder.dateX.setText(line.date);
        } else
            holder.dateX.setVisibility(View.GONE);
        holder.output.setText(line.print);
        holder.dateX.setClickable(false);
        holder.output.setClickable(false);
        if (line.isRx()) {
            holder.output.setTextColor(Color.GREEN);
            holder.dateX.setTextColor(Color.GREEN);
        } else {
            holder.output.setTextColor(Color.BLUE);
            holder.dateX.setTextColor(Color.BLUE);
        }
        final View textContainer = holder.contTextContainer;
        final View scrollContainer = holder.contScrollView;

        holder.contScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_CANCEL:
                        if (isTextSelected) {
                            textContainer.setBackgroundColor(Color.BLACK);
                            scrollContainer.setBackgroundColor(Color.BLACK);
                            isTextSelected = false;
                            uiHandler.removeCallbacks(copyRunnable);
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (!isTextSelected) {
                            textContainer.setBackgroundColor(activity.getResources().getColor(R.color.arduino_conn_gray_bg));
                            scrollContainer.setBackgroundColor(activity.getResources().getColor(R.color.arduino_conn_gray_bg));
                            if (isTimeOn)
                                copyLine = line.date+line.print;
                            else
                                copyLine = line.print;
                            uiHandler.postDelayed(copyRunnable, 1500);
                            isTextSelected = true;
                        }
                        break;
                    default:
                        //textContainer.setBackgroundColor(Color.BLACK);
                        break;
                }
                return false;
            }
        });
        ((ViewGroup) holder.output.getParent()).invalidate();
        ((ViewGroup) holder.output.getParent().getParent()).invalidate();
        return row;
    }

    private StringBuilder allLines;
    public void copyAll(){
        if (!lines.isEmpty()) {
            allLines = new StringBuilder("");
            if (isTimeOn){
                for (int lineCounter = 0; lineCounter < lines.size(); lineCounter++) {
                    allLines.append(lines.get(lineCounter).date+lines.get(lineCounter).print + "\n");
                }
            }else {
                for (int lineCounter = 0; lineCounter < lines.size(); lineCounter++) {
                    allLines.append(lines.get(lineCounter).print + "\n");
                }
            }

            clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("OneSheeldClipboard", new String(allLines)));
                Toast.makeText(activity.getApplicationContext(), R.string.terminal_lines_copied_toast, Toast.LENGTH_SHORT).show();
            }
        }
    }

    static class Holder {
        View contScrollView;
        View contTextContainer;
        OneSheeldTextView dateX, output;
        TextView seperator;
    }

}
