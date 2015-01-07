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
package com.emuneee.nctrafficcams.common;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Handles common conversions
 *
 * @author Evan
 *
 */
@SuppressLint("SimpleDateFormat")
public class ConversionUtils {
	public final static double MILES_PER_METER = 0.000621371;
	public final static String DATE_FORMAT = "MM/dd hh:mm:ss aaa";
	public final static String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
	/**
	 * Converts milliseconds to a proper time
	 *
	 * @param milliseconds
	 *            time to convert
	 * @return time in the following format, MM/dd hh:mm aaa (ex. 8:45 PM)
	 */
	public static String getDateFromMS(long milliseconds) {
		// Create a DateFormatter object for displaying date in specified
		// format.
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

		// Create a calendar object that will convert the date and time value in
		// milliseconds to date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds);
		return formatter.format(calendar.getTime()).trim();
	}

	public static Date parseDate(String dateText, String dateFormat) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

		Date date = formatter.parse(dateText);

		return date;
	}

	public static double metersToMiles(double meters) {
		double miles = meters * MILES_PER_METER;
		return miles;
	}
}
