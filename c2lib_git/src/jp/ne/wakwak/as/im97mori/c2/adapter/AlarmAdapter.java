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

package jp.ne.wakwak.as.im97mori.c2.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlarmAdapter extends ArrayAdapter<AlarmVo> {
	private SimpleDateFormat formatter;

	public AlarmAdapter(Context context, List<AlarmVo> list) {
		super(context, 0, list);
		this.formatter = new SimpleDateFormat(this.getContext().getString(
				R.string.alarmDateFormat));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) convertView;
		if (layout == null) {
			layout = (LinearLayout) LayoutInflater.from(this.getContext())
					.inflate(R.layout.alarm_row, null);
		}
		AlarmVo vo = this.getItem(position);
		TextView textView = (TextView) layout
				.findViewById(R.id.alarmRowNameText);
		textView.setText(vo.getName());
		textView = (TextView) layout.findViewById(R.id.alarmRowTimeText);
		textView.setText(vo.getTime());

		textView = (TextView) layout.findViewById(R.id.alarmRowNextText);
		if (vo.getNext() == null) {
			textView.setText(this.getContext().getString(R.string.alarmNoNext));
		} else {
			Date date = new Date(vo.getNext());
			textView.setText(formatter.format(date));
		}

		return layout;
	}

	@Override
	public long getItemId(int position) {
		return this.getItem(position).getId();
	}
}