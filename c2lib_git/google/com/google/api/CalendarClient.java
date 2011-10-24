/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.db.AlarmDb;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.xml.atom.AtomParser;
import com.google.api.client.xml.XmlNamespaceDictionary;

/**
 * @author Yaniv Inbar
 */
public class CalendarClient {

	/** Whether to enable debugging. */
	public static final boolean DEBUG = true;

	static final XmlNamespaceDictionary DICTIONARY = new XmlNamespaceDictionary()
			.set("", "http://www.w3.org/2005/Atom")
			.set("batch", "http://schemas.google.com/gdata/batch")
			.set("gd", "http://schemas.google.com/g/2005");

	private final HttpRequestFactory requestFactory;
	private String account;
	private String authToken;
	private String gsessionid;
	private Date start;
	private Date end;
	private Context context;

	public CalendarClient(HttpRequestFactory requestFactory) {
		this.requestFactory = requestFactory;
	}

	public CalendarClient(String account, Date start, Date end, Context context) {
		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		this.requestFactory = transport.createRequestFactory(new Initializer());
		this.account = account;
		this.start = start;
		this.end = end;
		this.context = context;
		AlarmDb db = new AlarmDb(context);
		this.authToken = db.getToken(this.account);
		db.close();
	}

	public void initializeParser(HttpRequest request) {
		AtomParser parser = new AtomParser();
		parser.namespaceDictionary = DICTIONARY;
		request.addParser(parser);
	}

	<F extends Feed> F executeGetFeed(CalendarUrl url, Class<F> feedClass)
			throws IOException {
		url.fields = GoogleAtom.getFieldsFor(feedClass);
		HttpRequest request = requestFactory.buildGetRequest(url);
		return request.execute().parseAs(feedClass);
	}

	public CalendarFeed executeGetCalendarFeed(CalendarUrl url)
			throws IOException {
		return executeGetFeed(url, CalendarFeed.class);
	}

	public EventFeed executeGetEventFeed(CalendarUrl url) throws IOException {
		return executeGetFeed(url, EventFeed.class);
	}

	private class Initializer implements HttpRequestInitializer {

		public void initialize(com.google.api.client.http.HttpRequest arg)
				throws IOException {
			GoogleHeaders headers = new GoogleHeaders();

			PackageManager pm = CalendarClient.this.context.getPackageManager();
			PackageInfo packageInfo = null;
			try {
				packageInfo = pm.getPackageInfo(
						CalendarClient.this.context.getPackageName(), 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			String developperName = CalendarClient.this.context
					.getString(R.string.developperName);
			String versionName = packageInfo.versionName;
			String appName = CalendarClient.this.context
					.getString(R.string.uaAppName);
			headers.setApplicationName(developperName + "-" + appName + "/"
					+ versionName);
			headers.gdataVersion = "2";
			arg.headers = headers;
			CalendarClient.this.initializeParser(arg);
			arg.interceptor = new Interceptor();
			arg.unsuccessfulResponseHandler = new ResponseHandler();
		}
	}

	private class Interceptor implements HttpExecuteInterceptor {

		public void intercept(com.google.api.client.http.HttpRequest arg)
				throws IOException {
			GoogleHeaders headers = (GoogleHeaders) arg.headers;
			headers.setGoogleLogin(authToken);
			arg.url.set("gsessionid", gsessionid);

			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss");
			SimpleDateFormat format2 = new SimpleDateFormat("Z");

			if (arg.url.containsKey("start-min")) {
				arg.url.remove("start-min");
			}
			StringBuilder sb = new StringBuilder();
			sb.append(format.format(start));
			sb.append(format2.format(start).substring(0, 3));
			sb.append(":");
			sb.append(format2.format(start).substring(3));
			String startMin = sb.toString();
			arg.url.put("start-min", startMin);

			if (arg.url.containsKey("start-max")) {
				arg.url.remove("start-max");
			}
			sb.setLength(0);
			sb.append(format.format(end));
			sb.append(format2.format(end).substring(0, 3));
			sb.append(":");
			sb.append(format2.format(end).substring(3));
			String startMax = sb.toString();
			arg.url.put("start-max", startMax);

			MethodOverride override = new MethodOverride();
			override.intercept(arg);
		}
	}

	private class ResponseHandler implements HttpUnsuccessfulResponseHandler {

		public boolean handleResponse(
				com.google.api.client.http.HttpRequest request,
				com.google.api.client.http.HttpResponse response,
				boolean retrySupported) throws IOException {

			boolean result = false;
			if (response.statusCode == 302) {
				GoogleUrl url = new GoogleUrl(response.headers.location);
				gsessionid = (String) url.getFirst("gsessionid");
				result = true;
			} else if (response.statusCode == 403 || response.statusCode == 401) {
				GoogleAccountManager accountManager = new GoogleAccountManager(
						CalendarClient.this.context);
				accountManager.invalidateAuthToken(authToken);
				authToken = null;
				AlarmDb db = new AlarmDb(CalendarClient.this.context);
				db.deleteToken(CalendarClient.this.account);
				db.close();
			}
			return result;
		}
	}
}