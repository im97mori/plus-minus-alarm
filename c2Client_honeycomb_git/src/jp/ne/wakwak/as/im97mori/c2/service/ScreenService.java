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

import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingVo;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenService extends IntentService {

	private static final Object[] LOCK = new Object[0];

	private static boolean reenableKeyguard;
	private static KeyguardLock keyguardLock;
	private static ScreenOffReceiver receiver;

	public ScreenService() {
		super(ScreenService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(Constants.Action.START)) {
			long id = intent.getLongExtra(Constants.IntentKey.ID,
					Constants.TEMPOLARY_ID);
			if (id == Constants.TEMPOLARY_ID) {
				this.stopSelf();
				return;
			}

			AlarmDb db = new AlarmDb(this);
			AlarmSettingVo settingVo = db.getAlarmSetting(id,
					Constants.AlarmSetting.SCREEN);
			db.close();

			boolean disableKeyguard;
			if (settingVo == null) {
				disableKeyguard = true;
				reenableKeyguard = true;
			} else {
				String[] values = settingVo.getTypeValue().split("\n");
				disableKeyguard = Boolean.valueOf(values[0]);
				reenableKeyguard = Boolean.valueOf(values[1]);
			}

			synchronized (LOCK) {
				if (disableKeyguard && keyguardLock == null) {
					KeyguardManager km = (KeyguardManager) this
							.getSystemService(Context.KEYGUARD_SERVICE);
					if (km.inKeyguardRestrictedInputMode()) {
						keyguardLock = km.newKeyguardLock(this.getClass()
								.getName());
						keyguardLock.disableKeyguard();

						if (receiver == null) {
							ScreenOffReceiver innerReceiver = new ScreenOffReceiver();
							IntentFilter filter = new IntentFilter(
									Intent.ACTION_SCREEN_OFF);
							this.getApplicationContext().registerReceiver(
									innerReceiver, filter);
							receiver = innerReceiver;
						}
					}
				}
			}
		} else if (intent.getAction().equals(Constants.Action.STOP)) {
			synchronized (LOCK) {
				if (reenableKeyguard && keyguardLock != null) {
					keyguardLock.reenableKeyguard();
					keyguardLock = null;
				}
				if (keyguardLock == null && receiver != null) {
					this.getApplicationContext().unregisterReceiver(receiver);
					receiver = null;
				}
			}
		}
	}

	private class ScreenOffReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (LOCK) {
				if (keyguardLock != null) {
					keyguardLock.reenableKeyguard();
					keyguardLock = null;
				}
				if (ScreenService.receiver != null) {
					ScreenService.this.getApplicationContext()
							.unregisterReceiver(ScreenService.receiver);
					receiver = null;
				}
			}
		}
	}
}