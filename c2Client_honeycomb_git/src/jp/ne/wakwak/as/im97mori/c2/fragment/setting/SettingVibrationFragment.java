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

import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.activity.VibrationActivity;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingVo;
import jp.ne.wakwak.as.im97mori.c2.vo.VibrationVo;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingVibrationFragment extends Fragment implements
		OnCheckedChangeListener, OnClickListener {

	private OnSettingVibrationFragmentListener listener;

	public static SettingVibrationFragment newInstance(long id) {
		SettingVibrationFragment fragment = new SettingVibrationFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Constants.ArgumentKey.ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static interface OnSettingVibrationFragmentListener {
		void onVibrationPatternChoice();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnSettingVibrationFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSettingVibrationFragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.alarm_setting_vibration, null);
		CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.alarmSettingVibrationCheck);
		checkBox.setOnCheckedChangeListener(this);

		view.findViewById(R.id.alarmSettingVibrationSelect).setOnClickListener(
				this);
		view.findViewById(R.id.alarmSettingVibrationEditPatternButton)
				.setOnClickListener(this);

		return view;
	}

	@Override
	public void onCheckedChanged(CompoundButton paramCompoundButton,
			boolean paramBoolean) {
		TextView textView = (TextView) this.getView().findViewById(
				R.id.alarmSettingVibrationSelect);
		textView.setText(this.getString(R.string.noSelect));
		textView.setClickable(paramBoolean);
		if (!paramBoolean) {
			Bundle bundle = this.getArguments();
			AlarmDb db = new AlarmDb(this.getActivity());
			db.deleteAlarmSetting(bundle.getLong(Constants.ArgumentKey.ID),
					Constants.AlarmSetting.VIBRATION);
			db.close();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.alarmSettingVibrationSelect) {
			AlarmDb db = new AlarmDb(this.getActivity());
			List<VibrationVo> list = db.getVibrationList();
			db.close();

			if (list.size() > 0 && this.listener != null) {
				this.listener.onVibrationPatternChoice();
			} else {
				Toast.makeText(this.getActivity(), R.string.noVibrationPattern,
						Toast.LENGTH_SHORT).show();
			}
		} else if (v.getId() == R.id.alarmSettingVibrationEditPatternButton) {
			Intent intent = new Intent(this.getActivity()
					.getApplicationContext(), VibrationActivity.class);
			this.startActivity(intent);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		Bundle bundle = this.getArguments();
		AlarmDb db = new AlarmDb(this.getActivity());
		AlarmSettingVo vo = db.getAlarmSetting(
				bundle.getLong(Constants.ArgumentKey.ID),
				Constants.AlarmSetting.VIBRATION);

		CheckBox checkBox = (CheckBox) this.getView().findViewById(
				R.id.alarmSettingVibrationCheck);
		TextView textView = (TextView) this.getView().findViewById(
				R.id.alarmSettingVibrationSelect);
		if (vo == null) {
			checkBox.setChecked(false);
			textView.setClickable(false);
		} else {
			checkBox.setChecked(true);
			textView.setClickable(true);

			long vibrationId = Long.valueOf(vo.getTypeValue());
			VibrationVo vibrationVo = db.getVibration(vibrationId);

			textView.setText(vibrationVo.getName());
		}
		db.close();
	}

	public void onVibrationPatternSelected(long id, String name) {
		Bundle bundle = this.getArguments();
		AlarmDb db = new AlarmDb(this.getActivity());
		db.setAlarmSetting(bundle.getLong(Constants.ArgumentKey.ID),
				Constants.AlarmSetting.VIBRATION, String.valueOf(id));
		db.close();

		TextView textView = (TextView) this.getView().findViewById(
				R.id.alarmSettingVibrationSelect);
		textView.setText(name);
	}
}