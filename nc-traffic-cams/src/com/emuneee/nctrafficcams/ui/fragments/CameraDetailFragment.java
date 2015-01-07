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
package com.emuneee.nctrafficcams.ui.fragments;

import java.math.BigDecimal;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.common.Category;
import com.emuneee.nctrafficcams.tasks.FavoriteCamera;
import com.emuneee.nctrafficcams.tasks.RefreshCamera;
import com.emuneee.nctrafficcams.tasks.Share;
import com.emuneee.nctrafficcams.ui.Constants;
import com.emuneee.nctrafficcams.ui.activities.AboutActivity;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;
import com.emuneee.nctrafficcams.ui.activities.MapActivity;
import com.emuneee.nctrafficcams.ui.activities.PreferencesActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Displays detailed information and larger traffic picture to the user
 *
 * @author Evan
 *
 */
public class CameraDetailFragment extends Fragment {
	public static final String TAG = "CameraDetailFragment";
	private Camera mCamera;
	private MenuItem mFavoriteMenuItem;
	private MenuItem mUnfavoriteMenuItem;
	private Vibrator mVibrator;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.camera_detail_fragment, menu);
		mFavoriteMenuItem = menu.findItem(R.id.favorite);
		mUnfavoriteMenuItem = menu.findItem(R.id.unfavorite);
		Bundle args = getArguments();
		mCamera = args.getParcelable(Constants.BUNDLE_CAMERA);
		// set the appropriate favorite icon
		if (mCamera.isFavorite()) {
			mFavoriteMenuItem.setVisible(false);
			mUnfavoriteMenuItem.setVisible(true);
		} else {
			mFavoriteMenuItem.setVisible(true);
			mUnfavoriteMenuItem.setVisible(false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(Constants.BUNDLE_CAMERA, mCamera);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share_camera:
			EasyTracker.getInstance(getActivity()).send(MapBuilder
				.createEvent(Category.Other.name(), mCamera.getTitle(), "", null)
				.build());
			new Share(getActivity()).execute(mCamera.getUrl());
			break;
		case R.id.refresh_camera:
			mCamera.setUpdated(Calendar.getInstance().getTimeInMillis());
			Toast.makeText(getActivity(), R.string.refreshing,
					Toast.LENGTH_SHORT).show();
			new RefreshCamera(mCamera, (ImageView) getView().findViewById(
					R.id.image_view_detail)).execute();
			break;
		case R.id.favorite:
			setFavorite(true);
			break;
		case R.id.unfavorite:
			setFavorite(false);
			break;
		case R.id.show_on_map:
			int resultCode = GooglePlayServicesUtil
					.isGooglePlayServicesAvailable(getActivity());
			if (resultCode != ConnectionResult.SUCCESS) {
				GooglePlayServicesUtil.getErrorDialog(resultCode,
						getActivity(), -1).show();
			} else {
				EasyTracker.getInstance(getActivity()).send(MapBuilder
					      .createEvent(Category.Other.name(), "show on map", mCamera.getTitle(), null)
					      .build());
				Intent mapIntent = new Intent(getActivity(), MapActivity.class);
				mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mapIntent.putExtra(Constants.BUNDLE_METRO, mCamera.getTitle());
				mapIntent.putExtra(Constants.BUNDLE_CAMERA, mCamera);
				startActivity(mapIntent);
			}
			break;
		case android.R.id.home:
			getActivity().finish();
			break;
		case R.id.settings:
			Intent intent = new Intent(getActivity(), PreferencesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.about:
			intent = new Intent(getActivity(), AboutActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		return false;
	}

	private void setFavorite(boolean toFavorite) {
		if(toFavorite) {
			EasyTracker.getInstance(getActivity()).send(MapBuilder
		      .createEvent(Category.Favorite.name(), mCamera.getTitle(), "favorite", null)
		      .build());
		} else {
			EasyTracker.getInstance(getActivity()).send(MapBuilder
		      .createEvent(Category.Favorite.name(), mCamera.getTitle(), "unfavorite", null)
		      .build());
		}

		mCamera.setIsFavorite(toFavorite);
		mFavoriteMenuItem.setVisible(!toFavorite);
		mUnfavoriteMenuItem.setVisible(toFavorite);
		new FavoriteCamera(toFavorite).execute(mCamera);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		Bundle args = getArguments();
		mCamera = args.getParcelable(Constants.BUNDLE_CAMERA);
		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

		if (savedInstanceState != null) {
			mCamera = savedInstanceState.getParcelable(Constants.BUNDLE_CAMERA);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.camera_detail_fragment,
				container, false);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		EasyTracker.getInstance(getActivity()).set(Fields.SCREEN_NAME, TAG);
		EasyTracker.getInstance(getActivity()).send(MapBuilder.createAppView().build());
		View view = getView();

		// set the variables
		TextView textViewTitle = (TextView) view
				.findViewById(R.id.text_view_detail_title);
		TextView textViewMetro = (TextView) view
				.findViewById(R.id.text_view_detail_metro);
		ImageView imageView = (ImageView) view
				.findViewById(R.id.image_view_detail);
		TextView textViewCoord = (TextView) view
				.findViewById(R.id.text_view_detail_coordinates);

		// populate the form
		imageView.setOnLongClickListener(new CameraItemLongClickListener());
		textViewTitle.setText(mCamera.getTitle());
		textViewMetro.setText(mCamera.getCity() + ", NC " + mCamera.getZipCode());
		BigDecimal latitude = new BigDecimal(mCamera.getLatitude()).setScale(3, BigDecimal.ROUND_HALF_UP);
		BigDecimal longitude = new BigDecimal(mCamera.getLongitude()).setScale(3, BigDecimal.ROUND_HALF_UP);
		textViewCoord.setText(latitude + getString(R.string.latitude) + ", " +
				longitude + getString(R.string.longitude));
		MainActivity.getImageWorker().loadImage(mCamera.getUrl(), imageView, mCamera);
	}

	private class CameraItemLongClickListener implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			mVibrator.vibrate(10);
			setFavorite(!mCamera.isFavorite());
			return true;
		}

	}
}
