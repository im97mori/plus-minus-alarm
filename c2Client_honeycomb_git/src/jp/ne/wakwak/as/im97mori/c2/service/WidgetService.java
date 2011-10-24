package jp.ne.wakwak.as.im97mori.c2.service;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.activity.WidgetConfigurationActivity;
import jp.ne.wakwak.as.im97mori.c2.db.WidgetDb;
import jp.ne.wakwak.as.im97mori.c2.receiver.WidgetReceiver;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class WidgetService extends IntentService {

	public WidgetService() {
		super(WidgetService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent paramIntent) {
		int command = paramIntent.getIntExtra(
				Constants.IntentKey.WIDGET_COMMAND,
				Constants.WidgetCommand.UPDATE_ALL);
		if (command == Constants.WidgetCommand.UPDATE_ALL) {
			this.refreshWidget();
		} else if (command == Constants.WidgetCommand.IN_CALCULATE) {
			long alarmId = paramIntent.getLongExtra(Constants.IntentKey.ID,
					Constants.TEMPOLARY_ID);
			long next = paramIntent.getLongExtra(Constants.IntentKey.NEXT, 0);
			this.inCalculate(alarmId, next);
		} else if (command == Constants.WidgetCommand.FINISH_CALCULATE) {
			this.finishCalculate();
		}
	}

	private void refreshWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(
						this.getApplicationContext(), WidgetReceiver.class));
		for (int i = 0; i < appWidgetIds.length; i++) {
			Intent intent = new Intent(this.getApplicationContext(),
					WidgetConfigurationActivity.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			PendingIntent pendingIntent = PendingIntent.getActivity(
					this.getApplicationContext(), 0, intent, 0);

			RemoteViews views = new RemoteViews(this.getApplicationContext()
					.getPackageName(), R.layout.widget);
			views.setOnClickPendingIntent(R.id.widgetSettingButton,
					pendingIntent);

			intent = new Intent(this.getApplicationContext(),
					WidgetAdapterService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

			views.setRemoteAdapter(appWidgetIds[i], R.id.widgetGrid, intent);

			intent = new Intent(this.getApplicationContext(),
					WidgetReceiver.class);
			intent.setAction(Constants.Action.IN_CALCULATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			pendingIntent = PendingIntent.getBroadcast(
					this.getApplicationContext(), 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			views.setPendingIntentTemplate(R.id.widgetGrid, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
	}

	private void inCalculate(long alarmId, long next) {
		WidgetDb db = new WidgetDb(this);
		db.inCalculate(alarmId);
		db.close();

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(this
				.getApplicationContext(), WidgetReceiver.class));
		appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.widgetGrid);

		Intent intent = new Intent(this.getApplicationContext(),
				AlarmService.class);
		intent.putExtra(AlarmService.SETTING, Constants.AlarmCommand.SKIP_ONCE);
		intent.putExtra(Constants.IntentKey.ID, alarmId);
		intent.putExtra(Constants.IntentKey.NEXT, next);
		this.startService(intent);
	}

	private void finishCalculate() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());
		int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(
						this.getApplicationContext(), WidgetReceiver.class));
		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
				R.id.widgetGrid);
	}
}