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

package jp.ne.wakwak.as.im97mori.c2.vo;

import android.os.Parcel;
import android.os.Parcelable;

public class RuleCalendarVo implements Parcelable {
	private String account;
	private String name;
	private String url;

	public RuleCalendarVo() {
	}

	public RuleCalendarVo(Parcel in) {
		this.account = in.readString();
		this.name = in.readString();
		this.url = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.account);
		dest.writeString(this.name);
		dest.writeString(this.url);
	}

	public static final Parcelable.Creator<RuleCalendarVo> CREATOR = new Parcelable.Creator<RuleCalendarVo>() {
		public RuleCalendarVo createFromParcel(Parcel in) {
			return new RuleCalendarVo(in);
		}

		public RuleCalendarVo[] newArray(int size) {
			return new RuleCalendarVo[size];
		}
	};

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}