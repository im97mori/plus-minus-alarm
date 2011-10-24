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

import java.util.Calendar;
import java.util.GregorianCalendar;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmNameFragment extends DialogFragment implements
		OnClickListener {

	private OnAlarmAddFragmentListener listener;

	public static AlarmNameFragment newInstance() {
		AlarmNameFragment fragment = new AlarmNameFragment();
		return fragment;
	}

	public static AlarmNameFragment newInstance(String name, String time,
			int enable) {
		AlarmNameFragment fragment = new AlarmNameFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.ArgumentKey.NAME, name);
		bundle.putString(Constants.ArgumentKey.TIME, time);
		bundle.putInt(Constants.ArgumentKey.ENABLE, enable);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnAlarmAddFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAlarmAddFragmentListener");
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
		View view = inflater.inflate(R.layout.alarm_name, container, false);
		view.findViewById(R.id.alarmNameOkButton).setOnClickListener(this);
		view.findViewById(R.id.alarmNameCancelButton).setOnClickListener(this);
		TimePicker picker = (TimePicker) view.findViewById(R.id.alarmNameTime);
		picker.setIs24HourView(true);

		Bundle bundle = this.getArguments();
		if (bundle != null && bundle.containsKey(Constants.ArgumentKey.NAME)) {
			String[] time = bundle.getString(Constants.ArgumentKey.TIME).split(
					":");
			picker.setCurrentHour(Integer.parseInt(time[0]));
			picker.setCurrentMinute(Integer.parseInt(time[1]));

			TextView textView = (TextView) view
					.findViewById(R.id.alarmNameValue);
			textView.setText(bundle.getString(Constants.ArgumentKey.NAME));

			CompoundButton toggleButton = (CompoundButton) view
					.findViewById(R.id.alarmNameEnableToggle);
			if (bundle.getInt(Constants.ArgumentKey.ENABLE) == Constants.AlarmEnable.ALARM_ENABLED) {
				toggleButton.setChecked(true);
			} else {
				toggleButton.setChecked(false);
			}
		} else {
			Calendar calendar = GregorianCalendar.getInstance();
			picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		}
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		Bundle bundle = this.getArguments();
		if (bundle != null && bundle.containsKey(Constants.ArgumentKey.NAME)) {
			dialog.setTitle(this.getString(R.string.alarmEditString));
		} else {
			dialog.setTitle(this.getString(R.string.alarmAddTitleString));
		}
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		return dialog;
	}

	@Override
	public void onClick(View paramView) {
		if (this.listener != null) {
			if (paramView.getId() == R.id.alarmNameOkButton) {
				TimePicker picker = (TimePicker) this.getDialog().findViewById(
						R.id.alarmNameTime);
				picker.clearFocus();
				TextView textView = (TextView) this.getDialog().findViewById(
						R.id.alarmNameValue);
				String name = textView.getText().toString();
				if (name != null && name.length() > 0) {
					StringBuilder sb = new StringBuilder("0");
					sb.append(picker.getCurrentHour());
					String hour = sb.substring(sb.length() - 2, sb.length());
					sb.setLength(0);
					sb.append("0");
					sb.append(picker.getCurrentMinute());
					String minute = sb.substring(sb.length() - 2, sb.length());
					String time = hour + ":" + minute;
					CompoundButton toggleButton = (CompoundButton) this
							.getView().findViewById(R.id.alarmNameEnableToggle);
					int enable = Constants.AlarmEnable.ALARM_DISABLED;
					if (toggleButton.isChecked()) {
						enable = Constants.AlarmEnable.ALARM_ENABLED;
					}
					this.listener.onAlarmNameOk(name, time, enable);
				} else {
					textView.setError(this
							.getString(R.string.alarmNameTitleErrorString));
					textView.requestFocus();
				}
			} else if (paramView.getId() == R.id.alarmNameCancelButton) {
				this.listener.onAlarmNameCancel();
			}
		}
	}

	public interface OnAlarmAddFragmentListener {
		void onAlarmNameOk(String name, String time, int enable);

		void onAlarmNameCancel();
	}
}