package com.sunny.criminalintent;

import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class CrimeFragment extends Fragment {
	
	public static final String EXTRA_CRIME_ID = 
			"com.sunny.criminalintent.crime_id";
	
	private static final String TAG = "CrimeFragment";
	private static final String DIALOG_DATE = "date";
	private static final int REQUEST_DATE = 0;
	private static final int REQUEST_PHOTO = 1;
	
	private Crime mCrime;
	
	private EditText mCrimeTitleEdt;
	private Button mDateBtn;
	private CheckBox mSolvedCb;
	private ImageButton mPhotoIbtn;
	
	/**
	 * 附加argument给fragment
	 * 
	 * @param crimeId
	 * @return
	 */
	public static CrimeFragment newInstance(UUID crimeId) {
		// 附加argument bundle给fragment，需调用Fragment.setArguments(Bundle)方法。
		// 注意，该任务必须在fragment创建后、添加给activity前完成。
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_CRIME_ID, crimeId);
		
		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true); // 通知FragmentManager需接收选项菜单方法回调
		
		// 直接获取托管Activity中数据，这样破坏了fragment的封装性
		// 因为这需要fragment必须由某个特定的Activity托管
		/*UUID crimeId = (UUID) getActivity().getIntent()
				.getSerializableExtra(EXTRA_CRIME_ID);*/
		
		UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
		mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_crime, container, false);
		
		// 为启用应用图标向上导航按钮的功能的方法来自于API11级，因此需进行系统版本判断保证应用向下兼容
		// 使用@TargetApi(11)注解阻止Android Lint报告兼容性问题
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		
		initView(view);
		
		return view;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void initView(View view) {
		mCrimeTitleEdt = (EditText) view.findViewById(R.id.edt_crime_title);
		mCrimeTitleEdt.setText(mCrime.getTitle());
		mCrimeTitleEdt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mCrime.setTitle(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		mDateBtn = (Button) view.findViewById(R.id.btn_crime_date);
		updateDate();
		mDateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity()
						.getSupportFragmentManager();
				// DatePickerFragment dialog = new DatePickerFragment();
				DatePickerFragment dialog = DatePickerFragment
						.newInstance(mCrime.getDate());
				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				dialog.show(fm, DIALOG_DATE); // 弹出日期对话框
			}
		});
		
		mSolvedCb = (CheckBox) view.findViewById(R.id.cb_crime_solved);
		mSolvedCb.setChecked(mCrime.isSolved());
		mSolvedCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCrime.setSolved(isChecked);
			}
		});
		
		mPhotoIbtn = (ImageButton) view.findViewById(R.id.ibtn_crime);
		mPhotoIbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Launch the camera activity
				Intent intent = new Intent(getActivity(), CrimeCameraActivity.class);
				startActivityForResult(intent, REQUEST_PHOTO);
			}
		});
		
		// If camera is not available, disable camera functionality
		PackageManager pm = getActivity().getPackageManager();
		boolean hasACamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || 
				pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) || 
				Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD || 
				Camera.getNumberOfCameras() > 0;
		if (!hasACamera) {
			mPhotoIbtn.setEnabled(false);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		
		if (requestCode == REQUEST_DATE) {
			Date date = (Date) data
					.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			mCrime.setDate(date);
			updateDate();
		} else if (requestCode == REQUEST_PHOTO) {
			// Create a new Photo object and attach it to the crime
			String filename = data
					.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
			if (filename != null) {
				Log.i(TAG, "filename: " + filename);
			}
		}
	}

	private void updateDate() {
		mDateBtn.setText(mCrime.getDate().toString());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// 利用层级式导航返回父Activity，要配合Mainifest使用
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onPause() {
		super.onResume();
		
		CrimeLab.get(getActivity()).saveCrimes(); // 保存数据
	}

}
