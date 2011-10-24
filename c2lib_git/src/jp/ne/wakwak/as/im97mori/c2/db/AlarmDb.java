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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingVo;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmTypeVo;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import jp.ne.wakwak.as.im97mori.c2.vo.VibrationVo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class AlarmDb extends SQLiteOpenHelper {
	private static final Object LOCK = new Object();
	private Context context;

	public AlarmDb(Context context) {
		this(context, null, 3);
	}

	public AlarmDb(Context context, CursorFactory factory, int version) {
		super(context, AlarmDb.class.getName(), factory, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS ALARM (_id INTEGER PRIMARY KEY ASC, NAME TEXT NOT NULL, TIME TEXT NOT NULL, NEXT INTEGER, ENABLE INTEGER DEFAULT 0, NEED_CALCULATE INTEGER DEFAULT 0)");
		db.execSQL("CREATE TABLE IF NOT EXISTS ALARM_TYPE (_id INTEGER PRIMARY KEY ASC, TYPE INTEGER NOT NULL, TYPE_VALUE TEXT, SIGN INTEGER NOT NULL, ALARM_ID INTEGER NOT NULL, FOREIGN KEY (ALARM_ID) REFERENCES ALARM(_id) ON DELETE CASCADE)");
		db.execSQL("CREATE TABLE IF NOT EXISTS ALARM_SETTING (_id, INTEGER PRIMARY KEY ASC, SETTING_TYPE INTEGER NOT NULL, TYPE_VALUE TEXT, ALARM_ID INTEGER NOT NULL, FOREIGN KEY (ALARM_ID) REFERENCES ALARM(_id) ON DELETE CASCADE)");
		db.execSQL("CREATE TABLE IF NOT EXISTS GOOGLE_ACCOUNT (_id INTEGER PRIMARY KEY ASC, ACCOUNT TEXT NOT NULL, TOKEN TEXT NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL("ALTER TABLE ALARM ADD NEED_CALCULATE INTEGER DEFAULT 0");
			db.execSQL("CREATE TABLE IF NOT EXISTS GOOGLE_ACCOUNT (_id INTEGER PRIMARY KEY ASC, ACCOUNT TEXT NOT NULL, TOKEN TEXT NOT NULL)");
			SharedPreferences sp = context
					.getSharedPreferences(
							"jp.ne.wakwak.as.im97mori.c2.fragment.rule.RuleCalendarFragment",
							Context.MODE_PRIVATE);
			Map<String, ?> map = sp.getAll();
			Iterator<?> it = map.entrySet().iterator();
			Editor editor = sp.edit();
			SQLiteStatement statement = db
					.compileStatement("INSERT INTO GOOGLE_ACCOUNT(ACCOUNT, TOKEN) VALUES(?,?)");
			int index;
			while (it.hasNext()) {
				@SuppressWarnings("unchecked")
				Map.Entry<String, ?> entry = (Entry<String, ?>) it.next();

				index = 0;
				statement.bindString(++index, entry.getKey());
				statement.bindString(++index, (String) entry.getValue());
				statement.executeInsert();
				editor.remove(entry.getKey());
			}
			editor.commit();
		}
		if (oldVersion < 3 && newVersion == 3) {
			db.execSQL("CREATE TABLE IF NOT EXISTS VIBRATION (_id INTEGER PRIMARY KEY ASC, NAME TEXT NOT NULL)");
			db.execSQL("CREATE TABLE IF NOT EXISTS VIBRATION_PATTERN (_id INTEGER PRIMARY KEY ASC, INDEX INTEGER NOT NULL, DURATION INTEGER NOT NULL, VIBRATION_ID INTEGER NOT NULL FORIGN KEY (VIBRATION_ID) REFERENCES VIBRATION(_id) ON DELETE CASCADE)");
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	public void setAlarmSetting(long id, int settingType, String typeValue) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("DELETE FROM ALARM_SETTING WHERE ALARM_ID = ? AND SETTING_TYPE = ?");
				int index = 0;
				statement.bindLong(++index, id);
				statement.bindLong(++index, settingType);
				statement.execute();
				statement.close();

				index = 0;
				statement = db
						.compileStatement("INSERT INTO ALARM_SETTING(ALARM_ID, SETTING_TYPE, TYPE_VALUE) VALUES(?, ?, ?)");
				statement.bindLong(++index, id);
				statement.bindLong(++index, settingType);
				statement.bindString(++index, typeValue);
				statement.executeInsert();

				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
	}

	public void deleteAlarmSetting(long id, int settingType) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("DELETE FROM ALARM_SETTING WHERE ALARM_ID = ? AND SETTING_TYPE = ?");
				int index = 0;
				statement.bindLong(++index, id);
				statement.bindLong(++index, settingType);
				statement.execute();

				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
	}

	public AlarmSettingVo getAlarmSetting(long id, int settingType) {
		AlarmSettingVo vo = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		synchronized (LOCK) {
			try {
				db = this.getReadableDatabase();
				cursor = db
						.rawQuery(
								"SELECT * FROM ALARM_SETTING WHERE ALARM_ID = ? AND SETTING_TYPE = ?",
								new String[] { String.valueOf(id),
										String.valueOf(settingType) });
				if (cursor.getCount() == 1) {
					cursor.moveToFirst();
					vo = new AlarmSettingVo();
					vo.setId(cursor.getLong(cursor
							.getColumnIndex(AlarmSettingColumns._ID)));
					vo.setAlarmId(cursor.getLong(cursor
							.getColumnIndex(AlarmSettingColumns.ALARM_ID)));
					vo.setSettingType(settingType);
					vo.setTypeValue(cursor.getString(cursor
							.getColumnIndex(AlarmSettingColumns.TYPE_VALUE)));
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
		return vo;
	}

	public long addAlarm(String name, String time, Date next, int enable) {
		long id = -1;
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("INSERT INTO ALARM(NAME, TIME, NEXT, ENABLE) VALUES(?, ?, ?, ?)");
				int index = 0;
				statement.bindString(++index, name);
				statement.bindString(++index, time);
				if (next == null) {
					statement.bindNull(++index);
				} else {
					statement.bindLong(++index, next.getTime());
				}
				statement.bindLong(++index, enable);
				id = statement.executeInsert();
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
		return id;
	}

	public void deleteAlarm(long id) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("DELETE FROM ALARM WHERE _id = ?");
				statement.bindLong(1, id);
				statement.execute();
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
	}

	public void updateAlarm(long id, String name, String time, Long next,
			int enable) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("UPDATE ALARM SET NAME = ?, TIME = ?, NEXT = ?, ENABLE = ? WHERE _id = ?");
				int index = 0;
				statement.bindString(++index, name);
				statement.bindString(++index, time);
				if (next == null) {
					statement.bindNull(++index);
				} else {
					statement.bindLong(++index, next);
				}
				statement.bindLong(++index, enable);
				statement.bindLong(++index, id);
				statement.execute();
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
	}

	public void updateAlarmType(long id, List<AlarmTypeVo> list) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				db.beginTransaction();

				SQLiteStatement statement = db
						.compileStatement("DELETE FROM ALARM_TYPE WHERE ALARM_ID = ?");
				int index = 0;
				statement.bindLong(++index, id);
				statement.execute();
				statement.close();

				statement = db
						.compileStatement("INSERT INTO ALARM_TYPE(TYPE, TYPE_VALUE, SIGN, ALARM_ID) VALUES(?, ?, ?, ?)");
				Iterator<AlarmTypeVo> it = list.iterator();
				while (it.hasNext()) {
					AlarmTypeVo vo = it.next();
					statement.clearBindings();
					index = 0;

					statement.bindLong(++index, vo.getType());
					statement.bindString(++index, vo.getTypeValue());
					statement.bindLong(++index, vo.getSign());
					statement.bindLong(++index, vo.getAlarmId());
					statement.executeInsert();
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
	}

	public List<AlarmVo> getAlarmList() {
		List<AlarmVo> list = null;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getReadableDatabase();
				cursor = db.rawQuery("SELECT * FROM ALARM ORDER BY _id", null);
				list = new ArrayList<AlarmVo>(cursor.getCount());
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					AlarmVo vo = new AlarmVo();
					vo.setId(cursor.getLong(cursor
							.getColumnIndex(AlarmColumns._ID)));
					vo.setName(cursor.getString(cursor
							.getColumnIndex(AlarmColumns.NAME)));
					vo.setTime(cursor.getString(cursor
							.getColumnIndex(AlarmColumns.TIME)));
					if (!cursor
							.isNull(cursor.getColumnIndex(AlarmColumns.NEXT))) {
						vo.setNext(cursor.getLong(cursor
								.getColumnIndex(AlarmColumns.NEXT)));
					}
					vo.setEnable(cursor.getInt(cursor
							.getColumnIndex(AlarmColumns.ENABLE)));
					vo.setNeedCaluculate(cursor.getInt(cursor
							.getColumnIndex(AlarmColumns.NEED_CALCULATE)));
					list.add(vo);
					cursor.moveToNext();
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

	public List<AlarmTypeVo> getAlarmTypeList(long id) {
		List<AlarmTypeVo> list = null;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getReadableDatabase();
				cursor = db
						.rawQuery(
								"SELECT * FROM ALARM_TYPE WHERE ALARM_ID = ? ORDER BY _id",
								new String[] { String.valueOf(id) });
				list = new ArrayList<AlarmTypeVo>(cursor.getCount());
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					AlarmTypeVo vo = new AlarmTypeVo();
					vo.setId(cursor.getLong(cursor
							.getColumnIndex(AlarmColumns._ID)));
					vo.setType(cursor.getInt(cursor
							.getColumnIndex(AlarmTypeColumns.TYPE)));
					vo.setTypeValue(cursor.getString(cursor
							.getColumnIndex(AlarmTypeColumns.TYPE_VALUE)));
					vo.setSign(cursor.getInt(cursor
							.getColumnIndex(AlarmTypeColumns.SIGN)));
					vo.setAlarmId(id);
					list.add(vo);
					cursor.moveToNext();
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

	public AlarmVo getAlarm(long id) {
		AlarmVo vo = null;
		Cursor cursor = null;
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getReadableDatabase();
				cursor = db.rawQuery("SELECT * FROM ALARM WHERE _id = ?",
						new String[] { String.valueOf(id) });
				if (cursor.getCount() == 1) {
					cursor.moveToFirst();

					vo = new AlarmVo();
					vo.setId(id);
					vo.setName(cursor.getString(cursor
							.getColumnIndex(AlarmColumns.NAME)));
					vo.setTime(cursor.getString(cursor
							.getColumnIndex(AlarmColumns.TIME)));
					if (!cursor
							.isNull(cursor.getColumnIndex(AlarmColumns.NEXT))) {
						vo.setNext(cursor.getLong(cursor
								.getColumnIndex(AlarmColumns.NEXT)));
					}
					vo.setEnable(cursor.getInt(cursor
							.getColumnIndex(AlarmColumns.ENABLE)));
					vo.setNeedCaluculate(cursor.getInt(cursor
							.getColumnIndex(AlarmColumns.NEED_CALCULATE)));
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
		return vo;
	}

	public void disableAlarm(long id) {
		SQLiteDatabase db = null;
		synchronized (LOCK) {
			try {
				db = this.getWritableDatabase();
				SQLiteStatement statement = db
						.compileStatement("UPDATE ALARM SET NEXT = NULL, ENABLE = 1 WHERE _id = ?");

				statement.bindLong(1, id);
				statement.execute();

				statement.close();
			} finally {
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}
	}

	public String getToken(String account) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		String token = "";
		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery(
					"SELECT * FROM GOOGLE_ACCOUNT WHERE ACCOUNT = ?",
					new String[] { account });

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				token = cursor.getString(cursor
						.getColumnIndex(GoogleAccountColumns.TOKEN));
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		return token;
	}

	public void updateToken(String account, String token) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getWritableDatabase();
			db.beginTransaction();

			cursor = db.rawQuery(
					"SELECT * FROM GOOGLE_ACCOUNT WHERE ACCOUNT = ?",
					new String[] { account });

			SQLiteStatement statement = null;
			int index = 0;
			if (cursor.getCount() > 0) {
				cursor.close();
				statement = db
						.compileStatement("UPDATE GOOGLE_ACCOUNT SET TOKEN = ? WHERE ACCOUNT = ?");
				statement.bindString(++index, token);
				statement.bindString(++index, account);
				statement.execute();
			} else {
				cursor.close();
				statement = db
						.compileStatement("INSERT INTO GOOGLE_ACCOUNT(TOKEN, ACCOUNT) VALUES(?,?)");
				statement.bindString(++index, token);
				statement.bindString(++index, account);
				statement.executeInsert();
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void deleteToken(String account) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			db.beginTransaction();

			SQLiteStatement statement = db
					.compileStatement("DELETE FROM GOOGLE_ACCOUNT WHERE ACCOUNT = ?");
			statement.bindString(1, account);
			statement.execute();

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void setNeedCalculate(long id, int needCalculate) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			db.beginTransaction();

			SQLiteStatement statement = db
					.compileStatement("UPDATE ALARM SET NEED_CALCULATE = ? WHERE _id = ?");
			statement.bindLong(1, needCalculate);
			statement.bindLong(2, id);
			statement.execute();

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void addVibration(String name, long[] pattern) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			db.beginTransaction();

			SQLiteStatement statement = db
					.compileStatement("INSERT INTO VIBRATION(NAME) VALUES(?)");
			statement.bindString(1, name);
			long id = statement.executeInsert();
			statement.close();

			statement = db
					.compileStatement("INSERT INTO VIBRATION_PATTERN(INDEX, DURATION, VIBRATION_ID) VALUES(?, ?, ?)");
			int size = pattern.length;
			int index = 0;
			for (int i = 0; i < size; i++) {
				statement.clearBindings();
				index = 0;

				statement.bindLong(++index, i + 1);
				statement.bindLong(++index, pattern[i]);
				statement.bindLong(++index, id);
				statement.executeInsert();
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void updateVibration(long id, String name, long[] pattern) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getWritableDatabase();
			db.beginTransaction();

			cursor = db.rawQuery("SELECT _id FROM VIBRATION WHERE _id = ?",
					new String[] { String.valueOf(id) });
			if (cursor.getCount() == 1) {
				int index = 0;
				SQLiteStatement statement = db
						.compileStatement("UPDATE VIBRATION SET NAME = ?  WHERE _id = ?");
				statement.bindString(++index, name);
				statement.bindLong(++index, id);
				statement.execute();
				statement.close();

				statement = db
						.compileStatement("DELETE FROM VIBRATION_PATTERN WHERE VIBRATION_ID = ?");
				statement.bindLong(1, id);
				statement.execute();

				statement = db
						.compileStatement("INSERT INTO VIBRATION_PATTERN(INDEX, DURATION, VIBRATION_ID) VALUES(?, ?, ?)");
				int size = pattern.length;
				for (int i = 0; i < size; i++) {
					statement.clearBindings();
					index = 0;

					statement.bindLong(++index, i + 1);
					statement.bindLong(++index, pattern[i]);
					statement.bindLong(++index, id);
					statement.executeInsert();
				}
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public VibrationVo getVibration(long id) {
		VibrationVo vo = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery("SELECT * FROM VIBRATION WHERE _id = ?",
					new String[] { String.valueOf(id) });
			if (cursor.getCount() == 1) {
				vo = new VibrationVo();
				cursor.moveToFirst();
				vo.setName(cursor.getString(cursor
						.getColumnIndex(VibrationColumns.NAME)));
				cursor.close();

				cursor = db
						.rawQuery(
								"SELECT DURATION FROM VIBRATION_PATTERN WHERE VIBRATION_ID = ? ORDER BY INDEX",
								new String[] { String.valueOf(id) });
				cursor.moveToFirst();
				int index = 0;
				long[] pattern = new long[cursor.getCount()];
				while (!cursor.isAfterLast()) {
					pattern[index] = cursor.getLong(cursor
							.getColumnIndex(VibrationColumns.DURATION));

					cursor.moveToNext();
					index++;
				}
				vo.setPattern(pattern);
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		return vo;
	}

	public List<VibrationVo> getVibrationList() {
		List<VibrationVo> list = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery("SELECT * FROM VIBRATION ORDER BY _id", null);
			list = new ArrayList<VibrationVo>(cursor.getCount());
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				VibrationVo vo = new VibrationVo();
				vo.setId(cursor.getLong(cursor
						.getColumnIndex(VibrationColumns._ID)));
				vo.setName(cursor.getString(cursor
						.getColumnIndex(VibrationColumns.NAME)));
				list.add(vo);
				cursor.moveToNext();
			}
			cursor.close();

			Iterator<VibrationVo> it = list.iterator();
			while (it.hasNext()) {
				VibrationVo vo = it.next();
				cursor = db
						.rawQuery(
								"SELECT DURATION FROM VIBRATION_PATTERN WHERE VIBRATION_ID = ? ORDER BY INDEX",
								new String[] { String.valueOf(vo.getId()) });
				cursor.moveToFirst();
				int index = 0;
				long[] pattern = new long[cursor.getCount()];
				while (!cursor.isAfterLast()) {
					pattern[index] = cursor.getLong(cursor
							.getColumnIndex(VibrationColumns.DURATION));

					cursor.moveToNext();
					index++;
				}
				vo.setPattern(pattern);
				cursor.close();
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}

		return list;
	}
}