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
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmFragment.OnAlarmFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmNameFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmNameFragment.OnAlarmNameFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmTypeFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.AlarmTypeFragment.OnAlarmTypeListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.OnRefreshListener;
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
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetAlarmFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetAlarmFragment.OnTargetAlarmFragmentListner;
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetDateFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.target.TargetDateFragment.OnTargetDateFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.receiver.FinishCalculateReceiver;
import jp.ne.wakwak.as.im97mori.c2.service.AlarmService;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements
		OnAlarmFragmentListener, OnAlarmNameFragmentListener,
		OnAlarmTypeListener, OnAlarmRuleFragmentListener,
		OnGoogleAccountFragmentListener, OnPreviewFragmentListener,
		OnTargetAlarmFragmentListner, OnTargetDateFragmentListener,
		OnRuleListener, OnBackStackChangedListener {

	private static final String TAG_ADD_ALARM_DIALOG = "addAlarmDialog";
	private static final String TAG_ADD_RULE_DIALOG = "addRuleDialog";
	private static final String TAG_TARGET_DIALOG = "targetDialog";
	private FinishCalculateReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		this.setContentView(R.layout.main);

		FragmentManager fm = this.getFragmentManager();
		fm.addOnBackStackChangedListener(this);

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources()
				.getColor(R.color.title_background)));

		FragmentBreadCrumbs crumbs = (FragmentBreadCrumbs) actionBar
				.getCustomView();
		crumbs = new FragmentBreadCrumbs(this);
		crumbs.setActivity(this);
		crumbs.setMaxVisible(10);
		actionBar.setCustomView(crumbs);

		this.receiver = new FinishCalculateReceiver(null);
		Fragment fragment = fm.findFragmentById(R.id.listFragment);
		if (fragment == null) {
			fragment = AlarmFragment.newInstance();
			this.changeListFragment(fragment);
			crumbs.setTitle(this.getString(R.string.alarmListTitleString), null);
		} else if (fragment instanceof AlarmFragment) {
			crumbs.setTitle(this.getString(R.string.alarmListTitleString), null);
			this.receiver.setLinstener((OnRefreshListener) fragment);
		} else if (fragment instanceof AlarmTypeFragment) {
			crumbs.setParentTitle(
					this.getString(R.string.alarmListTitleString), null, null);

			Bundle bundle = fragment.getArguments();
			long id = bundle.getLong(Constants.ArgumentKey.ID);
			AlarmDb db = new AlarmDb(this);
			AlarmVo vo = db.getAlarm(id);
			db.close();
			crumbs.setTitle(vo.getName(), null);
			this.receiver.setLinstener((OnRefreshListener) fragment);
		}
	}

	private void changeListFragment(Fragment fragment) {
		FragmentManager manager = this.getFragmentManager();
		Fragment oldfragment = manager.findFragmentById(R.id.listFragment);
		FragmentTransaction transaction = manager.beginTransaction();

		if (oldfragment == null) {
			transaction.add(R.id.listFragment, fragment);
		} else {
			transaction.replace(R.id.listFragment, fragment);
			transaction.addToBackStack(null);
		}

		transaction.commit();

		this.receiver.setLinstener((OnRefreshListener) fragment);
	}

	@Override
	public void onAddAlarm() {
		AlarmNameFragment fragment = AlarmNameFragment.newInstance();
		this.showDialogFragment(fragment, TAG_ADD_ALARM_DIALOG);
	}

	@Override
	public void onAlarmSelected(long id) {
		AlarmDb db = new AlarmDb(this);
		AlarmVo vo = db.getAlarm(id);
		db.close();
		if (vo != null) {
			this.startAlarmTypeFragment(id);
		}
	}

	public void startAlarmTypeFragment(long id) {
		AlarmTypeFragment fragment = AlarmTypeFragment.newInstance(id);
		this.changeListFragment(fragment);
	}

	@Override
	public void onAlarmNameOk(String name, String time, int enable) {
		if (TextUtils.isEmpty(name)) {
			Toast.makeText(this, R.string.alarmNameNeedInputString,
					Toast.LENGTH_SHORT).show();
		} else {
			FragmentManager fm = this.getFragmentManager();
			Fragment fragment = fm.findFragmentById(R.id.listFragment);

			if (fragment instanceof AlarmFragment) {
				AlarmDb db = new AlarmDb(this);
				long id = db.addAlarm(name, time, null, enable);
				db.close();
				this.startAlarmTypeFragment(id);
			} else if (fragment instanceof AlarmTypeFragment) {
				AlarmTypeFragment alarmTypeFragment = (AlarmTypeFragment) fragment;
				alarmTypeFragment.alarmEditComplete(name, time, enable);
				FragmentBreadCrumbs crumbs = (FragmentBreadCrumbs) this
						.getActionBar().getCustomView();
				crumbs.setTitle(name, null);
			}
		}
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
		this.getFragmentManager().popBackStackImmediate();
	}

	@Override
	public void onUpdate(ArrayList<AlarmTypeVo> list) {
		PreviewFragment fragment = (PreviewFragment) this.getFragmentManager()
				.findFragmentById(R.id.preview);
		fragment.update(list);
	}

	@Override
	public void onShowAll() {
		FragmentManager manager = getFragmentManager();
		OnRefreshListener listener = (OnRefreshListener) manager
				.findFragmentById(R.id.listFragment);
		listener.onRefresh();
	}

	@Override
	public void onEditAlarm(String name, String time, int enable) {
		AlarmNameFragment fragment = AlarmNameFragment.newInstance(name, time,
				enable);
		this.showDialogFragment(fragment, TAG_ADD_ALARM_DIALOG);
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

	private void addAlarmTypeVo(AlarmTypeVo vo) {
		AlarmTypeFragment fragment = (AlarmTypeFragment) this
				.getFragmentManager().findFragmentById(R.id.listFragment);
		fragment.addAlarmType(vo);
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
	public void onMoveMonth() {
		FragmentManager fm = this.getFragmentManager();
		OnRefreshListener listener = (OnRefreshListener) fm
				.findFragmentById(R.id.listFragment);
		listener.onRefresh();
	}

	@Override
	public void onCellClick(Date date, Map<Long, Set<Date>> map) {
		FragmentManager fm = this.getFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.listFragment);

		DialogFragment targetFragment = null;
		if (fragment instanceof AlarmFragment && !map.isEmpty()
				&& !map.keySet().contains(Constants.TEMPOLARY_ID)) {
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
				targetFragment = TargetAlarmFragment.newInstance(ids, date);
			}
		} else if (fragment instanceof AlarmTypeFragment
				&& map.keySet().contains(Constants.TEMPOLARY_ID)) {
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
	public void onTargetAlarmSelected(long id) {
		this.dismissDialog(TAG_TARGET_DIALOG);

		this.startAlarmTypeFragment(id);
	}

	@Override
	public void onTargetDateOk(String typeValue, int sign) {
		this.dismissDialog(TAG_TARGET_DIALOG);

		AlarmTypeVo vo = new AlarmTypeVo();
		vo.setType(Constants.AlarmType.TYPE_DATE);
		vo.setTypeValue(typeValue);
		vo.setSign(sign);

		this.addAlarmTypeVo(vo);
	}

	@Override
	public void onTargetDateCancel() {
		this.dismissDialog(TAG_TARGET_DIALOG);
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
	public void onAddRule(int type, String typeValue, int sign) {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);

		AlarmTypeVo vo = new AlarmTypeVo();
		vo.setType(type);
		vo.setTypeValue(typeValue);
		vo.setSign(sign);

		this.addAlarmTypeVo(vo);

	}

	@Override
	public void onAddRuleCancel() {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);
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
		}
		return result;
	}

	@Override
	public void onBackStackChanged() {
		FragmentManager fm = this.getFragmentManager();
		FragmentBreadCrumbs crumbs = (FragmentBreadCrumbs) this.getActionBar()
				.getCustomView();

		Fragment fragment = fm.findFragmentById(R.id.listFragment);

		if (fragment instanceof AlarmFragment) {
			crumbs.setParentTitle(null, null, null);
			crumbs.setTitle(this.getString(R.string.alarmListTitleString), null);
		} else if (fragment instanceof AlarmTypeFragment) {
			crumbs.setParentTitle(
					this.getString(R.string.alarmListTitleString), null, null);

			Bundle bundle = fragment.getArguments();
			long id = bundle.getLong(Constants.ArgumentKey.ID);
			AlarmDb db = new AlarmDb(this);
			AlarmVo vo = db.getAlarm(id);
			db.close();
			crumbs.setTitle(vo.getName(), null);
		}

		OnRefreshListener listener = (OnRefreshListener) fragment;
		listener.onRefresh();

		this.receiver.setLinstener(listener);
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

	@Override
	public void onUpdate() {
		PreviewFragment fragment = (PreviewFragment) this.getFragmentManager()
				.findFragmentById(R.id.preview);
		fragment.update();
	}

	@Override
	public void onEditRuleComplete(int position, String typeValue) {
		this.dismissDialog(TAG_ADD_RULE_DIALOG);

		FragmentManager fm = this.getFragmentManager();
		AlarmTypeFragment fragment = (AlarmTypeFragment) fm
				.findFragmentById(R.id.listFragment);
		fragment.ruleEditComplete(position, typeValue);
	}
}