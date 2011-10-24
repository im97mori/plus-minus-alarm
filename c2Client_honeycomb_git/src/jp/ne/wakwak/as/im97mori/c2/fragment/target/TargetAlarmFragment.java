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

package jp.ne.wakwak.as.im97mori.c2.fragment.target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TargetAlarmFragment extends DialogFragment implements
		OnItemClickListener {

	private OnTargetAlarmFragmentListner listener;

	public static TargetAlarmFragment newInstance(long[] ids, Date date) {
		Bundle bundle = new Bundle();
		TargetAlarmFragment fragment = new TargetAlarmFragment();
		bundle.putLongArray(Constants.ArgumentKey.IDS, ids);
		bundle.putLong(Constants.ArgumentKey.DATE, date.getTime());
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnTargetAlarmFragmentListner) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTargetAlarmFragmentListner");
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
		View view = inflater.inflate(R.layout.target_alarm, container, false);

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setOnItemClickListener(this);

		Bundle bundle = this.getArguments();
		long[] ids = bundle.getLongArray(Constants.ArgumentKey.IDS);
		List<AlarmVo> list = new ArrayList<AlarmVo>();
		AlarmDb db = new AlarmDb(this.getActivity());
		for (int i = 0; i < ids.length; i++) {
			list.add(db.getAlarm(ids[i]));

		}
		db.close();

		TargetAlarmAdapter adapter = new TargetAlarmAdapter(list);
		Comparator<AlarmVo> comparetor = new Comparator<AlarmVo>() {
			@Override
			public int compare(AlarmVo paramT1, AlarmVo paramT2) {
				return paramT1.getTime().compareTo(paramT2.getTime());
			}
		};
		adapter.sort(comparetor);
		listView.setAdapter(adapter);

		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		Bundle bundle = this.getArguments();
		Date date = new Date(bundle.getLong(Constants.ArgumentKey.DATE));

		SimpleDateFormat formatter = new SimpleDateFormat(this.getActivity()
				.getString(R.string.targetAlarmDateFormat));

		dialog.setTitle(formatter.format(date));
		return dialog;
	}

	public interface OnTargetAlarmFragmentListner {
		void onTargetAlarmSelected(long id);
	}

	private class TargetAlarmAdapter extends ArrayAdapter<AlarmVo> {
		public TargetAlarmAdapter(List<AlarmVo> list) {
			super(TargetAlarmFragment.this.getActivity(), 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = (TextView) convertView;
			if (textView == null) {
				textView = (TextView) LayoutInflater.from(this.getContext())
						.inflate(android.R.layout.select_dialog_item, null);
			}
			AlarmVo vo = this.getItem(position);
			textView.setText(vo.getName() + "(" + vo.getTime() + ")");
			return textView;
		}

		@Override
		public long getItemId(int position) {
			return this.getItem(position).getId();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
			int paramInt, long paramLong) {
		if (paramInt != AdapterView.INVALID_POSITION) {
			this.listener.onTargetAlarmSelected(paramLong);
		}
	}
}