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

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.api.QueryType;
import com.emuneee.nctrafficcams.tasks.FavoriteCamera;
import com.emuneee.nctrafficcams.tasks.GetCameras;
import com.emuneee.nctrafficcams.tasks.LoadDetailActivity;
import com.emuneee.nctrafficcams.tasks.LoadMapActivity;
import com.emuneee.nctrafficcams.tasks.OnPostExecuteListener;
import com.emuneee.nctrafficcams.tasks.RefreshCameras;
import com.emuneee.nctrafficcams.ui.Constants;
import com.emuneee.nctrafficcams.ui.activities.AboutActivity;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;
import com.emuneee.nctrafficcams.ui.activities.PreferencesActivity;
import com.emuneee.nctrafficcams.ui.adapters.RetrievableCameraAdapter;
import com.emuneee.nctrafficcams.ui.adapters.DefaultCamerasAdapter;
import com.emuneee.nctrafficcams.ui.adapters.NearMeCamerasAdapter;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Provides common functionality between the multiple view fragments
 *
 * @author Evan
 *
 */
public class CameraGalleryFragment extends Fragment implements OnRefreshListener {
	public final static String TAG = "CamerasGalleryFragment";
	private boolean mWasPaused = false;
	private PullToRefreshLayout mPullToRefreshLayout;

	public enum ViewMode {
		List, Gallery, About
	}

	private int mCurrentIndex;
	private AdapterView<ListAdapter> mAdapterView;
	private ListAdapter mAdapter;
	private Vibrator mVibrator;
	private QueryType mQueryType;
	private TextView mEmptyGalleryTextView;
	private DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerList;
	private ActionBar mActionBar;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(Constants.BUNDLE_CURRENT_INDEX,
				mAdapterView.getFirstVisiblePosition());
	}

	public AdapterView<ListAdapter> getAdapterView() {
		return mAdapterView;
	}

	public void getCameras(QueryType queryType) {
		mQueryType = queryType;
		new GetCameras(getActivity(), MainActivity.getDBHelper(),
				new PostGetCamerasListener()).execute(mQueryType);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cameras_gallery_fragment,
				container, false);
		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		mAdapterView = (GridView) view.findViewById(R.id.grid_view_cameras);
		mEmptyGalleryTextView = (TextView) view.findViewById(R.id.text_view_no_favorites);
	    mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

		mAdapterView.setOnItemClickListener(new CameraItemClickListener());
		mAdapterView.setOnItemLongClickListener(new CameraItemLongClickListener());

		if (savedInstanceState != null) {
			mCurrentIndex = savedInstanceState.getInt(
					Constants.BUNDLE_CURRENT_INDEX, 0);
		}

		mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
		mDrawerList = (ExpandableListView) getActivity().findViewById(R.id.left_drawer);
		mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

		// Now setup the PullToRefreshLayout
	    ActionBarPullToRefresh.from(getActivity())
            // Mark All Children as pullable
            .allChildrenArePullable()
            // Set the OnRefreshListener
            .listener(this)
            .options(Options.create().refreshOnUp(true).build())
            // Finally commit the setup to our PullToRefreshLayout
            .setup(mPullToRefreshLayout);

		return view;
	}

	@Override
	public void onDestroy() {
		if (mAdapterView != null) {
			if (mAdapterView.getAdapter() != null
					&& mAdapterView.getAdapter() instanceof DefaultCamerasAdapter) {
				DefaultCamerasAdapter adapter = (DefaultCamerasAdapter) mAdapterView
						.getAdapter();
				adapter.getCursor().close();
			}
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		EasyTracker.getInstance(getActivity()).set(Fields.SCREEN_NAME, TAG);
		EasyTracker.getInstance(getActivity()).send(MapBuilder.createAppView().build());
		mAdapterView.setSelection(mCurrentIndex);
		if(mWasPaused) {
			new GetCameras(getActivity(), MainActivity.getDBHelper(),
					new PostGetCamerasListener()).execute(mQueryType);
			mWasPaused = false;
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mWasPaused = true;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mWasPaused = false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.camera_view_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			if(mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
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
		case R.id.map_cameras:
			if(mAdapter != null && mAdapter.getCount() > 0) {
				int resultCode = GooglePlayServicesUtil
						.isGooglePlayServicesAvailable(getActivity());
				if (resultCode != ConnectionResult.SUCCESS) {
					GooglePlayServicesUtil.getErrorDialog(resultCode,
							getActivity(), -1).show();
				} else {
					new LoadMapActivity((RetrievableCameraAdapter) mAdapter, getActivity(), mActionBar.getSubtitle().toString())
							.execute();
				}
			}
			break;
		}

		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (savedInstanceState != null) {
			mCurrentIndex = savedInstanceState.getInt(
					Constants.BUNDLE_CURRENT_INDEX, 0);
		}
	}

	public class PostGetCamerasListener implements OnPostExecuteListener {

		@Override
		public void onPostExecute(Object obj) {
			if(obj instanceof Cursor) {
				Cursor cursor = (Cursor) obj;
				try {

					if (mAdapter != null && mAdapter instanceof DefaultCamerasAdapter) {
						((CursorAdapter) mAdapter).changeCursor(cursor);
					} else {
						mAdapter = new DefaultCamerasAdapter(getActivity(), cursor);
						mAdapterView.setAdapter(mAdapter);
					}

					toggleEmptyGalleryView(mAdapter.getCount() == 0, R.string.no_favorites);
				} catch (IllegalStateException e) {
					Log.e(TAG, e.getMessage());
				}
			} else if (obj instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Camera> cameras = (List<Camera>) obj;
				mAdapter = new NearMeCamerasAdapter(getActivity(), cameras);
				mAdapterView.setAdapter(mAdapter);
				((NearMeCamerasAdapter) mAdapter).notifyDataSetChanged();

				toggleEmptyGalleryView(cameras.size() == 0, R.string.no_cameras_near);
			}
		}
	}

	private void toggleEmptyGalleryView(boolean isEmpty, int stringId) {
		mAdapterView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
		mEmptyGalleryTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
		mEmptyGalleryTextView.setText(stringId);
	}

	public void setIsProcessing(boolean isLoading) {
		View loadingView = (ProgressBar) getView().findViewById(R.id.progress_bar_loading);

		if (loadingView != null) {
			loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
			mAdapterView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
		}
	}

	public class CameraItemLongClickListener implements ListView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View arg1,
				int arg2, long arg3) {
			mVibrator.vibrate(10);
			RetrievableCameraAdapter adapter = (RetrievableCameraAdapter) adapterView.getAdapter();
			Camera camera = adapter.getCamera(arg2);
			new FavoriteCamera(!camera.isFavorite(), getActivity(),
					MainActivity.getDBHelper(), mQueryType, new PostGetCamerasListener()).execute(camera);
			return true;
		}
	}

	public class CameraItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
				long arg3) {
			// save the position so we can load it again when the user hits back
			mCurrentIndex = position;
			new LoadDetailActivity((RetrievableCameraAdapter) adapterView.getAdapter(),
					getActivity(), position).execute();
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		new RefreshCameras(getActivity(), (BaseAdapter) mAdapter, new OnPostExecuteListener() {

			@Override
			public void onPostExecute(Object obj) {
				mPullToRefreshLayout.setRefreshComplete();
			}

		}).execute();

	}
}