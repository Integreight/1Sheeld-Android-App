package com.integreight.onesheeld.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;

public class SelectedShieldsListAdapter extends BaseAdapter {
	Activity activity;
	List<UIShield> shieldList;
	LayoutInflater inflater;

	public SelectedShieldsListAdapter(Activity a) {
		this.activity = a;
		this.shieldList = new ArrayList<UIShield>();
		List<UIShield> tempShieldsList = Arrays.asList(UIShield.values());
		for (UIShield shield : tempShieldsList) {
			if (shield.isMainActivitySelection())
				this.shieldList.add(shield);
		}

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

		UIShield shield = shieldList.get(position);
		Integer iconId = shield.getSymbolId();
		Integer imageId = shield.getSmallImageStripId();
		holder.symbol.setBackgroundResource(iconId);

		row.setBackgroundResource(imageId);

		if (UIShield.getShieldsActivitySelection() == shield) {
			holder.selectionCircle.setVisibility(View.VISIBLE);
		} else {
			holder.selectionCircle.setVisibility(View.INVISIBLE);
		}
		// RelativeLayout.LayoutParams head_params =
		// (RelativeLayout.LayoutParams)((RelativeLayout)row).getLayoutParams();
		// head_params.setMargins(0, -20, 0, 0); //substitute parameters for
		// left, top, right, bottom
		// row.setLayoutParams(head_params);

		// row.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// //iv.animate();
		// // String url = data.get(position).getUrl();
		// // Intent intent = new Intent(context, Tutorial3Activity.class);
		// // intent.putExtra("itemId", data.get(position).getId());
		// // context.startActivity(intent);
		// }
		// });
		return row;
	}

	static class Holder {
		ImageView symbol;
		ImageView selectionCircle;
	}

}
