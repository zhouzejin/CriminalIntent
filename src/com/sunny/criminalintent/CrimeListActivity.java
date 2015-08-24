package com.sunny.criminalintent;

import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleCrimeActivity {

	@Override
	protected Fragment createFragment() {
		return new CrimeListFragment();
	}

}
