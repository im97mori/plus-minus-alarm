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

package jp.ne.wakwak.as.im97mori.c2.receiver;

import jp.ne.wakwak.as.im97mori.c2.db.WidgetDb;
import jp.ne.wakwak.as.im97mori.c2.service.WidgetService;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class WidgetReceiver extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Intent intent = new Intent(context.getApplicationContext(),
				WidgetService.class);
		intent.putExtra(Constants.IntentKey.WIDGET_COMMAND,
				Constants.WidgetCommand.UPDATE_ALL);
		context.startService(intent);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Constants.Action.IN_CALCULATE)) {
			Intent serviceIntent = new Intent(context.getApplicationContext(),
					WidgetService.class);
			serviceIntent.putExtra(Constants.IntentKey.WIDGET_COMMAND,
					Constants.WidgetCommand.IN_CALCULATE);
			serviceIntent.putExtra(Constants.IntentKey.ID, intent.getLongExtra(
					Constants.IntentKey.ID, Constants.TEMPOLARY_ID));
			serviceIntent.putExtra(Constants.IntentKey.NEXT,
					intent.getLongExtra(Constants.IntentKey.NEXT, 0));
			context.startService(serviceIntent);
		} else if (intent.getAction().equals(Constants.Action.FINISH_CALCULATE)) {
			Intent serviceIntent = new Intent(context.getApplicationContext(),
					WidgetService.class);
			serviceIntent.putExtra(Constants.IntentKey.WIDGET_COMMAND,
					Constants.WidgetCommand.FINISH_CALCULATE);
			context.startService(serviceIntent);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		WidgetDb db = new WidgetDb(context);
		db.deleteWidget(appWidgetIds);
		db.close();
		super.onDeleted(context, appWidgetIds);
	}
}