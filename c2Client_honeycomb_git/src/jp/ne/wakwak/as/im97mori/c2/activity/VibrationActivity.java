package jp.ne.wakwak.as.im97mori.c2.activity;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.fragment.vibration.VibrationDetailFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.vibration.VibrationDetailFragment.OnVibrationDetailFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.vibration.VibrationListFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.vibration.VibrationListFragment.OnVibrationListFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.fragment.vibration.VibrationNameFragment;
import jp.ne.wakwak.as.im97mori.c2.fragment.vibration.VibrationNameFragment.OnVibrationNameFragmentListener;
import jp.ne.wakwak.as.im97mori.c2.vo.VibrationVo;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

public class VibrationActivity extends Activity implements
		OnVibrationListFragmentListener, OnVibrationDetailFragmentListener,
		OnVibrationNameFragmentListener {

	private static final String TAG_VIBRATION_NAME_DIALOG = "vibrationNameDialog";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.vibration);

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(this.getResources()
				.getColor(R.color.title_background)));
	}

	@Override
	public void onSelect(VibrationVo vo) {
		FragmentManager fm = this.getFragmentManager();
		VibrationDetailFragment fragment = (VibrationDetailFragment) fm
				.findFragmentById(R.id.vibrationDetail);
		FragmentTransaction transaction = fm.beginTransaction();
		if (vo == null && fragment != null) {
			transaction.remove(fragment);
		} else if (vo != null) {
			if (fragment == null) {
				fragment = VibrationDetailFragment.newInstance(vo.getId());
				transaction.add(R.id.vibrationDetail, fragment);
			} else {
				VibrationDetailFragment newFragment = VibrationDetailFragment
						.newInstance(vo.getId());
				transaction.replace(R.id.vibrationDetail, newFragment);
			}
		}
		transaction.commit();
	}

	@Override
	public void onAddPattern() {
		VibrationNameFragment fragment = VibrationNameFragment.newInstance();
		this.showDialogFragment(fragment, TAG_VIBRATION_NAME_DIALOG);
	}

	@Override
	public void onEditName(String name) {
		VibrationNameFragment fragment = VibrationNameFragment
				.newInstance(name);
		this.showDialogFragment(fragment, TAG_VIBRATION_NAME_DIALOG);
	}

	private void showDialogFragment(DialogFragment fragment, String tag) {
		FragmentManager fm = this.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		fragment.show(ft, tag);
	}

	@Override
	public void onVibrationNameOk(String name) {
		if (TextUtils.isEmpty(name)) {
			Toast.makeText(this, R.string.vibrationNameNeedInputString,
					Toast.LENGTH_SHORT).show();
		} else {
			FragmentManager fm = this.getFragmentManager();
			VibrationNameFragment fragment = (VibrationNameFragment) fm
					.findFragmentByTag(TAG_VIBRATION_NAME_DIALOG);
			Bundle bundle = fragment.getArguments();
			if (bundle == null || bundle.isEmpty()) {
				VibrationListFragment listFragment = (VibrationListFragment) fm
						.findFragmentById(R.id.vibrationList);
				listFragment.onAddVibration(name);
			} else {
				VibrationDetailFragment oldDetailFragment = (VibrationDetailFragment) fm
						.findFragmentById(R.id.vibrationDetail);
				oldDetailFragment.onNameChange(name);
			}
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
}