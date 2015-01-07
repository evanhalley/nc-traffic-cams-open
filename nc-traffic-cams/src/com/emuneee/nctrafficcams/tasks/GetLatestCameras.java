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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.api.CameraDBHelper;
import com.emuneee.nctrafficcams.common.ConversionUtils;
import com.emuneee.nctrafficcams.common.HttpUtils;
import com.emuneee.nctrafficcams.ui.Constants;

/**
 * @author evan
 *
 */
public class GetLatestCameras extends AsyncTask<Void, Void, Boolean> {
	private final String TAG = "GetLatestCameras";
	private static final String sCamerasPath = "/cameras";
	private static final String sUpdatePath =  "/latest";
	// used in the traffic camera update lottery
	private static final int sMagicNumber = 3;
	private static final int sMaxNumber = 5;
	private String mBaseUrl = "https://[hostname]:[port]/v1";
	private String mLocalCameras = "cameras_2014_05_08.json";
	private Context mContext;
	private CameraDBHelper mDBHelper;
	private List<String> mCities;
	private OnFinishListener mListener;

	public GetLatestCameras(Context context, CameraDBHelper helper, OnFinishListener listener) {
		mContext = context;
		mDBHelper = helper;
		mListener = listener;
		mBaseUrl = mBaseUrl.replace("[hostname]", context.getString(R.string.hostname));
		mBaseUrl = mBaseUrl.replace("[port]", context.getString(R.string.port));
	}

	public List<String> getCities() {
		return mCities;
	}

	private String filterStrings(String input) {
		String output = input;
		output = output.replace("&amp;", "&");
		output = output.trim();
		return output;
	}

	private Map<String, Camera> jsonToCamera(String cameraJson) throws JSONException {
		JSONArray cameraJsonArray = new JSONArray(cameraJson);
		Map<String, Camera> cameraSet = new HashMap<String, Camera>();

		for(int i = 0; i < cameraJsonArray.length(); i++) {
			JSONObject cameraJsonObj = cameraJsonArray.getJSONObject(i);
			Camera camera = new Camera();
			camera.setId(filterStrings(cameraJsonObj.getString("_id")));
			camera.setTitle(filterStrings(cameraJsonObj.getString("title")));
			camera.setUrl(filterStrings(cameraJsonObj.getString("url")));
			camera.setLatitude(cameraJsonObj.getDouble("latitude"));
			camera.setLongitude(cameraJsonObj.getDouble("longitude"));
			camera.setCity(filterStrings(cameraJsonObj.getString("city")));
			camera.setZipCode(filterStrings(cameraJsonObj.getString("zipcode")));
			cameraSet.put(camera.getId(), camera);
		}
		return cameraSet;
	}

	private Boolean isUpdateNeeded(String updateDatetime) throws ParseException {
		Boolean updateNeeded = null;
		String storedUpdateDatetime = PreferenceManager
				.getDefaultSharedPreferences(mContext).getString(
						Constants.PREF_LAST_UPDATE_DATETIME, null);

		if(storedUpdateDatetime == null) {
			updateNeeded = null;
		} else {
			Date storedUpdate = ConversionUtils.parseDate(storedUpdateDatetime,
					ConversionUtils.ISO_DATE_FORMAT);
			Date update = ConversionUtils.parseDate(updateDatetime,
					ConversionUtils.ISO_DATE_FORMAT);
			Log.d(TAG, "Stored update datetime: " + storedUpdate);
			Log.d(TAG, "Server Update datetime: " + update);
			updateNeeded = update.after(storedUpdate);
		}
		return updateNeeded;
	}

	private boolean isCheckNeeded() {
		boolean checkNeeded = false;
		boolean hasCheckedBefore = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(Constants.PREF_HAS_CHECKED, false);

		if(!hasCheckedBefore) {
			Log.d(TAG, "Has Checked Before: " + hasCheckedBefore);
			checkNeeded = true;
		} else {
			Random random = new Random();
			int randomNum = random.nextInt(sMaxNumber);
			checkNeeded = randomNum == sMagicNumber;
			Log.d(TAG, "Magic Number: " + sMagicNumber + ", The Number: " + randomNum);
		}
		Log.d(TAG, "Is Check Needed: " + checkNeeded);
		return checkNeeded;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		Toast.makeText(mContext, R.string.updating_cameras_message, Toast.LENGTH_LONG).show();
	}

