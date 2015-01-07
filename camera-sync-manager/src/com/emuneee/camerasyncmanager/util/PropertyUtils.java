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

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author evan
 *
 */
public class PropertyUtils {

	private static final Logger sLogger = Logger.getLogger("PropertiesHelper");

	/**
	 * Creates a properties object from a properties file
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Properties readFile(String file) throws Exception {
		FileInputStream inputStream = null;
		// initiate the properties
		Properties properties = null;

		try {
			sLogger.info("Reading properties file: " + file);
			inputStream = new FileInputStream(file);
			properties = new Properties();
			properties.load(inputStream);
		} catch (Exception e) {
			sLogger.error("Error encountered");
			sLogger.error(e.getMessage());
			throw e;
		} finally {
			HttpHelper.closeResources(new Object[]{ inputStream });
		}

		return properties;
	}

}