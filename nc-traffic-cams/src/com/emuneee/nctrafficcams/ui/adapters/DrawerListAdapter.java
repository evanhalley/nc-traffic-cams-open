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

import java.util.List;

import com.emuneee.nctrafficcams.R;
import com.emuneee.nctrafficcams.ui.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class DrawerListAdapter extends BaseExpandableListAdapter {
	public static final int ALL_GROUP = 0;
	public static final int FAVORITES_GROUP = 1;
	public static final int NEAR_ME_GROUP = 2;
	public static final int ROUTES_GROUP = -10;
	public static final int CITIES_GROUP = 3;

	private List<String> mGroups;
	private List<String> mMetros;
	private LayoutInflater mInflater;

	public DrawerListAdapter(Context context, List<String> groups, List<String> metros) {
		mGroups = groups;
		mMetros = metros;
		mInflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		Object child = null;
		switch(groupPosition) {
		case CITIES_GROUP:
			child = mMetros.get(childPosition);
		}
		return child;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		long child = -1;
		switch(groupPosition) {
		case CITIES_GROUP:
			child = mMetros.get(childPosition).hashCode();
		}
		return child;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(groupPosition == CITIES_GROUP) {
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.drawer_child_item, parent,
						false);
				holder = new ViewHolder();
				holder.textViewTitle = (TextView) convertView
						.findViewById(R.id.text_view_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textViewTitle.setText(mMetros.get(childPosition));
			return convertView;
		}
		return null;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int count = -1;
		switch(groupPosition) {
		case CITIES_GROUP:
			count = mMetros.size();
		}
		return count;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return mGroups.get(groupPosition).hashCode();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.drawer_group_item, parent,
					false);
			holder = new ViewHolder();
			holder.textViewTitle = (TextView) convertView
					.findViewById(R.id.text_view_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textViewTitle.setText(mGroups.get(groupPosition));
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}