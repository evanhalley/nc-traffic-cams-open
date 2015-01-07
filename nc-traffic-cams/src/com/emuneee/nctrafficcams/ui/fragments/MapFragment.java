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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.ui.Constants;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Handles map UI interaction
 * @author evan
 *
 */
public class MapFragment extends SupportMapFragment implements OnMarkerClickListener {
	private static final int sPadding = 100;
	private static final LatLng sNCLatLng = new LatLng(35.6006, -79.4508);
	private GoogleMap mMap;
	private Map<String, Camera> mCameraMap;
	private ActionBar mActionBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        return root;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mMap = getMap();
		if(mMap != null) {
			UiSettings settings = mMap.getUiSettings();
			settings.setCompassEnabled(true);
			settings.setMyLocationButtonEnabled(true);
			mMap.setMyLocationEnabled(true);
	        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	        mMap.setTrafficEnabled(true);
	        mMap.setOnMarkerClickListener(this);
	        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sNCLatLng, 6), 1, null);
		} else {
			Toast.makeText(getActivity(), "Error accessig Maps API", Toast.LENGTH_SHORT).show();
		}
		mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
	}

	@Override
	public void onDestroy() {
		if(mMap != null) {
			mMap.clear();
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && mMap != null) {
			ArrayList<Parcelable> cameras = null;
			String title = extras.getString(Constants.BUNDLE_METRO);
			mActionBar.setTitle(R.string.app_name);
			mActionBar.setSubtitle(title);

			if (extras.containsKey(Constants.BUNDLE_CAMERAS)) {
				cameras = extras
						.getParcelableArrayList(Constants.BUNDLE_CAMERAS);
			} else if (extras.containsKey(Constants.BUNDLE_CAMERA)) {
				cameras = new ArrayList<Parcelable>(1);
				cameras.add(extras.getParcelable(Constants.BUNDLE_CAMERA));
			}

			if (cameras != null) {
				new AddCamerasToMap(this.getActivity(), cameras, mMap).execute();
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		Camera camera = mCameraMap.get(arg0.getTitle());
		Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.camera_detail_fragment);
		// set the variables
		TextView textViewTitle = (TextView) dialog
				.findViewById(R.id.text_view_detail_title);
		TextView textViewMetro = (TextView) dialog
				.findViewById(R.id.text_view_detail_metro);
		ImageView imageView = (ImageView) dialog
				.findViewById(R.id.image_view_detail);
		textViewTitle.setText(camera.getTitle());
		textViewMetro.setText(camera.getCity());
		MainActivity.getImageWorker().loadImage(camera.getUrl(), imageView, camera);
		dialog.show();
		return true;
	}

	/**
	 * Puts the cameras into a map overlay to be displayed on our map
	 * @author Evan
	 *
	 */
	private class AddCamerasToMap extends AsyncTask<Void, MarkerOptions, CameraUpdate> {
		private Context mContext;
		private ArrayList<Parcelable> mParcels;
		private GoogleMap mMap;
		private ProgressDialog mDialog;

		public AddCamerasToMap(Context context, ArrayList<Parcelable> parcels,
				GoogleMap map) {
			mContext = context;
			mParcels = parcels;
			mMap = map;
			mCameraMap = new HashMap<String, Camera>(parcels.size());
		}

		@Override
		protected void onPreExecute() {
			mDialog = ProgressDialog.show(mContext,
					mContext.getString(R.string.please_wait),
					mContext.getString(R.string.populating_map));
		}

		@Override
		protected void onProgressUpdate(MarkerOptions... values) {
			MarkerOptions options = values[0];
			mMap.addMarker(options);
		}

		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		protected CameraUpdate doInBackground(Void... params) {
			Double maxLat = null;
			Double minLat = null;
			Double maxLon = null;
			Double minLon = null;

			for (Parcelable parcel : mParcels) {
				Camera camera = (Camera) parcel;
				LatLng latLng = new LatLng(camera.getLatitude(), camera.getLongitude());
				MarkerOptions options = new MarkerOptions();
				options.title(camera.getTitle());
				options.position(latLng);
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin));
				options.draggable(false);
				publishProgress(options);
				mCameraMap.put(camera.getTitle(), camera);

				maxLat = maxLat == null || camera.getLatitude() > maxLat ? camera.getLatitude() : maxLat;
				minLat = minLat == null || camera.getLatitude() < minLat ? camera.getLatitude() : minLat;
				maxLon = maxLon == null || camera.getLongitude() > maxLon ? camera.getLongitude() : maxLon;
				minLon = minLon == null || camera.getLongitude() < minLon ? camera.getLongitude() : minLon;
			}
			// get the screen size
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			int width = 0;
			int height = 0;
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
				Point size = new Point();
				display.getSize(size);
				width = size.x;
				height = size.y;
			} else {
				width = display.getWidth();
				height = display.getHeight();
			}

			LatLng sw = new LatLng(minLat, minLon);
			LatLng ne = new LatLng(maxLat, maxLon);
			CameraUpdate update = CameraUpdateFactory.newLatLngBounds(
					new LatLngBounds(sw, ne), width, height, sPadding);
			return update;
		}

		@Override
		protected void onPostExecute(CameraUpdate update) {
			mMap.animateCamera(update, 1, null);
			mDialog.dismiss();
		}
	}
}