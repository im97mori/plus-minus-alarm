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

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingVo;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingScreenFragment extends Fragment implements
		OnCheckedChangeListener {

	public static SettingScreenFragment newInstance(long id) {
		SettingScreenFragment fragment = new SettingScreenFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Constants.ArgumentKey.ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static interface OnSettingScreenFragmentListener {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.alarm_setting_screen, null);

		Bundle bundle = this.getArguments();
		AlarmDb db = new AlarmDb(this.getActivity());
		AlarmSettingVo vo = db.getAlarmSetting(
				bundle.getLong(Constants.ArgumentKey.ID),
				Constants.AlarmSetting.SCREEN);
		db.close();

		CheckBox checkBox = null;
		if (vo == null) {
			checkBox = (CheckBox) view
					.findViewById(R.id.alarmSettingScreenDisableKeyguardCheck);
			checkBox.setOnCheckedChangeListener(this);
			checkBox = (CheckBox) view
					.findViewById(R.id.alarmSettingScreenReenableKeyguardCheck);
			checkBox.setOnCheckedChangeListener(this);
		} else {
			String[] values = vo.getTypeValue().split("\n");
			checkBox = (CheckBox) view
					.findViewById(R.id.alarmSettingScreenDisableKeyguardCheck);
			if (values[0].equals(Boolean.FALSE.toString())) {
				checkBox.setChecked(false);
			}
			checkBox.setOnCheckedChangeListener(this);

			boolean checked = checkBox.isChecked();
			checkBox = (CheckBox) view
					.findViewById(R.id.alarmSettingScreenReenableKeyguardCheck);

			if (checked) {
				if (values[1].equals(Boolean.FALSE.toString())) {
					checkBox.setChecked(false);
				}
			} else {
				checkBox.setChecked(false);
				checkBox.setEnabled(false);
			}
			checkBox.setOnCheckedChangeListener(this);
		}

		return view;
	}

	@Override
	public void onCheckedChanged(CompoundButton paramCompoundButton,
			boolean paramBoolean) {
		boolean disable = false;
		boolean reenable = false;
		if (paramCompoundButton.getId() == R.id.alarmSettingScreenDisableKeyguardCheck) {
			disable = paramCompoundButton.isChecked();
			paramCompoundButton = (CheckBox) this.getView().findViewById(
					R.id.alarmSettingScreenReenableKeyguardCheck);
			paramCompoundButton.setEnabled(disable);
			reenable = paramCompoundButton.isChecked();
		} else {
			reenable = paramCompoundButton.isChecked();
			paramCompoundButton = (CheckBox) this.getView().findViewById(
					R.id.alarmSettingScreenDisableKeyguardCheck);
			disable = paramCompoundButton.isChecked();
		}

		AlarmDb db = new AlarmDb(this.getActivity());
		Bundle bundle = this.getArguments();
		db.setAlarmSetting(bundle.getLong(Constants.ArgumentKey.ID),
				Constants.AlarmSetting.SCREEN, Boolean.toString(disable) + "\n"
						+ Boolean.toString(reenable));
		db.close();
	}
}