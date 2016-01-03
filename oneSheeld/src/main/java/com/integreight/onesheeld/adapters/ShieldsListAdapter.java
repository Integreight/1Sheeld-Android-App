package com.integreight.onesheeld.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.OneSheeldApplication;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.model.Shield;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.ControllerParent.SelectionAction;
import com.integreight.onesheeld.utils.AppShields;
import com.integreight.onesheeld.utils.Log;

public class ShieldsListAdapter extends BaseAdapter implements Filterable {
    MainActivity activity;
    LayoutInflater inflater;
    ControllerParent<?> type = null;
    OneSheeldApplication app;
    private Handler uiHandler;
    private SparseArray<Shield> shieldsList;

    public ShieldsListAdapter(Activity a) {
        this.activity = (MainActivity) a;
        this.shieldsList = AppShields.getInstance().getShieldsArray();
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        app = (OneSheeldApplication) activity.getApplication();
        uiHandler = new Handler();
    }

    public int getCount() {
        return shieldsList.size();
    }

    public Shield getItem(int position) {
        return shieldsList.get(position);
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
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

            row = inflater.inflate(R.layout.shield_list_item, parent, false);

            holder = new Holder();
            holder.name = (TextView) row
                    .findViewById(R.id.shield_list_item_name_textview);
            holder.icon = (ImageView) row
                    .findViewById(R.id.shield_list_item_symbol_imageview);
            holder.selectionCircle = (ImageView) row
                    .findViewById(R.id.shield_list_item_selection_circle_imageview);
            holder.blackUpperLayer = (ImageView) row
                    .findViewById(R.id.shildListItemBlackSquare);
            holder.container = (ViewGroup) row.findViewById(R.id.container);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }
        final Holder tempHolder = holder;
        final Shield shield = shieldsList.get(position);
        Integer iconId = shield.symbolId;
        Integer imageId = shield.itemBackgroundColor;

        holder.name.setText(shield.name);
        if (holder.icon.getDrawingCache() != null) {
            holder.icon.getDrawingCache().recycle();
        }
        holder.icon.setImageBitmap(null);
        holder.icon.setImageDrawable(null);
        holder.icon.setBackgroundResource(iconId);

        holder.container.setBackgroundColor(imageId);

