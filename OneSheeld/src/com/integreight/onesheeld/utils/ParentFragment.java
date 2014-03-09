package com.integreight.onesheeld.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.integreight.onesheeld.R;

public class ParentFragment extends BaseContainerFragment {
	private Fragment fragmentToReplace;

	public ParentFragment(Fragment fragmentToReplace) {
		this.fragmentToReplace = fragmentToReplace;
	}

	public ParentFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.container_fragment, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (fragmentToReplace != null) {
			replaceFragment(fragmentToReplace, true, false);
		}
		super.onViewCreated(view, savedInstanceState);
	}

}
