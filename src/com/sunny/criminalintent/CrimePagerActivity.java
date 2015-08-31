package com.sunny.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class CrimePagerActivity extends FragmentActivity 
	implements CrimeFragment.Callbacks {
	
	private ViewPager mCrimeVp;
	
	private ArrayList<Crime> mCrimes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mCrimeVp = new ViewPager(this);
		mCrimeVp.setId(R.id.vp_crime);
		setContentView(mCrimeVp);
		
		mCrimes = CrimeLab.get(this).getCrimes();
		
		FragmentManager fm = getSupportFragmentManager();
		mCrimeVp.setAdapter(new FragmentStatePagerAdapter(fm) {
			
			@Override
			public int getCount() {
				return mCrimes.size();
			}
			
			@Override
			public Fragment getItem(int pos) {
				Crime crime = mCrimes.get(pos);
				return CrimeFragment.newInstance(crime.getId());
			}
		});
		
		// 设置ViewPager的初始Page为当前点击的Page
		UUID crimeId = (UUID) getIntent()
				.getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
		for (int i = 0; i < mCrimes.size(); i++) {
			if (crimeId.equals(mCrimes.get(i).getId())) {
				mCrimeVp.setCurrentItem(i);
				setTitle(mCrimes.get(i).getTitle());
				break;
			}
		}
		
		// 为每个Page设置标题
		mCrimeVp.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				
			}

			@Override
			public void onPageSelected(int position) {
				Crime crime = mCrimes.get(position);
				if (crime.getTitle() != null) {
					setTitle(crime.getTitle());
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});
	}

	@Override
	public void onCrimeUpdated(Crime crime) {
		// TODO Auto-generated method stub
		
	}

}
