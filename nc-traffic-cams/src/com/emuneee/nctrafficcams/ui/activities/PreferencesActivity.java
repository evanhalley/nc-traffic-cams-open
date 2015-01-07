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

import android.annotation.SuppressLint;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.ui.Constants;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;

/**
 * Shows the user preferences
 *
 * @author ehalley
 *
 */
public class PreferencesActivity extends PreferenceActivity {

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

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		// start and shut down analytics on preference change
		CheckBoxPreference checkBoxAnayltics = (CheckBoxPreference) findPreference(getString(R.string.send_anonymous_statisitcs));
		checkBoxAnayltics
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						boolean value = (Boolean) newValue;
						GoogleAnalytics.getInstance(PreferencesActivity.this).setAppOptOut(!value);
						return true;
					}
				});

		// changes the list image quality
		ListPreference listImageQuality = (ListPreference) findPreference(getString(R.string.image_quality));
		listImageQuality
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						Editor edit = PreferenceManager
								.getDefaultSharedPreferences(
										PreferencesActivity.this).edit();
						edit.putBoolean(Constants.PREF_CLEAR_CACHE, true);
						edit.commit();
						return true;
					}
				});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}
}
