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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.api.CameraDBHelper;
import com.emuneee.nctrafficcams.api.QueryType;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;

/**
 * Updates a camera record in the database
 *
 * @author Evan
 *
 */
public class FavoriteCamera extends AsyncTask<Camera, Void, Boolean> {
	private static final String TAG = "UpdateCamera";
	private boolean mMakeFavorite = false;
	private CameraDBHelper mDBHelper = null;
	private Context mContext;
	private QueryType mQueryType;
	private OnPostExecuteListener mListener;

	public FavoriteCamera(boolean makeFavorite) {
		mMakeFavorite = makeFavorite;
	}

	public FavoriteCamera(boolean makeFavorite, Context context,
			CameraDBHelper dbHelper, QueryType queryType, OnPostExecuteListener listener) {
		mListener = listener;
		mContext = context;
		mDBHelper = dbHelper;
		mQueryType = queryType;
		mMakeFavorite = makeFavorite;
	}

	@Override
	protected Boolean doInBackground(Camera... arg0) {
		boolean result = false;
		if(mMakeFavorite) {
			result = MainActivity.getDBHelper().addFavoriteCamera(arg0[0]);
		} else {
			result = MainActivity.getDBHelper().removeFavoriteCamera(arg0[0]);
		}
		return result;
	}

	@Override
	protected void onPostExecute(Boolean result) {

		if (result) {
			Log.d(TAG, "Updated camera successfully!");
		} else {
			Log.d(TAG, "Updated camera unsuccessfully!");
		}

		if (mDBHelper != null) {
			new GetCameras(mContext, mDBHelper, mListener).execute(mQueryType);
		}
	}
}
