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

package jp.ne.wakwak.as.im97mori.c2.fragment.setting;

import java.util.ArrayList;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingListVo;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AlarmSettingFragment extends ListFragment {

	private OnAlarmSettingFragmentListener listener;

	public static interface OnAlarmSettingFragmentListener {
		void onSelect(int id);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnAlarmSettingFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAlarmSettingFratmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.createList();
	}

	private void createList() {
		List<AlarmSettingListVo> list = new ArrayList<AlarmSettingListVo>(2);

		AlarmSettingListVo vo = new AlarmSettingListVo();
		vo.setTypeId(Constants.AlarmSetting.SOUND);
		vo.setTypeNameId(R.string.alarmSettingSound);
		list.add(vo);

		vo = new AlarmSettingListVo();
		vo.setTypeId(Constants.AlarmSetting.SCREEN);
		vo.setTypeNameId(R.string.alarmSettingScreen);
		list.add(vo);

		vo = new AlarmSettingListVo();
		vo.setTypeId(Constants.AlarmSetting.VIBRATION);
		vo.setTypeNameId(R.string.alarmSettingVibration);
		list.add(vo);

		AlarmSettingAdapter adapter = new AlarmSettingAdapter(list);
		this.setListAdapter(adapter);
		ListView listView = this.getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setItemChecked(0, true);

	}

	private class AlarmSettingAdapter extends ArrayAdapter<AlarmSettingListVo> {
		public AlarmSettingAdapter(List<AlarmSettingListVo> list) {
			super(AlarmSettingFragment.this.getActivity(), 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(
						android.R.layout.simple_list_item_activated_1, null);
			}
			AlarmSettingListVo vo = this.getItem(position);
			TextView textView = (TextView) convertView
					.findViewById(android.R.id.text1);
			textView.setText(vo.getTypeNameId());

			return convertView;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		AlarmSettingListVo vo = (AlarmSettingListVo) l
				.getItemAtPosition(position);
		this.listener.onSelect(vo.getTypeId());
	}
}