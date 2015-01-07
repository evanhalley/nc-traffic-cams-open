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
import java.util.List;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.api.Camera;
import com.emuneee.nctrafficcams.ui.ViewHolder;
import com.emuneee.nctrafficcams.ui.activities.MainActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Binds the cursor containing pointers to camera records to your list view
 *
 * @author ehalley
 *
 */
public class NearMeCamerasAdapter extends ArrayAdapter<Camera> implements RetrievableCameraAdapter {
	private LayoutInflater mInflater;

	public NearMeCamerasAdapter(Context context, List<Camera> cameras) {
		super(context, 0, cameras);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if(convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.gallery_item, parent, false);
			holder.textViewTitle = (TextView) convertView
					.findViewById(R.id.text_view_title);
			holder.imageViewThumbnail = (ImageView) convertView
					.findViewById(R.id.image_view_camera);
			holder.imageViewFavorite = (ImageView) convertView
					.findViewById(R.id.image_view_favorite);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Camera camera = getItem(position);
		holder.textViewTitle.setText(camera.getTitle());
		holder.imageViewFavorite.setVisibility(camera.isFavorite() ?
				View.VISIBLE : View.INVISIBLE);
		MainActivity.getImageWorker().loadImage(camera.getUrl(),
				holder.imageViewThumbnail, camera);

		return convertView;
	}

	@Override
	public Camera getCamera(int i) {
		return getItem(i);
	}

	@Override
	public ArrayList<Camera> getCameras() {
		ArrayList<Camera> cameras = new ArrayList<Camera>(getCount());
		for(int i = 0; i < getCount(); i++) {
			cameras.add(getItem(i));
		}
		return cameras;
	}
}