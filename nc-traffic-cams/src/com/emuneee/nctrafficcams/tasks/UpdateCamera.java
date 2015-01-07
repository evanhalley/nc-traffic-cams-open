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

import android.os.AsyncTask;
import android.util.Log;

import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;

/**
 * Updates a camera record in the database
 *
 * @author Evan
 *
 */
public class UpdateCamera extends AsyncTask<Camera, Void, Boolean> {
	private static final String TAG = "UpdateCamera";

	@Override
	protected Boolean doInBackground(Camera... arg0) {
		return MainActivity.getDBHelper().updateTrafficCamera(arg0[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Log.d(TAG, "Updated camera successfully!");
		} else {
			Log.d(TAG, "Updated camera unsuccessfully!");
		}
	}
}
