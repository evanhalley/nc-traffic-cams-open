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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.ui.Constants;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;

/**
 * Refreshes the current cameras image
 *
 * @author ehalley
 *
 */
public class RefreshCamera extends AsyncTask<Void, Void, Void> {
	private Camera mCamera;
	private ImageView mImageView;

	public RefreshCamera(Camera camera, ImageView imageView) {
		mCamera = camera;
		mImageView = imageView;
	}

	@Override
	protected Void doInBackground(Void... params) {
		MainActivity.getImageWorker().getImageCache().remove(mCamera.getUrl());
		return null;
	}

	@Override
	protected void onPostExecute(Void arg0) {
		// need to set last refresh time
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mImageView.getContext());
		Editor editor = prefs.edit();
		editor.putLong(Constants.PREF_LAST_REFRESH, Calendar.getInstance()
				.getTimeInMillis());
		editor.commit();
		mImageView.setImageDrawable(null);
		MainActivity.getImageWorker().loadImage(mCamera.getUrl(), mImageView,
				mCamera);
	}
}
