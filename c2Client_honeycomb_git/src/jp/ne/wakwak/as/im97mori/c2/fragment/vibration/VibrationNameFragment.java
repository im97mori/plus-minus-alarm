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

package jp.ne.wakwak.as.im97mori.c2.fragment.vibration;

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
import android.widget.TextView;

public class VibrationNameFragment extends DialogFragment implements
		TextWatcher, OnClickListener {

	private OnVibrationNameFragmentListener listener;

	public static VibrationNameFragment newInstance() {
		VibrationNameFragment fragment = new VibrationNameFragment();
		return fragment;
	}

	public static VibrationNameFragment newInstance(String name) {
		VibrationNameFragment fragment = new VibrationNameFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.ArgumentKey.NAME, name);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnVibrationNameFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnVibrationNameFragmentListener");
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
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);

		View view = LayoutInflater.from(this.getActivity()).inflate(
				R.layout.vibration_name, null, false);

		TextView textView = (TextView) view
				.findViewById(R.id.vibrationNameValue);

		Bundle bundle = this.getArguments();
		if (bundle != null && bundle.containsKey(Constants.ArgumentKey.NAME)) {
			textView.setText(bundle.getString(Constants.ArgumentKey.NAME));
		}
		textView.addTextChangedListener(this);

		if (bundle != null && bundle.containsKey(Constants.ArgumentKey.NAME)) {
			builder.setTitle(this.getString(R.string.vibrationEditString));
		} else {
			builder.setTitle(this.getString(R.string.vibrationAddTitleString));
		}

		builder.setView(view);
		Dialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		return dialog;
	}

	public interface OnVibrationNameFragmentListener {
		void onVibrationNameOk(String name);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			TextView textView = (TextView) this.getDialog().findViewById(
					R.id.vibrationNameValue);
			String name = textView.getText().toString();
			this.listener.onVibrationNameOk(name);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		TextView textView = (TextView) this.getDialog().findViewById(
				R.id.vibrationNameValue);
		if (TextUtils.isEmpty(s)) {
			textView.setError(this
					.getString(R.string.vibrationNameTitleErrorString));
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