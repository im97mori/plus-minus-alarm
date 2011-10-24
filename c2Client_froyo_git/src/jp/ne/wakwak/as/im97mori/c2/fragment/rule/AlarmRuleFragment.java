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

package jp.ne.wakwak.as.im97mori.c2.fragment.rule;

import java.util.ArrayList;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.RuleVo;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AlarmRuleFragment extends DialogFragment implements
		OnItemClickListener {

	private OnAlarmRuleFragmentListener listener;

	public static AlarmRuleFragment newInstance(int sign) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.SIGN, sign);
		AlarmRuleFragment fragment = new AlarmRuleFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnAlarmRuleFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAlarmRuleFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.alarm_rule, container, false);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setBackgroundColor(this.getActivity().getResources()
				.getColor(android.R.color.background_light));
		listView.setOnItemClickListener(this);

		List<RuleVo> list = new ArrayList<RuleVo>(3);
		RuleVo vo = new RuleVo();
		vo.setType(Constants.AlarmType.TYPE_DAY_OF_WEEK);
		vo.setText(this.getString(R.string.typeDayOfWeek));
		list.add(vo);
		vo = new RuleVo();
		vo.setType(Constants.AlarmType.TYPE_DATE);
		vo.setText(this.getString(R.string.typeDate));
		list.add(vo);
		vo = new RuleVo();
		vo.setType(Constants.AlarmType.TYPE_BETWEEN);
		vo.setText(this.getString(R.string.typeBetween));
		list.add(vo);
		vo = new RuleVo();
		vo.setType(Constants.AlarmType.TYPE_GOOGLE_CALENDAR);
		vo.setText(this.getString(R.string.typeCalendar));
		list.add(vo);
		RuleAdapter adapter = new RuleAdapter(list);
		listView.setAdapter(adapter);
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(this.getString(R.string.alarmRuleTitleString));
		return dialog;
	}

	public interface OnAlarmRuleFragmentListener {
		void onAlarmRuleCreate(int type, int sign);
	}

	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
			int paramInt, long paramLong) {
		if (this.listener != null) {
			RuleVo vo = (RuleVo) paramAdapterView.getItemAtPosition(paramInt);
			Bundle bundle = this.getArguments();
			this.listener.onAlarmRuleCreate(vo.getType(),
					bundle.getInt(Constants.ArgumentKey.SIGN));
		}
	}

	private class RuleAdapter extends ArrayAdapter<RuleVo> {

		public RuleAdapter(List<RuleVo> list) {
			super(AlarmRuleFragment.this.getActivity(), 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = (TextView) convertView;
			if (textView == null) {
				textView = (TextView) LayoutInflater.from(this.getContext())
						.inflate(android.R.layout.select_dialog_item, null);
			}
			RuleVo vo = this.getItem(position);
			textView.setText(vo.getText());

			return textView;
		}
	}
}