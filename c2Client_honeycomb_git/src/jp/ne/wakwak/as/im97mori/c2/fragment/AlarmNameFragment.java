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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmNameFragment extends DialogFragment implements
		OnClickListener, TextWatcher {

	private OnAlarmNameFragmentListener listener;

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
			listener = (OnAlarmNameFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAlarmNameFragmentListener");
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
		builder.setTitle(this.getString(R.string.soundTypeAudio));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);

		View view = LayoutInflater.from(this.getActivity()).inflate(
				R.layout.alarm_name, null, false);
		TimePicker picker = (TimePicker) view.findViewById(R.id.alarmNameTime);
		picker.setIs24HourView(true);

		TextView textView = (TextView) view.findViewById(R.id.alarmNameValue);

		Bundle bundle = this.getArguments();
		if (bundle != null && bundle.containsKey(Constants.ArgumentKey.NAME)) {
			String[] time = bundle.getString(Constants.ArgumentKey.TIME).split(
					":");
			picker.setCurrentHour(Integer.parseInt(time[0]));
			picker.setCurrentMinute(Integer.parseInt(time[1]));

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
		textView.addTextChangedListener(this);

		if (bundle != null && bundle.containsKey(Constants.ArgumentKey.NAME)) {
			builder.setTitle(this.getString(R.string.alarmEditString));
		} else {
			builder.setTitle(this.getString(R.string.alarmAddTitleString));
		}
		builder.setView(view);
		Dialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		return dialog;
	}

	public interface OnAlarmNameFragmentListener {
		void onAlarmNameOk(String name, String time, int enable);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			TimePicker picker = (TimePicker) this.getDialog().findViewById(
					R.id.alarmNameTime);
			picker.clearFocus();
			TextView textView = (TextView) this.getDialog().findViewById(
					R.id.alarmNameValue);
			String name = textView.getText().toString();
			StringBuilder sb = new StringBuilder("0");
			sb.append(picker.getCurrentHour());
			String hour = sb.substring(sb.length() - 2, sb.length());
			sb.setLength(0);
			sb.append("0");
			sb.append(picker.getCurrentMinute());
			String minute = sb.substring(sb.length() - 2, sb.length());
			String time = hour + ":" + minute;
			CompoundButton toggleButton = (CompoundButton) this.getDialog()
					.findViewById(R.id.alarmNameEnableToggle);
			int enable = Constants.AlarmEnable.ALARM_DISABLED;
			if (toggleButton.isChecked()) {
				enable = Constants.AlarmEnable.ALARM_ENABLED;
			}
			this.listener.onAlarmNameOk(name, time, enable);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		TextView textView = (TextView) this.getDialog().findViewById(
				R.id.alarmNameValue);
		if (TextUtils.isEmpty(s)) {
			textView.setError(this
					.getString(R.string.alarmNameTitleErrorString));
		} else {
			textView.setError(null);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}