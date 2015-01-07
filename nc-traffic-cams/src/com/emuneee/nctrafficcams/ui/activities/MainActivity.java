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

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.CameraDBHelper;
import com.emuneee.nctrafficcams.api.QueryType;
import com.emuneee.nctrafficcams.api.QueryType.QueryMode;
import com.emuneee.nctrafficcams.common.Category;
import com.emuneee.nctrafficcams.common.HttpUtils;
import com.emuneee.nctrafficcams.common.util.ImageCache;
import com.emuneee.nctrafficcams.common.util.ImageCache.ImageCacheParams;
import com.emuneee.nctrafficcams.common.util.ImageFetcher;
import com.emuneee.nctrafficcams.common.util.ImageResizer;
import com.emuneee.nctrafficcams.common.util.ImageWorker;
import com.emuneee.nctrafficcams.tasks.GetLatestCameras;
import com.emuneee.nctrafficcams.tasks.GetLatestCameras.OnFinishListener;
import com.emuneee.nctrafficcams.ui.Constants;
import com.emuneee.nctrafficcams.ui.adapters.DrawerListAdapter;
import com.emuneee.nctrafficcams.ui.fragments.CameraGalleryFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

/**
 * @author Evan
 *
 */
public class MainActivity extends ActionBarActivity implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener{

	private final String TAG = getClass().getSimpleName();
	private static CameraDBHelper mDBHelper;

	private static ImageResizer mImageWorker;
	private static ImageCacheParams mCacheParams;
	private static LocationClient mClient;

	private ActionBar mActionBar;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private DrawerListAdapter mDrawerAdapter;
	private ExpandableListView mDrawerList;

	private List<String> mCities;

	private CameraGalleryFragment mFragment;

	private GetLatestCameras mGetLatestCamerasTask;


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
	public void onResume() {
		super.onResume();
	}

	private void setCurrentChild(int index) {
		Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putInt(Constants.PREF_CURRENT_CHILD, index);
		editor.commit();
	}

	private int getCurrentChild() {
		return getPreferences(Context.MODE_PRIVATE).getInt(
				Constants.PREF_CURRENT_CHILD, 0);
	}

	private void setCurrentGroup(int index) {
		Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putInt(Constants.PREF_CURRENT_GROUP, index);
		editor.commit();
	}

	private int getCurrentGroup() {
		return getPreferences(Context.MODE_PRIVATE).getInt(
				Constants.PREF_CURRENT_GROUP, 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		setActionBar();
		HttpUtils.initializeVerifiedHostnames(this);
		boolean isDebuggable = ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
		// configure Google Analytics
		GoogleAnalytics.getInstance(this).setDryRun(isDebuggable);
		GoogleAnalytics.getInstance(this).getLogger().setLogLevel(isDebuggable ? LogLevel.VERBOSE : LogLevel.WARNING);
		// setup fragment
		mFragment = new CameraGalleryFragment();
		mFragment.setRetainInstance(true);
		String tag = CameraGalleryFragment.TAG;
		getSupportFragmentManager().beginTransaction().add(
				R.id.fragmentContainer, mFragment, tag).commit();

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		mDBHelper = new CameraDBHelper(this);
		mGetLatestCamerasTask = new GetLatestCameras(this, mDBHelper,
				new MainActivityFinishListener());
		if (!pref.getBoolean(Constants.PREF_EULA_ACCEPTED, false)) {
			showEula();
		} else {
			mGetLatestCamerasTask.execute();
		}
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

	public static Location getLocation(Context context) {
		Location location = null;
		if(mClient != null && mClient.isConnected()) {
			location = mClient.getLastLocation();
		}
		if(location == null){
			location = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE))
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		return location;
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d(TAG, "Connected to location client");
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "Disconnected from location client");
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.d(TAG, "Failed to connect to location client");
	}

