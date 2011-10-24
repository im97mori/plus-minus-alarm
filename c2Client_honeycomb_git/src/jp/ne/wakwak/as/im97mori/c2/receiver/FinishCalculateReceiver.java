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

import jp.ne.wakwak.as.im97mori.c2.fragment.OnRefreshListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FinishCalculateReceiver extends BroadcastReceiver {

	private OnRefreshListener listener;

	public FinishCalculateReceiver(OnRefreshListener arg) {
		this.listener = arg;
	}

	@Override
	public void onReceive(Context paramContext, Intent paramIntent) {
		listener.onRefresh();
	}

	public void setLinstener(OnRefreshListener arg) {
		this.listener = arg;
	}
}