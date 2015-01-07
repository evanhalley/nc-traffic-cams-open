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
package com.emuneee.camerasyncmanager;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.emuneee.camerasyncmanager.adapter.NCDOTAdapter;
import com.emuneee.camerasyncmanager.util.HttpHelper;

/**
 * @author evan
 *
 */
public class CameraSyncManager {

	private static final Logger sLogger = Logger.getLogger("CameraSyncManager");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FileInputStream inputStream = null;
		// initiate the properties
		Properties properties = new Properties();
		PropertyConfigurator.configure("conf/log4j.properties");

		try {
			sLogger.info("Starting Camera Sync Manager");
			inputStream = new FileInputStream("conf/camerasyncmanager.properties");
			properties.load(inputStream);
			new NCDOTAdapter(properties).sync();
		} catch (Exception e) {
			sLogger.error("Error encountered");
			sLogger.error(e.getMessage());
			sLogger.error(e);
			e.printStackTrace();
		} finally {
			HttpHelper.closeResources(new Object[]{ inputStream });
		}
	}
}