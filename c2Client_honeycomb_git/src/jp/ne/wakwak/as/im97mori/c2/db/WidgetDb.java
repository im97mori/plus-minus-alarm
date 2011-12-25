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

package jp.ne.wakwak.as.im97mori.c2.db;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import jp.ne.wakwak.as.im97mori.c2.vo.WidgetVo;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class WidgetDb extends SQLiteOpenHelper {
	private static final Object LOCK = new Object();

	public WidgetDb(Context context) {
		this(context, null, 1);
	}

	public WidgetDb(Context context, CursorFactory factory, int version) {
		super(context, WidgetDb.class.getName(), factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS WIDGET (_id INTEGER PRIMARY KEY ASC, WIDGET_ID INTEGER NOT NULL, ALARM_ID INTEGER NOT NULL, NAME TEXT NOT NULL, NEXT INTEGER, IN_CALCULATE INTEGER NOT NULL DEFAULT 0, TIME TEXT NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public List<Long> getAllAlarmList() {
		List<Long> list = new LinkedList<Long>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (LOCK) {
			try {
				db = this.getReadableDatabase();
				cursor = db.rawQuery("SELECT DISTINCT ALARM_ID FROM WIDGET",
						null);
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						list.add(cursor.getLong(cursor
								.getColumnIndex(WidgetColumns.ALARM_ID)));
						cursor.moveToNext();
					}
				}
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
		return list;
	}

	public List<WidgetVo> getAlarmList(int widgetId) {
		List<WidgetVo> list = new LinkedList<WidgetVo>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (LOCK) {
			try {
				db = this.getReadableDatabase();
				cursor = db
						.rawQuery(
								"SELECT _id, WIDGET_ID, ALARM_ID, NAME, NEXT, IN_CALCULATE, TIME, ifnull(NEXT, ?) ORDER_NEXT FROM WIDGET WHERE WIDGET_ID = ? ORDER BY ORDER_NEXT, _id",
								new String[] { String.valueOf(Long.MIN_VALUE),
										String.valueOf(widgetId) });
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						WidgetVo vo = new WidgetVo();
						vo.setId(cursor.getLong(cursor
								.getColumnIndex(WidgetColumns._ID)));
						vo.setWidgetId(cursor.getInt(cursor
								.getColumnIndex(WidgetColumns.WIDGET_ID)));
						vo.setAlarmId(cursor.getLong(cursor
								.getColumnIndex(WidgetColumns.ALARM_ID)));
						vo.setName(cursor.getString(cursor
								.getColumnIndex(WidgetColumns.NAME)));
						if (!cursor.isNull(cursor
								.getColumnIndex(WidgetColumns.NEXT))) {
							vo.setNext(cursor.getLong(cursor
									.getColumnIndex(WidgetColumns.NEXT)));
						}
						vo.setInCalculate(cursor.getInt(cursor
								.getColumnIndex(WidgetColumns.IN_CALCULATE)));
						vo.setTime(cursor.getString(cursor
								.getColumnIndex(WidgetColumns.TIME)));
						list.add(vo);

						cursor.moveToNext();
					}
				}
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
		return list;
	}

	public void createWidgetList(int widgetId, List<WidgetVo> list) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("DELETE FROM WIDGET WHERE WIDGET_ID = ?");
				statement.bindLong(1, widgetId);
				statement.executeUpdateDelete();
				statement.close();

				statement = db
						.compileStatement("INSERT INTO WIDGET(WIDGET_ID, ALARM_ID, NAME, NEXT, TIME) VALUES(?,?,?,?,?)");
				int index = 0;
				Iterator<WidgetVo> it = list.iterator();
				while (it.hasNext()) {
					WidgetVo vo = it.next();

					index = 0;
					statement.clearBindings();

					statement.bindLong(++index, widgetId);
					statement.bindLong(++index, vo.getAlarmId());
					statement.bindString(++index, vo.getName());
					if (vo.getNext() == null) {
						statement.bindNull(++index);
					} else {
						statement.bindLong(++index, vo.getNext());
					}
					statement.bindString(++index, vo.getTime());
					statement.executeInsert();
				}

				db.setTransactionSuccessful();
			} finally {
				if (db != null && db.isOpen()) {
					db.endTransaction();
					db.close();
				}
			}
		}
	}

	public void deleteWidget(int[] widgetIds) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("DELETE FROM WIDGET WHERE WIDGET_ID = ?");
				for (int i = 0; i < widgetIds.length; i++) {
					statement.clearBindings();

					statement.bindLong(1, widgetIds[i]);
					statement.executeUpdateDelete();
				}

				db.setTransactionSuccessful();
			} finally {
				if (db != null && db.isOpen()) {
					db.endTransaction();
					db.close();
				}
			}
		}
	}

	public void inCalculate(long alarmId) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("UPDATE WIDGET SET IN_CALCULATE = 1 WHERE ALARM_ID = ?");
				statement.bindLong(1, alarmId);
				statement.executeUpdateDelete();

				db.setTransactionSuccessful();
			} finally {
				if (db != null && db.isOpen()) {
					db.endTransaction();
					db.close();
				}
			}
		}
	}

	public void finishCalculate(long alarmId, Long next) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("UPDATE WIDGET SET IN_CALCULATE = 0, NEXT = ? WHERE ALARM_ID = ?");
				if (next == null) {
					statement.bindNull(1);
				} else {
					statement.bindLong(1, next);
				}
				statement.bindLong(2, alarmId);
				statement.executeUpdateDelete();

				db.setTransactionSuccessful();
			} finally {
				if (db != null && db.isOpen()) {
					db.endTransaction();
					db.close();
				}
			}
		}
	}

	public void deleteAlarm(long alarmId) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("DELETE FROM WIDGET WHERE ALARM_ID = ?");
				statement.bindLong(1, alarmId);
				statement.executeUpdateDelete();

				db.setTransactionSuccessful();
			} finally {
				if (db != null && db.isOpen()) {
					db.endTransaction();
					db.close();
				}
			}
		}
	}

	public void updateWidget(AlarmVo vo) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("UPDATE WIDGET SET NAME = ?, NEXT = ?, TIME = ? WHERE ALARM_ID = ?");
				int index = 0;
				statement.bindString(++index, vo.getName());
				if (vo.getNext() == null) {
					statement.bindNull(++index);
				} else {
					statement.bindLong(++index, vo.getNext());
				}
				statement.bindString(++index, vo.getTime());
				statement.bindLong(++index, vo.getId());
				statement.executeUpdateDelete();

				db.setTransactionSuccessful();
			} finally {
				if (db != null && db.isOpen()) {
					db.endTransaction();
					db.close();
				}
			}
		}
	}
}