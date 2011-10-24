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

package jp.ne.wakwak.as.im97mori.c2.fragment;

import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.adapter.AlarmAdapter;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.service.AlarmService;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class AlarmFragment extends ListFragment implements OnRefreshListener {

	private OnAlarmFragmentListener listener;

	public static AlarmFragment newInstance() {
		AlarmFragment fragment = new AlarmFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnAlarmFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAlarmFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.createList();

		this.registerForContextMenu(this.getListView());
	}

	private void createList() {
		AlarmDb db = new AlarmDb(this.getActivity());
		List<AlarmVo> list = db.getAlarmList();
		db.close();

		AlarmAdapter adapter = new AlarmAdapter(this.getActivity(), list);
		this.setListAdapter(adapter);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (this.listener != null) {
			this.listener.onAlarmSelected(id);
		}
	}

	public interface OnAlarmFragmentListener extends OnShowAllListener {
		void onAddAlarm();

		void onAlarmSelected(long id);

		void onUpdate();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		MenuInflater inflater = new MenuInflater(this.getActivity());
		inflater.inflate(R.menu.list_context_menu, menu);
		MenuItem item = menu.findItem(R.id.listContextMenuDelete);
		Intent intent = new Intent();
		intent.putExtra(Constants.IntentKey.ID, info.id);
		item.setIntent(intent);

		inflater.inflate(R.menu.alarm_enable_context_menu, menu);
		AlarmDb db = new AlarmDb(this.getActivity());
		AlarmVo vo = db.getAlarm(info.id);
		db.close();

		if (vo.getEnable() == Constants.AlarmEnable.ALARM_ENABLED) {
			menu.removeItem(R.id.alarmEnableContextMenu);
			item = menu.findItem(R.id.alarmDisableContextMenu);
		} else {
			menu.removeItem(R.id.alarmDisableContextMenu);
			item = menu.findItem(R.id.alarmEnableContextMenu);
		}
		item.setIntent(intent);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent = item.getIntent();
		long id = intent.getLongExtra(Constants.IntentKey.ID,
				Constants.TEMPOLARY_ID);
		AlarmDb db = new AlarmDb(this.getActivity());
		if (item.getItemId() == R.id.listContextMenuDelete) {
			db.deleteAlarm(id);
		} else {
			AlarmVo vo = db.getAlarm(id);
			int enable = Constants.AlarmEnable.ALARM_ENABLED;
			if (item.getItemId() == R.id.alarmDisableContextMenu) {
				enable = Constants.AlarmEnable.ALARM_DISABLED;
				vo.setNext(null);
			}
			vo.setEnable(enable);
			db.updateAlarm(id, vo.getName(), vo.getTime(), vo.getNext(),
					vo.getEnable());
		}
		db.close();
		this.createList();

		this.listener.onShowAll();

		intent = new Intent(this.getActivity().getApplicationContext(),
				AlarmService.class);
		intent.putExtra(AlarmService.SETTING, Constants.AlarmCommand.EDIT_ALARM);
		intent.putExtra(Constants.IntentKey.ID, id);
		this.getActivity().startService(intent);
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.alarm_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		if (item.getItemId() == R.id.alarmAddMenu) {
			this.listener.onAddAlarm();
			result = true;
		} else {
			result = super.onOptionsItemSelected(item);
		}
		return result;
	}

	@Override
	public void onRefresh() {
		this.createList();
		this.listener.onUpdate();
	}
}