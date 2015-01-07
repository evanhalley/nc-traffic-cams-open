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
package com.emuneee.nctrafficcams.ui.adapters;

import java.util.ArrayList;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.api.CameraDBHelper;
import com.emuneee.nctrafficcams.ui.ViewHolder;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Binds the cursor containing pointers to camera records to your list view
 *
 * @author ehalley
 *
 */
public class DefaultCamerasAdapter extends CursorAdapter implements RetrievableCameraAdapter {
	private LayoutInflater mInflater;

	public DefaultCamerasAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public Camera getCamera(int position) {
		Camera trafficCamera = null;
		Cursor cursor = getCursor();
		int oldPos = cursor.getPosition();
		if (cursor.moveToPosition(position)) {
			trafficCamera = CameraDBHelper.cursorToCamera(cursor);
		}
		cursor.moveToPosition(oldPos);
		return trafficCamera;
	}

	@Override
	public void bindView(View v, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) v.getTag();
		Camera camera = CameraDBHelper.cursorToCamera(cursor);
		holder.textViewTitle.setText(camera.getTitle());
		holder.imageViewFavorite.setVisibility(camera.isFavorite() ?
				View.VISIBLE : View.INVISIBLE);
		MainActivity.getImageWorker().loadImage(camera.getUrl(),
				holder.imageViewThumbnail, camera);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup container) {
		ViewHolder holder = new ViewHolder();
		View view = mInflater.inflate(R.layout.gallery_item, container, false);
		holder.textViewTitle = (TextView) view
				.findViewById(R.id.text_view_title);
		holder.imageViewThumbnail = (ImageView) view
				.findViewById(R.id.image_view_camera);
		holder.imageViewFavorite = (ImageView) view
				.findViewById(R.id.image_view_favorite);
		view.setTag(holder);
		return view;
	}

	@Override
	public ArrayList<Camera> getCameras() {
		Cursor cursor = getCursor();
		int oldPos = cursor.getPosition();
		ArrayList<Camera> cameras = CameraDBHelper.cursorToCameras(cursor);
		cursor.moveToPosition(oldPos);
		return cameras;
	}
}
