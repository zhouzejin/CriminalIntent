package com.sunny.criminalintent;

import android.support.v4.app.Fragment;

public class CrimeCameraActivity extends SingleCrimeActivity {

	@Override
	protected Fragment createFragment() {
		return new CrimeCameraFragment();
	}

}
