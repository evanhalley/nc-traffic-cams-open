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
package com.emuneee.camerasyncmanager.adapter;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.emuneee.camerasyncmanager.Camera;
import com.emuneee.camerasyncmanager.util.DatabaseUtil;
import com.emuneee.camerasyncmanager.util.GeocodeHelper;
import com.emuneee.camerasyncmanager.util.HashUtil;
import com.emuneee.camerasyncmanager.util.HttpHelper;

/**
 * Retrieves, geocodes, and stores traffic camera data
 * @author evan
 *
 */
public class NCDOTAdapter {
	private final static Logger sLogger = Logger.getLogger("NCDOTAdapter");
	private final static String sCameraUrl = "http://tims.ncdot.gov/TIMS/RSS/CameraGeoRSS.aspx";
	private final static String sBLatitude = "BLatitude";
	private final static String sBLongitude = "BLongitude";
	private final static String sTLatitude = "TLatitude";
	private final static String sTLongitude = "TLongitude";
	private final static Double sTLatitudeVal = 37.02;
	private final static Double sTLongitudeVal = -87.82;
	private final static Double sBLatitudeVal = 31.58;
	private final static Double sBLongitudeVal = -69.77;

	private Properties mProperties = null;
	private DatabaseUtil mDbUtil = null;
	private int mMaxCamerasToProcess = -1;

	public NCDOTAdapter(Properties properties) throws Exception {
		sLogger.debug("Constructing...");
		mProperties = properties;
		mDbUtil = DatabaseUtil.getInstance();
		mMaxCamerasToProcess = Integer.parseInt(mProperties.getProperty("MAX_CAMERAS_TO_SYNC"));
	}

	public boolean sync() throws Exception {
		boolean result = false;
		long sleep = Long.parseLong(mProperties.getProperty("GEOCODE_SLEEP_TIME_MS"));
		boolean writeToDb = Boolean.parseBoolean(mProperties.getProperty("WRITE_TO_DATABASE"));

		// get the cameras from NC DOT
		List<Camera> cameras = getCameraXML();
		// filter out cameras that haven't changed
		cameras = getNewOrUpdatedCameras(cameras);
		cameras = GeocodeHelper.geocodeCameras(cameras, sleep);

		if(writeToDb) {
			mDbUtil.upsertCameras(cameras);
		} else {
			sLogger.info("Not inserting cameras, dumping them to the logs");

			for (Camera camera : cameras) {
				sLogger.info(camera);
			}
		}

		mDbUtil.destroyInstance();
		return result;
	}

	/**
	 * Determines which cameras we need to update or add
	 * These cameras will then have their geocoding data updated
	 * @param cameras
	 * @return
	 */
	private List<Camera> getNewOrUpdatedCameras(List<Camera> parsedCameras) {
		List<Camera> cameras = new ArrayList<Camera>();

		for(Camera parsedCamera : parsedCameras) {
			Camera existingCamera = mDbUtil.getCamera(parsedCamera.getId());

			if (existingCamera == null || !parsedCamera.equalsWithoutGeo(existingCamera)) {

				if(!parsedCamera.getTitle().contains("Camera Image Currently Unavailable")) {
					sLogger.info("Camera is new or needs to be updated: " + parsedCamera);
					// new camera to add or some parts of the xml metadata for the camera has changed
					cameras.add(parsedCamera);
				} else {
					sLogger.warn("Unavailable camera found: " + parsedCamera);
				}
			}
		}

		sLogger.info("Number of cameras that are new or need to be updated: " + cameras.size());
		return cameras;
	}

	private List<Camera> getCameraXML() {
		List<Camera> cameras = null;
		HttpURLConnection conn = null;
		BufferedReader br = null;

		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append(sCameraUrl).append("?");
			urlBuilder.append(sBLatitude).append("=").append(sBLatitudeVal).append("&");
			urlBuilder.append(sBLongitude).append("=").append(sBLongitudeVal).append("&");
			urlBuilder.append(sTLatitude).append("=").append(sTLatitudeVal).append("&");
			urlBuilder.append(sTLongitude).append("=").append(sTLongitudeVal);

			sLogger.info("Cameras URL: " + urlBuilder.toString());

			URL url = new URL(urlBuilder.toString());
			conn = (HttpURLConnection) url.openConnection();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser parser = spf.newSAXParser();
            XMLReader xr = parser.getXMLReader();
            XmlHandler handler = new XmlHandler();
            xr.setContentHandler(handler);
            InputSource is = new InputSource(conn.getInputStream());
            xr.parse(is);
			cameras = handler.getCameras(mMaxCamerasToProcess);
			sLogger.info("Parsed " + cameras.size() + " cameras from XML");
		} catch (Exception e) {
			sLogger.error("Error parsing NC DOT Camera XML");
			sLogger.error(e);
		} finally {
			HttpHelper.closeResources(new Object[]{ conn, br });
		}
		return cameras;
	}

	private class XmlHandler extends DefaultHandler {
		private boolean mInItem = false;
		private boolean mGetVal = false;
		private HashMap<String, Camera> mCameras = null;
		private Camera mCurrentCamera = null;
		private StringBuilder mValue = null;

		public XmlHandler() {
			super();
			mCameras = new HashMap<String, Camera>();
			mValue = new StringBuilder();
		}

		public List<Camera> getCameras(int maxCameras) {
			List<Camera> cameras = null;

			if(maxCameras > -1) {
				cameras = new ArrayList<Camera>(maxCameras);

				for(Camera camera : mCameras.values()) {
					cameras.add(camera);

					if(cameras.size() == maxCameras) {
						break;
					}
				}
			} else {
				cameras = new ArrayList<Camera>(mCameras.values());
			}

			return cameras;
		}

		private void resetGetVal() {
			mGetVal = false;
			mValue = new StringBuilder();
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (mGetVal) {
				mValue.append(new String(ch, start, length));
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (qName.equalsIgnoreCase("item")) {
				mInItem = true;

				if(mCurrentCamera != null) {
					mCameras.put(mCurrentCamera.getId(), mCurrentCamera);
				}
				mCurrentCamera = new Camera();
			}

			if(mInItem) {
				if(qName.equalsIgnoreCase("title")) {
					mGetVal = true;
				} else if (qName.equalsIgnoreCase("georss:point")) {
					mGetVal = true;
				} else if (qName.equalsIgnoreCase("description")) {
					mGetVal = true;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equalsIgnoreCase("item")) {
				mInItem = false;
			}

			if(mInItem) {
				if(qName.equalsIgnoreCase("title")) {
					mCurrentCamera.setTitle(mValue.toString());
					resetGetVal();
				} else if (qName.equalsIgnoreCase("georss:point")) {
					String rawCoord = mValue.toString();
					resetGetVal();
					if(rawCoord != null) {
						String[] coordArr = rawCoord.split(" ");
						mCurrentCamera.setLatitude(Double.parseDouble(coordArr[0]));
						mCurrentCamera.setLongitude(Double.parseDouble(coordArr[1]));
					}
				} else if(qName.equalsIgnoreCase("description")) {
					String haystack = mValue.toString();
					int start = haystack.indexOf(".open('") + 7;
					int end = haystack.indexOf("'", start);
					String needle = haystack.substring(start, end).toLowerCase();
					mCurrentCamera.setUrl(needle);
					mCurrentCamera.setId(HashUtil.hash(needle));
					resetGetVal();
				}
			}
		}
	}
}