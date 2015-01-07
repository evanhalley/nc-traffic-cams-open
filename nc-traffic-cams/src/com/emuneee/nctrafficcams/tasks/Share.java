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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.emuneee.nctrafficcams.ui.activities.MainActivity;

/**
 * Gets a URI from the image cache and starts the process
 * for sharing it via intent
 * @author ehalley
 *
 */
public class Share extends AsyncTask<String, Void, Uri> {
	private Activity mActivity;

	public Share(Activity activity) {
		mActivity = activity;
	}

	@Override
	protected Uri doInBackground(String... params) {
		return Uri.parse("file://"
				+ MainActivity.getImageWorker().getImageCache()
						.getPath(params[0]));
	}

	@Override
	protected void onPostExecute(Uri uri) {
		if (uri != null) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			intent.setType("image/jpeg");
			mActivity.startActivity(Intent.createChooser(intent, "Share via"));
		} else {

		}
	}
}
