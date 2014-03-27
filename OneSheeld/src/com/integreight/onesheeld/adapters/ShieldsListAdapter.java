package com.integreight.onesheeld.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ControllerParent;

public class ShieldsListAdapter extends BaseAdapter implements Filterable {
	MainActivity activity;
	public List<UIShield> shieldList;
	LayoutInflater inflater;
	ControllerParent<?> type = null;
	OneSheeldApplication app;

	public ShieldsListAdapter(Activity a) {
		this.activity = (MainActivity) a;
		this.shieldList = Arrays.asList(UIShield.values());
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		app = (OneSheeldApplication) activity.getApplication();
	}

	public int getCount() {
		return shieldList.size();
	}

	public UIShield getItem(int position) {
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

			row = inflater.inflate(R.layout.shield_list_item, parent, false);

			holder = new Holder();
			holder.name = (TextView) row
					.findViewById(R.id.shield_list_item_name_textview);
			holder.icon = (ImageView) row
					.findViewById(R.id.shield_list_item_symbol_imageview);
			// holder.selectionButton = (ToggleButton) row
			// .findViewById(R.id.shield_list_item_selection_toggle_button);
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
		final UIShield shield = shieldList.get(position);
		String name = shield.getName();
		Integer iconId = shield.getSymbolId();
		Integer imageId = shield.getItemBackgroundColor();

		holder.name.setText(name);
		holder.icon.setBackgroundResource(iconId);

		row.setBackgroundColor(imageId);

		if (shield.isMainActivitySelection()) {
			// holder.selectionButton.setChecked(true);
			// tempHolder.selectionButton.setVisibility(View.VISIBLE);
			tempHolder.selectionCircle.setVisibility(View.VISIBLE);
			tempHolder.blackUpperLayer.setVisibility(View.INVISIBLE);
		} else {
			// holder.selectionButton.setChecked(false);
			// tempHolder.selectionButton.setVisibility(View.INVISIBLE);
			tempHolder.selectionCircle.setVisibility(View.INVISIBLE);
			tempHolder.blackUpperLayer.setVisibility(View.VISIBLE);
		}
		// RelativeLayout.LayoutParams head_params =
		// (RelativeLayout.LayoutParams)((RelativeLayout)row).getLayoutParams();
		// head_params.setMargins(0, -20, 0, 0); //substitute parameters for
		// left, top, right, bottom
		// row.setLayoutParams(head_params);
		holder.container.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				shield.setMainActivitySelection(!shield
						.isMainActivitySelection());
				if (shield.isMainActivitySelection()
						&& shield.getShieldType() != null) {
					// tempHolder.selectionButton.setVisibility(View.VISIBLE);
					tempHolder.selectionCircle.setVisibility(View.VISIBLE);
					tempHolder.blackUpperLayer.setVisibility(View.INVISIBLE);
					try {
						type = shield.getShieldType().newInstance();
					} catch (java.lang.InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					type.setActivity(activity).setTag(shield.getName());
				} else {
					// tempHolder.selectionButton.setVisibility(View.INVISIBLE);
					tempHolder.selectionCircle.setVisibility(View.INVISIBLE);
					tempHolder.blackUpperLayer.setVisibility(View.VISIBLE);
					if (app.getRunningShields().get(shield.getName()) != null) {
						app.getRunningShields().get(shield.getName())
								.resetThis();
						app.getRunningShields().remove(shield.getName());
					}
				}
			}
		});
		return row;
	}

	public void updateList(List<UIShield> shieldsList) {
		if (shieldsList != null) {
			this.shieldList = shieldsList;
			// else
			// shieldList = new ArrayList<UIShield>();
			notifyDataSetChanged();
		}
	}

	public void selectAll() {
		shieldList = Arrays.asList(UIShield.values());
		applyToControllerTable();
		notifyDataSetChanged();
	}

	public void reset() {
		// for (UIShield item : shieldList) {
		// item.setMainActivitySelection(false);
		// }
		shieldList = Arrays.asList(UIShield.values());
		applyToControllerTable();
		notifyDataSetChanged();
	}

	Handler handler = new Handler();

	public void applyToControllerTable() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				for (final UIShield shield : shieldList) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							if (shield.isMainActivitySelection()
									&& shield.getShieldType() != null) {
								if (app.getRunningShields().get(
										shield.getName()) == null) {
									try {
										type = shield.getShieldType()
												.newInstance();
									} catch (java.lang.InstantiationException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IllegalAccessException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									type.setActivity(activity).setTag(
											shield.getName());
								}
							} else {
								if (app.getRunningShields().get(
										shield.getName()) != null) {
									app.getRunningShields()
											.get(shield.getName()).resetThis();
									app.getRunningShields().remove(
											shield.getName());
								}
							}
						}
					});
				}
			}
		}).start();
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

			@Override
			protected void publishResults(CharSequence arg0, FilterResults arg1) {
				@SuppressWarnings("unchecked")
				List<UIShield> values = (List<UIShield>) arg1.values;
				updateList(values);
			}

			@Override
			protected FilterResults performFiltering(CharSequence arg0) {
				FilterResults results = new FilterResults();
				List<UIShield> filteredShields = new ArrayList<UIShield>();
				if (arg0 != null) {
					for (UIShield uiShield : UIShield.values()) {
						if (uiShield.getName().toLowerCase()
								.startsWith(arg0.toString().toLowerCase())) {
							filteredShields.add(uiShield);
						}
					}
				} else
					filteredShields = Arrays.asList(UIShield.values());
				results.values = filteredShields;
				results.count = filteredShields.size();
				return results;
			}
		};
	}

}
