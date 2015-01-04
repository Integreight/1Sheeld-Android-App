package com.integreight.onesheeld.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.TerminalPrintedLine;
import com.integreight.onesheeld.utils.customviews.OneSheeldTextView;

import java.util.List;

public class TerminalLinesAdapter extends BaseAdapter {
    Activity activity;
    List<TerminalPrintedLine> lines;
    LayoutInflater inflater;
    public boolean isTimeOn = true;

    public TerminalLinesAdapter(Activity a, List<TerminalPrintedLine> lines) {
        this.activity = a;
        this.lines = lines;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            holder.dateX = (OneSheeldTextView) row.findViewById(R.id.dateX);
            holder.output = (OneSheeldTextView) row
                    .findViewById(R.id.terminalOutput);
            holder.seperator = (TextView) row.findViewById(R.id.seperator);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }
        TerminalPrintedLine line = lines.get(position);
        if (isTimeOn) {
            holder.dateX.setVisibility(View.VISIBLE);
            holder.dateX.setText(line.date);
        } else
            holder.dateX.setVisibility(View.GONE);
        holder.output.setText(line.print);
        if (line.isRx()) {
            holder.output.setTextColor(Color.GREEN);
            holder.dateX.setTextColor(Color.GREEN);
        } else {
            holder.output.setTextColor(Color.BLUE);
            holder.dateX.setTextColor(Color.BLUE);
        }
        ((ViewGroup) holder.output.getParent()).invalidate();
        ((ViewGroup) holder.output.getParent().getParent()).invalidate();
        return row;
    }

    static class Holder {
        OneSheeldTextView dateX, output;
        TextView seperator;
    }

}
