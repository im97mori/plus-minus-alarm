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

public class AlarmTypeVo implements Parcelable {

	private long id;
	private int type;
	private String typeValue;
	private int sign;
	private long alarmId;

	public AlarmTypeVo() {
	}

	public AlarmTypeVo(Parcel in) {
		this.id = in.readLong();
		this.type = in.readInt();
		this.typeValue = in.readString();
		this.sign = in.readInt();
		this.alarmId = in.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeInt(this.type);
		dest.writeString(this.typeValue);
		dest.writeInt(this.sign);
		dest.writeLong(this.alarmId);
	}

	public static final Parcelable.Creator<AlarmTypeVo> CREATOR = new Parcelable.Creator<AlarmTypeVo>() {
		public AlarmTypeVo createFromParcel(Parcel in) {
			return new AlarmTypeVo(in);
		}

		public AlarmTypeVo[] newArray(int size) {
			return new AlarmTypeVo[size];
		}
	};

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTypeValue() {
		return typeValue;
	}

	public void setTypeValue(String typeValue) {
		this.typeValue = typeValue;
	}

	public int getSign() {
		return sign;
	}

	public void setSign(int sign) {
		this.sign = sign;
	}

	public long getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(long alarmId) {
		this.alarmId = alarmId;
	}
}