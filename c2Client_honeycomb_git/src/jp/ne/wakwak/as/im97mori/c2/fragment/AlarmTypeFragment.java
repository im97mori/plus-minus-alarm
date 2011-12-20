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

import java.util.ArrayList;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.activity.AlarmSettingActivity;
import jp.ne.wakwak.as.im97mori.c2.adapter.AlarmTypeAdapter;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.service.AlarmService;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmTypeFragment extends ListFragment implements
		OnRefreshListener, OnClickListener {

	private static final String LIST = "LIST";

	private OnAlarmTypeListener listener;

	public static AlarmTypeFragment newInstance(long id) {
		AlarmTypeFragment fragment = new AlarmTypeFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Constants.ArgumentKey.ID, id);
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnAlarmTypeListener) activity;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.alarm_type, container, false);
	}

	public void onResume() {
		super.onResume();

		ActionBar bar = this.getActivity().getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);

		AlarmTypeAdapter adapter = (AlarmTypeAdapter) this.getListAdapter();
		ArrayList<AlarmTypeVo> newList = new ArrayList<AlarmTypeVo>(adapter
				.getList().size());
		newList.addAll(adapter.getList());
		this.upadte(newList);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		List<AlarmTypeVo> list = null;
		Bundle bundle = this.getArguments();
		long alarmId = bundle.getLong(Constants.ArgumentKey.ID);
		AlarmDb db = new AlarmDb(this.getActivity());
		AlarmVo vo = db.getAlarm(alarmId);
		if (savedInstanceState != null && savedInstanceState.containsKey(LIST)) {
			list = savedInstanceState.getParcelableArrayList(LIST);
		} else {
			list = db.getAlarmTypeList(alarmId);
		}
		db.close();

		TextView textView = (TextView) this.getView().findViewById(
				R.id.alarmTypeAlarmNameText);
		textView.setText(vo.getName());
		textView = (TextView) this.getView().findViewById(
				R.id.alarmTypeAlarmTimeText);
		textView.setText(vo.getTime());

		ToggleButton toggleButton = (ToggleButton) this.getView().findViewById(
				R.id.alarmTypeToggle);
		if (vo.getEnable() == Constants.AlarmEnable.ALARM_ENABLED) {
			toggleButton.setChecked(true);
		} else {
			toggleButton.setChecked(false);
		}

		AlarmTypeAdapter adapter = new AlarmTypeAdapter(this.getActivity(),
				list);
		this.setListAdapter(adapter);

		this.registerForContextMenu(this.getListView());

		this.getView().findViewById(R.id.alarmTypeNameLayout)
				.setOnClickListener(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
//		this.listener.onShowAll();
		this.listener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		AlarmTypeVo vo = (AlarmTypeVo) l.getItemAtPosition(position);
		this.listener.onEditRule(position, vo);
	}

	public interface OnAlarmTypeListener extends OnShowAllListener {
		void onAddRule(int sign);

		void onDeleteAlarm(long id);

		void onUpdate(ArrayList<AlarmTypeVo> list);

		void onEditRule(int position, AlarmTypeVo vo);

		void onEditAlarm(String name, String time, int enable);
	}

	public void addAlarmType(AlarmTypeVo vo) {
		AlarmTypeAdapter adapter = (AlarmTypeAdapter) this.getListAdapter();
		Bundle bundle = this.getArguments();
		vo.setAlarmId(bundle.getLong(Constants.ArgumentKey.ID));
		List<AlarmTypeVo> list = adapter.getList();
		ArrayList<AlarmTypeVo> newList = new ArrayList<AlarmTypeVo>(list.size());
		newList.addAll(list);
		newList.add(vo);
		adapter.add(vo);
		this.upadte(newList);
	}

	private void upadte(ArrayList<AlarmTypeVo> list) {
		if (this.listener != null) {
			this.listener.onUpdate(list);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		MenuInflater inflater = new MenuInflater(this.getActivity());
		inflater.inflate(R.menu.list_context_menu, menu);
		MenuItem item = menu.findItem(R.id.listContextMenuDelete);
		Intent intent = new Intent();
		intent.putExtra(Constants.IntentKey.POSITION, info.position);
		item.setIntent(intent);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AlarmTypeAdapter adapter = (AlarmTypeAdapter) this.getListAdapter();
		Intent intent = item.getIntent();
		int position = intent.getIntExtra(Constants.IntentKey.POSITION, -1);
		List<AlarmTypeVo> list = adapter.getList();
		list.remove(position);
		adapter.notifyDataSetChanged();

		ArrayList<AlarmTypeVo> newList = new ArrayList<AlarmTypeVo>(list.size());
		newList.addAll(list);
		this.upadte(newList);

		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.alarm_type_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		if (item.getItemId() == R.id.alarmTypeSaveMenu) {

			Bundle bundle = this.getArguments();
			long alarmId = bundle.getLong(Constants.ArgumentKey.ID);
			AlarmDb db = new AlarmDb(this.getActivity());
			TextView textView = (TextView) this.getView().findViewById(
					R.id.alarmTypeAlarmNameText);
			String name = textView.getText().toString();
			textView = (TextView) this.getView().findViewById(
					R.id.alarmTypeAlarmTimeText);
			String time = textView.getText().toString();

			ToggleButton toggleButton = (ToggleButton) this.getView()
					.findViewById(R.id.alarmTypeToggle);
			int enable = Constants.AlarmEnable.ALARM_DISABLED;
			if (toggleButton.isChecked()) {
				enable = Constants.AlarmEnable.ALARM_ENABLED;
			}
			db.updateAlarm(alarmId, name, time, null, enable);

			this.getListAdapter();
			AlarmTypeAdapter adapter = (AlarmTypeAdapter) this.getListAdapter();
			List<AlarmTypeVo> list = new ArrayList<AlarmTypeVo>(
					adapter.getCount());
			for (int i = 0; i < adapter.getCount(); i++) {
				list.add(adapter.getItem(i));
			}
			db.updateAlarmType(alarmId, list);
			db.close();

			Intent intent = new Intent(this.getActivity()
					.getApplicationContext(), AlarmService.class);
			intent.putExtra(AlarmService.SETTING,
					Constants.AlarmCommand.EDIT_ALARM);
			intent.putExtra(Constants.IntentKey.ID, alarmId);
			this.getActivity().startService(intent);
			result = true;
		} else if (item.getItemId() == R.id.alarmTypePlusAddMenu) {
			this.listener.onAddRule(Constants.AlarmTypeSign.SIGN_PLUS);
			result = true;
		} else if (item.getItemId() == R.id.alarmTypeMinusAddMenu) {
			this.listener.onAddRule(Constants.AlarmTypeSign.SIGN_MINUS);
			result = true;
		} else if (item.getItemId() == R.id.alarmTypeDeleteMenu) {
			this.listener.onDeleteAlarm(this.getArguments().getLong(
					Constants.ArgumentKey.ID));
			result = true;
		} else if (item.getItemId() == android.R.id.home) {
			this.getFragmentManager().popBackStack();
			result = true;
		} else if (item.getItemId() == R.id.alarmTypeDetailSettingMenu) {
			Intent intent = new Intent(this.getActivity()
					.getApplicationContext(), AlarmSettingActivity.class);
			Bundle bundle = this.getArguments();
			long alarmId = bundle.getLong(Constants.ArgumentKey.ID);
			intent.putExtra(Constants.IntentKey.ID, alarmId);
			this.startActivity(intent);
			result = true;
		} else {
			result = super.onOptionsItemSelected(item);
		}
		return result;
	}

	@Override
	public void onRefresh() {
		AlarmTypeAdapter adapter = (AlarmTypeAdapter) this.getListAdapter();
		List<AlarmTypeVo> list = adapter.getList();
		ArrayList<AlarmTypeVo> newList = new ArrayList<AlarmTypeVo>(list.size());
		newList.addAll(list);
		this.upadte(newList);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		AlarmTypeAdapter adapter = (AlarmTypeAdapter) this.getListAdapter();
		if (adapter != null) {
			outState.putParcelableArrayList(LIST, new ArrayList<AlarmTypeVo>(
					adapter.getList()));
		}
	}

	public void ruleEditComplete(int position, String typeValue) {
		AlarmTypeAdapter adapter = (AlarmTypeAdapter) this.getListAdapter();
		List<AlarmTypeVo> list = adapter.getList();
		AlarmTypeVo vo = list.get(position);
		vo.setTypeValue(typeValue);
		ArrayList<AlarmTypeVo> newList = new ArrayList<AlarmTypeVo>(list.size());
		newList.addAll(list);

		adapter.notifyDataSetChanged();

		this.upadte(newList);
	}

	public void alarmEditComplete(String name, String time, int enable) {
		TextView textView = (TextView) this.getView().findViewById(
				R.id.alarmTypeAlarmNameText);
		textView.setText(name);
		textView = (TextView) this.getView().findViewById(
				R.id.alarmTypeAlarmTimeText);
		textView.setText(time);

		ToggleButton toggleButton = (ToggleButton) this.getView().findViewById(
				R.id.alarmTypeToggle);
		if (enable == Constants.AlarmEnable.ALARM_ENABLED) {
			toggleButton.setChecked(true);
		} else {
			toggleButton.setChecked(false);
		}
	}

	@Override
	public void onClick(View paramView) {
		if (this.listener != null) {
			if (paramView.getId() == R.id.alarmTypeNameLayout) {
				TextView textView = (TextView) this.getView().findViewById(
						R.id.alarmTypeAlarmNameText);
				String name = textView.getText().toString();
				textView = (TextView) this.getView().findViewById(
						R.id.alarmTypeAlarmTimeText);
				String time = textView.getText().toString();

				ToggleButton toggleButton = (ToggleButton) this.getView()
						.findViewById(R.id.alarmTypeToggle);
				int enable = Constants.AlarmEnable.ALARM_DISABLED;
				if (toggleButton.isChecked()) {
					enable = Constants.AlarmEnable.ALARM_ENABLED;
				}
				this.listener.onEditAlarm(name, time, enable);
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		this.getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
	}
}