	private String getLatestUpdateTime() {
		HttpsURLConnection connection = null;
		BufferedReader reader = null;
		String updateDatetime = null;

		try {
			StringBuilder updateStr = new StringBuilder();
			connection = HttpUtils.getAuthUrlConnection(mBaseUrl + sUpdatePath, mContext);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;

			while((line = reader.readLine()) != null) {
				updateStr.append(line);
			}
			Log.d(TAG, "Content Size: " + updateStr.length());
			Log.d(TAG, "Response Code: " + connection.getResponseCode());
			JSONObject updateObj = new JSONObject(updateStr.toString());
			updateDatetime = updateObj.getString("updated");
		} catch (Exception e) {
			Log.e(TAG, "Error getting latest update time");
			Log.e(TAG, e.getMessage());
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.w(TAG, "Error retrieving latest update time");
					Log.w(TAG, e.getMessage());
				}
			}
		}

		return updateDatetime;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Map<String, Camera> cameras = null;
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		InputStream inputStream = null;
		boolean updateSuccessful = false;

		if(isCheckNeeded()) {

			try {
				// check for the last update date
				String updateDatetime = getLatestUpdateTime();
				Boolean updateNeeded = isUpdateNeeded(updateDatetime);

				if(updateNeeded == null || updateNeeded) {
					StringBuilder cameraStr = new StringBuilder();

					if(updateNeeded == null) {
						Log.d(TAG, "First time starting up, let's get the camera json from local storage");
						inputStream = mContext.getAssets().open(mLocalCameras);
					} else {
						connection = HttpUtils.getAuthUrlConnection(mBaseUrl + sCamerasPath, mContext);
						inputStream = connection.getInputStream();
						Log.d(TAG, "Response Code: " + connection.getResponseCode());
					}
					reader = new BufferedReader(new InputStreamReader(inputStream));
					publishProgress();
					Log.d(TAG, "Update needed!");
					String line = null;

					while((line = reader.readLine()) != null) {
						cameraStr.append(line);
					}
					Log.d(TAG, "Content Size: " + cameraStr.length());
			        cameras = jsonToCamera(cameraStr.toString());
			        mDBHelper.updateCameras(cameras);

			        // save the last modified date
					Editor edit = PreferenceManager
							.getDefaultSharedPreferences(mContext).edit();
					edit.putString(Constants.PREF_LAST_UPDATE_DATETIME, updateDatetime);
					edit.putBoolean(Constants.PREF_HAS_CHECKED, true);
					edit.commit();
					updateSuccessful = true;
				}
			} catch (IOException e) {
				Log.w(TAG, "Error retrieving cameras");
				Log.w(TAG, e.getMessage());
			} catch (JSONException e) {
				Log.w(TAG, "Error retrieving cameras");
				Log.w(TAG, e.getMessage());
			}  catch (Exception e) {
				Log.w(TAG, "Error retrieving cameras");
				Log.w(TAG, e.getMessage());
			} finally {
				if(connection != null) {
					connection.disconnect();
				}
				if(reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						Log.w(TAG, "Error retrieving cameras");
						Log.w(TAG, e.getMessage());
					}
				}
				if(inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						Log.w(TAG, "Error retrieving cameras");
						Log.w(TAG, e.getMessage());
					}
				}
			}
		} else {
			updateSuccessful = true;
		}

		// is the update was unsuccessful, lets see if we've updated once,
		// if we have, lets override the failed updated
		if(updateSuccessful == false) {
			updateSuccessful = PreferenceManager
					.getDefaultSharedPreferences(mContext).getString(
							Constants.PREF_LAST_UPDATE_DATETIME, null) != null;
		}

		if(updateSuccessful) {
			mCities = mDBHelper.getCities();
		}

		return updateSuccessful;
	}

	protected void onPostExecute(Boolean result) {

		if(result) {
			mListener.onSuccessFinish();
		} else {
			mListener.onErrorFinish();
		}
	}

	public interface OnFinishListener {

		public void onSuccessFinish();
		public void onErrorFinish();

	}
}