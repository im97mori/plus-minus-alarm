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

package jp.ne.wakwak.as.im97mori.c2.fragment.rule;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

public class RuleBetweenFragment extends DialogFragment implements
		OnClickListener {

	private OnRuleListener listener;

	public static RuleBetweenFragment newInstance(int sign) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.SIGN, sign);
		RuleBetweenFragment fragment = new RuleBetweenFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public static RuleBetweenFragment newInstance(int position, String typeValue) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.POSITION, position);
		bundle.putString(Constants.ArgumentKey.TYPE_VALUE, typeValue);
		RuleBetweenFragment fragment = new RuleBetweenFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnRuleListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnRuleListener");
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
		builder.setTitle(this.getString(R.string.typeBetween));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);

		View view = LayoutInflater.from(this.getActivity()).inflate(
				R.layout.rule_between, null, false);

		Bundle bundle = this.getArguments();
		if (bundle.containsKey(Constants.ArgumentKey.TYPE_VALUE)) {
			String typeValue = bundle
					.getString(Constants.ArgumentKey.TYPE_VALUE);
			String[] types = typeValue.split(":");

			String[] subTypes = types[0].split("/");
			DatePicker picker = (DatePicker) view
					.findViewById(R.id.ruleBetweenStartPicker);
			picker.init(Integer.parseInt(subTypes[0]),
					Integer.parseInt(subTypes[1]),
					Integer.parseInt(subTypes[2]), null);

			subTypes = types[1].split("/");
			picker = (DatePicker) view.findViewById(R.id.ruleBetweenEndPicker);
			picker.init(Integer.parseInt(subTypes[0]),
					Integer.parseInt(subTypes[1]),
					Integer.parseInt(subTypes[2]), null);
		}

		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (this.listener != null) {
			if (which == DialogInterface.BUTTON_POSITIVE) {
				AlertDialog alertDialog = (AlertDialog) dialog;
				StringBuilder sb = new StringBuilder();
				DatePicker picker = (DatePicker) alertDialog
						.findViewById(R.id.ruleBetweenStartPicker);
				picker.clearFocus();
				sb.append(picker.getYear());
				sb.append("/");
				sb.append(picker.getMonth());
				sb.append("/");
				sb.append(picker.getDayOfMonth());
				sb.append(":");
				picker = (DatePicker) alertDialog
						.findViewById(R.id.ruleBetweenEndPicker);
				picker.clearFocus();
				sb.append(picker.getYear());
				sb.append("/");
				sb.append(picker.getMonth());
				sb.append("/");
				sb.append(picker.getDayOfMonth());
				Bundle bundle = this.getArguments();
				String typeValue = sb.toString();
				if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
					this.listener.onEditRuleComplete(
							bundle.getInt(Constants.ArgumentKey.POSITION),
							typeValue);
				} else {
					this.listener.onAddRule(Constants.AlarmType.TYPE_BETWEEN,
							typeValue,
							bundle.getInt(Constants.ArgumentKey.SIGN));
				}
			} else if (which == DialogInterface.BUTTON_NEGATIVE) {
				this.listener.onAddRuleCancel();
			}
		}
	}
}