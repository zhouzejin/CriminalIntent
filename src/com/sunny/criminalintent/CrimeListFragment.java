package com.sunny.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class CrimeListFragment extends ListFragment {
	
	private static final String TAG = "CrimeListFragment";
	
	private ArrayList<Crime> mCrimes;
	private boolean mSubtitleVisible; // 记录子标题的状态，当设备旋转时使用

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true); // 通知FragmentManager需接收选项菜单方法回调
		getActivity().setTitle(R.string.crimes_title);
		
		// 设置Activity销毁时保持该fragment实例，即可以保存mSubtitleVisible
		setRetainInstance(true);
		mSubtitleVisible = false;
		
		mCrimes = CrimeLab.get(getActivity()).getCrimes();
		
		/*ArrayAdapter<Crime> adapter = 
				new ArrayAdapter<Crime>(getActivity(), 
						android.R.layout.simple_list_item_1, 
						mCrimes);*/
		CrimeAdapter adapter = new CrimeAdapter(mCrimes);
		setListAdapter(adapter);
	}

	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		// 当设备旋转，使子标题状态与旋转前一致
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mSubtitleVisible) {
				getActivity().getActionBar().setSubtitle(R.string.subtitle);
			}
		}
		
		// 在onCreateView(...)方法中，使用android.R.id.list资源ID获取ListFragment管理着
		// 的ListView。 ListFragment也有一个getListView()方法，但在onCreateView(...)方法中却
		// 无法使用。这是因为，在onCreateView(...)方法完成调用并返回视图之前， getListView()
		// 方法返回的永远是null值。
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		// 在API11以下的版本只支持浮动上下文菜单，以上的版本还支持操作栏(ActionBar)上的上下文菜单
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// Use floating context menus on Froyo and Gingerbread
			// 默认情况下，长按视图不会触发上下文菜单的创建；
			// 要触发onCreateContextMenu方法，必须调用此方法。
			registerForContextMenu(listView);
		} else {
			// Use contextual action bar on Honeycomb and higher
			// 开启多选模式，可以同时操作多个ListView的Item
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			
			// 设置多选模式的监听器
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Required, but not used in this implementation
					return false;
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					// Required, but not used in this implementation
				}
				
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					// ActionMode.Callback methods
					// 创建上下文菜单
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.crime_list_item_context, menu);
					return true;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
					case R.id.menu_item_delete_crime:
						CrimeAdapter adapter = (CrimeAdapter) getListAdapter();
						CrimeLab crimeLab = CrimeLab.get(getActivity());
						for (int i = adapter.getCount() - 1; i >= 0; i--) {
							if (getListView().isItemChecked(i)) {
								crimeLab.deleteCrime(adapter.getItem(i));
							}
						}
						mode.finish(); // 销毁操作模式
						adapter.notifyDataSetChanged();
						return true;

					default:
						return false;
					}
				}
				
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) {
					// Required, but not used in this implementation
				}
			});
		}
		
		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Crime crime = (Crime) (getListAdapter()).getItem(position);
		Crime crime = ((CrimeAdapter)getListAdapter()).getItem(position);
		// Log.d(TAG, crime.getTitle() + "was click!");
		
		// Start CrimeActivity
		// Intent intent = new Intent(getActivity(), CrimeActivity.class);
		// Start CrimePagerActivity with this crime
		Intent intent = new Intent(getActivity(), CrimePagerActivity.class);
		intent.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
		startActivity(intent);
	}
	
	private class CrimeAdapter extends ArrayAdapter<Crime> {
		
		public CrimeAdapter (ArrayList<Crime> crimes) {
			super(getActivity(), 0, crimes); // 参数0表示使用自定义的Item布局
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// If we weren't given a view, inflate one
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater()
						.inflate(R.layout.list_item_crime, null);
			}
			
			// Configure the view for this Crime
			Crime c = getItem(position);
			
			TextView titleTv = 
					(TextView) convertView.findViewById(R.id.tv_title_crime_list_item);
			titleTv.setText(c.getTitle());
			
			TextView dateTv = 
					(TextView) convertView.findViewById(R.id.tv_date_crime_list_item);
			dateTv.setText(c.getDate().toString());
			
			CheckBox solvedCb = 
					(CheckBox) convertView.findViewById(R.id.cb_solved_crime_list_item);
			solvedCb.setChecked(c.isSolved());
			
			return convertView;
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// 当该Activity重新处于栈顶，通知Adapter数据集改变，需要刷新视图
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_list, menu);
		
		// 当设备旋转，使标题状态与旋转前一致
		MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
		if (mSubtitleVisible && showSubtitle != null) {
			showSubtitle.setTitle(R.string.hide_subtitle);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
	}

	@TargetApi(11)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// 通用的选项菜单项，适用于所有版本
		case R.id.menu_item_new_crime:
			Crime crime = new Crime();
			CrimeLab.get(getActivity()).addCrime(crime);
			
			Intent intent = new Intent(getActivity(), CrimePagerActivity.class);
			intent.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
			startActivity(intent);
			
			return true;
		
		// 在操作栏(ActionBar)上设置可选菜单项，只适用于API11及以上的版本
		// 这里不需要进行版本判断，因为该菜单项放置在menu-v11的文件夹下，只有APP11及以上才会出现该菜单项
		case R.id.menu_item_show_subtitle:
			if (getActivity().getActionBar().getSubtitle() == null) {
				getActivity().getActionBar().setSubtitle(R.string.subtitle); // 设置子标题
				mSubtitleVisible = true;
				item.setTitle(R.string.hide_subtitle);
			} else {
				getActivity().getActionBar().setSubtitle(null);
				mSubtitleVisible = false;
				item.setTitle(R.string.show_subtitle);
			}
			return true;
				
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int position = info.position;
		CrimeAdapter adapter = (CrimeAdapter) getListAdapter();
		Crime crime = adapter.getItem(position);
		
		switch (item.getItemId()) {
		case R.id.menu_item_delete_crime:
			CrimeLab.get(getActivity()).deleteCrime(crime);
			adapter.notifyDataSetChanged();
			return true;
		}

		return super.onContextItemSelected(item);
	}

}
