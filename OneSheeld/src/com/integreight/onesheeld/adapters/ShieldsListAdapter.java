package com.integreight.onesheeld.adapters;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.integreight.onesheeld.MainActivity;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;

public class ShieldsListAdapter extends BaseAdapter {
	MainActivity activity;
	List<UIShield> shieldList;
	LayoutInflater inflater;

	public ShieldsListAdapter(Activity a) {
		this.activity = (MainActivity) a;
		this.shieldList = Arrays.asList(UIShield.values());
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			holder.selectionButton = (ToggleButton) row
					.findViewById(R.id.shield_list_item_selection_toggle_button);
			holder.selectionCircle = (ImageView) row
					.findViewById(R.id.shield_list_item_selection_circle_imageview);
			holder.blackUpperLayer = (ImageView) row
					.findViewById(R.id.shildListItemBlackSquare);
			holder.container = (ViewGroup) row.findViewById(R.id.container);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		UIShield shield = shieldList.get(position);
		String name = shield.getName();
		Integer iconId = shield.getSymbolId();
		Integer imageId = shield.getItemBackgroundColor();

		holder.name.setText(name);
		holder.icon.setBackgroundResource(iconId);

		row.setBackgroundColor(imageId);

		if (shield.isMainActivitySelection()) {
			holder.selectionButton.setChecked(true);
			holder.selectionButton.setVisibility(View.VISIBLE);
			holder.selectionCircle.setVisibility(View.VISIBLE);
			holder.blackUpperLayer.setVisibility(View.INVISIBLE);
		} else {
			holder.selectionButton.setChecked(false);
			holder.selectionButton.setVisibility(View.INVISIBLE);
			holder.selectionCircle.setVisibility(View.INVISIBLE);
			holder.blackUpperLayer.setVisibility(View.VISIBLE);
		}
		// RelativeLayout.LayoutParams head_params =
		// (RelativeLayout.LayoutParams)((RelativeLayout)row).getLayoutParams();
		// head_params.setMargins(0, -20, 0, 0); //substitute parameters for
		// left, top, right, bottom
		// row.setLayoutParams(head_params);
		final Holder tempHolder = holder;
		holder.container.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (tempHolder.selectionButton.isChecked()) {
					tempHolder.selectionButton.setChecked(false);
					tempHolder.selectionButton.setVisibility(View.INVISIBLE);
					tempHolder.selectionCircle.setVisibility(View.INVISIBLE);
					tempHolder.blackUpperLayer.setVisibility(View.VISIBLE);
					UIShield.getPosition(position + 1)
							.setMainActivitySelection(false);
				} else {
					tempHolder.selectionButton.setChecked(true);
					tempHolder.selectionButton.setVisibility(View.VISIBLE);
					tempHolder.selectionCircle.setVisibility(View.VISIBLE);
					tempHolder.blackUpperLayer.setVisibility(View.INVISIBLE);
					UIShield.getPosition(position + 1)
							.setMainActivitySelection(true);
				}
			}
		});
		return row;
	}

	static class Holder {
		TextView name;
		ImageView icon;
		ToggleButton selectionButton;
		ImageView selectionCircle;
		ImageView blackUpperLayer;
		ViewGroup container;
	}

}
