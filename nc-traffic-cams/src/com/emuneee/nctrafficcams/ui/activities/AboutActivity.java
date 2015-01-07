/*
 * Copyright (C) 2012 http://emuneee.com/blog/apps/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.emuneee.nctrafficcams.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;

import com.emuneee.nctrafficcams.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Shows the about screen
 *
 * @author ehalley
 *
 */
public class AboutActivity extends ActionBarActivity {
	public final static String TAG = "AboutFragment";

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about_activity, menu);
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		((TextView) findViewById(R.id.text_view_google_play))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						showGooglePlayLicense();
					}

				});

		((TextView) findViewById(R.id.text_view_actionbar_ptr))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLicense("file:///android_asset/apache_license.txt");
			}

		});

		((TextView) findViewById(R.id.text_view_map_icons))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						showLicense("file:///android_asset/creative_commons_license.txt");
					}

				});
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			TextView version = (TextView) findViewById(R.id.text_view_version);
			version.setText(pInfo.versionName);
		} catch (NameNotFoundException e) {
			Log.w(TAG, e.getMessage());
		}

	}

	/**
	 * Sends an intent to open a website
	 *
	 * @param url
	 */
	private void showGooglePlayLicense() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.license_dialog, null);
		builder.setCancelable(true);
		builder.setView(view);
		builder.setTitle(R.string.open_source_title);
		Dialog dialog = builder.create();
		dialog.show();
		// load the license from assets
		String licenseText = GooglePlayServicesUtil
				.getOpenSourceSoftwareLicenseInfo(this);
		WebView wv = (WebView) view.findViewById(R.id.web_view_license);
		wv.loadData(licenseText, "text/text", "UTF-8");
	}

	/**
	 * Sends an intent to open a website
	 *
	 * @param url
	 */
	private void showLicense(String url) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.license_dialog, null);
		builder.setCancelable(true);
		builder.setView(view);
		builder.setTitle(R.string.open_source_title);
		Dialog dialog = builder.create();
		dialog.show();
		// load the license from assets
		WebView wv = (WebView) view.findViewById(R.id.web_view_license);
		wv.loadUrl(url);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.rate:
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.playstore_link_market)));
			startActivity(intent);
			break;
		case R.id.share_app:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(android.content.Intent.EXTRA_TEXT,
					getString(R.string.playstore_link));
			startActivity(Intent.createChooser(intent, "Share via"));
			break;
		}
		return false;
	}

}
