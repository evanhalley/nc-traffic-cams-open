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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.emuneee.camerasyncmanager.Camera;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * @author evan
 *
 */
public class DatabaseUtil {

	private static final Logger sLogger = Logger.getLogger("DatabaseUtil");
	private static DatabaseUtil sInstance = null;

	private MongoClient mMongoClient = null;
	private Properties mProperties = null;

	public static DatabaseUtil getInstance() throws Exception {

		if(sInstance == null) {
			sInstance = new DatabaseUtil();
		}

		return sInstance;
	}

	private DatabaseUtil() throws Exception {
		try {
			mProperties = PropertyUtils.readFile("conf/database.properties");
			initMongoClient();
		} catch (Exception e) {
			sLogger.error("Error occurred during construction");
			sLogger.error(e);
			throw e;
		}
	}

	/**
	 * Closes the MongoDB connection
	 */
	public void destroyInstance() {
		if(mMongoClient != null) {
			mMongoClient.close();
		}
		sInstance = null;
	}

	private void initMongoClient() throws UnknownHostException {
		sLogger.info("Initializing MongoDB Client");
		String dbUrl = mProperties.getProperty("DATABASE_URL");
		int port = Integer.parseInt(mProperties.getProperty("DATABASE_PORT"));
		sLogger.debug("Database URL: " + dbUrl);
		sLogger.debug("Database Port: " + String.valueOf(port));

		// create the mongo client and connect
		ServerAddress serverAddr = new ServerAddress(mProperties.getProperty("DATABASE_URL"),
				Integer.parseInt(mProperties.getProperty("DATABASE_PORT")));
		MongoCredential credential = MongoCredential.createMongoCRCredential(
				mProperties.getProperty("DATABASE_USER"), mProperties.getProperty("DATABASE_NAME"),
				mProperties.getProperty("DATABASE_AUTH").toCharArray());
		mMongoClient = new MongoClient(serverAddr, Arrays.asList(credential));
	}

	/**
	 * Returns a reference to the database
	 * @return
	 */
	private DB getDatabase() {
		DB db = mMongoClient.getDB(mProperties.getProperty("DATABASE_NAME"));
		return db;
	}

	/**
	 * Takes a list of cameras and updates existing cameras and inserts new ones
	 * @param cameras
	 * @return
	 */
	public boolean upsertCameras(List<Camera> cameras) {
		sLogger.info("Attempting to upsert " + cameras.size() + " cameras");
		boolean result = false;
		List<Camera> insert = new ArrayList<Camera>();
		List<Camera> update = new ArrayList<Camera>();

		for(Camera currentCamera : cameras) {
			Camera camera = getCamera(currentCamera.getId());

			if(camera == null) {

				if (currentCamera.containsAllRequiredData()) {
					// insert new camera
					sLogger.info("Inserting new camera");
					sLogger.debug(currentCamera);
					currentCamera.setCreated(new Date());
					currentCamera.setUpdated(new Date());
					insert.add(currentCamera);
				} else {
					sLogger.warn("Camera data was not complete: " + currentCamera);
				}
			} else {
				if (camera.containsAllRequiredData()) {

					// update existing camera
					if(!camera.equals(currentCamera)) {
						sLogger.info("Updating existing camera with id: " + currentCamera.getId());
						sLogger.debug(currentCamera);
						currentCamera.setUpdated(new Date());
						update.add(currentCamera);
					} else {
						sLogger.warn("Camera data was not complete: " + camera);
					}
				}
			}
		}

		insertCameras(insert);
		updateCameras(update);
		return result;
	}

	/**
	 * Returns a camera with id
	 * @param id
	 * @return
	 */
	public Camera getCamera(String id) {
		Camera camera = null;
		DB db = getDatabase();
		DBCursor cursor = null;

		try {
			DBCollection collection = db.getCollection("camera");
			BasicDBObject query = new BasicDBObject("_id", id);
			cursor = collection.find(query);

			if(cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				camera = dbObjectToCamera(dbObj);
			}
		} catch (Exception e) {
			sLogger.error("Exception retrieving camera with id " + id);
			sLogger.error(e);
		} finally {
			HttpHelper.closeResources(new Object[]{ cursor });
		}

		return camera;
	}

	/**
	 * Updates a list cameras
	 * @param cameras
	 * @return
	 */
	public boolean updateCameras(List<Camera> cameras) {
		sLogger.info("Updating " + cameras.size() + " cameras");
		boolean result = false;
		DB db = getDatabase();

		try {
			DBCollection collection = db.getCollection("camera");

			for (Camera camera : cameras) {
				BasicDBObject query = new BasicDBObject("_id", camera.getId());
				BasicDBObject dbObj = cameraToDBObject(camera);
				dbObj.append("updated", new Date());
				sLogger.debug("Updating: " + dbObj.toString());
				collection.update(query, dbObj);
				result = true;
			}
		} catch (Exception e) {
			sLogger.error("Exception inserting cameras");
			sLogger.error(e);
		}

		return result;
	}

	/**
	 * Inserts a list of cameras
	 * @param cameras
	 * @return
	 */
	public boolean insertCameras(List<Camera> cameras) {
		sLogger.info("Inserting " + cameras.size() + " cameras");
		boolean result = false;
		DB db = getDatabase();

		try {
			DBCollection collection = db.getCollection("camera");

			for (Camera camera : cameras) {
				BasicDBObject dbObj = cameraToDBObject(camera);
				sLogger.debug("Inserting: " + dbObj.toString());
				collection.insert(dbObj);
				result = true;
			}
		} catch (Exception e) {
			sLogger.error("Exception inserting cameras");
			sLogger.error(e);
		}

		return result;
	}

	/**
	 * Converts BasicDBObject to camera
	 * @param dbObj
	 * @return
	 */
	public Camera dbObjectToCamera(BasicDBObject dbObj) {
		Camera camera = new Camera();
		camera.setId(dbObj.getString("_id"));
		camera.setTitle(dbObj.getString("title"));
		camera.setLatitude(dbObj.getDouble("latitude"));
		camera.setLongitude(dbObj.getDouble("longitude"));
		camera.setCity(dbObj.getString("city"));
		camera.setArea(dbObj.getString("area"));
		camera.setState(dbObj.getString("state"));
		camera.setZipCode(dbObj.getString("zipcode"));
		camera.setCountry(dbObj.getString("country"));
		camera.setUrl(dbObj.getString("url"));
		camera.setCreated(dbObj.getDate("created"));
		camera.setUpdated(dbObj.getDate("updated"));
		return camera;
	}

	/**
	 * Converts camera to BasicDBObject
	 * @param camera
	 * @return
	 */
	public BasicDBObject cameraToDBObject(Camera camera) {
		BasicDBObject dbObj = new BasicDBObject();
		dbObj.append("_id", camera.getId());
		dbObj.append("title", camera.getTitle());
		dbObj.append("latitude", camera.getLatitude());
		dbObj.append("longitude", camera.getLongitude());
		dbObj.append("city", camera.getCity());
		dbObj.append("area", camera.getArea());
		dbObj.append("state", camera.getState());
		dbObj.append("zipcode", camera.getZipCode());
		dbObj.append("country", camera.getCountry());
		dbObj.append("url", camera.getUrl());
		dbObj.append("created", camera.getCreated());
		dbObj.append("updated", camera.getUpdated());
		return dbObj;
	}
}