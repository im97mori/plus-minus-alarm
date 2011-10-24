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

package jp.ne.wakwak.as.im97mori.c2.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public abstract class FragmentPagerAdapterImpl extends FragmentPagerAdapter {

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;

	public FragmentPagerAdapterImpl(FragmentManager fm) {
		super(fm);
		mFragmentManager = fm;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		// Do we already have this fragment?
		String name = this.getNamePrefix() + position;
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null) {
			mCurTransaction.attach(fragment);
		} else {
			fragment = getItem(position);
			mCurTransaction.add(container.getId(), fragment, name);
		}

		return fragment;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		mCurTransaction.detach((Fragment) object);
	}

	@Override
	public void finishUpdate(View container) {
		if (mCurTransaction != null) {
			mCurTransaction.commit();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	public Fragment getItemAt(int position) {
		return mFragmentManager.findFragmentByTag(this.getNamePrefix()
				+ position);
	}

	protected abstract String getNamePrefix();
}
