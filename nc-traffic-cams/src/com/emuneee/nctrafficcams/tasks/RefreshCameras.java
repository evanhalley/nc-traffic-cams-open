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
package com.emuneee.nctrafficcams.tasks;

import java.util.Calendar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.ui.Constants;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;

/**
 * Starts the process of refreshing the cameras
 *
 * @author ehalley
 *
 */
public class RefreshCameras extends AsyncTask<Void, Void, Void> {
	private Activity mActivity;
	private BaseAdapter mAdapter;
	private OnPostExecuteListener mListener;

	public RefreshCameras(Activity context, BaseAdapter adapter, OnPostExecuteListener listener) {
		mActivity = context;
		mAdapter = adapter;
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		Toast.makeText(mActivity, R.string.refreshing,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	protected Void doInBackground(Void... params) {
		MainActivity.getImageWorker().clearCache();
		return null;
	}

	@Override
	protected void onPostExecute(Void arg0) {
		// need to set last refresh time
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mActivity);
		Editor editor = prefs.edit();
		editor.putLong(Constants.PREF_LAST_REFRESH, Calendar.getInstance()
				.getTimeInMillis());
		editor.commit();
		mAdapter.notifyDataSetChanged();

		if(mListener != null) {
			mListener.onPostExecute(null);
		}
	}
}
