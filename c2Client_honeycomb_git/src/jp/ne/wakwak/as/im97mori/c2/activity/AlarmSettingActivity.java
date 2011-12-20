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

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.AlarmSettingFragment.OnAlarmSettingFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingAudioFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingAudioFragment.OnSettingAudioFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingScreenFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundFragment.OnSettingSoundFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundTypeFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundTypeFragment.OnSettingSoundTypeFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingVibrationFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingVibrationFragment.OnSettingVibrationFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingVibrationPatternFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingVibrationPatternFragment.OnSettingVibrationPatternFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingVo;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

public class AlarmSettingActivity extends BaseActivity implements
		OnAlarmSettingFragmentListener, OnSettingSoundFragmentListener,
		OnSettingSoundTypeFragmentListener, OnSettingAudioFragmentListener,
		OnSettingVibrationFragmentListener,
		OnSettingVibrationPatternFragmentListener {

	private static final String TAG_SETTING_DIALOG = "settingDialog";

	private static final int REQUEST_CODE_RINGTONE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.alarm_setting);

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources()
				.getColor(R.color.title_background)));

		FragmentManager manager = this.getFragmentManager();
		Fragment fragment = manager
				.findFragmentById(R.id.alarmSettingDetailLayout);

		if (fragment == null) {
			Intent intent = this.getIntent();
			fragment = SettingSoundFragment.newInstance(intent.getLongExtra(
					Constants.IntentKey.ID, 1));
			this.changeListFragment(fragment);
		}
	}

	private void changeListFragment(Fragment fragment) {
		FragmentManager manager = this.getFragmentManager();
		Fragment oldfragment = manager
				.findFragmentById(R.id.alarmSettingDetailLayout);
		FragmentTransaction transaction = manager.beginTransaction();
		if (oldfragment == null) {
			transaction.add(R.id.alarmSettingDetailLayout, fragment);
		} else {
			transaction.replace(R.id.alarmSettingDetailLayout, fragment);
		}
		transaction.commit();
	}

	@Override
	public void onSelect(int id) {
		Intent intent = this.getIntent();
		Fragment fragment = null;
		if (id == Constants.AlarmSetting.SOUND) {
			fragment = SettingSoundFragment.newInstance(intent.getLongExtra(
					Constants.IntentKey.ID, 1));
		} else if (id == Constants.AlarmSetting.SCREEN) {
			fragment = SettingScreenFragment.newInstance(intent.getLongExtra(
					Constants.IntentKey.ID, 1));
		} else if (id == Constants.AlarmSetting.VIBRATION) {
			fragment = SettingVibrationFragment.newInstance(intent
					.getLongExtra(Constants.IntentKey.ID, 1));
		}

		this.changeListFragment(fragment);
	}

	@Override
	public void onSoundChoice() {
		SettingSoundTypeFragment fragment = SettingSoundTypeFragment
				.newInstance();
		this.showDialogFragment(fragment, TAG_SETTING_DIALOG);
	}

	@Override
	public void onSoundTypeChoice(int type) {
		this.dismissDialog(TAG_SETTING_DIALOG);
		Intent intent = this.getIntent();
		long id = intent.getLongExtra(Constants.IntentKey.ID,
				Constants.TEMPOLARY_ID);
		AlarmDb db = new AlarmDb(this);
		AlarmSettingVo vo = db
				.getAlarmSetting(id, Constants.AlarmSetting.SOUND);
		db.close();
		if (Constants.SoundType.AUDIO == type) {
			long mediaId = -1;
			if (vo != null) {
				String[] values = vo.getTypeValue().split("\n");
				if (Integer.valueOf(values[0]) == Constants.SoundType.AUDIO) {
					mediaId = Long.parseLong(values[1]);
				}
			}

			SettingAudioFragment fragment = SettingAudioFragment
					.newInstance(mediaId);
			this.showDialogFragment(fragment, TAG_SETTING_DIALOG);
		} else if (Constants.SoundType.RINGTONE == type) {
			intent = new Intent();
			intent.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);

			Uri uri = null;
			if (vo != null) {
				String[] values = vo.getTypeValue().split("\n");
				if (Integer.valueOf(values[0]) == Constants.SoundType.RINGTONE) {
					if (!"null".equals(values[1])) {
						uri = Uri.parse(values[1]);
					}
				} else {
					uri = Settings.System.DEFAULT_RINGTONE_URI;
				}
			} else {
				uri = Settings.System.DEFAULT_RINGTONE_URI;
			}

			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
			this.startActivityForResult(intent, REQUEST_CODE_RINGTONE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_RINGTONE) {
			if (resultCode == Activity.RESULT_OK) {
				FragmentManager fragmentManager = this.getFragmentManager();
				Fragment fragment = fragmentManager
						.findFragmentById(R.id.alarmSettingDetailLayout);
				if (fragment != null
						&& fragment instanceof SettingSoundFragment) {
					SettingSoundFragment soundFragment = (SettingSoundFragment) fragment;
					Uri uri = data
							.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					String typeValue = null;
					if (uri != null) {
						typeValue = uri.toString();
					}
					soundFragment.onSoundSelected(Constants.SoundType.RINGTONE,
							typeValue);
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onSoundTypeAudioChoice(long id, String name) {
		FragmentManager fragmentManager = this.getFragmentManager();
		Fragment fragment = fragmentManager
				.findFragmentById(R.id.alarmSettingDetailLayout);
		if (fragment != null && fragment instanceof SettingSoundFragment) {
			SettingSoundFragment soundFragment = (SettingSoundFragment) fragment;
			soundFragment.onSoundSelected(Constants.SoundType.AUDIO,
					String.valueOf(id) + "\n" + name);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = true;
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			result = true;
		} else {
			result = super.onOptionsItemSelected(item);
		}
		return result;
	}

	@Override
	public void onVibrationPatternChoice() {
		Intent intent = this.getIntent();
		long id = intent.getLongExtra(Constants.IntentKey.ID,
				Constants.TEMPOLARY_ID);
		AlarmDb db = new AlarmDb(this);
		AlarmSettingVo vo = db.getAlarmSetting(id,
				Constants.AlarmSetting.VIBRATION);
		db.close();

		long patternIid = -1;
		if (vo != null) {
			patternIid = Long.valueOf(vo.getTypeValue());
		}

		SettingVibrationPatternFragment fragment = SettingVibrationPatternFragment
				.newInstance(patternIid);
		this.showDialogFragment(fragment, TAG_SETTING_DIALOG);
	}

	@Override
	public void onVibrationPatternChoice(long id, String name) {
		FragmentManager fragmentManager = this.getFragmentManager();
		Fragment fragment = fragmentManager
				.findFragmentById(R.id.alarmSettingDetailLayout);
		if (fragment != null && fragment instanceof SettingVibrationFragment) {
			SettingVibrationFragment vibrationFragment = (SettingVibrationFragment) fragment;
			vibrationFragment.onVibrationPatternSelected(id, name);
		}
	}
}