package jp.ne.wakwak.as.im97mori.c2.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class BaseActivity extends Activity {

	protected void showDialogFragment(DialogFragment fragment, String tag) {
		FragmentManager fm = this.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		fragment.show(ft, tag);
	}

	protected void dismissDialog(String tag) {
		FragmentManager fm = getFragmentManager();
		DialogFragment prev = (DialogFragment) fm.findFragmentByTag(tag);
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			prev.dismiss();
			ft.commit();
		}
	}
}