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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.CameraDBHelper;
import com.emuneee.nctrafficcams.api.QueryType;
import com.emuneee.nctrafficcams.api.QueryType.QueryMode;
import com.emuneee.nctrafficcams.common.Category;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * Gets traffic cameras for a selected metro
 *
 * @author Evan
 *
 */
public class GetCameras extends AsyncTask<QueryType, Void, Object> {
	private CameraDBHelper mDBHelper;
	private Context mContext;
	private OnPostExecuteListener mListener;

	public GetCameras(Context context, CameraDBHelper dbHelper, OnPostExecuteListener listener) {
		this.mDBHelper = dbHelper;
		this.mContext = context;
		this.mListener = listener;
	}

	@Override
	protected Object doInBackground(QueryType... queryType) {
		Object cursor = null;

		if (queryType == null || queryType.length == 0) {
			queryType = new QueryType[]{ new QueryType(QueryMode.All, null) };
		}

		switch (queryType[0].queryMode) {
		case City:
			EasyTracker.getInstance(mContext).send(MapBuilder
		      .createEvent(Category.CitySwitch.name(), queryType[0].query, "", null)
		      .build());
			cursor = mDBHelper.getCamerasByCity(queryType[0].query);
			break;
		case Favorites:
			EasyTracker.getInstance(mContext).send(MapBuilder
		      .createEvent(Category.CitySwitch.name(), "Show Favorites", "", null)
		      .build());
			cursor = mDBHelper.getFavoriteCameras();
			break;
		case NearMe:
			SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
			double radius = Double.parseDouble(prefs.getString(mContext.getString(R.string.near_me_radius), "1 mile").split(" ")[0]);
			EasyTracker.getInstance(mContext).send(MapBuilder
		      .createEvent(Category.NearMe.name(), "Show Near Me", "", null)
		      .build());
			cursor = mDBHelper.getCamerasWithDistance(
					radius, MainActivity.getLocation(mContext));
			break;
		case All:
		default:
			EasyTracker.getInstance(mContext).send(MapBuilder
		      .createEvent(Category.CitySwitch.name(), "Show All", "", null)
		      .build());
			cursor = mDBHelper.getAllCameras();
			break;
		}

		return cursor;
	}

	@Override
	protected void onPostExecute(Object cursor) {
		if (cursor != null) {
			mListener.onPostExecute(cursor);
		}
	}
}
