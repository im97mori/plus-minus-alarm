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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class TargetDateFragment extends DialogFragment implements
		OnClickListener {

	private OnTargetDateFragmentListener listener;

	public static TargetDateFragment newInstance(int sign, Date date) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.SIGN, sign);

		bundle.putSerializable(Constants.ArgumentKey.DATE, date);
		TargetDateFragment fragment = new TargetDateFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnTargetDateFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnTargetDateFragmentListener");
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
		Bundle bundle = this.getArguments();
		int resId;
		if (Constants.AlarmTypeSign.SIGN_PLUS == bundle
				.getInt(Constants.ArgumentKey.SIGN)) {
			resId = R.string.targetDatePlusString;
		} else {
			resId = R.string.targetDateMinusString;
		}
		Date date = (Date) bundle.getSerializable(Constants.ArgumentKey.DATE);
		SimpleDateFormat formatter = new SimpleDateFormat(getActivity()
				.getString(R.string.targetDateFormat));
		builder.setTitle(formatter.format(date));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
		builder.setMessage(resId);

		View view = LayoutInflater.from(this.getActivity()).inflate(
				R.layout.target_date, null, false);
		builder.setView(view);

		return builder.create();
	}

	public interface OnTargetDateFragmentListener {
		void onTargetDateOk(String typeValue, int sign);

		void onTargetDateCancel();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (this.listener != null) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				StringBuilder sb = new StringBuilder();
				Calendar calendar = GregorianCalendar.getInstance();
				Bundle bundle = this.getArguments();
				Date date = (Date) bundle.get(Constants.ArgumentKey.DATE);
				calendar.setTime(date);
				sb.append(calendar.get(Calendar.YEAR));
				sb.append("/");
				sb.append(calendar.get(Calendar.MONTH));
				sb.append("/");
				sb.append(calendar.get(Calendar.DAY_OF_MONTH));
				this.listener.onTargetDateOk(sb.toString(),
						bundle.getInt(Constants.ArgumentKey.SIGN));
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
				this.listener.onTargetDateCancel();
			}
		}
	}
}