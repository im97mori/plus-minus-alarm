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

package jp.ne.wakwak.as.im97mori.c2.fragment.vibration;

import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.VibrationVo;
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
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class VibrationListFragment extends ListFragment {

	private static final String INDEX = "INDEX";

	private OnVibrationListFragmentListener listener;

	public static interface OnVibrationListFragmentListener {
		void onSelect(VibrationVo vo);

		void onAddPattern();
	}

	public static VibrationListFragment newInstance() {
		VibrationListFragment fragment = new VibrationListFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnVibrationListFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnVibrationListFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.registerForContextMenu(this.getListView());

		AlarmDb db = new AlarmDb(this.getActivity());
		List<VibrationVo> list = db.getVibrationList();
		db.close();

		VibrationAdapter adapter = new VibrationAdapter(list);
		this.setListAdapter(adapter);

		ListView listView = this.getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (savedInstanceState == null && list.size() > 0) {
			listView.setItemChecked(0, true);
			this.listener.onSelect(list.get(0));
		} else if (savedInstanceState != null
				&& savedInstanceState.containsKey(INDEX)) {
			listView.setItemChecked(savedInstanceState.getInt(INDEX), true);
		}
	}

	private class VibrationAdapter extends ArrayAdapter<VibrationVo> {
		public VibrationAdapter(List<VibrationVo> list) {
			super(VibrationListFragment.this.getActivity(), 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(
						android.R.layout.simple_list_item_activated_1, null);
			}
			VibrationVo vo = this.getItem(position);
			TextView textView = (TextView) convertView
					.findViewById(android.R.id.text1);
			textView.setText(vo.getName());

			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return this.getItem(position).getId();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		VibrationVo vo = (VibrationVo) l.getItemAtPosition(position);
		this.listener.onSelect(vo);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.vibration_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		if (item.getItemId() == R.id.vibrationAddMenu) {
			this.listener.onAddPattern();
			result = true;
		} else {
			result = super.onOptionsItemSelected(item);
		}
		return result;
	}

	public void onAddVibration(String name) {
		AlarmDb db = new AlarmDb(this.getActivity());
		long id = db.addVibration(name, new long[0]);
		List<VibrationVo> list = db.getVibrationList();
		db.close();

		int index;
		for (index = 0; index < list.size(); index++) {
			VibrationVo vo = list.get(index);
			if (vo.getId() == id) {
				break;
			}
		}

		VibrationAdapter adapter = new VibrationAdapter(list);
		this.setListAdapter(adapter);

		ListView listView = this.getListView();
		listView.setItemChecked(index, true);
		this.listener.onSelect(list.get(index));
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
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ListView listView = this.getListView();
		int index = listView.getCheckedItemPosition();
		VibrationVo vo = (VibrationVo) listView.getItemAtPosition(index);

		Intent intent = item.getIntent();
		long id = intent.getLongExtra(Constants.IntentKey.ID,
				Constants.TEMPOLARY_ID);

		AlarmDb db = new AlarmDb(this.getActivity());
		db.deleteVibration(id);
		List<VibrationVo> list = db.getVibrationList();
		db.close();

		VibrationAdapter adapter = new VibrationAdapter(list);
		this.setListAdapter(adapter);

		if (list.size() > 0) {
			index = 0;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getId() == vo.getId()) {
					index = i;
					break;
				}
			}
			listView.setItemChecked(index, true);
			this.listener.onSelect(list.get(index));
		} else {
			this.listener.onSelect(null);
		}

		return true;
	}

	public void onVibrationUpdate() {
		ListView listView = this.getListView();
		int index = listView.getCheckedItemPosition();
		VibrationVo vo = (VibrationVo) listView.getItemAtPosition(index);

		AlarmDb db = new AlarmDb(this.getActivity());
		List<VibrationVo> list = db.getVibrationList();
		db.close();

		VibrationAdapter adapter = new VibrationAdapter(list);
		this.setListAdapter(adapter);

		index = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId() == vo.getId()) {
				index = i;
				break;
			}
		}
		listView.setItemChecked(index, true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		ListView listView = this.getListView();
		outState.putInt(INDEX, listView.getCheckedItemPosition());
	}
}