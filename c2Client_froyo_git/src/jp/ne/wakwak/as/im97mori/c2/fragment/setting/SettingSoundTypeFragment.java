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
import jp.ne.wakwak.as.im97mori.c2.vo.SoundTypeVo;
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

public class SettingSoundTypeFragment extends DialogFragment implements
		OnItemClickListener {

	private OnSettingSoundTypeFragmentListener listener;

	public static SettingSoundTypeFragment newInstance() {
		SettingSoundTypeFragment fragment = new SettingSoundTypeFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnSettingSoundTypeFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSettingSoundTypeFragmentListener");
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
		View view = inflater.inflate(R.layout.alarm_setting_sound_choice,
				container, false);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setBackgroundColor(this.getActivity().getResources()
				.getColor(android.R.color.background_light));
		listView.setOnItemClickListener(this);

		List<SoundTypeVo> list = new ArrayList<SoundTypeVo>(2);
		SoundTypeVo vo = new SoundTypeVo();
		vo.setType(Constants.SoundType.AUDIO);
		vo.setText(this.getString(R.string.soundTypeAudio));
		list.add(vo);
		vo = new SoundTypeVo();
		vo.setType(Constants.SoundType.RINGTONE);
		vo.setText(this.getString(R.string.soundTypeRingtone));
		list.add(vo);
		SoundTypeAdapter adapter = new SoundTypeAdapter(list);
		listView.setAdapter(adapter);
		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(this.getString(R.string.soundType));
		return dialog;
	}

	public interface OnSettingSoundTypeFragmentListener {
		void onSoundTypeChoice(int type);
	}

	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
			int paramInt, long paramLong) {
		if (this.listener != null) {
			SoundTypeVo vo = (SoundTypeVo) paramAdapterView
					.getItemAtPosition(paramInt);
			this.listener.onSoundTypeChoice(vo.getType());

		}
	}

	private class SoundTypeAdapter extends ArrayAdapter<SoundTypeVo> {

		public SoundTypeAdapter(List<SoundTypeVo> list) {
			super(SettingSoundTypeFragment.this.getActivity(), 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = (TextView) convertView;
			if (textView == null) {
				textView = (TextView) LayoutInflater.from(this.getContext())
						.inflate(android.R.layout.select_dialog_item, null);
			}
			SoundTypeVo vo = this.getItem(position);
			textView.setText(vo.getText());

			return textView;
		}
	}
}