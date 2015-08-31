package com.sunny.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class CrimeListActivity extends SingleCrimeActivity 
	implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

	@Override
	protected Fragment createFragment() {
		return new CrimeListFragment();
	}

	@Override
	protected int getLayoutResId() {
		// return R.layout.activity_towpane;
		return R.layout.activity_masterdetail; // 使用资源别名适配平板和手机的布局
	}

	@Override
	public void onCrimeSelected(Crime crime) {
		if (findViewById(R.id.fragment_container_detail) == null) { // 表明运行的手机设备
			// Start an instance of CrimePagerActivity
			Intent intent = new Intent(this, CrimePagerActivity.class);
			intent.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
			startActivity(intent);
		} else { // 否则是平板设备
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			
			Fragment oldDetail = fm.findFragmentById(R.id.fragment_container_detail);
			Fragment newDetail = CrimeFragment.newInstance(crime.getId());
			
			setTitle(crime.getTitle()); // 更新标题
			
			if (oldDetail != null) { // 移除前一个fragment
				ft.remove(oldDetail);
			}
			
			ft.add(R.id.fragment_container_detail, newDetail);
			ft.commit();
		}
	}

	@Override
	public void onCrimeUpdated(Crime crime) {
		FragmentManager fm = getSupportFragmentManager();
		CrimeListFragment listFragment = (CrimeListFragment) 
				fm.findFragmentById(R.id.fragment_container);
		listFragment.updateUI();
	}

}
