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
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlarmTypeAdapter extends ArrayAdapter<AlarmTypeVo> {

	private List<AlarmTypeVo> list = null;
	private Calendar calendar = GregorianCalendar.getInstance();
	private SimpleDateFormat formatter;

	public AlarmTypeAdapter(Context context, List<AlarmTypeVo> list) {
		super(context, 0, list);
		this.list = list;
		this.formatter = new SimpleDateFormat(this.getContext().getString(
				R.string.dayOfWeekFormat));
	}

	public List<AlarmTypeVo> getList() {
		return list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) convertView;
		if (layout == null) {
			layout = (LinearLayout) LayoutInflater.from(this.getContext())
					.inflate(R.layout.alarm_type_row, null);
		}
		AlarmTypeVo vo = this.getItem(position);

		TextView textView = (TextView) layout
				.findViewById(R.id.alarmTypeRowTypeText);
		if (vo.getType() == Constants.AlarmType.TYPE_DAY_OF_WEEK) {
			textView.setText(this.getContext()
					.getString(R.string.typeDayOfWeek));
			textView = (TextView) layout
					.findViewById(R.id.alarmTypeRowTypeValueText);
			String[] types = vo.getTypeValue().split(",");
			Arrays.sort(types);
			for (int i = 0; i < types.length; i++) {
				int dayOfWeek = Integer.parseInt(types[i]);
				this.calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
				types[i] = this.formatter.format(this.calendar.getTime());
			}
			textView.setText(Arrays.toString(types));
		} else if (vo.getType() == Constants.AlarmType.TYPE_DATE) {
			textView.setText(this.getContext().getString(R.string.typeDate));
			textView = (TextView) layout
					.findViewById(R.id.alarmTypeRowTypeValueText);
			String[] types = vo.getTypeValue().split("/");
			StringBuilder sb = new StringBuilder();
			sb.append(types[0]);
			sb.append("/");
			sb.append(Integer.parseInt(types[1]) + 1);
			sb.append("/");
			sb.append(types[2]);
			textView.setText(sb.toString());
		} else if (vo.getType() == Constants.AlarmType.TYPE_BETWEEN) {
			textView.setText(this.getContext().getString(R.string.typeBetween));
			textView = (TextView) layout
					.findViewById(R.id.alarmTypeRowTypeValueText);
			String[] types = vo.getTypeValue().split(":")[0].split("/");
			StringBuilder sb = new StringBuilder();
			sb.append(types[0]);
			sb.append("/");
			sb.append(Integer.parseInt(types[1]) + 1);
			sb.append("/");
			sb.append(types[2]);
			sb.append(" - ");
			types = vo.getTypeValue().split(":")[1].split("/");
			sb.append(types[0]);
			sb.append("/");
			sb.append(Integer.parseInt(types[1]) + 1);
			sb.append("/");
			sb.append(types[2]);
			textView.setText(sb.toString());
		} else if (vo.getType() == Constants.AlarmType.TYPE_GOOGLE_CALENDAR) {
			textView.setText(this.getContext().getString(R.string.typeCalendar));
			textView = (TextView) layout
					.findViewById(R.id.alarmTypeRowTypeValueText);
			textView.setGravity(Gravity.RIGHT);
			String[] typeValue = vo.getTypeValue().split("\n");
			textView.setText(typeValue[0] + "\n" + typeValue[1]);
		}

		textView = (TextView) layout.findViewById(R.id.alarmTypeRowNumberText);
		textView.setText(String.valueOf(position + 1));
		if (vo.getSign() == Constants.AlarmTypeSign.SIGN_PLUS) {
			textView.setBackgroundResource(R.color.type_plus);
		} else {
			textView.setBackgroundResource(R.color.type_minus);
		}

		return layout;
	}

	@Override
	public long getItemId(int position) {
		return this.getItem(position).getId();
	}
}
