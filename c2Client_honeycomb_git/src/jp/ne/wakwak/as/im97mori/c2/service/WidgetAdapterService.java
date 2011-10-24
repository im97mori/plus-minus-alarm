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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.WidgetDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.WidgetVo;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class WidgetAdapterService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetRemoteViewsFactory(this.getApplicationContext(),
				intent);
	}

	private class WidgetRemoteViewsFactory implements
			RemoteViewsService.RemoteViewsFactory {

		private Context context;
		private Intent intent;
		private List<WidgetVo> list;

		public WidgetRemoteViewsFactory(Context applicationContext,
				Intent intent) {
			this.context = applicationContext;
			this.intent = intent;
		}

		@Override
		public int getCount() {
			return this.list.size();
		}

		@Override
		public long getItemId(int position) {
			return this.list.get(position).getId();
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public RemoteViews getViewAt(int position) {
			RemoteViews remoteViews = new RemoteViews(
					this.context.getPackageName(), R.layout.widget_item);
			WidgetVo vo = this.list.get(position);
			remoteViews.setTextViewText(R.id.widgetItemNameText, vo.getName()
					+ "(" + vo.getTime() + ")");
			Long next = this.list.get(position).getNext();
			if (next == null) {
				remoteViews.setTextViewText(R.id.widgetItemNextText,
						this.context.getString(R.string.alarmNoNext));
				remoteViews.setViewVisibility(R.id.widgetItemSkipButton,
						View.INVISIBLE);
			} else {
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTimeInMillis(next);
				Date date = calendar.getTime();
				SimpleDateFormat formatter = new SimpleDateFormat(
						this.context.getString(R.string.targetDateFormat));
				remoteViews.setTextViewText(R.id.widgetItemNextText,
						formatter.format(date));

				if (vo.getInCalculate() == 0) {
					Intent intent = new Intent();
					intent.putExtra(Constants.IntentKey.ID, vo.getAlarmId());
					intent.putExtra(Constants.IntentKey.NEXT, vo.getNext());
					remoteViews.setOnClickFillInIntent(
							R.id.widgetItemSkipButton, intent);
					remoteViews.setViewVisibility(R.id.widgetItemSkipButton,
							View.VISIBLE);
					remoteViews.setViewVisibility(android.R.id.progress,
							View.INVISIBLE);
				} else {
					remoteViews.setViewVisibility(R.id.widgetItemSkipButton,
							View.INVISIBLE);
					remoteViews.setViewVisibility(android.R.id.progress,
							View.VISIBLE);
				}
			}
			return remoteViews;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public void onCreate() {
			this.updateList();
		}

		private void updateList() {
			WidgetDb db = new WidgetDb(this.context);
			int widgetId = this.intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			this.list = db.getAlarmList(widgetId);
			db.close();
		}

		@Override
		public void onDataSetChanged() {
			this.updateList();
		}

		@Override
		public void onDestroy() {
			this.context = null;
			this.intent = null;
			this.list = null;
		}
	}
}