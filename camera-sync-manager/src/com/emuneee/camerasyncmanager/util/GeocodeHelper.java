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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.emuneee.camerasyncmanager.Camera;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Given a camera, it uses Google Geocoding API to pull
 * back additional location info for it
 * @author evan
 *
 */
public class GeocodeHelper {
	private final static Logger sLogger = Logger.getLogger("GeocodeHelper");
	private static final String sGeocodeUrl = "http://maps.googleapis.com/maps/api/geocode/json";
	private static final String sLatLng = "latlng";
	private static final String sSensor = "sensor=true";
	private static final int sRetryCount = 3;

	public static List<Camera> geocodeCameras(List<Camera> cameras, long sleep) throws Exception {
		List<Camera> geocoded = new ArrayList<Camera>(cameras.size());
		int count = 1;

		// loop through and geocode each camera
		for (Camera camera : cameras) {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(sGeocodeUrl).append("?");
			urlBuilder.append(sLatLng).append("=");
			urlBuilder.append(camera.getLatitude()).append(",")
					.append(camera.getLongitude());
			urlBuilder.append("&").append(sSensor);
			sLogger.debug("Geocode URL " + urlBuilder.toString());

			try {
				// retry
				String data = null;
				int retryCount = 0;

				while (data == null && retryCount < sRetryCount) {
					if(retryCount > 0) {
						sLogger.info("Retrying geocoding");
					}
					data = HttpHelper.getStringDataFromUrl(urlBuilder
							.toString());
					Thread.sleep(sleep);
					retryCount++;
				}

				if (data == null) {
					sLogger.warn("Unable to geocode the camera, no data returned " + camera);
				} else if (data != null && data.contains("OVER_QUERY_LIMIT")) {
					sLogger.warn("Unable to geocode the camera, query limit exceeded " + camera);
					throw new Exception("Unable to geocode the camera, query limit exceeded");
				} else {
					JSONObject jsonObj = (JSONObject) new JSONParser()
							.parse(data);
					JSONArray resultsArr = (JSONArray) jsonObj.get("results");

					if (resultsArr.size() > 0) {

						for (int i = 0; i < resultsArr.size(); i++) {
							JSONObject result = (JSONObject) resultsArr.get(i);

							// loop through the address components
							JSONArray addressComponents = (JSONArray) result
									.get("address_components");
							for (Object compObj : addressComponents) {
								JSONObject component = (JSONObject) compObj;
								String shortName = (String) component
										.get("short_name");
								JSONArray types = (JSONArray) component
										.get("types");

								// loop through the types
								for (Object typeObj : types) {
									String type = (String) typeObj;

									if (type.equalsIgnoreCase("administrative_area_level_3")) {
										camera.setArea(shortName);
										break;
									} else if (type.equalsIgnoreCase("locality")) {
										camera.setCity(shortName);
										break;
									} else if (type
											.equalsIgnoreCase("administrative_area_level_1")) {
										camera.setState(shortName);
										break;
									} else if (type.equalsIgnoreCase("country")) {
										camera.setCountry(shortName);
										break;
									} else if (type.equalsIgnoreCase("postal_code")) {
										camera.setZipCode(shortName);
										break;
									}
								}
							}

							if (camera.containsAllRequiredData()) {
								break;
							} else {
								sLogger.info("Some required data is missing, moving to the next address component set");
							}
						}
					}

					if (camera.containsAllRequiredData()) {
						geocoded.add(camera);
						sLogger.info("Camera " + count++ + " of " + cameras.size()
								+ " geocoded");
						sLogger.debug("Geocoded camera " + camera);
						sLogger.debug("Sleeping for " + sleep + " milliseconds");
					} else {
						sLogger.warn("Some required data is missing and geocoding results have been exhausted for camera: " + camera);
					}
				}
			} catch (InterruptedException e) {
				sLogger.error("Error geocoding address");
				sLogger.error(e.getMessage());
			} catch (ParseException e) {
				sLogger.error("Error geocoding address");
				sLogger.error(e.getMessage());
			}
		}

		return geocoded;
	}
}
