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
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.util.Util;
import jp.ne.wakwak.as.im97mori.c2.vo.VibrationVo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SettingVibrationPatternFragment extends DialogFragment implements
		OnClickListener, OnItemClickListener {

	private Vibrator vibrator;
	private OnSettingVibrationPatternFragmentListener listener;

	public static SettingVibrationPatternFragment newInstance(long id) {
		SettingVibrationPatternFragment fragment = new SettingVibrationPatternFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Constants.ArgumentKey.ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnSettingVibrationPatternFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					activity.toString()
							+ " must implement OnSettingVibrationPatternFragmentListener");
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
		builder.setTitle(this.getString(R.string.soundTypeVibration));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);

		View view = LayoutInflater.from(this.getActivity()).inflate(
				R.layout.alarm_setting_vibration_pattern_choice, null, false);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setOnItemClickListener(this);

		AlarmDb db = new AlarmDb(this.getActivity());
		List<VibrationVo> list = db.getVibrationList();
		db.close();
		listView.setAdapter(new PatternAdapter(list));

		Bundle bundle = this.getArguments();
		long id = bundle.getLong(Constants.ArgumentKey.ID);
		int targetPosition = 0;
		for (int i = 0; i < list.size(); i++) {
			VibrationVo vo = list.get(i);
			if (id == vo.getId()) {
				targetPosition = i;
				break;
			}
		}
		listView.setItemChecked(targetPosition, true);
		listView.setSelectionFromTop(targetPosition, 0);

		builder.setView(view);
		return builder.create();
	}

	public interface OnSettingVibrationPatternFragmentListener {
		void onVibrationPatternChoice(long id, String name);
	}

	private class PatternAdapter extends ArrayAdapter<VibrationVo> {

		private LayoutInflater inflater;

		public PatternAdapter(List<VibrationVo> list) {
			super(SettingVibrationPatternFragment.this.getActivity(), 0, list);
			this.inflater = SettingVibrationPatternFragment.this.getActivity()
					.getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = this.inflater.inflate(
						android.R.layout.select_dialog_singlechoice, null);
			}
			VibrationVo vo = this.getItem(position);
			TextView textView = (TextView) view
					.findViewById(android.R.id.text1);
			textView.setText(vo.getName());
			return view;
		}
	}

	@Override
	public void onDestroyView() {
		this.stopVibration();
		super.onDestroyView();
	}

	private void stopVibration() {
		if (this.vibrator != null) {
			this.vibrator.cancel();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (this.listener != null) {
			ListView listView = (ListView) this.getDialog().findViewById(
					android.R.id.list);
			if (which == DialogInterface.BUTTON_POSITIVE) {
				int position = listView.getCheckedItemPosition();
				VibrationVo vo = (VibrationVo) listView
						.getItemAtPosition(position);
				if (vo != null) {
					this.listener.onVibrationPatternChoice(vo.getId(),
							vo.getName());
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		this.stopVibration();

		VibrationVo vo = (VibrationVo) parent.getItemAtPosition(position);
		Util.vibrate(this.vibrator, vo.getPattern());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.vibrator = (Vibrator) this.getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);
	}
}