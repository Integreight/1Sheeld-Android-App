package com.integreight.onesheeld.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.integreight.onesheeld.R;

public class BaseContainerFragment extends Fragment {
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
            Log.e("TAG", "Exception", e);
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
}
