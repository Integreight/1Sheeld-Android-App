package com.integreight.onesheeld.utils;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.integreight.onesheeld.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class BaseContainerFragment extends SherlockFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}
	public void replaceFragment(Fragment fragment, boolean addToBackStack,
			boolean animate) {
		try {
			FragmentTransaction transaction = getChildFragmentManager()
					.beginTransaction();
			if (addToBackStack) {
				transaction.addToBackStack(null);
			}
			transaction.replace(R.id.container_framelayout, fragment);
			getChildFragmentManager().executePendingTransactions();
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.i("test", "pop fragment: "
				+ getChildFragmentManager().getBackStackEntryCount());
	}

	public boolean popFragment() {
		boolean isPop = false;
		if (getChildFragmentManager().getBackStackEntryCount() > 1) {
			isPop = true;
			getChildFragmentManager().popBackStack();
		}
		return isPop;
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
	}
}

