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

import java.util.ArrayList;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.common.Category;
import com.emuneee.nctrafficcams.ui.Constants;
import com.emuneee.nctrafficcams.ui.fragments.CameraDetailFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * @author Evan
 *
 */
public class DetailActivity extends ActionBarActivity {
	private ViewPager mViewPager;
	private DetailAdapter mAdapter;

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
		setContentView(R.layout.detail_activity);

		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			ArrayList<Parcelable> cameras = extra
					.getParcelableArrayList(Constants.BUNDLE_CAMERAS);
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setOffscreenPageLimit(3);
			mAdapter = new DetailAdapter(getSupportFragmentManager(), cameras);
			mViewPager.setAdapter(mAdapter);
			mViewPager.setCurrentItem(extra
					.getInt(Constants.BUNDLE_CURRENT_INDEX));
		}
		// configure the action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			actionBar.hide();
		}
	}

	/**
	 * Binds our list of cameras to the pager adapter that allows the user to
	 * horizontally scroll there a list of camerass
	 *
	 * @author Evan
	 *
	 */
	private class DetailAdapter extends FragmentStatePagerAdapter {
		private ArrayList<Parcelable> mCameras;

		public DetailAdapter(FragmentManager fm, ArrayList<Parcelable> cameras) {
			super(fm);
			mCameras = cameras;
		}

		@Override
		public Fragment getItem(int position) {
			Camera camera = (Camera) mCameras.get(position);
			CameraDetailFragment detailFragment = new CameraDetailFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable(Constants.BUNDLE_CAMERA, camera);
			EasyTracker.getInstance(DetailActivity.this).send(MapBuilder
				      .createEvent(Category.ViewCamera.name(), camera.getTitle(), "", null)
				      .build());
			detailFragment.setArguments(bundle);
			return detailFragment;
		}

		@Override
		public int getCount() {
			return mCameras.size();
		}

	}
}
