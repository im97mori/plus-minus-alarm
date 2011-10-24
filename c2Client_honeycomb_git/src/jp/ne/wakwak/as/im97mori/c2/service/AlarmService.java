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

package jp.ne.wakwak.as.im97mori.c2.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.db.WidgetDb;
import jp.ne.wakwak.as.im97mori.c2.receiver.AlarmReceiver;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.util.Util;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;

public class AlarmService extends IntentService {

	public static final String SETTING = "setting";
	private static final List<Integer> addList;
	static {
		List<Integer> list = new ArrayList<Integer>(4);
		list.add(Calendar.DAY_OF_MONTH);
		list.add(Calendar.WEEK_OF_YEAR);
		list.add(Calendar.MONTH);
		list.add(Calendar.YEAR);
		addList = Collections.unmodifiableList(list);
	}

	public AlarmService() {
		super(AlarmService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		wakeLock.acquire();

		long id = intent.getLongExtra(Constants.IntentKey.ID,
				Constants.TEMPOLARY_ID);
		int setting = intent.getIntExtra(SETTING, -1);
		if (Constants.AlarmCommand.EDIT_ALARM == setting) {
			this.editAlarm(id);
		} else if (Constants.AlarmCommand.NEXT == setting) {
			this.toNext(id);
		} else if (Constants.AlarmCommand.RESET == setting) {
			this.resetAllAlarm();
		} else if (Constants.AlarmCommand.SKIP_ONCE == setting) {
			long next = intent.getLongExtra(Constants.IntentKey.NEXT, 0);
			this.skipOnceAlarm(id, next);
		} else if (Constants.AlarmCommand.CALCULATE == setting) {
			this.calculateAllAlarm();
		}

		this.updateWidget();
		intent = new Intent(Constants.Action.FINISH_CALCULATE);
		this.sendBroadcast(intent);

		wakeLock.release();
	}

	private void updateWidget() {
		AlarmDb alarmDb = new AlarmDb(this);
		WidgetDb widgetDb = new WidgetDb(this);
		List<Long> list = widgetDb.getAllAlarmList();
		Iterator<Long> it = list.iterator();
		AlarmVo vo = null;
		while (it.hasNext()) {
			long id = it.next();
			vo = alarmDb.getAlarm(id);
			if (vo == null) {
				widgetDb.deleteAlarm(id);
			} else {
				widgetDb.updateWidget(vo);
				widgetDb.finishCalculate(vo.getId(), vo.getNext());
			}
		}
		widgetDb.close();
		alarmDb.close();

		Intent intent = new Intent(getApplicationContext(), WidgetService.class);
		intent.putExtra(Constants.IntentKey.WIDGET_COMMAND,
				Constants.WidgetCommand.UPDATE_ALL);
		this.startService(intent);
	}

	private void editAlarm(long id) {
		AlarmDb db = new AlarmDb(this);
		AlarmVo alarmVo = db.getAlarm(id);
		db.close();
		if (alarmVo != null) {
			this.toNext(alarmVo);
		}
		this.updateAlarmSetting(id, alarmVo);
	}

	private void toNext(AlarmVo alarmVo) {
		if (alarmVo.getEnable() == Constants.AlarmEnable.ALARM_ENABLED) {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			int now = calendar.get(Calendar.HOUR_OF_DAY) * 60;
			now += calendar.get(Calendar.MINUTE);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			String[] times = alarmVo.getTime().split(":");
			int offset = Integer.parseInt(times[0]) * 60;
			offset += Integer.parseInt(times[1]);
			offset += 1;
			if (now >= offset) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			} else if (alarmVo.getNext() != null
					&& System.currentTimeMillis() >= alarmVo.getNext()) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}

			AlarmDb db = new AlarmDb(this);
			List<AlarmTypeVo> typeList = db.getAlarmTypeList(alarmVo.getId());
			db.close();
			Set<Date> dateSet = new HashSet<Date>();
			boolean connectionSuccess = true;
			for (int i = 0; i < addList.size(); i++) {
				Calendar innerCalendar = (Calendar) calendar.clone();
				innerCalendar.add(addList.get(i), 10);
				List<AlarmTypeVo> innerList = new ArrayList<AlarmTypeVo>(
						typeList.size());
				innerList.addAll(typeList);
				dateSet = Util.calculate(calendar.getTime(),
						innerCalendar.getTime(), innerList, this);
				if (!innerList.containsAll(typeList)) {
					connectionSuccess = false;
					break;
				}
				if (dateSet.size() > 0) {
					break;
				}
			}
			if (!connectionSuccess) {
				alarmVo.setNext(null);
				alarmVo.setNeedCaluculate(Constants.NeedCalculate.NEED_CALCULATE);
			} else if (dateSet.size() == 0) {
				alarmVo.setNext(null);
				alarmVo.setNeedCaluculate(Constants.NeedCalculate.CALCULATED);
			} else {
				Date[] dates = new Date[dateSet.size()];
				dateSet.toArray(dates);
				Arrays.sort(dates);
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(dates[0]);
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[0]));
				calendar.set(Calendar.MINUTE, Integer.parseInt(times[1]));
				alarmVo.setNext(calendar.getTimeInMillis());
				alarmVo.setNeedCaluculate(Constants.NeedCalculate.CALCULATED);
			}
		} else {
			alarmVo.setNext(null);
			alarmVo.setNeedCaluculate(Constants.NeedCalculate.CALCULATED);
		}
		AlarmDb db = new AlarmDb(this);
		db.updateAlarm(alarmVo.getId(), alarmVo.getName(), alarmVo.getTime(),
				alarmVo.getNext(), alarmVo.getEnable());
		db.setNeedCalculate(alarmVo.getId(), alarmVo.getNeedCaluculate());
		db.close();
	}

	private void updateAlarmSetting(long id, AlarmVo vo) {
		AlarmManager am = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this.getApplicationContext(),
				AlarmReceiver.class);
		intent.setAction(Constants.Action.ALARM);
		Uri uri = Uri.parse("c2://c2.im97mori.as.wakwak.ne.jp/?ID=" + id);
		intent.setData(uri);
		if (vo == null || vo.getNext() == null
				|| vo.getEnable() == Constants.AlarmEnable.ALARM_DISABLED) {
			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					this.getApplicationContext(), 0, intent,
					PendingIntent.FLAG_NO_CREATE);
			if (pendingIntent != null) {
				am.cancel(pendingIntent);
			}
		} else {
			long next = vo.getNext();
			long diff = next - System.currentTimeMillis();
			if (diff < 120000) {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						this.getApplicationContext(), 0, intent,
						PendingIntent.FLAG_NO_CREATE);
				if (pendingIntent != null) {
					am.cancel(pendingIntent);
				}
				diff = next - System.currentTimeMillis();
				while (System.currentTimeMillis() < next) {
					try {
						Thread.sleep(diff);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					diff = next - System.currentTimeMillis();
				}
				this.sendBroadcast(intent);
			} else {
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						this.getApplicationContext(), 0, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
				Date date = new Date(vo.getNext());
				am.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
			}
		}
	}

	private void skipOnceAlarm(long id, long next) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTimeInMillis(next);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		StringBuilder sb = new StringBuilder();
		sb.append(calendar.get(Calendar.YEAR));
		sb.append("/");
		sb.append(calendar.get(Calendar.MONTH));
		sb.append("/");
		sb.append(calendar.get(Calendar.DAY_OF_MONTH));

		AlarmDb db = new AlarmDb(this);
		List<AlarmTypeVo> list = db.getAlarmTypeList(id);
		AlarmTypeVo vo = new AlarmTypeVo();
		vo.setAlarmId(id);
		vo.setSign(Constants.AlarmTypeSign.SIGN_MINUS);
		vo.setType(Constants.AlarmType.TYPE_DATE);
		vo.setTypeValue(sb.toString());
		list.add(vo);
		db.updateAlarmType(id, list);

		AlarmVo alarmVo = db.getAlarm(id);
		db.close();
		this.toNext(alarmVo);
		db = new AlarmDb(this);
		alarmVo = db.getAlarm(id);
		db.close();
		this.updateAlarmSetting(id, alarmVo);
	}

	private void resetAllAlarm() {
		AlarmDb db = new AlarmDb(this);
		List<AlarmVo> list = db.getAlarmList();
		db.close();
		Iterator<AlarmVo> it = list.iterator();
		while (it.hasNext()) {
			this.editAlarm(it.next().getId());
		}
	}

	private void toNext(long id) {
		AlarmDb db = new AlarmDb(this);
		AlarmVo alarmVo = db.getAlarm(id);
		db.close();
		this.toNext(alarmVo);
		db = new AlarmDb(this);
		alarmVo = db.getAlarm(id);
		db.close();
		this.updateAlarmSetting(id, alarmVo);
	}

	private void calculateAllAlarm() {
		AlarmDb db = new AlarmDb(this);
		List<AlarmVo> list = db.getAlarmList();
		db.close();
		Iterator<AlarmVo> it = list.iterator();
		while (it.hasNext()) {
			AlarmVo vo = it.next();
			if (vo.getNeedCaluculate() == Constants.NeedCalculate.NEED_CALCULATE) {
				this.editAlarm(vo.getId());
			}
		}
	}
}