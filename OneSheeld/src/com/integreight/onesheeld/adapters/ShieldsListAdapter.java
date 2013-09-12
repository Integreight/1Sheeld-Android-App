package com.integreight.onesheeld.adapters;

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

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.UIShield;

public class ShieldsListAdapter extends BaseAdapter {
	Activity activity;
	List<UIShield> shieldList;
	LayoutInflater inflater;
	
	

	public ShieldsListAdapter(Activity a, List<UIShield> shieldList) {
		this.activity = a;
		this.shieldList = shieldList;
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
			holder.name = (TextView) row.findViewById(R.id.shield_list_item_name_textview);
			holder.icon = (ImageView) row.findViewById(R.id.shield_list_item_symbol_imageview);
			holder.selectionButton=(ToggleButton)row.findViewById(R.id.shield_list_item_selection_toggle_button);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		String name = shieldList.get(position).getName();
		Integer iconId = shieldList.get(position).getIconId();
		Integer imageId = shieldList.get(position).getImageId();

		holder.name.setText(name);
		holder.icon.setBackgroundResource(iconId);

		row.setBackgroundResource(imageId);
		
//		RelativeLayout.LayoutParams head_params = (RelativeLayout.LayoutParams)((RelativeLayout)row).getLayoutParams();
//		head_params.setMargins(0, -20, 0, 0); //substitute parameters for left, top, right, bottom
//		row.setLayoutParams(head_params);

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
		TextView name;
		ImageView icon;
		ToggleButton selectionButton;
	}
	


}
