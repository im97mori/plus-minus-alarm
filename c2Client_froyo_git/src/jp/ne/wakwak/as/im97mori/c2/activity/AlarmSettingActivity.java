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
import jp.ne.wakwak.as.im97mori.c2.app.FragmentPagerAdapterImpl;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingAudioFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingAudioFragment.OnSettingAudioFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingScreenFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundFragment.OnSettingSoundFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundTypeFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.setting.SettingSoundTypeFragment.OnSettingSoundTypeFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.AlarmSettingVo;
import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class AlarmSettingActivity extends BaseFragmentActivity implements
		OnSettingSoundFragmentListener, OnSettingSoundTypeFragmentListener,
		OnSettingAudioFragmentListener, OnPageChangeListener,
		OnCheckedChangeListener {

	private static final String TAG_SETTING_DIALOG = "settingDialog";
	private static final int REQUEST_CODE_RINGTONE = 0;
	private static final int SCREEN_FRAGMENT = 0;
	private static final int SOUND_FRAGMENT = 1;

	private SettingScreenFragment screenFragment = null;
	private SettingSoundFragment soundFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.alarm_setting);

		ViewPager pager = (ViewPager) this.findViewById(R.id.alarmSettingPager);
		AlarmSettingPagerAdapter adapter = new AlarmSettingPagerAdapter(
				this.getSupportFragmentManager());

		Intent intent = this.getIntent();
		long id = intent.getLongExtra(Constants.IntentKey.ID,
				Constants.TEMPOLARY_ID);
		FragmentManager fm = this.getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag(adapter.getNamePrefix()
				+ SCREEN_FRAGMENT);
		if (fragment == null) {
			screenFragment = SettingScreenFragment.newInstance(id);
		} else {
			screenFragment = (SettingScreenFragment) fragment;
		}
		fragment = fm.findFragmentByTag(adapter.getNamePrefix()
				+ SOUND_FRAGMENT);
		if (fragment == null) {
			soundFragment = SettingSoundFragment.newInstance(id);
		} else {
			soundFragment = (SettingSoundFragment) fragment;
		}

		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(this);

		RadioGroup radioGroup = (RadioGroup) this
				.findViewById(R.id.alarmSettingRadioGroup);
		radioGroup.setOnCheckedChangeListener(this);
	}

	private class AlarmSettingPagerAdapter extends FragmentPagerAdapterImpl {

		public AlarmSettingPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (position == SCREEN_FRAGMENT) {
				fragment = AlarmSettingActivity.this.screenFragment;
			} else if (position == SOUND_FRAGMENT) {
				fragment = AlarmSettingActivity.this.soundFragment;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		protected String getNamePrefix() {
			return AlarmSettingPagerAdapter.class.getName();
		}
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
				Uri uri = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				String typeValue = null;
				if (uri != null) {
					typeValue = uri.toString();
				}
				soundFragment.onSoundSelected(Constants.SoundType.RINGTONE,
						typeValue);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onSoundTypeAudioChoice(long id, String name) {
		this.dismissDialog(TAG_SETTING_DIALOG);

		soundFragment.onSoundSelected(Constants.SoundType.AUDIO,
				String.valueOf(id) + "\n" + name);
	}

	@Override
	public void onSoundTypeAudioChoiceCancel() {
		this.dismissDialog(TAG_SETTING_DIALOG);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		ViewPager pager = (ViewPager) this.findViewById(R.id.alarmSettingPager);
		if (checkedId == R.id.alarmSettingScreenRadioButton) {
			pager.setCurrentItem(SCREEN_FRAGMENT);
		} else {
			pager.setCurrentItem(SOUND_FRAGMENT);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		RadioGroup group = (RadioGroup) this
				.findViewById(R.id.alarmSettingRadioGroup);
		group.setOnCheckedChangeListener(null);
		if (position == SCREEN_FRAGMENT) {
			group.check(R.id.alarmSettingScreenRadioButton);
		} else {
			group.check(R.id.alarmSettingSoundRadioButton);
		}
		group.setOnCheckedChangeListener(this);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}
}