        if (shield.mainActivitySelection) {
            tempHolder.selectionCircle.setVisibility(View.VISIBLE);
            tempHolder.blackUpperLayer.setVisibility(View.INVISIBLE);
        } else {
            tempHolder.selectionCircle.setVisibility(View.INVISIBLE);
            tempHolder.blackUpperLayer.setVisibility(View.VISIBLE);
        }
        holder.container.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                shield.mainActivitySelection = !shield.mainActivitySelection;
                if (activity.looperThread == null
                        || (!activity.looperThread.isAlive() || activity.looperThread
                        .isInterrupted()))
                    activity.initLooperThread();
                activity.backgroundThreadHandler
                        .removeCallbacksAndMessages(null);
                activity.backgroundThreadHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (shield.mainActivitySelection
                                && shield.shieldType != null) {
                            try {
                                type = shield.shieldType.newInstance();
                            } catch (java.lang.InstantiationException e) {
                                // TODO Auto-generated catch block
                                Log.e("TAG",
                                        "backgroundThreadHandler::InstantiationException",
                                        e);
                            } catch (IllegalAccessException e) {
                                // TODO Auto-generated catch block
                                Log.e("TAG",
                                        "backgroundThreadHandler::IllegalAccessException",
                                        e);
                            }
                            final SelectionAction selectionAction = new SelectionAction() {

                                @Override
                                public void onSuccess() {
                                    // tempHolder.selectionButton.setVisibility(View.VISIBLE);
                                    uiHandler.removeCallbacksAndMessages(null);
                                    uiHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            tempHolder.selectionCircle
                                                    .setVisibility(View.VISIBLE);
                                            tempHolder.blackUpperLayer
                                                    .setVisibility(View.INVISIBLE);
                                        }
                                    });
                                    shield.mainActivitySelection = true;
                                    shieldsList.put(position, shield);
                                    AppShields.getInstance().putShield(
                                            shield.position, shield);
                                }

                                @Override
                                public void onFailure() {
                                    shield.mainActivitySelection = false;
                                    uiHandler.removeCallbacksAndMessages(null);
                                    uiHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            tempHolder.selectionCircle
                                                    .setVisibility(View.INVISIBLE);
                                            tempHolder.blackUpperLayer
                                                    .setVisibility(View.VISIBLE);
                                        }
                                    });
                                    if (app.getRunningShields().get(shield.tag) != null) {
                                        app.getRunningShields().get(shield.tag)
                                                .resetThis();
                                        app.getRunningShields().remove(
                                                shield.tag);
                                    }
                                    shieldsList.put(position, shield);
                                    AppShields.getInstance().putShield(
                                            shield.position, shield);
                                }
                            };
                            if (type != null) {
                                if (shield.isInvalidatable == 1) {
                                    type.setActivity(activity)
                                            .init(shield.tag)
                                            .invalidate(selectionAction, true);
                                } else {
                                    selectionAction.onSuccess();
                                    type.setActivity(activity).init(
                                            shield.tag);
                                }
                            }
                        } else {
                            uiHandler.removeCallbacksAndMessages(null);
                            uiHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    tempHolder.selectionCircle
                                            .setVisibility(View.INVISIBLE);
                                    tempHolder.blackUpperLayer
                                            .setVisibility(View.VISIBLE);
                                }
                            });
                            if (app.getRunningShields().get(shield.tag) != null) {
                                app.getRunningShields().get(shield.tag)
                                        .resetThis();
                                app.getRunningShields().remove(shield.tag);
                            }
                        }
                    }
                });
            }
        });
        return row;
    }

    public void updateList(SparseArray<Shield> shieldsList) {
        if (shieldsList != null) {
            this.shieldsList = shieldsList;
            notifyDataSetChanged();
        }
    }

    public void reset() {
        shieldsList = AppShields.getInstance().getShieldsArray();
        applyToControllerTable();
        notifyDataSetChanged();
    }

    Handler handler = new Handler();

    public void applyToControllerTable() {
        activity.findViewById(R.id.progressShieldInit).setVisibility(
                View.VISIBLE);
        activity.findViewById(R.id.operationsLogo)
                .setVisibility(View.INVISIBLE);
        for (int i = 0; i < shieldsList.size(); i++) {
            final Shield shield = shieldsList.get(i);
            final int x = i;
            if (activity.looperThread == null
                    || (!activity.looperThread.isAlive() || activity.looperThread
                    .isInterrupted()))
                activity.initLooperThread();
            activity.backgroundThreadHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    if (shield.mainActivitySelection
                            && shield.shieldType != null) {
                        if (app.getRunningShields().get(shield.tag) == null) {
                            SelectionAction selectionAction = new SelectionAction() {

                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onFailure() {
                                    shield.mainActivitySelection = false;
                                    shieldsList.setValueAt(x, shield);
                                    AppShields.getInstance().putShield(x,
                                            shield);
                                    if (app.getRunningShields().get(shield.tag) != null) {
                                        app.getRunningShields().get(shield.tag)
                                                .resetThis();
                                        app.getRunningShields().remove(
                                                shield.tag);
                                    }
                                    uiHandler.removeCallbacksAndMessages(null);
                                    uiHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            };
                            try {
                                type = shield.shieldType.newInstance();
                            } catch (java.lang.InstantiationException e) {
                                // TODO Auto-generated catch block
                                Log.e("TAG",
                                        "applyToControllerTable()::InstantiationException",
                                        e);
                            } catch (IllegalAccessException e) {
                                // TODO Auto-generated catch block
                                Log.e("TAG",
                                        "applyToControllerTable()::IllegalAccessException",
                                        e);
                            }
                            if (type != null) {
                                if (shield.isInvalidatable == 1) {
                                    type.setActivity(activity)
                                            .init(shield.tag)
                                            .invalidate(selectionAction, false);
                                } else {
                                    type.setActivity(activity).init(
                                            shield.tag);
                                }
                            }
                        }
                    } else {
                        if (app.getRunningShields().get(shield.tag) != null) {
                            app.getRunningShields().get(shield.tag).resetThis();
                            app.getRunningShields().remove(shield.tag);
                        }
                    }
                    if (x == shieldsList.size() - 1) {
                        uiHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                activity.findViewById(R.id.progressShieldInit)
                                        .setVisibility(View.INVISIBLE);
                                activity.findViewById(R.id.operationsLogo)
                                        .setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
        }
    }

    static class Holder {
        TextView name;
        ImageView icon;
        // ToggleButton selectionButton;
        ImageView selectionCircle;
        ImageView blackUpperLayer;
        ViewGroup container;
    }

    @Override
    public Filter getFilter() {
        // TODO Auto-generated method stub
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence arg0, FilterResults arg1) {
                updateList((SparseArray<Shield>) arg1.values);
            }

            @Override
            protected FilterResults performFiltering(CharSequence arg0) {
                FilterResults results = new FilterResults();
                SparseArray<Shield> filteredShields = new SparseArray<Shield>();
                if (arg0 != null) {
                    for (int i = 0; i < AppShields.getInstance()
                            .getShieldsArray().size(); i++) {
                        Shield uiShield = AppShields.getInstance().getShield(i);
                        if (uiShield.name.toLowerCase().startsWith(
                                arg0.toString().toLowerCase())) {
                            filteredShields.put(filteredShields.size(),
                                    uiShield);
                        }
                    }
                } else
                    filteredShields = AppShields.getInstance()
                            .getShieldsArray();
                results.values = filteredShields;
                results.count = filteredShields.size();
                return results;
            }
        };
    }
}
