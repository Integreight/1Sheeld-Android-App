package com.integreight.onesheeld;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.integreight.onesheeld.activities.ShieldsOperationActivity;
import com.integreight.onesheeld.adapters.SelectedShieldsListAdapter;
import com.integreight.onesheeld.shieldsfragments.EmptyShieldFragment;
import com.integreight.onesheeld.shieldsfragments.LedFragment;

public class SelectedShieldsListFragment extends ListFragment {
	SelectedShieldsListAdapter UIShieldAdapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.selected_shields_list, null);
	}

	public Fragment getShieldFragment(int position){
		switch (UIShieldAdapter.getItem(position)) {
		case LED_SHIELD:return new LedFragment();
		default:return new EmptyShieldFragment();
		}
	}
	
	public UIShield getUIShield(int position){
		return UIShieldAdapter.getItem(position);
	}
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		UIShieldAdapter = new SelectedShieldsListAdapter(getActivity());
		setListAdapter(UIShieldAdapter);
	}
	
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Fragment newContent = null;
		newContent=getShieldFragment(position);
		getActivity().setTitle(UIShieldAdapter.getItem(position).getName()+" Shield");
		if (newContent != null)
			switchFragment(newContent);
	}
	
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;
		
		if (getActivity() instanceof ShieldsOperationActivity) {
			ShieldsOperationActivity fca = (ShieldsOperationActivity) getActivity();
			fca.switchContent(fragment);
		}
	}
}
