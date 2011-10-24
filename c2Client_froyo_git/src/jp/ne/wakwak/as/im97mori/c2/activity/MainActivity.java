/*
 * Copyright (c) 2011 im97mori.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package jp.ne.wakwak.as.im97mori.c2.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.app.FragmentPagerAdapterImpl;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmFragment.OnAlarmFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmNameFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmNameFragment.OnAlarmAddFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.PreviewFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.PreviewFragment.OnPreviewFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetAlarmFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetAlarmFragment.OnTargetAlarmFragmentListner;
import jp.ne.wakwak.as.im97mori.c2.receiver.FinishCalculateReceiver;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends BaseFragmentActivity implements
		OnAlarmFragmentListener, OnPreviewFragmentListener,
		OnAlarmAddFragmentListener, OnTargetAlarmFragmentListner,
		OnPageChangeListener, OnCheckedChangeListener {

	private static final String TAG_ALARM_NAME_DIALOG = "alarmNameDialog";
	private static final String TAG_TARGET_DIALOG = "targetDialog";

	private static final int LIST_FRAGMENT = 0;
	private static final int PREVIEW_FRAGMENT = 1;

	private AlarmFragment alarmFragment = null;
	private PreviewFragment previewFragment = null;
	private FinishCalculateReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.main);

		ViewPager pager = (ViewPager) this.findViewById(R.id.mainPager);
		MainPagerAdapter adapter = new MainPagerAdapter(
				this.getSupportFragmentManager());

		FragmentManager fm = this.getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag(adapter.getNamePrefix()
				+ LIST_FRAGMENT);
		if (fragment == null) {
			alarmFragment = AlarmFragment.newInstance();
		} else {
			alarmFragment = (AlarmFragment) fragment;
		}
		fragment = fm.findFragmentByTag(adapter.getNamePrefix()
				+ PREVIEW_FRAGMENT);
		if (fragment == null) {
			previewFragment = PreviewFragment.newInstance();
		} else {
			previewFragment = (PreviewFragment) fragment;
		}

		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(this);

		RadioGroup radioGroup = (RadioGroup) this
				.findViewById(R.id.mainRadioGroup);
		radioGroup.setOnCheckedChangeListener(this);

		this.receiver = new FinishCalculateReceiver(this.alarmFragment);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.global_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		if (item.getItemId() == R.id.globalInformationMenu) {
			Intent intent = new Intent(this.getApplicationContext(),
					InformationActivity.class);
			this.startActivity(intent);
			result = true;
		} else {
			result = super.onOptionsItemSelected(item);
		}
		return result;
	}

	private class MainPagerAdapter extends FragmentPagerAdapterImpl {

		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (position == LIST_FRAGMENT) {
				fragment = MainActivity.this.alarmFragment;
			} else if (position == PREVIEW_FRAGMENT) {
				fragment = MainActivity.this.previewFragment;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		protected String getNamePrefix() {
			return MainPagerAdapter.class.getName();
		}

	}

	@Override
	public void onShowAll() {
		this.previewFragment.update();
	}

	@Override
	public void onMoveMonth() {
		this.previewFragment.update();
	}

	@Override
	public void onCellClick(Date date, Map<Long, Set<Date>> map) {

		if (!map.isEmpty() && !map.keySet().contains(Constants.TEMPOLARY_ID)) {
			Iterator<Map.Entry<Long, Set<Date>>> it = map.entrySet().iterator();
			ArrayList<Long> idList = new ArrayList<Long>(map.size());
			while (it.hasNext()) {
				Map.Entry<Long, Set<Date>> entry = it.next();
				Set<Date> dateSet = entry.getValue();
				if (dateSet.contains(date)) {
					idList.add(entry.getKey());
				}
			}

			if (idList.size() > 0) {
				long[] ids = new long[idList.size()];
				for (int i = 0; i < idList.size(); i++) {
					ids[i] = idList.get(i);
				}
				DialogFragment targetFragment = TargetAlarmFragment
						.newInstance(ids, date);
				this.showDialogFragment(targetFragment, TAG_TARGET_DIALOG);
			}
		}
	}

	@Override
	public void onAddAlarm() {
		AlarmNameFragment fragment = AlarmNameFragment.newInstance();
		this.showDialogFragment(fragment, TAG_ALARM_NAME_DIALOG);
	}

	@Override
	public void onAlarmSelected(long id) {
		Intent intent = new Intent(this.getApplicationContext(),
				AlarmTypeActivity.class);
		intent.putExtra(Constants.IntentKey.ID, id);
		this.startActivity(intent);
	}

	@Override
	public void onAlarmNameOk(String name, String time, int enable) {
		this.dismissDialog(TAG_ALARM_NAME_DIALOG);

		AlarmDb db = new AlarmDb(this);
		long id = db.addAlarm(name, time, null, enable);
		db.close();

		Intent intent = new Intent(this.getApplicationContext(),
				AlarmTypeActivity.class);
		intent.putExtra(Constants.IntentKey.ID, id);
		this.startActivity(intent);
	}

	@Override
	public void onAlarmNameCancel() {
		this.dismissDialog(TAG_ALARM_NAME_DIALOG);
	}

	@Override
	public void onTargetAlarmSelected(long id) {
		this.dismissDialog(TAG_TARGET_DIALOG);

		Intent intent = new Intent(this.getApplicationContext(),
				AlarmTypeActivity.class);
		intent.putExtra(Constants.IntentKey.ID, id);
		this.startActivity(intent);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		RadioGroup group = (RadioGroup) this.findViewById(R.id.mainRadioGroup);
		group.setOnCheckedChangeListener(null);
		if (position == LIST_FRAGMENT) {
			group.check(R.id.mainListRadioButton);
		} else {
			group.check(R.id.mainPreviewRadioButton);
		}
		group.setOnCheckedChangeListener(this);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		ViewPager pager = (ViewPager) this.findViewById(R.id.mainPager);
		if (checkedId == R.id.mainListRadioButton) {
			pager.setCurrentItem(LIST_FRAGMENT);
		} else {
			pager.setCurrentItem(PREVIEW_FRAGMENT);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		this.registerReceiver(receiver, new IntentFilter(
				Constants.Action.FINISH_CALCULATE));
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.unregisterReceiver(receiver);
	}
}