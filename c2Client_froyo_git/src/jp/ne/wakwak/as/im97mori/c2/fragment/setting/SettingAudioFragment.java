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

import java.io.IOException;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SettingAudioFragment extends DialogFragment implements
		OnClickListener, OnItemClickListener {

	private Cursor cursor;
	private MediaPlayer player;
	private OnSettingAudioFragmentListener listener;

	public static SettingAudioFragment newInstance(long id) {
		SettingAudioFragment fragment = new SettingAudioFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Constants.ArgumentKey.ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnSettingAudioFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnSettingAudioFragmentListener");
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
				R.layout.alarm_setting_sound_audio_choice, null, false);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setBackgroundColor(this.getActivity().getResources()
				.getColor(android.R.color.background_light));
		listView.setOnItemClickListener(this);

		ContentResolver cr = this.getActivity().getContentResolver();
		cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				null, null, null);
		if (cursor != null) {
			listView.setAdapter(new AudioAdapter(cursor));

			Bundle bundle = this.getArguments();
			long id = bundle.getLong(Constants.ArgumentKey.ID);
			cursor.moveToFirst();
			int position = 0;
			int targetPosition = 0;
			while (!cursor.isAfterLast()) {
				long currentMediaId = cursor.getLong(cursor
						.getColumnIndex(MediaStore.MediaColumns._ID));
				if (currentMediaId == id) {
					targetPosition = position;
					break;
				}
				position++;
				cursor.moveToNext();
			}
			listView.setItemChecked(targetPosition, true);
			listView.setSelectionFromTop(targetPosition, 0);
		}

		builder.setView(view);
		return builder.create();
	}

	public interface OnSettingAudioFragmentListener {
		void onSoundTypeAudioChoice(long id, String name);

		void onSoundTypeAudioChoiceCancel();
	}

	private class AudioAdapter extends SimpleCursorAdapter {

		public AudioAdapter(Cursor c) {
			super(SettingAudioFragment.this.getActivity(),
					android.R.layout.select_dialog_singlechoice, c,
					new String[] { MediaStore.MediaColumns.DISPLAY_NAME },
					new int[] { android.R.id.text1 });
		}
	}

	@Override
	public void onDestroyView() {
		if (this.cursor != null && !this.cursor.isClosed()) {
			this.cursor.close();
		}
		this.stopSound();
		super.onDestroyView();
	}

	private void stopSound() {
		if (this.player != null) {
			if (this.player.isPlaying()) {
				this.player.stop();
			}
			this.player.release();
			this.player = null;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (this.listener != null) {
			ListView listView = (ListView) this.getDialog().findViewById(
					android.R.id.list);
			if (which == DialogInterface.BUTTON_POSITIVE) {
				int position = listView.getCheckedItemPosition();
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);
				if (cursor == null) {
					this.listener.onSoundTypeAudioChoiceCancel();
				} else {
					long id = cursor.getLong(cursor
							.getColumnIndex(MediaStore.MediaColumns._ID));
					String name = cursor
							.getString(cursor
									.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
					this.listener.onSoundTypeAudioChoice(id, name);
				}
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
				this.listener.onSoundTypeAudioChoiceCancel();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		this.stopSound();
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String path = cursor.getString(cursor
				.getColumnIndex(MediaStore.Audio.Media.DATA));
		this.player = new MediaPlayer();
		try {
			this.player.setDataSource(path);
			this.player.prepare();
			this.player.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}