	private void setActionBar() {
		// configure the action bar
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// configure the navigation drawer
		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
		mDrawerList.setOnChildClickListener(new DrawerItemClickListener());
		mDrawerList.setOnGroupClickListener(new DrawerGroupItemClickListener());
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.about,
				R.string.about);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	/**
	 * @param savedInstanceState
	 */
	private void applicationStartup() {
		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
			mClient = new LocationClient(this, this, this);
			mClient.connect();
		} else {
			EasyTracker.getInstance(this).send(MapBuilder
		      .createEvent(Category.Other.name(), "GooglePlayServicesNotAvailable", "", null)
		      .build());
		}
		mCities = mGetLatestCamerasTask.getCities();
		mCacheParams = new ImageCacheParams("/image-cache");
		setImageQuality();
		configureAnalytics();
		mImageWorker = new ImageFetcher(this, 400);
		mImageWorker.setImageCache(ImageCache.findOrCreateCache(this,
				mCacheParams));
		mImageWorker.setLoadingImage(R.drawable.placeholder_camera);
		mFragment.setIsProcessing(false);
		mDrawerAdapter = new DrawerListAdapter(this,
				Arrays.asList((getResources().getStringArray(R.array.drawer_list_groups))),
				mCities);
		mDrawerList.setAdapter(mDrawerAdapter);
		selectGroup(mDrawerList, getCurrentGroup(), true);
	}

	@Override
	public void onDestroy() {
		if(mDBHelper != null) {
			mDBHelper.close();
		}
		if(mClient != null) {
			mClient.disconnect();
			mClient = null;
		}
		super.onDestroy();
	}

	/**
	 * Returns the traffic camera database helper
	 *
	 * @return
	 */
	public static CameraDBHelper getDBHelper() {
		return mDBHelper;
	}

	/**
	 * Configure analytics
	 */
	private void configureAnalytics() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean analyticsEnabled = prefs.getBoolean(
				getString(R.string.send_anonymous_statisitcs), true);
		GoogleAnalytics.getInstance(this).setAppOptOut(!analyticsEnabled);
	}

	/**
	 * Displays the end user license agreement at startup
	 */
	private void showEula() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.eula_dialog, null);
		final Dialog dialog;
		builder.setCancelable(false);
		builder.setView(view);
		builder.setTitle(R.string.eula_title);

		builder.setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.dismiss();
					Editor editor = PreferenceManager
							.getDefaultSharedPreferences(MainActivity.this)
							.edit();
					editor.putBoolean(Constants.PREF_EULA_ACCEPTED, true);
					editor.commit();

					editor = getPreferences(Context.MODE_PRIVATE).edit();
					editor.putBoolean(Constants.PREF_TRAFFIC_CAMS_INIT, true);
					editor.commit();
					mGetLatestCamerasTask.execute();
	           }
	       });

		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.dismiss();
					Editor editor = PreferenceManager
							.getDefaultSharedPreferences(MainActivity.this)
							.edit();
					editor.putBoolean(Constants.PREF_EULA_ACCEPTED, false);
					editor.commit();
					MainActivity.this.finish();
	           }
	       });

		dialog = builder.create();
		// load the eula from assets
		WebView wv = (WebView) view.findViewById(R.id.web_view_eula);
		wv.loadUrl("file:///android_asset/eula.htm");
		dialog.show();
	}

	/**
	 * Initializes image quality
	 */
	private void setImageQuality() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String value = prefs.getString(getString(R.string.image_quality),
				getString(R.string.medium_quality));
		String[] values = getResources().getStringArray(
				R.array.image_quality_settings);
		// hi - med - low
		if (value.contentEquals(values[0])) {
			mCacheParams.compressQuality = ImageWorker.HIGH;
		} else if (value.contentEquals(values[1])) {
			mCacheParams.compressQuality = ImageWorker.MEDIUM;
		} else if (value.contentEquals(values[2])) {
			mCacheParams.compressQuality = ImageWorker.LOW;
		}
	}

	/**
	 * @return the imageWorker
	 */
	public static ImageResizer getImageWorker() {
		return mImageWorker;
	}

	private class DrawerItemClickListener implements ExpandableListView.OnChildClickListener {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			switch(groupPosition) {
			case DrawerListAdapter.CITIES_GROUP:
				setCurrentGroup(groupPosition);
				selectCamera(childPosition);
				break;
			}
			return true;
		}
	}

	private class DrawerGroupItemClickListener implements ExpandableListView.OnGroupClickListener {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			selectGroup(parent, groupPosition, false);
			return true;
		}
	}

	private void selectGroup(ExpandableListView parent, int groupPosition, boolean simulateChildClick) {
		QueryType queryType = null;
		String subTitle = null;

		switch (groupPosition) {
		case DrawerListAdapter.ALL_GROUP:
			// selected all group item
			queryType = new QueryType(QueryMode.All);
			subTitle = (String) mDrawerAdapter.getGroup(groupPosition);
			break;
		case DrawerListAdapter.FAVORITES_GROUP:
			// selected favorite group item
			queryType = new QueryType(QueryMode.Favorites);
			subTitle = (String) mDrawerAdapter.getGroup(groupPosition);
			break;
		case DrawerListAdapter.CITIES_GROUP:
		case DrawerListAdapter.ROUTES_GROUP:
			// selected routes group item
			if (parent.isGroupExpanded(groupPosition)) {
				parent.collapseGroup(groupPosition);
			} else {
				parent.expandGroup(groupPosition);
			}
			setCurrentGroup(groupPosition);
			if(simulateChildClick) {
				selectCamera(getCurrentChild());
			}
			break;
		case DrawerListAdapter.NEAR_ME_GROUP:
			// selected favorite group item
			queryType = new QueryType(QueryMode.NearMe);
			subTitle = (String) mDrawerAdapter.getGroup(groupPosition);
			break;
		}

		if (queryType != null) {
			mFragment.getCameras(queryType);
			mDrawerLayout.closeDrawer(mDrawerList);
			mActionBar.setSubtitle(subTitle);
			setCurrentGroup(groupPosition);
		}
	}

	private void selectCamera(int index) {
		setCurrentChild(index);
		mDrawerLayout.closeDrawer(mDrawerList);
		mDrawerList.setItemChecked(index, true);
		mActionBar.setSubtitle(mCities.get(index));
		mFragment.getCameras(new QueryType(QueryMode.City, mCities.get(index)));
	}

	private class MainActivityFinishListener implements OnFinishListener {

		@Override
		public void onSuccessFinish() {
			applicationStartup();
		}

		@Override
		public void onErrorFinish() {
			Toast.makeText(MainActivity.this, R.string.error_fetching_camera_update, Toast.LENGTH_SHORT).show();
		}
	}
}
