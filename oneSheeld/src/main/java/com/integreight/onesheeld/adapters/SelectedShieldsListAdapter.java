package com.integreight.onesheeld.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.Shield;
import com.integreight.onesheeld.utils.AppShields;

import java.util.ArrayList;
import java.util.List;

public class SelectedShieldsListAdapter extends BaseAdapter {
    Activity activity;
    List<Shield> shieldList;
    LayoutInflater inflater;

    public SelectedShieldsListAdapter(Activity a) {
        this.activity = a;
        this.shieldList = new ArrayList<Shield>();
        for (int i = 0; i < AppShields.getInstance().getShieldsArray().size(); i++) {
            Shield shield = AppShields.getInstance().getShield(i);
            if (shield.mainActivitySelection)
                this.shieldList.add(shield);
        }

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public int getCount() {
        return shieldList.size();
    }

    public Shield getItem(int position) {
        return shieldList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        View row = convertView;
        Holder holder = null;
        if (row == null) {

            row = inflater.inflate(R.layout.selected_shields_list_item, parent,
                    false);

            holder = new Holder();
            holder.symbol = (ImageView) row
                    .findViewById(R.id.selected_shield_list_item_symbol_imageview);
            holder.selectionCircle = (ImageView) row
                    .findViewById(R.id.selected_shield_list_item_selection_circle_imageview);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        Shield shield = shieldList.get(position);
        Integer iconId = shield.symbolId;
        Integer imageId = shield.itemBackgroundColor;
        if (holder.symbol.getDrawingCache() != null) {
            holder.symbol.getDrawingCache().recycle();
        }
        holder.symbol.setImageBitmap(null);
        holder.symbol.setImageDrawable(null);
        holder.symbol.setBackgroundResource(iconId);

        row.setBackgroundColor(imageId);

        if (UIShield.getShieldsActivitySelection() != null
                && UIShield.getShieldsActivitySelection().getId() == shield.id) {
            holder.selectionCircle.setVisibility(View.VISIBLE);
        } else {
            holder.selectionCircle.setVisibility(View.INVISIBLE);
        }
        return row;
    }

    static class Holder {
        ImageView symbol;
        ImageView selectionCircle;
    }

}
