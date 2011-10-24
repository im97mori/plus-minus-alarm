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
import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class SettingSoundFragment extends Fragment implements
		OnCheckedChangeListener, OnClickListener {

	private OnSettingSoundFragmentListener listener;

	public static SettingSoundFragment newInstance(long id) {
		SettingSoundFragment fragment = new SettingSoundFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Constants.ArgumentKey.ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static interface OnSettingSoundFragmentListener {
		void onSoundChoice();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnSettingSoundFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSettingSoundFragmentListener");
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
		View view = inflater.inflate(R.layout.alarm_setting_sound, null);
		CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.alarmSettingSoundCheck);

		Bundle bundle = this.getArguments();
		AlarmDb db = new AlarmDb(this.getActivity());
		AlarmSettingVo vo = db.getAlarmSetting(
				bundle.getLong(Constants.ArgumentKey.ID),
				Constants.AlarmSetting.SOUND);
		db.close();

		if (vo == null) {
			checkBox.setChecked(false);
		} else {
			checkBox.setChecked(true);
		}
		checkBox.setOnCheckedChangeListener(this);

		TextView textView = (TextView) view
				.findViewById(R.id.alarmSettingSoundSelect);
		textView.setOnClickListener(this);
		if (vo == null) {
			textView.setClickable(false);
		} else {
			textView.setClickable(true);
			String[] values = vo.getTypeValue().split("\n");
			if (Integer.parseInt(values[0]) == Constants.SoundType.AUDIO) {
				textView.setText(this.getString(R.string.soundTypeAudio) + ":"
						+ values[2]);
			} else if (Integer.parseInt(values[0]) == Constants.SoundType.RINGTONE) {
				textView.setText(this.getString(R.string.soundTypeRingtone)
						+ ":" + createRingtoneName(values[1]));
			}
		}
		return view;
	}

	@Override
	public void onCheckedChanged(CompoundButton paramCompoundButton,
			boolean paramBoolean) {
		TextView textView = (TextView) this.getView().findViewById(
				R.id.alarmSettingSoundSelect);
		textView.setText(this.getString(R.string.noSelect));
		textView.setClickable(paramBoolean);
		if (!paramBoolean) {
			AlarmDb db = new AlarmDb(this.getActivity());
			Bundle bundle = this.getArguments();
			db.deleteAlarmSetting(bundle.getLong(Constants.ArgumentKey.ID),
					Constants.AlarmSetting.SOUND);
			db.close();
		}
	}

	@Override
	public void onClick(View paramView) {
		if (this.listener != null) {
			this.listener.onSoundChoice();
		}
	}

	public void onSoundSelected(int type, String typeValue) {
		AlarmDb db = new AlarmDb(this.getActivity());
		Bundle bundle = this.getArguments();
		db.setAlarmSetting(bundle.getLong(Constants.ArgumentKey.ID),
				Constants.AlarmSetting.SOUND, type + "\n" + typeValue);
		db.close();

		TextView textView = (TextView) this.getView().findViewById(
				R.id.alarmSettingSoundSelect);
		if (type == Constants.SoundType.AUDIO) {
			textView.setText(this.getString(R.string.soundTypeAudio) + ":"
					+ typeValue.split("\n")[1]);
		} else if (type == Constants.SoundType.RINGTONE) {
			textView.setText(this.getString(R.string.soundTypeRingtone) + ":"
					+ createRingtoneName(typeValue));
		}
	}

	private String createRingtoneName(String typeValue) {
		String ringtoneName;
		if (typeValue == null) {
			ringtoneName = this.getString(R.string.silentRingtoneString);
		} else {
			Uri uri = Uri.parse(typeValue);
			Ringtone ringtone = RingtoneManager.getRingtone(this.getActivity(),
					uri);

			if (ringtone == null) {
				ringtoneName = this.getString(R.string.silentRingtoneString);
			} else {
				ringtoneName = ringtone.getTitle(this.getActivity());
			}
			if (RingtoneManager.isDefault(uri)) {
				ringtoneName += this.getString(R.string.defaultRingtoneString);
			}
		}
		return ringtoneName;
	}
}