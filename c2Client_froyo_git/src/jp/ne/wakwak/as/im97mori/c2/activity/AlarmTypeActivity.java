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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.app.FragmentPagerAdapterImpl;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmNameFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmNameFragment.OnAlarmAddFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmTypeFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmTypeFragment.OnAlarmTypeListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.PreviewFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.PreviewFragment.OnPreviewFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.AlarmRuleFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.AlarmRuleFragment.OnAlarmRuleFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.GoogleAccoountFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.GoogleAccoountFragment.OnGoogleAccountFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.OnRuleListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.RuleBetweenFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.RuleCalendarFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.RuleDateFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.rule.RuleDayOfWeekFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetDateFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetDateFragment.OnTargetDateFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.receiver.FinishCalculateReceiver;
import jp.ne.wakwak.as.im97mori.c2.service.AlarmService;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
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

public class AlarmTypeActivity extends BaseFragmentActivity implements
		OnAlarmTypeListener, OnPreviewFragmentListener,
		OnAlarmRuleFragmentListener, OnRuleListener,
		OnGoogleAccountFragmentListener, OnTargetDateFragmentListener,
		OnPageChangeListener, OnCheckedChangeListener,
		OnAlarmAddFragmentListener {

	private static final String TAG_ALARM_NAME_DIALOG = "alarmNameDialog";
	private static final String TAG_ADD_RULE_DIALOG = "addRuleDialog";
	private static final String TAG_TARGET_DIALOG = "targetDialog";

	private static final int LIST_FRAGMENT = 0;
	private static final int PREVIEW_FRAGMENT = 1;

	private AlarmTypeFragment alarmTypeFragment = null;
	private PreviewFragment previewFragment = null;
	private FinishCalculateReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.alarm_type_main);

		ViewPager pager = (ViewPager) this
				.findViewById(R.id.alarmTypeMainPager);
		AlarmTypeMainPagerAdapter adapter = new AlarmTypeMainPagerAdapter(
				this.getSupportFragmentManager());
		Intent intent = this.getIntent();
		FragmentManager fm = this.getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag(adapter.getNamePrefix()
				+ LIST_FRAGMENT);
		if (fragment == null) {
			alarmTypeFragment = AlarmTypeFragment.newInstance(intent
					.getLongExtra(Constants.IntentKey.ID,
							Constants.TEMPOLARY_ID));
		} else {
			alarmTypeFragment = (AlarmTypeFragment) fragment;
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
				.findViewById(R.id.alarmTypeMainRadioGroup);
		radioGroup.setOnCheckedChangeListener(this);

		this.receiver = new FinishCalculateReceiver(this.alarmTypeFragment);
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

	private class AlarmTypeMainPagerAdapter extends FragmentPagerAdapterImpl {

		public AlarmTypeMainPagerAdapter(FragmentManager arg) {
			super(arg);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (position == LIST_FRAGMENT) {
				fragment = alarmTypeFragment;
			} else if (position == PREVIEW_FRAGMENT) {
				fragment = previewFragment;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		protected String getNamePrefix() {
			return AlarmTypeMainPagerAdapter.class.getName() + ":";
		}

	}

	@Override
	public void onShowAll() {
		this.alarmTypeFragment.onRefresh();
	}

	@Override
	public void onAddRule(int sign) {
		AlarmRuleFragment fragment = AlarmRuleFragment.newInstance(sign);
		this.showDialogFragment(fragment, TAG_ADD_RULE_DIALOG);
	}

	@Override
	public void onDeleteAlarm(long id) {
		AlarmDb db = new AlarmDb(this);
		db.deleteAlarm(id);
		db.close();

		Intent intent = new Intent(this.getApplicationContext(),
				AlarmService.class);
		intent.putExtra(AlarmService.SETTING, Constants.AlarmCommand.EDIT_ALARM);
		intent.putExtra(Constants.IntentKey.ID, id);
		this.startService(intent);
		this.finish();
	}

	@Override
	public void onUpdate(ArrayList<AlarmTypeVo> list) {
		this.previewFragment.update(list);
	}

	@Override
	public void onEditRule(int position, AlarmTypeVo vo) {
		DialogFragment fragment = null;
		int type = vo.getType();
		if (type == Constants.AlarmType.TYPE_DAY_OF_WEEK) {
			fragment = RuleDayOfWeekFragment.newInstance(position,
					vo.getTypeValue());
		} else if (type == Constants.AlarmType.TYPE_DATE) {
			fragment = RuleDateFragment
					.newInstance(position, vo.getTypeValue());
		} else if (type == Constants.AlarmType.TYPE_BETWEEN) {
			fragment = RuleBetweenFragment.newInstance(position,
					vo.getTypeValue());
		} else if (type == Constants.AlarmType.TYPE_GOOGLE_CALENDAR) {
			fragment = GoogleAccoountFragment.newInstance(position,
					vo.getTypeValue());
		}
		this.showDialogFragment(fragment, TAG_ADD_RULE_DIALOG);
	}

	@Override
	public void onEditAlarm(String name, String time, int enable) {
		AlarmNameFragment fragment = AlarmNameFragment.newInstance(name, time,
				enable);
		this.showDialogFragment(fragment, TAG_ALARM_NAME_DIALOG);
	}

	@Override
	public void onMoveMonth() {
		this.alarmTypeFragment.onRefresh();
	}

	@Override
	public void onCellClick(Date date, Map<Long, Set<Date>> map) {
		DialogFragment targetFragment = null;

		if (map.keySet().contains(Constants.TEMPOLARY_ID)) {
			Iterator<Set<Date>> it = map.values().iterator();
			Set<Date> dateSet = new LinkedHashSet<Date>();
			while (it.hasNext()) {
				dateSet.addAll(it.next());
			}
			int sign = Constants.AlarmTypeSign.SIGN_PLUS;
			if (dateSet.contains(date)) {
				sign = Constants.AlarmTypeSign.SIGN_MINUS;
			}

			targetFragment = TargetDateFragment.newInstance(sign, date);
		}
		if (targetFragment != null) {
			this.showDialogFragment(targetFragment, TAG_TARGET_DIALOG);
		}
	}

	@Override
	public void onAlarmRuleCreate(int type, int sign) {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);

		DialogFragment fragment = null;
		if (type == Constants.AlarmType.TYPE_DAY_OF_WEEK) {
			fragment = RuleDayOfWeekFragment.newInstance(sign);
		} else if (type == Constants.AlarmType.TYPE_DATE) {
			fragment = RuleDateFragment.newInstance(sign);
		} else if (type == Constants.AlarmType.TYPE_BETWEEN) {
			fragment = RuleBetweenFragment.newInstance(sign);
		} else if (type == Constants.AlarmType.TYPE_GOOGLE_CALENDAR) {
			fragment = GoogleAccoountFragment.newInstance(sign);
		}
		this.showDialogFragment(fragment, TAG_ADD_RULE_DIALOG);
	}

	@Override
	public void onAddRule(int type, String typeValue, int sign) {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);

		AlarmTypeVo vo = new AlarmTypeVo();
		vo.setType(type);
		vo.setTypeValue(typeValue);
		vo.setSign(sign);

		this.alarmTypeFragment.addAlarmType(vo);
	}

	@Override
	public void onAddRuleCancel() {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);
	}

	@Override
	public void onEditRuleComplete(int position, String typeValue) {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);

		this.alarmTypeFragment.ruleEditComplete(position, typeValue);
	}

	@Override
	public void onGoogleAccountSelected(String typeValue, int sign) {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);

		RuleCalendarFragment fragment = RuleCalendarFragment.newInstance(
				typeValue, sign);
		this.showDialogFragment(fragment, TAG_ADD_RULE_DIALOG);
	}

	@Override
	public void onGoogleAccountCancel() {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);
	}

	@Override
	public void onGoogleAccountReSelected(int position, String account,
			String calendarName) {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);

		RuleCalendarFragment fragment = RuleCalendarFragment.newInstance(
				position, account, calendarName);
		this.showDialogFragment(fragment, TAG_ADD_RULE_DIALOG);
	}

	@Override
	public void onTargetDateOk(String typeValue, int sign) {
		this.dismissDialog(TAG_TARGET_DIALOG);

		AlarmTypeVo vo = new AlarmTypeVo();
		vo.setType(Constants.AlarmType.TYPE_DATE);
		vo.setTypeValue(typeValue);
		vo.setSign(sign);

		this.alarmTypeFragment.addAlarmType(vo);
	}

	@Override
	public void onTargetDateCancel() {
		this.dismissDialog(TAG_TARGET_DIALOG);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		ViewPager pager = (ViewPager) this
				.findViewById(R.id.alarmTypeMainPager);
		if (checkedId == R.id.alarmTypeMainListRadioButton) {
			pager.setCurrentItem(LIST_FRAGMENT);
		} else {
			pager.setCurrentItem(PREVIEW_FRAGMENT);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		RadioGroup group = (RadioGroup) this
				.findViewById(R.id.alarmTypeMainRadioGroup);
		group.setOnCheckedChangeListener(null);
		if (position == LIST_FRAGMENT) {
			group.check(R.id.alarmTypeMainListRadioButton);
		} else {
			group.check(R.id.alarmTypeMainPreviewRadioButton);
		}
		group.setOnCheckedChangeListener(this);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onAlarmNameOk(String name, String time, int enable) {
		this.dismissDialog(TAG_ALARM_NAME_DIALOG);

		alarmTypeFragment.alarmEditComplete(name, time, enable);
	}

	@Override
	public void onAlarmNameCancel() {
		this.dismissDialog(TAG_ALARM_NAME_DIALOG);
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