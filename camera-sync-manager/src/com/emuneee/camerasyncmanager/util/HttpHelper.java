/*
 * Copyright (C) 2014 http://emuneee.com/blog/apps
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
package com.emuneee.camerasyncmanager.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

import com.mongodb.DBCursor;

/**
 * @author evan
 *
 */
public class HttpHelper {

	private static final Logger sLogger = Logger.getLogger("HttpHelper");

	public static void closeResources(Object[] resources) {

		for (Object resource : resources) {
			if (resource != null) {
				try {
					if (resource instanceof HttpURLConnection) {
						((HttpURLConnection) resource).disconnect();
					} else if (resource instanceof Closeable) {
						((Closeable) resource).close();
					} else if (resource instanceof Connection) {
						((Connection) resource).close();
					} else if (resource instanceof PreparedStatement) {
						((PreparedStatement) resource).close();
					} else if (resource instanceof DBCursor) {
						((DBCursor) resource).close();
					}
				} catch (Exception e) {
					sLogger.error("Error closing resources");
					sLogger.error(e.getMessage());
				}
			}
		}
	}

	public static String getStringDataFromUrl(String urlStr) {
		HttpURLConnection conn = null;
		InputStream stream = null;
		InputStreamReader iReader = null;
		BufferedReader bReader = null;
		StringBuilder data = new StringBuilder();
		String dataStr = null;

		try {
			sLogger.debug("Getting string data from " + urlStr);
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			stream = conn.getInputStream();
			iReader = new InputStreamReader(stream);
			bReader = new BufferedReader(iReader);
			String line = null;

			while ((line = bReader.readLine()) != null) {
				data.append(line);
			}
			dataStr = data.toString();
		} catch (Exception e) {
			sLogger.error("Error getting string data from url");
			sLogger.error(e.getMessage());
		} finally {
			closeResources(new Object[] { conn, stream, iReader, bReader });
		}

		return dataStr;
	}

}
