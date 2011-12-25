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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.service.ScreenService;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.util.Util;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingVo;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmVo;
import jp.ne.wakwak.as.im97mori.c2.vo.VibrationVo;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;

public class AlarmActivity extends Activity implements OnClickListener {

	private AlarmVo previousVo;
	private Map<Integer, AlarmSettingVo> settingMap = new HashMap<Integer, AlarmSettingVo>();

	private MediaPlayer player;
	private Ringtone ringtone;
	private Vibrator vibartor;

	private WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn() && this.wakeLock == null) {
			this.wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.SCREEN_DIM_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, this.getClass().getName());
			this.wakeLock.acquire();
		}

		this.setContentView(R.layout.alarm);
		this.updateLayout(this.getIntent());
	}

	@Override
	public void onClick(View paramView) {
		if (paramView.getId() == R.id.alarmOkButton) {
			this.finish();
		}
	}

	private void stopSound() {
		if (this.player != null) {
			if (this.player.isPlaying()) {
				this.player.stop();
			}
			this.player.release();
			this.player = null;
		} else if (this.ringtone != null) {
			this.ringtone.stop();
			this.ringtone = null;
		} else if (this.vibartor != null) {
			this.vibartor.cancel();
			this.vibartor = null;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.updateLayout(intent);
	}

	private synchronized void updateLayout(Intent intent) {
		long id = intent.getLongExtra(Constants.IntentKey.ID,
				Constants.TEMPOLARY_ID);
		if (id == Constants.TEMPOLARY_ID) {
			this.finish();
			return;
		}

		AlarmDb db = new AlarmDb(this);
		AlarmVo vo = db.getAlarm(id);
		Map<Integer, AlarmSettingVo> map = new HashMap<Integer, AlarmSettingVo>();
		map.put(Constants.AlarmSetting.SOUND,
				db.getAlarmSetting(id, Constants.AlarmSetting.SOUND));
		AlarmSettingVo settingVo = db.getAlarmSetting(id,
				Constants.AlarmSetting.VIBRATION);
		map.put(Constants.AlarmSetting.VIBRATION, settingVo);
		VibrationVo vibrationVo = null;
		if (settingVo != null) {
			vibrationVo = db.getVibration(Long.parseLong(settingVo
					.getTypeValue()));
		}
		db.close();

		if (this.previousVo != null && vo.getId() == this.previousVo.getId()) {
			if (this.previousVo.getNext() == vo.getNext()) {
				return;
			}

			boolean sameFlag = true;
			settingVo = map.get(Constants.AlarmSetting.SOUND);
			AlarmSettingVo previousSettingVo = this.settingMap
					.get(Constants.AlarmSetting.SOUND);
			if (settingVo != null && previousSettingVo == null) {
				sameFlag = false;
			} else if (settingVo == null && previousSettingVo != null) {
				sameFlag = false;
			} else if (settingVo != null
					&& previousSettingVo != null
					&& settingVo.getTypeValue().equals(
							previousSettingVo.getTypeValue())) {
				sameFlag = true;
			}

			settingVo = map.get(Constants.AlarmSetting.VIBRATION);
			previousSettingVo = this.settingMap
					.get(Constants.AlarmSetting.VIBRATION);
			if (settingVo != null && previousSettingVo == null) {
				sameFlag = false;
			} else if (settingVo == null && previousSettingVo != null) {
				sameFlag = false;
			} else if (settingVo != null
					&& previousSettingVo != null
					&& settingVo.getTypeValue().equals(
							previousSettingVo.getTypeValue())) {
				sameFlag = true;
			}

			if (sameFlag) {
				return;
			}
		}

		this.stopSound();

		this.setTitle(vo.getName() + "(" + vo.getTime() + ")");

		this.findViewById(R.id.alarmOkButton).setOnClickListener(this);
		settingVo = map.get(Constants.AlarmSetting.SOUND);
		if (settingVo != null) {
			String[] values = settingVo.getTypeValue().split("\n");
			if (Integer.valueOf(values[0]) == Constants.SoundType.AUDIO) {
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
						MediaStore.Audio.Media._ID + " = ?",
						new String[] { values[1] }, null);
				if (cursor.getCount() == 1) {
					cursor.moveToFirst();
					String path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Audio.Media.DATA));
					this.player = new MediaPlayer();
					try {
						this.player.setDataSource(path);
						this.player.prepare();
						this.player.start();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				cursor.close();
			} else if (Integer.valueOf(values[0]) == Constants.SoundType.RINGTONE) {
				Uri uri = Uri.parse(values[1]);
				ringtone = RingtoneManager.getRingtone(this, uri);
				ringtone.play();
			}
		}
		settingVo = map.get(Constants.AlarmSetting.VIBRATION);
		if (settingVo != null) {
			this.vibartor = (Vibrator) this
					.getSystemService(Context.VIBRATOR_SERVICE);
			Util.vibrate(this.vibartor, vibrationVo.getPattern());
		}

		Intent serviceIntent = new Intent(this.getApplicationContext(),
				ScreenService.class);
		serviceIntent.setAction(Constants.Action.START);
		serviceIntent.putExtra(Constants.IntentKey.ID, id);
		this.startService(serviceIntent);

		this.previousVo = vo;
		this.settingMap.clear();
		this.settingMap.putAll(map);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			this.finish();
		}
	}

	@Override
	protected void onDestroy() {
		this.stopSound();

		if (this.wakeLock != null) {
			this.wakeLock.release();
			this.wakeLock = null;
		}

		Intent serviceIntent = new Intent(this.getApplicationContext(),
				ScreenService.class);
		serviceIntent.setAction(Constants.Action.STOP);
		this.startService(serviceIntent);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();

		this.finish();
	}
}