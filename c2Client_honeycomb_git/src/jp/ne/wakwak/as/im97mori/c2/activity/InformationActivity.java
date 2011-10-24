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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.ne.wakwak.as.im97mori.c2.R;
import jp.ne.wakwak.as.im97mori.c2.util.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class InformationActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.information);

		this.findViewById(R.id.licenseGoogleApiButton).setOnClickListener(this);
		this.findViewById(R.id.licenseGuavaButton).setOnClickListener(this);
		this.findViewById(R.id.licenseHttpClientButton)
				.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.licenseGoogleApiButton) {
			this.showDialog(R.id.licenseGoogleApiDialog);
		} else if (v.getId() == R.id.licenseGuavaButton) {
			this.showDialog(R.id.licenseGuavaDialog);
		} else if (v.getId() == R.id.licenseHttpClientButton) {
			this.showDialog(R.id.licenseHttpClientDialog);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog dialog = null;
		if (id == R.id.licenseGoogleApiDialog || id == R.id.licenseGuavaDialog) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this
					.getLicense(Constants.AssetName.APACHE_LICENSE));
			dialog = builder.create();
		} else if (id == R.id.licenseHttpClientDialog) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			StringBuilder sb = new StringBuilder();
			sb.append(Constants.AssetName.APACHE_LICENSE + " -- start\n");
			sb.append(this.getLicense(Constants.AssetName.APACHE_LICENSE));
			sb.append(Constants.AssetName.APACHE_LICENSE + " -- end\n\n");
			sb.append(Constants.AssetName.HTTP_CLIENT_NOTICE + " -- start\n");
			sb.append(this.getLicense(Constants.AssetName.HTTP_CLIENT_DIRECTORY
					+ "/" + Constants.AssetName.HTTP_CLIENT_NOTICE));
			sb.append(Constants.AssetName.HTTP_CLIENT_NOTICE + " -- end\n");
			builder.setMessage(sb.toString());
			dialog = builder.create();
		} else {
			dialog = super.onCreateDialog(id, args);
		}
		return dialog;
	}

	private String getLicense(String fileName) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = this.getAssets().open(fileName);
			byte[] buffer = new byte[2048];
			int read = 0;
			do {
				read = is.read(buffer);
				if (read == -1) {
					break;
				}
				baos.write(buffer, 0, read);
			} while (true);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return baos.toString();
	}
}