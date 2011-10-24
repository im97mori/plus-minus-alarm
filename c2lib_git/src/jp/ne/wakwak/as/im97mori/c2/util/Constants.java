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

package jp.ne.wakwak.as.im97mori.c2.util;

public final class Constants {
	private Constants() {
	}

	public static final long TEMPOLARY_ID = -1;

	public static final class AlarmEnable {
		public static final int ALARM_ENABLED = 0;
		public static final int ALARM_DISABLED = 1;
	}

	public static final class AlarmType {
		public static final int TYPE_DAY_OF_WEEK = 0;
		public static final int TYPE_DATE = 1;
		public static final int TYPE_BETWEEN = 2;
		public static final int TYPE_GOOGLE_CALENDAR = 3;
	}

	public static final class AlarmTypeSign {
		public static final int SIGN_PLUS = 0;
		public static final int SIGN_MINUS = 1;
	}

	public static final class AlarmCommand {
		public static final int EDIT_ALARM = 0;
		public static final int NEXT = 1;
		public static final int RESET = 2;
		public static final int SKIP_ONCE = 3;
		public static final int CALCULATE = 4;
	}

	public static final class IntentKey {
		public static final String ID = "ID";
		public static final String POSITION = "POSITION";
		public static final String WIDGET_COMMAND = "WIDGET_COMMAND";
		public static final String NEXT = "NEXT";
	}

	public static final class ArgumentKey {
		public static final String ID = "ID";
		public static final String IDS = "IDS";
		public static final String SIGN = "SIGN";
		public static final String POSITION = "POSITION";
		public static final String TYPE_VALUE = "TYPE_VALUE";
		public static final String ACCOUNT = "ACCOUNT";
		public static final String CALENDAR_NAME = "CALENDAR_NAME";
		public static final String DATE = "DATE";
		public static final String NAME = "NAME";
		public static final String TIME = "TIME";
		public static final String ENABLE = "ENABLE";
	}

	public static final class AlarmSetting {
		public static final int SOUND = 0;
		public static final int SCREEN = 1;
		public static final int VIBRATION = 2;
	}

	public static final class SoundType {
		public static final int AUDIO = 0;
		public static final int RINGTONE = 1;
	}

	public static final class TokenType {
		public static final String GOOGLE_CALENDAR = "cl";
	}

	public static final class AssetName {
		public static final String APACHE_LICENSE = "LICENSE-2.0.txt";
		public static final String HTTP_CLIENT_DIRECTORY = "httpclient";
		public static final String HTTP_CLIENT_NOTICE = "NOTICE.txt";
	}

	public static final class WidgetCommand {
		public static final int UPDATE_ALL = 0;
		public static final int IN_CALCULATE = 1;
		public static final int FINISH_CALCULATE = 2;
	}

	public static final class Action {
		public static final String IN_CALCULATE = "jp.ne.wakwak.as.im97mori.c2.IN_CALCULATE";
		public static final String FINISH_CALCULATE = "jp.ne.wakwak.as.im97mori.c2.FINISH_CALCULATE";
		public static final String ALARM = "jp.ne.wakwak.as.im97mori.c2.ALARM";
		public static final String START = "jp.ne.wakwak.as.im97mori.c2.START";
		public static final String STOP = "jp.ne.wakwak.as.im97mori.c2.STOP";
	}

	public static final class NeedCalculate {
		public static final int CALCULATED = 0;
		public static final int NEED_CALCULATE = 1;
	}
}