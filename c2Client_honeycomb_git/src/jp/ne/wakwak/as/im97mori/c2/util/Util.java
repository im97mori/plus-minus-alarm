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

package jp.ne.wakwak.as.im97mori.c2.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
import android.content.Context;

import com.google.api.CalendarClient;
import com.google.api.CalendarUrl;
import com.google.api.EventEntry;
import com.google.api.EventFeed;

public final class Util {
	private Util() {
	}

	public static Set<Date> calculate(Date start, Date end,
			List<AlarmTypeVo> target, Context context) {
		Set<Date> resultSet = new LinkedHashSet<Date>();
		Iterator<AlarmTypeVo> it = target.iterator();

		while (it.hasNext()) {
			AlarmTypeVo vo = it.next();
			int type = vo.getType();
			String typeValue = vo.getTypeValue();

			Set<Date> innerSet = new LinkedHashSet<Date>();
			if (type == Constants.AlarmType.TYPE_DAY_OF_WEEK) {
				innerSet.addAll(getDayOfWeekSet(start, end, typeValue));
			} else if (type == Constants.AlarmType.TYPE_DATE) {
				innerSet.addAll(getDateSet(start, end, typeValue));
			} else if (type == Constants.AlarmType.TYPE_BETWEEN) {
				innerSet.addAll(getBetweenSet(start, end, typeValue));
			} else if (type == Constants.AlarmType.TYPE_GOOGLE_CALENDAR) {
				try {
					innerSet.addAll(getGoogleCalendarSet(start, end, typeValue,
							context));
				} catch (IOException e) {
					e.printStackTrace();
					it.remove();
				}
			}

			if (vo.getSign() == Constants.AlarmTypeSign.SIGN_PLUS) {
				resultSet.addAll(innerSet);
			} else {
				resultSet.removeAll(innerSet);
			}
		}
		return resultSet;
	}

	private static Set<Date> getDayOfWeekSet(Date start, Date end,
			String typeValue) {
		Set<Date> set = new LinkedHashSet<Date>();

		String[] types = typeValue.split(",");
		List<Integer> dayOfWeekList = new ArrayList<Integer>(types.length);
		for (int i = 0; i < types.length; i++) {
			dayOfWeekList.add(Integer.parseInt(types[i]));
		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(start);
		while (calendar.getTimeInMillis() <= end.getTime()) {
			if (dayOfWeekList.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
				set.add(calendar.getTime());
			}
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		return set;
	}

	private static Set<Date> getDateSet(Date start, Date end, String typeValue) {
		Set<Date> set = new LinkedHashSet<Date>();

		String[] types = typeValue.split("/");
		Calendar calendar = getClearCalendarInstance();
		calendar.set(Integer.parseInt(types[0]), Integer.parseInt(types[1]),
				Integer.parseInt(types[2]));
		if (start.getTime() <= calendar.getTimeInMillis()
				&& end.getTime() >= calendar.getTimeInMillis()) {
			set.add(calendar.getTime());
		}

		return set;
	}

	private static Set<Date> getBetweenSet(Date start, Date end,
			String typeValue) {
		Set<Date> set = new LinkedHashSet<Date>();

		String[] types = typeValue.split(":");
		String[] startString = types[0].split("/");
		String[] endString = types[1].split("/");
		Calendar startCalendar = getClearCalendarInstance();
		startCalendar.set(Integer.parseInt(startString[0]),
				Integer.parseInt(startString[1]),
				Integer.parseInt(startString[2]));
		if (startCalendar.getTime().before(start)) {
			startCalendar.setTime(start);
		}
		Calendar endCalendar = getClearCalendarInstance();
		endCalendar.set(Integer.parseInt(endString[0]),
				Integer.parseInt(endString[1]), Integer.parseInt(endString[2]));
		if (endCalendar.getTime().after(end)) {
			endCalendar.setTime(end);
		}
		while (startCalendar.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
			set.add(startCalendar.getTime());
			startCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		return set;
	}

	private static Set<Date> getGoogleCalendarSet(Date start, Date end,
			String typeValue, Context context) throws IOException {
		Set<Date> set = new LinkedHashSet<Date>();

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(start);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Calendar calendar2 = getGMTCalendarInstance();
		calendar2.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
		calendar2.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
		calendar2.set(Calendar.DAY_OF_MONTH,
				calendar.get(Calendar.DAY_OF_MONTH));
		calendar2.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
		calendar2.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
		calendar2.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
		calendar2.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));
		Date gmtStart = calendar2.getTime();

		calendar.setTime(end);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar2.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
		calendar2.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
		calendar2.set(Calendar.DAY_OF_MONTH,
				calendar.get(Calendar.DAY_OF_MONTH));
		calendar2.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
		calendar2.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
		calendar2.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
		calendar2.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));
		Date gmtEnd = calendar2.getTime();

		String[] types = typeValue.split("\n");

		CalendarClient client = new CalendarClient(types[0], gmtStart, gmtEnd,
				context);

		CalendarUrl url = new CalendarUrl(types[2]);
		EventFeed eventFeed = null;
		eventFeed = client.executeGetEventFeed(url);
		if (eventFeed != null) {
			Iterator<EventEntry> eventIt = eventFeed.events.iterator();
			while (eventIt.hasNext()) {
				EventEntry entry = eventIt.next();

				Calendar startWhenCalendar = GregorianCalendar.getInstance();
				startWhenCalendar.setTimeInMillis(entry.when.startTime.value);
				startWhenCalendar.set(Calendar.HOUR_OF_DAY, 0);
				startWhenCalendar.set(Calendar.MINUTE, 0);
				startWhenCalendar.set(Calendar.SECOND, 0);
				startWhenCalendar.set(Calendar.MILLISECOND, 0);
				if (entry.when.endTime == null) {
					set.add(startWhenCalendar.getTime());
				} else {
					Calendar endWhenCalendar = GregorianCalendar.getInstance();
					endWhenCalendar.setTimeInMillis(entry.when.endTime.value);
					endWhenCalendar.set(Calendar.HOUR_OF_DAY, 0);
					endWhenCalendar.set(Calendar.MINUTE, 0);
					endWhenCalendar.set(Calendar.SECOND, 0);
					endWhenCalendar.set(Calendar.MILLISECOND, 0);

					if (entry.when.endTime.dateOnly) {
						endWhenCalendar.add(Calendar.DAY_OF_MONTH, -1);
					}

					while (startWhenCalendar.getTimeInMillis() <= endWhenCalendar
							.getTimeInMillis()) {
						set.add(startWhenCalendar.getTime());
						startWhenCalendar.add(Calendar.DAY_OF_MONTH, 1);
					}
				}
			}
		}

		return set;
	}

	public static Calendar getGMTCalendarInstance() {
		return GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

	public static Calendar getClearCalendarInstance() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
}