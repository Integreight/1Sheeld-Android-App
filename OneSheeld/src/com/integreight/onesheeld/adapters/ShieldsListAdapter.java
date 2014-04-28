package com.integreight.onesheeld.adapters;

import java.util.ArrayList;
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
import com.integreight.onesheeld.utils.ControllerParent.SelectionAction;

public class ShieldsListAdapter extends BaseAdapter implements Filterable {
	MainActivity activity;
	public List<UIShield> shieldList;
	LayoutInflater inflater;
	ControllerParent<?> type = null;
	OneSheeldApplication app;

	public ShieldsListAdapter(Activity a) {
		this.activity = (MainActivity) a;
		this.shieldList = UIShield.valuesFiltered();
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
		if (holder.icon.getDrawingCache() != null) {
			holder.icon.getDrawingCache().recycle();
		}
		holder.icon.setImageBitmap(null);
		holder.icon.setImageDrawable(null);
		holder.icon.setBackgroundResource(iconId);

		row.setBackgroundColor(imageId);

		if (shield.isMainActivitySelection()) {
			tempHolder.selectionCircle.setVisibility(View.VISIBLE);
			tempHolder.blackUpperLayer.setVisibility(View.INVISIBLE);
		} else {
			tempHolder.selectionCircle.setVisibility(View.INVISIBLE);
			tempHolder.blackUpperLayer.setVisibility(View.VISIBLE);
		}
		holder.container.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				shield.setMainActivitySelection(!shield
						.isMainActivitySelection());
				if (shield.isMainActivitySelection()
						&& shield.getShieldType() != null) {

					try {
						type = shield.getShieldType().newInstance();
					} catch (java.lang.InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					final SelectionAction selectionAction = new SelectionAction() {

						@Override
						public void onSuccess() {
							// tempHolder.selectionButton.setVisibility(View.VISIBLE);
							tempHolder.selectionCircle
									.setVisibility(View.VISIBLE);
							tempHolder.blackUpperLayer
									.setVisibility(View.INVISIBLE);
							// activity.backgroundThreadHandler.post(new
							// Runnable() {
							//
							// @Override
							// public void run() {
							// TODO Auto-generated method stub
							shield.setMainActivitySelection(true);
						}

						@Override
						public void onFailure() {
							shield.setMainActivitySelection(false);
							tempHolder.selectionCircle
									.setVisibility(View.INVISIBLE);
							tempHolder.blackUpperLayer
									.setVisibility(View.VISIBLE);
							activity.backgroundThreadHandler
									.post(new Runnable() {

										@Override
										public void run() {
											if (app.getRunningShields().get(
													shield.name()) != null) {
												app.getRunningShields()
														.get(shield.name())
														.resetThis();
												app.getRunningShields().remove(
														shield.name());
											}
										}
									});
						}
					};
					if (type != null) {
						if (shield.isInvalidatable()) {
							type.setActivity(activity).setTag(shield.name())
									.invalidate(selectionAction, true);
						} else {
							selectionAction.onSuccess();
							type.setActivity(activity).setTag(shield.name());
						}
					}
					// }
					// });
				} else {
					// tempHolder.selectionButton.setVisibility(View.INVISIBLE);
					tempHolder.selectionCircle.setVisibility(View.INVISIBLE);
					tempHolder.blackUpperLayer.setVisibility(View.VISIBLE);
					activity.backgroundThreadHandler.post(new Runnable() {

						@Override
						public void run() {
							if (app.getRunningShields().get(shield.name()) != null) {
								app.getRunningShields().get(shield.name())
										.resetThis();
								app.getRunningShields().remove(shield.name());
							}
						}
					});
				}
			}
		});
		return row;
	}

	public void updateList(List<UIShield> shieldsList) {
		if (shieldsList != null) {
			this.shieldList = shieldsList;
			notifyDataSetChanged();
		}
	}

	public void selectAll() {
		shieldList = UIShield.valuesFiltered();
		applyToControllerTable();
		notifyDataSetChanged();
	}

	public void reset() {
		shieldList = UIShield.valuesFiltered();
		applyToControllerTable();
		notifyDataSetChanged();
	}

	Handler handler = new Handler();

	public void applyToControllerTable() {
		int i = 0;
		for (final UIShield shield : shieldList) {
			final int x = i;
			// activity.backgroundThreadHandler.post(new Runnable() {
			//
			// @Override
			// public void run() {
			// TODO Auto-generated method stub

			if (shield.isMainActivitySelection()
					&& shield.getShieldType() != null) {
				if (app.getRunningShields().get(shield.name()) == null) {
					SelectionAction selectionAction = new SelectionAction() {

						@Override
						public void onSuccess() {
						}

						@Override
						public void onFailure() {
							shieldList.get(x).setMainActivitySelection(false);
							UIShield.valueOf(shield.name())
									.setMainActivitySelection(false);
							if (app.getRunningShields().get(shield.name()) != null) {
								app.getRunningShields().get(shield.name())
										.resetThis();
								app.getRunningShields().remove(shield.name());
							}
						}
					};
					try {
						type = shield.getShieldType().newInstance();
					} catch (java.lang.InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (type != null) {
						if (shield.isInvalidatable()) {
							type.setActivity(activity).setTag(shield.name())
									.invalidate(selectionAction, false);
							selectionAction.onFailure();
						} else {
							type.setActivity(activity).setTag(shield.name());
						}
					}
				}
			} else {
				if (app.getRunningShields().get(shield.name()) != null) {
					app.getRunningShields().get(shield.name()).resetThis();
					app.getRunningShields().remove(shield.name());
				}
			}
			// }
			// });
			i++;
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
					for (UIShield uiShield : UIShield.valuesFiltered()) {
						if (uiShield.getName().toLowerCase()
								.startsWith(arg0.toString().toLowerCase())) {
							filteredShields.add(uiShield);
						}
					}
				} else
					filteredShields = UIShield.valuesFiltered();
				results.values = filteredShields;
				results.count = filteredShields.size();
				return results;
			}
		};
	}

}
