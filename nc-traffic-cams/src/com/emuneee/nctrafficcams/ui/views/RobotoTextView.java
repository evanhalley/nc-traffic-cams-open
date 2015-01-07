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
package com.emuneee.nctrafficcams.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author evan
 *
 */
public class RobotoTextView extends TextView {

	private Context mContext;

	public RobotoTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		createFont();
	}

	public RobotoTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		createFont();
	}

	public RobotoTextView(Context context) {
		super(context);
		mContext = context;
		createFont();
	}

	public void createFont() {
		Typeface font = Typeface.createFromAsset(mContext.getAssets(),
				"Roboto-Light.ttf");
		setTypeface(font);
	}

	@Override
	public void setTypeface(Typeface tf) {
		super.setTypeface(tf);
	}
}
