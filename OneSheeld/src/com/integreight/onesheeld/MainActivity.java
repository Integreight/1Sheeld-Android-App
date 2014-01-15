package com.integreight.onesheeld;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class MainActivity extends FragmentActivity {
	private final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.one_sheeld_main);
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else
			finish();
		super.onBackPressed();
	}

	public void replaceCurrentFragment(Fragment targetFragment) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.appTransitionsContainer, targetFragment);
		transaction.commit();
	}
}
