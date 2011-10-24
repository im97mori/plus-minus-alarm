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

package jp.ne.wakwak.as.im97mori.c2.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jp.ne.wakwak.as.im97mori.c2.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class PreviewCell extends FrameLayout {

	public static Animation fadeInAnimation;
	public static Animation fadeOutAnimation;

	private SimpleDateFormat formatter;

	private Calendar currentMonth = GregorianCalendar.getInstance();
	private Calendar targetDate = GregorianCalendar.getInstance();
	private Map<Long, Set<Date>> map;

	private TextView dateTextView;
	private ImageView alarmImageView;

	public PreviewCell(Context context, AttributeSet attrs, int defStyle,
			Date month, Date date, Map<Long, Set<Date>> map) {
		super(context, attrs, defStyle);
		this.currentMonth.setTime(month);
		this.targetDate.setTime(date);
		this.map = map;
		this.initialize();
	}

	public PreviewCell(Context context, AttributeSet attrs, Date month,
			Date date, Map<Long, Set<Date>> map) {
		super(context, attrs);
		this.currentMonth.setTime(month);
		this.targetDate.setTime(date);
		this.map = map;
		this.initialize();
	}

	public PreviewCell(Context context, Date month, Date date,
			Map<Long, Set<Date>> map) {
		super(context);
		this.currentMonth.setTime(month);
		this.targetDate.setTime(date);
		this.map = map;
		this.initialize();
	}

	private void initialize() {
		this.formatter = new SimpleDateFormat(this.getContext().getString(
				R.string.previewDateFormat));

		this.setBackgroundResource(R.drawable.preview_cell);

		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		inflater.inflate(R.layout.preview_cell, this, true);

		dateTextView = (TextView) this.findViewById(R.id.previewDate);
		alarmImageView = (ImageView) this.findViewById(R.id.previewImage);

		this.update();
	}

	private void update() {
		dateTextView.setText(formatter.format(this.targetDate.getTime()));

		if (this.currentMonth.get(Calendar.YEAR) != this.targetDate
				.get(Calendar.YEAR)
				|| this.currentMonth.get(Calendar.MONTH) != this.targetDate
						.get(Calendar.MONTH)) {
			dateTextView.setTextColor(Color.GRAY);
			dateTextView.setTypeface(Typeface.DEFAULT);
		} else if (this.targetDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			dateTextView.setTextColor(Color.BLUE);
			dateTextView.setTypeface(Typeface.DEFAULT_BOLD);
		} else if (this.targetDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			dateTextView.setTextColor(Color.RED);
			dateTextView.setTypeface(Typeface.DEFAULT_BOLD);
		} else {
			dateTextView.setTextColor(Color.WHITE);
			dateTextView.setTypeface(Typeface.DEFAULT_BOLD);
		}
		Set<Date> dateSet = new HashSet<Date>();
		Iterator<Set<Date>> it = this.map.values().iterator();
		while (it.hasNext()) {
			dateSet.addAll(it.next());
		}
		if (dateSet.contains(this.targetDate.getTime())) {
			if (this.alarmImageView.getVisibility() == View.INVISIBLE) {
				this.alarmImageView.setVisibility(View.VISIBLE);
				this.alarmImageView.startAnimation(fadeInAnimation);
			}
		} else {
			if (this.alarmImageView.getVisibility() == View.VISIBLE) {
				this.alarmImageView.setVisibility(View.INVISIBLE);
				this.alarmImageView.startAnimation(fadeOutAnimation);
			}
		}
	}

	public void setData(Date month, Date date, Map<Long, Set<Date>> map) {
		this.currentMonth.setTime(month);
		this.targetDate.setTime(date);
		this.map = map;
		this.update();
	}

	public void setDate(Map<Long, Set<Date>> map) {
		this.setData(this.currentMonth.getTime(), this.targetDate.getTime(),
				map);
	}

	public Date getTargetDate() {
		return targetDate.getTime();
	}

	public Map<Long, Set<Date>> getMap() {
		return map;
	}
}