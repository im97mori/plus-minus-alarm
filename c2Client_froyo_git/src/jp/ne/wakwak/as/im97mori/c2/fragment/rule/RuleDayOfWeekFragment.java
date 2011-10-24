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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.RuleDayOfWeekVo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class RuleDayOfWeekFragment extends DialogFragment implements
		OnClickListener {

	private OnRuleListener listener;

	public static RuleDayOfWeekFragment newInstance(int sign) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.SIGN, sign);
		RuleDayOfWeekFragment fragment = new RuleDayOfWeekFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public static RuleDayOfWeekFragment newInstance(int position,
			String typeValue) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.POSITION, position);
		bundle.putString(Constants.ArgumentKey.TYPE_VALUE, typeValue);
		RuleDayOfWeekFragment fragment = new RuleDayOfWeekFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnRuleListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnRuleListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		builder.setTitle(this.getString(R.string.typeDayOfWeek));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);

		View view = LayoutInflater.from(this.getActivity()).inflate(
				R.layout.rule_day_of_week, null, false);

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setBackgroundResource(android.R.color.background_light);
		listView.setScrollingCacheEnabled(false);

		List<RuleDayOfWeekVo> list = new ArrayList<RuleDayOfWeekVo>(7);

		RuleDayOfWeekVo vo = new RuleDayOfWeekVo();
		vo.setDayOfWeek(Calendar.MONDAY);
		list.add(vo);

		vo = new RuleDayOfWeekVo();
		vo.setDayOfWeek(Calendar.TUESDAY);
		list.add(vo);

		vo = new RuleDayOfWeekVo();
		vo.setDayOfWeek(Calendar.WEDNESDAY);
		list.add(vo);

		vo = new RuleDayOfWeekVo();
		vo.setDayOfWeek(Calendar.THURSDAY);
		list.add(vo);

		vo = new RuleDayOfWeekVo();
		vo.setDayOfWeek(Calendar.FRIDAY);
		list.add(vo);

		vo = new RuleDayOfWeekVo();
		vo.setDayOfWeek(Calendar.SATURDAY);
		list.add(vo);

		vo = new RuleDayOfWeekVo();
		vo.setDayOfWeek(Calendar.SUNDAY);
		list.add(vo);

		RuleDayOfWeekAdapter adapter = new RuleDayOfWeekAdapter(list);
		listView.setAdapter(adapter);

		Bundle bundle = this.getArguments();
		if (bundle.containsKey(Constants.ArgumentKey.TYPE_VALUE)) {
			String typeValue = bundle
					.getString(Constants.ArgumentKey.TYPE_VALUE);
			String[] types = typeValue.split(",");
			for (int i = 0; i < types.length; i++) {
				int type = Integer.parseInt(types[i]);
				for (int j = 0; j < list.size(); j++) {
					vo = list.get(j);
					if (type == vo.getDayOfWeek()) {
						listView.setItemChecked(j, true);
					}
				}
			}
		}
		builder.setView(view);

		return builder.create();
	}

	public interface OnRuleDayOfWeekFragmentListener {
		void onRuleDayOfWeekCreate(String typeValue, int sign);

		void onRuleDayOfWeekCancel();

		void onRuleDayOfWeekUpdate(int position, String typeValue);
	}

	private class RuleDayOfWeekAdapter extends ArrayAdapter<RuleDayOfWeekVo> {

		private Calendar calendar = GregorianCalendar.getInstance();
		private SimpleDateFormat formatter;

		public RuleDayOfWeekAdapter(List<RuleDayOfWeekVo> list) {
			super(RuleDayOfWeekFragment.this.getActivity(), 0, list);
			this.formatter = new SimpleDateFormat(this.getContext().getString(
					R.string.dayOfWeekLongFormat));
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CheckedTextView checkedTextView = (CheckedTextView) convertView;
			if (checkedTextView == null) {
				checkedTextView = (CheckedTextView) LayoutInflater.from(
						this.getContext()).inflate(
						android.R.layout.select_dialog_multichoice, null);
			}
			RuleDayOfWeekVo vo = this.getItem(position);
			calendar.set(Calendar.DAY_OF_WEEK, vo.getDayOfWeek());
			checkedTextView.setText(this.formatter.format(calendar.getTime()));
			return checkedTextView;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (this.listener != null) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				ListView listView = (ListView) this.getDialog().findViewById(
						android.R.id.list);
				SparseBooleanArray array = listView.getCheckedItemPositions();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < array.size(); i++) {
					int key = array.keyAt(i);
					if (array.get(key)) {
						RuleDayOfWeekVo vo = (RuleDayOfWeekVo) listView
								.getItemAtPosition(key);
						sb.append(vo.getDayOfWeek());
						sb.append(",");
					}
				}
				if (sb.length() != 0) {
					Bundle bundle = this.getArguments();
					String typeValue = sb.substring(0, sb.length() - 1);
					if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
						this.listener.onEditRuleComplete(
								bundle.getInt(Constants.ArgumentKey.POSITION),
								typeValue);
					} else {
						this.listener.onAddRule(
								Constants.AlarmType.TYPE_DAY_OF_WEEK,
								typeValue,
								bundle.getInt(Constants.ArgumentKey.SIGN));
					}
				} else {
					Toast.makeText(this.getActivity(),
							R.string.needToCheckLeastOneString,
							Toast.LENGTH_SHORT).show();
				}
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
				this.listener.onAddRuleCancel();
			}
		}
	}
}