package com.sunny.criminalintent;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CrimeFragment extends Fragment {
	
	public static final String EXTRA_CRIME_ID = 
			"com.sunny.criminalintent.crime_id";
	
	private static final String TAG = "CrimeFragment";
	private static final String DIALOG_DATE = "date";
	private static final String DIALOG_IMAGE = "image";
	private static final int REQUEST_DATE = 0;
	private static final int REQUEST_PHOTO = 1;
	
	private Crime mCrime;
	
	private EditText mCrimeTitleEdt;
	private Button mDateBtn;
	private CheckBox mSolvedCb;
	private ImageButton mPhotoIbtn;
	private ImageView mPhotoIv;
	
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
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) { // 使用浮动上下文菜单
			registerForContextMenu(mPhotoIv);
		} else { // 在操作栏(ActionBar)上的上下文菜单
			// 长按删除该图片
			mPhotoIv.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					getActivity().startActionMode(new ActionMode.Callback() {
						
						@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							// TODO Auto-generated method stub
							return false;
						}
						
						@Override
						public void onDestroyActionMode(ActionMode mode) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							// 创建上下文菜单
							MenuInflater inflater = mode.getMenuInflater();
							inflater.inflate(R.menu.crime_item_context, menu);
							return true;
						}
						
						@Override
						public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
							switch (item.getItemId()) {
							case R.id.menu_item_delete_photo:		
								onDeletePhoto();
								mode.finish();
								return true;

							default:
								return false;
							}
						}
					});
					return true;
				}
			});
		}
		
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
		
		mPhotoIv = (ImageView) view.findViewById(R.id.iv_crime);
		// 点击查看大图
		mPhotoIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Photo photo = mCrime.getPhoto();
				if (photo == null)
					return;
				
				FragmentManager fm = getActivity().getSupportFragmentManager();
				String path = getActivity()
						.getFileStreamPath(photo.getFilename()).getAbsolutePath();
				ImageFragment.newInstance(path).show(fm, DIALOG_IMAGE);
			}
		});
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
				// 如果该Crime本身存在图片，首先删除该图片，从而节省存储空间
				boolean flag = deletePhoto();
				if (flag) 
					Log.i(TAG, "Delete old picture sucessed!");
				
				// 存储新的照片
				Photo photo = new Photo(filename);
				mCrime.setPhoto(photo);
				Log.i(TAG, "Crime: " + mCrime.getTitle() + " has a photo");
				showPhoto();
			}
		}
	}
	
	private boolean deletePhoto() {
		boolean flag = false;
		Photo photo = mCrime.getPhoto();
		if (photo != null) {
			String path = getActivity()
					.getFileStreamPath(photo.getFilename()).getAbsolutePath();
			flag = new File(path).delete();
		}
		return flag;
	}

	private void updateDate() {
		mDateBtn.setText(mCrime.getDate().toString());
	}
	
	private void showPhoto() {
		// Set the image button's image based on our photo
		Photo photo = mCrime.getPhoto();
		BitmapDrawable drawable  = null;
		
		if (photo != null) {
			String path = getActivity()
					.getFileStreamPath(photo.getFilename())
					.getAbsolutePath();
			drawable = PictureUtils.getScaledDrawable(getActivity(), path);
		}
		mPhotoIv.setImageDrawable(drawable);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.crime_item_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_delete_photo:
			onDeletePhoto();
			return true;
		}
		
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// 利用层级式导航返回父Activity，要配合Manifest使用
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		showPhoto(); // 加载图片
	}

	@Override
	public void onPause() {
		super.onPause();
		CrimeLab.get(getActivity()).saveCrimes(); // 保存数据
	}

	@Override
	public void onStop() {
		super.onStop();
		PictureUtils.cleanImageView(mPhotoIv); // 卸载图片
	}

	/**
	 * 删除照片，并且更新UI.
	 * 从磁盘、模型层以及ImageView中删除照片
	 */
	private void onDeletePhoto() {
		boolean flag = deletePhoto();
		if (flag) {
			mCrime.setPhoto(null); // 删除照片后更新Crime的Photo属性
			mPhotoIv.setImageDrawable(null); // 更新ImageView显示画面
			Toast.makeText(getActivity(), "Delete Picture Success!", 
					Toast.LENGTH_LONG).show();
		}
	}

}
