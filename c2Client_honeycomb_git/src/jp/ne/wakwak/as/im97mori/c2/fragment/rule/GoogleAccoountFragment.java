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

import java.util.ArrayList;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.GoogleAccountVo;
import android.accounts.Account;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

public class GoogleAccoountFragment extends DialogFragment implements
		OnItemClickListener {

	private OnGoogleAccountFragmentListener listener;

	public static GoogleAccoountFragment newInstance(int sign) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.SIGN, sign);
		GoogleAccoountFragment fragment = new GoogleAccoountFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public static GoogleAccoountFragment newInstance(int position,
			String typeValue) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.POSITION, position);
		bundle.putString(Constants.ArgumentKey.TYPE_VALUE, typeValue);
		GoogleAccoountFragment fragment = new GoogleAccoountFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnGoogleAccountFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnGoogleAccountFragmentListener");
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
		View view = inflater.inflate(R.layout.google_account, container, false);

		Bundle bundle = this.getArguments();
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setOnItemClickListener(this);

		GoogleAccountManager accountManager = new GoogleAccountManager(
				this.getActivity());
		Account[] accounts = accountManager.getAccounts();
		List<GoogleAccountVo> list = new ArrayList<GoogleAccountVo>(
				accounts.length);

		for (int i = 0; i < accounts.length; i++) {
			Account account = accounts[i];
			GoogleAccountVo vo = new GoogleAccountVo();
			vo.setName(account.name);
			list.add(vo);
		}

		GoogleAccountAdapter adapter = new GoogleAccountAdapter(list);
		listView.setAdapter(adapter);

		if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			String typeValue = bundle
					.getString(Constants.ArgumentKey.TYPE_VALUE);
			typeValue = typeValue.split("\n")[0];
			for (int i = 0; i < list.size(); i++) {
				GoogleAccountVo vo = list.get(i);
				if (vo.getName().equals(typeValue)) {
					listView.setItemChecked(i, true);
					break;
				}
			}
		}

		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(this.getString(R.string.typeGoogleAccount));

		return dialog;
	}

	public interface OnGoogleAccountFragmentListener {
		void onGoogleAccountSelected(String typeValue, int sign);

		void onGoogleAccountCancel();

		void onGoogleAccountReSelected(int position, String account,
				String calendarName);
	}

	private class GoogleAccountAdapter extends ArrayAdapter<GoogleAccountVo> {
		private int layout;

		public GoogleAccountAdapter(List<GoogleAccountVo> list) {
			super(GoogleAccoountFragment.this.getActivity(), 0, list);
			Bundle bundle = getArguments();
			if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
				layout = android.R.layout.select_dialog_singlechoice;
			} else {
				layout = android.R.layout.select_dialog_item;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = (TextView) convertView;
			if (textView == null) {
				textView = (TextView) LayoutInflater.from(this.getContext())
						.inflate(layout, null);
			}
			GoogleAccountVo vo = this.getItem(position);
			textView.setText(vo.getName());
			return textView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
			int paramInt, long paramLong) {
		if (paramInt != AdapterView.INVALID_POSITION) {
			GoogleAccountVo vo = (GoogleAccountVo) paramAdapterView
					.getItemAtPosition(paramInt);
			Bundle bundle = this.getArguments();
			if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
				this.listener.onGoogleAccountReSelected(bundle
						.getInt(Constants.ArgumentKey.POSITION), vo.getName(),
						bundle.getString(Constants.ArgumentKey.TYPE_VALUE)
								.split("\n")[1]);
			} else {
				this.listener.onGoogleAccountSelected(vo.getName(),
						bundle.getInt(Constants.ArgumentKey.SIGN));
			}
		}
	}
}