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

package jp.ne.wakwak.as.im97mori.c2.activity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.db.WidgetDb;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import jp.ne.wakwak.as.im97mori.c2.vo.WidgetVo;
import android.app.Activity;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WidgetConfigurationActivity extends ListActivity implements
		OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			this.setResult(Activity.RESULT_CANCELED);
			this.finish();
			return;
		}

		this.setContentView(R.layout.widget_configuration);

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
			}
		}

		List<AlarmVo> alarmList = alarmDb.getAlarmList();
		List<WidgetVo> widgetList = widgetDb.getAlarmList(widgetId);

		widgetDb.close();
		alarmDb.close();

		WidgetConfigurationAdapter adapter = new WidgetConfigurationAdapter(
				alarmList);
		this.setListAdapter(adapter);

		ListView listView = this.getListView();
		for (int i = 0; i < alarmList.size(); i++) {
			AlarmVo alarmVo = alarmList.get(i);
			for (int j = 0; j < widgetList.size(); j++) {
				WidgetVo widgetVo = widgetList.get(j);
				if (alarmVo.getId() == widgetVo.getAlarmId()) {
					listView.setItemChecked(i, true);
					break;
				}
			}
		}

		this.findViewById(R.id.widgetConfigurationOkButton).setOnClickListener(
				this);
		this.findViewById(R.id.widgetConfigurationCancelButton)
				.setOnClickListener(this);
	}

	private class WidgetConfigurationAdapter extends ArrayAdapter<AlarmVo> {
		public WidgetConfigurationAdapter(List<AlarmVo> list) {
			super(WidgetConfigurationActivity.this, 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(
						android.R.layout.simple_list_item_checked, null);
			}
			AlarmVo vo = this.getItem(position);
			TextView textView = (TextView) convertView
					.findViewById(android.R.id.text1);
			textView.setText(vo.getName());
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public long getItemId(int position) {
			return this.getItem(position).getId();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.widgetConfigurationOkButton) {
			ListView listView = this.getListView();
			SparseBooleanArray array = listView.getCheckedItemPositions();
			List<WidgetVo> list = new LinkedList<WidgetVo>();
			for (int i = 0; i < array.size(); i++) {

				int key = array.keyAt(i);
				if (array.get(key)) {
					WidgetVo widgetVo = new WidgetVo();
					AlarmVo alarmVo = (AlarmVo) listView.getItemAtPosition(key);
					widgetVo.setAlarmId(alarmVo.getId());
					widgetVo.setName(alarmVo.getName());
					widgetVo.setNext(alarmVo.getNext());
					widgetVo.setTime(alarmVo.getTime());
					list.add(widgetVo);
				}
			}

			Intent intent = this.getIntent();
			int widgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			WidgetDb db = new WidgetDb(this);
			db.createWidgetList(widgetId, list);
			db.close();

			intent = new Intent();
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			this.setResult(Activity.RESULT_OK, intent);

			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this);
			appWidgetManager.notifyAppWidgetViewDataChanged(widgetId,
					R.id.widgetGrid);

			this.finish();
		} else if (v.getId() == R.id.widgetConfigurationCancelButton) {
			this.setResult(Activity.RESULT_CANCELED);
			this.finish();
		}
	}
}