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

package jp.ne.wakwak.as.im97mori.c2.fragment.rule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import jp.ne.wakwak.as.im97mori.c2.vo.RuleCalendarVo;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.CalendarClient;
import com.google.api.CalendarEntry;
import com.google.api.CalendarFeed;
import com.google.api.CalendarUrl;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;

public class RuleCalendarFragment extends DialogFragment implements
		OnItemClickListener {

	private static final String LIST = "LIST";

	private static final int PROMPT = 0;
	private static final int SHOW = 1;

	private final HttpTransport transport = AndroidHttp
			.newCompatibleTransport();
	private final MethodOverride override = new MethodOverride();
	private final MainHandler mainHandler = new MainHandler();

	private OnRuleListener listener;
	private GoogleAccountManager accountManager;
	private CalendarClient client;
	private String authToken;
	private String gsessionid;

	public static RuleCalendarFragment newInstance(String account, int sign) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.ArgumentKey.ACCOUNT, account);
		bundle.putInt(Constants.ArgumentKey.SIGN, sign);
		RuleCalendarFragment fragment = new RuleCalendarFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public static RuleCalendarFragment newInstance(int position,
			String account, String calendarName) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.ArgumentKey.POSITION, position);
		bundle.putString(Constants.ArgumentKey.ACCOUNT, account);
		bundle.putString(Constants.ArgumentKey.CALENDAR_NAME, calendarName);
		RuleCalendarFragment fragment = new RuleCalendarFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			listener = (OnRuleListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnRuleListener");
		}

		accountManager = new GoogleAccountManager(this.getActivity());
		client = new CalendarClient(
				this.transport.createRequestFactory(new Initializer()));

		transport.createRequestFactory(new Initializer());
		Bundle bundle = this.getArguments();
		Account account = accountManager.getAccountByName(bundle
				.getString(Constants.ArgumentKey.ACCOUNT));
		AlarmDb db = new AlarmDb(activity);
		this.authToken = db.getToken(account.name);
		if (TextUtils.isEmpty(this.authToken)) {
			HandlerThread thread = new HandlerThread(this.getClass().getName());
			thread.start();
			Handler handler = new Handler(thread.getLooper());
			accountManager.manager.getAuthToken(account,
					Constants.TokenType.GOOGLE_CALENDAR, true, new Callback(),
					handler);
		} else {
			HandlerThread thread = new HandlerThread(this.getClass().getName());
			thread.start();
			RuleCalendarHandler handler = new RuleCalendarHandler(
					thread.getLooper());
			handler.sendEmptyMessage(SHOW);
		}
	}

	private class RuleCalendarHandler extends Handler {
		public RuleCalendarHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			CalendarUrl url = CalendarUrl.forAllCalendarsFeed();
			try {
				CalendarFeed calendarFeed = client.executeGetCalendarFeed(url);
				Iterator<CalendarEntry> it = calendarFeed.calendars.iterator();
				ArrayList<RuleCalendarVo> list = new ArrayList<RuleCalendarVo>(
						calendarFeed.calendars.size());
				Bundle bundle = RuleCalendarFragment.this.getArguments();
				while (it.hasNext()) {
					CalendarEntry entry = it.next();
					RuleCalendarVo vo = new RuleCalendarVo();
					vo.setAccount(bundle
							.getString(Constants.ArgumentKey.ACCOUNT));
					vo.setName(entry.title);
					vo.setUrl(entry.getEventFeedLink());
					list.add(vo);
				}
				Message message = Message.obtain(mainHandler, SHOW);
				bundle = new Bundle();
				bundle.putParcelableArrayList(LIST, list);
				message.setData(bundle);
				message.sendToTarget();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.getLooper().quit();
		}
	}

	private class Interceptor implements HttpExecuteInterceptor {

		public void intercept(com.google.api.client.http.HttpRequest arg)
				throws IOException {
			GoogleHeaders headers = (GoogleHeaders) arg.headers;
			headers.setGoogleLogin(authToken);
			arg.url.set("gsessionid", gsessionid);
			if (arg.url.containsKey("start-min")) {
				arg.url.remove("start-min");
			}
			override.intercept(arg);
		}
	}

	private class Initializer implements HttpRequestInitializer {

		public void initialize(com.google.api.client.http.HttpRequest arg)
				throws IOException {
			GoogleHeaders headers = new GoogleHeaders();
			headers.setApplicationName("im97mori-PlusMinusAlarm/"
					+ RuleCalendarFragment.this.getString(R.string.versionName));
			headers.gdataVersion = "2";
			arg.headers = headers;
			client.initializeParser(arg);
			arg.interceptor = new Interceptor();
			arg.unsuccessfulResponseHandler = new ResponseHandler();
		}
	}

	private class Callback implements AccountManagerCallback<Bundle> {

		public void run(AccountManagerFuture<Bundle> future) {
			Bundle bundle = null;
			try {
				bundle = future.getResult();
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (bundle != null) {
				if (bundle.containsKey(AccountManager.KEY_INTENT)) {
					Message message = Message.obtain(mainHandler, PROMPT);
					message.setData(bundle);
					message.sendToTarget();
				} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
					authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
					Bundle argment = RuleCalendarFragment.this.getArguments();
					AlarmDb db = new AlarmDb(
							RuleCalendarFragment.this.getActivity());
					db.updateToken(
							argment.getString(Constants.ArgumentKey.ACCOUNT),
							authToken);
					HandlerThread thread = new HandlerThread(this.getClass()
							.getName());
					thread.start();
					RuleCalendarHandler handler = new RuleCalendarHandler(
							thread.getLooper());
					handler.sendEmptyMessage(SHOW);
				}
			}
			Looper.myLooper().quit();
		}
	}

	private class ResponseHandler implements HttpUnsuccessfulResponseHandler {

		public boolean handleResponse(
				com.google.api.client.http.HttpRequest request,
				com.google.api.client.http.HttpResponse response,
				boolean retrySupported) throws IOException {

			boolean result = false;
			if (response.statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				GoogleUrl url = new GoogleUrl(response.headers.location);
				gsessionid = (String) url.getFirst("gsessionid");
				result = true;
			} else if (response.statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
				accountManager.invalidateAuthToken(authToken);
				authToken = null;
				Bundle argment = RuleCalendarFragment.this.getArguments();
				AlarmDb db = new AlarmDb(
						RuleCalendarFragment.this.getActivity());
				db.deleteToken(argment.getString(Constants.ArgumentKey.ACCOUNT));
				db.close();
			}
			return result;
		}
	}

	private class MainHandler extends Handler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == PROMPT) {
				Bundle bundle = msg.getData();
				Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
				RuleCalendarFragment.this
						.startActivityForResult(intent, PROMPT);
			} else if (msg.what == SHOW) {
				ArrayList<RuleCalendarVo> list = null;
				Bundle bundle = msg.getData();
				if (bundle == null) {
					list = new ArrayList<RuleCalendarVo>();
				} else {
					list = bundle.getParcelableArrayList(LIST);
				}
				RuleCalendarAdapter adapter = new RuleCalendarAdapter(list);

				View view = RuleCalendarFragment.this.getView();
				ListView listView = (ListView) view
						.findViewById(android.R.id.list);
				listView.setBackgroundColor(getActivity().getResources()
						.getColor(android.R.color.background_light));
				listView.setAdapter(adapter);

				view.findViewById(android.R.id.progress).setVisibility(
						View.GONE);
				listView.setVisibility(View.VISIBLE);

				bundle = getArguments();
				if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
					listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
					String calendarName = bundle
							.getString(Constants.ArgumentKey.CALENDAR_NAME);
					for (int i = 0; i < list.size(); i++) {
						RuleCalendarVo vo = list.get(i);
						if (vo.getName().equals(calendarName)) {
							listView.setItemChecked(i, true);
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.listener = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.rule_calendar, container, false);
		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setOnItemClickListener(this);

		return view;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(this.getString(R.string.typeCalendar));
		return dialog;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PROMPT && resultCode == Activity.RESULT_OK) {
			Bundle bundle = this.getArguments();
			Account account = accountManager.getAccountByName(bundle
					.getString(Constants.ArgumentKey.ACCOUNT));
			HandlerThread thread = new HandlerThread(this.getClass().getName());
			thread.start();
			Handler handler = new Handler(thread.getLooper());
			accountManager.manager.getAuthToken(account,
					Constants.TokenType.GOOGLE_CALENDAR, true, new Callback(),
					handler);
		} else {
			if (this.listener != null) {
				this.listener.onAddRuleCancel();
			}
		}
	}

	private class RuleCalendarAdapter extends ArrayAdapter<RuleCalendarVo> {

		private int layout;

		public RuleCalendarAdapter(List<RuleCalendarVo> list) {
			super(RuleCalendarFragment.this.getActivity(), 0, list);
			Bundle bundle = getArguments();
			if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
				layout = android.R.layout.select_dialog_singlechoice;
			} else {
				layout = android.R.layout.select_dialog_item;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = (TextView) convertView;
			if (textView == null) {
				textView = (TextView) LayoutInflater.from(this.getContext())
						.inflate(layout, null);
			}
			RuleCalendarVo vo = this.getItem(position);
			textView.setText(vo.getName());
			return textView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
			int paramInt, long paramLong) {
		if (AdapterView.INVALID_POSITION != paramInt) {
			RuleCalendarVo vo = (RuleCalendarVo) paramAdapterView
					.getItemAtPosition(paramInt);
			StringBuilder sb = new StringBuilder();
			sb.append(vo.getAccount());
			sb.append("\n");
			sb.append(vo.getName());
			sb.append("\n");
			sb.append(vo.getUrl());
			Bundle bundle = this.getArguments();
			String typeValue = sb.toString();
			if (bundle.containsKey(Constants.ArgumentKey.POSITION)) {
				this.listener.onEditRuleComplete(
						bundle.getInt(Constants.ArgumentKey.POSITION),
						typeValue);
			} else {
				this.listener.onAddRule(
						Constants.AlarmType.TYPE_GOOGLE_CALENDAR, typeValue,
						bundle.getInt(Constants.ArgumentKey.SIGN));
			}
		}
	}
}