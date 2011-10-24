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

import jp.ne.wakwak.as.im97mori.c2.activity.AlarmActivity;
import jp.ne.wakwak.as.im97mori.c2.service.AlarmService;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context paramContext, Intent paramIntent) {
		if (Constants.Action.ALARM.equals(paramIntent.getAction())) {
			Intent intent = new Intent(paramContext.getApplicationContext(),
					AlarmActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			Uri uri = paramIntent.getData();
			long id = Long.parseLong(uri
					.getQueryParameter(Constants.IntentKey.ID));
			intent.putExtra(Constants.IntentKey.ID, id);
			paramContext.startActivity(intent);

			intent = new Intent(paramContext.getApplicationContext(),
					AlarmService.class);
			intent.putExtras(paramIntent);
			intent.putExtra(AlarmService.SETTING, Constants.AlarmCommand.NEXT);
			intent.putExtra(Constants.IntentKey.ID, id);
			paramContext.startService(intent);
		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(paramIntent
				.getAction())) {
			if (!paramIntent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
				NetworkInfo info = paramIntent
						.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (info.isConnected()) {
					Intent intent = new Intent(
							paramContext.getApplicationContext(),
							AlarmService.class);
					intent.putExtras(paramIntent);
					intent.putExtra(AlarmService.SETTING,
							Constants.AlarmCommand.CALCULATE);
					paramContext.startService(intent);
				}
			}
		} else {
			Intent intent = new Intent(paramContext.getApplicationContext(),
					AlarmService.class);
			intent.putExtras(paramIntent);
			intent.putExtra(AlarmService.SETTING, Constants.AlarmCommand.RESET);
			paramContext.startService(intent);
		}
	}
}