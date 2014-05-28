package com.integreight.onesheeld.adapters;

import com.integreight.onesheeld.utils.TutorialImageFragment;
import com.integreight.onesheeld.utils.TutorialLastFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TutorialPagerAdapter extends FragmentPagerAdapter {

	public TutorialPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		if (arg0 != 7)
			return TutorialImageFragment.newInstance(arg0);
		else
			return TutorialLastFragment.newInstance(arg0);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 8;
	}

}
