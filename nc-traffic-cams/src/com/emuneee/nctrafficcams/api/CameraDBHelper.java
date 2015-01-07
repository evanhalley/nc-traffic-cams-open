/**
 * Copyright 	(C) 2012 Evan Halley
 * emuneee apps
 */
package com.emuneee.nctrafficcams.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emuneee.nctrafficcams.common.Category;
import com.emuneee.nctrafficcams.common.ConversionUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

/**
 * Manages our database of traffic cameras
 * 
 * @author Evan
 * 
 */
public class CameraDBHelper extends SQLiteOpenHelper {
	private final String TAG = getClass().getSimpleName();
	private final static int VERSION = 4;
	public final static String DATABASE = "nc_traffic_cams.sqlite";
	public final static String TC_TABLE = "traffic_camera";
	public final static String FAV_TABLE = "favorite_camera";
	
	private final static String sTcSchema =
			"CREATE TABLE IF NOT EXISTS " + TC_TABLE + " (" +
			"_id TEXT NOT NULL, " +
			"title TEXT NOT NULL, " +
			"latitude NUMERIC NOT NULL, " +
			"longitude NUMERIC NOT NULL, " +
			"url TEXT NOT NULL, " +
			"city TEXT NOT NULL, " +
			"zipCode TEXT NOT NULL, " +
			"updated NUMERIC);";
	
	private final static String sFavSchema =
			"CREATE TABLE IF NOT EXISTS " + FAV_TABLE + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
			"fk_trafficCameraId TEXT NOT NULL, " +
			"FOREIGN KEY (fk_trafficCameraId) REFERENCES traffic_camera(_id));";
	
	public Context mContext;
	private List<String> mFavoriteCameraUrls = null;

	public CameraDBHelper(Context context) {
		super(context, DATABASE, null, VERSION);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		createSchema(database);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
			
		Log.d(TAG, "Upgrade called...");
		Log.d(TAG, "Old Version: " + oldVersion + ", New Version: " + newVersion);
		
		// if moving from version 3 to 4, lets get the favorite cameras
		if(oldVersion == 3 && newVersion == 4) {
			//get favorite cameras
			String query = 
                    "SELECT tc.url AS url " +
                    "FROM " + TC_TABLE + " tc " +
                    "INNER JOIN " + FAV_TABLE + " fc ON tc.guid = fc.fk_trafficCameraGuid " +
                    "ORDER BY tc.title ASC";
			try {
				Cursor cursor = database.rawQuery(query, null);
				
				// get the fav cameras url
				if (cursor != null && cursor.moveToFirst()) {
					mFavoriteCameraUrls = new ArrayList<String>();
					
					do {
						mFavoriteCameraUrls.add(cursor.getString(cursor.getColumnIndex("url")));
					} while (cursor.moveToNext());
					
					cursor.close();
					Log.d(TAG, "Number of favorite cameras persisting: " + mFavoriteCameraUrls.size());
				}
			} catch (Exception e) {
				Log.e(TAG, "Error upgrading database");
				Log.w(TAG, e.getMessage());
			}
		}
		
		try {
			// drop the camera and favorite databases
			database.execSQL("DROP TABLE IF EXISTS " + TC_TABLE);
			database.execSQL("DROP TABLE IF EXISTS " + FAV_TABLE);
		} catch (Exception e) {
			Log.e(TAG, "Error dropping tables");
			Log.w(TAG, e.getMessage());
		}
		
		createSchema(database);
	}
	
	/**
	 * Creates the schema needed by the application
	 * @param database
	 */
	private void createSchema(SQLiteDatabase database) {
		
		try {
			Log.d(TAG, "Creating tables...");
			database.beginTransaction();
			database.execSQL(sTcSchema.toString());
			database.execSQL(sFavSchema.toString());
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, "Error creating database");
			Log.w(TAG, e.getMessage());
		} finally {
			database.endTransaction();
		}
	}

	/**
	 * Deletes all traffic cameras from the database
	 * 
	 * @return
	 */
	public boolean deleteAllTrafficCameras() {
		SQLiteDatabase database = getWritableDatabase();
		return database.delete(TC_TABLE, "1", null) > 0;
	}
	
	/**
	 * Deletes a traffic camera and removes the favorite if necessary
	 * @param camera
	 * @return
	 */
	public boolean deleteTrafficCamera(Camera camera) {
		boolean result = true;
		
		SQLiteDatabase database = getWritableDatabase();
		try {
			database.beginTransaction();
			// remove the favorite if exists
			database.delete(FAV_TABLE, "fk_trafficCameraId = '" + camera.getId() + "'", null);
			// delete the camera
			database.delete(TC_TABLE, "_id = " + camera.getId(), null);
			database.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.w(TAG, "Error deleting traffic camera");
			Log.w(TAG, e.getMessage());
		} finally {
			database.endTransaction();
		}
		
		return result;
	}

	/**
	 * Updates a traffic camera record
	 * 
	 * @param trafficCamera
	 *            traffic camera to update
	 * @return success of the insert operation
	 */
	public boolean updateTrafficCamera(Camera trafficCamera) {
		SQLiteDatabase database = getWritableDatabase();
		boolean result = false;
		String where = "_id = '" + trafficCamera.getId() + "'";
		ContentValues values = new ContentValues();
		values.put("title", trafficCamera.getTitle());
		values.put("url", trafficCamera.getUrl());
		values.put("latitude", trafficCamera.getLatitude());
		values.put("longitude", trafficCamera.getLongitude());
		values.put("updated", trafficCamera.getUpdated());
		values.put("city", trafficCamera.getCity());
		values.put("zipCode", trafficCamera.getZipCode());
		try {
			database.beginTransaction();
			result = database.update(TC_TABLE, values, where, null) > 0;
			database.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.w(TAG, "Error updating traffic camera");
			Log.w(TAG, e.getMessage());
		} finally {
			database.endTransaction();
		}

		return result;
	}
	
	public boolean addFavoriteCamera(Camera camera) {
		boolean result = false;
		SQLiteDatabase database = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("fk_trafficCameraId", camera.getId());
		try {
			database.beginTransaction();
			database.insert(FAV_TABLE, null, values);
			database.setTransactionSuccessful();
			result = true;
		} catch (SQLException e) {
			Log.w(TAG, "Error making traffic camera favorite");
			Log.w(TAG, e.getMessage());
		} finally {
			database.endTransaction();
		}
		
		return result;
	}
	
	public boolean removeFavoriteCamera(Camera camera) {
		boolean result = false;
		SQLiteDatabase database = getWritableDatabase();
		try {
			database.beginTransaction();
			database.delete(FAV_TABLE, "fk_trafficCameraId = '" + camera.getId() + "'", null);
			database.setTransactionSuccessful();
			result = true;
		} catch (SQLException e) {
			Log.w(TAG, "Error making traffic camera favorite");
			Log.w(TAG, e.getMessage());
		} finally {
			database.endTransaction();
		}
		
		return result;
	}

	/**
	 * Returns a cursor to a list of traffic cameras
	 * 
	 * @return cursor that points to records of traffic cameras
	 */
	public Cursor getAllCameras() {
		String query = 
				"SELECT tc._id AS _id, tc.title AS title, tc.city AS city, tc.url AS url, " +
				"tc.latitude AS latitude, tc.longitude AS longitude, tc.zipCode AS zipCode," +
				"tc.updated AS updated, IFNULL(fc._id, 0) AS favorite " +
				"FROM " + TC_TABLE + " tc " +
				"LEFT JOIN favorite_camera fc ON tc._id = fc.fk_trafficCameraId " +
				"ORDER BY tc.title ASC";
		SQLiteDatabase database = getReadableDatabase();
		return database.rawQuery(query, null);
	}

	/**
	 * Returns a cursor to a list of traffic cameras
	 * 
	 * @return cursor that points to records of traffic cameras
	 */
	public Cursor getFavoriteCameras() {
		
		String query = 
				"SELECT tc._id AS _id, tc.title AS title, tc.city AS city, tc.url AS url, " +
				"tc.latitude AS latitude, tc.longitude AS longitude, tc.zipCode AS zipCode," +
				"tc.updated AS updated, 1 AS favorite " +
				"FROM " + TC_TABLE + " tc " +
				"INNER JOIN favorite_camera fc ON tc._id = fc.fk_trafficCameraId " +
				"ORDER BY tc.title ASC";
		return getReadableDatabase().rawQuery(query, null);
	}

	/**
	 * Returns a cursor to a list of traffic cameras
	 * 
	 * @return cursor that points to records of traffic cameras
	 */
	public Cursor getCamerasByCity(String city) {
		String query = 
				"SELECT tc._id AS _id, tc.title AS title, tc.city AS city, tc.url AS url, " +
				"tc.latitude AS latitude, tc.longitude AS longitude, tc.zipCode AS zipCode," +
				"tc.updated AS updated, IFNULL(fc._id, 0) AS favorite " +
				"FROM " + TC_TABLE + " tc " +
				"LEFT JOIN favorite_camera fc ON tc._id = fc.fk_trafficCameraId " +
				"WHERE tc.city = '" + city + "' " +
				"ORDER BY tc.title ASC";
		return getReadableDatabase().rawQuery(query, null);
	}
	
	/**
	 * Returns a cursor to a list of traffic cameras
	 * 
	 * @return cursor that points to records of traffic cameras
	 */
	public Cursor getCamerasByUrl(String url) {
		String query = 
				"SELECT tc._id AS _id, tc.title AS title, tc.city AS city, tc.url AS url, " +
				"tc.latitude AS latitude, tc.longitude AS longitude, tc.zipCode AS zipCode," +
				"tc.updated AS updated, IFNULL(fc._id, 0) AS favorite " +
				"FROM " + TC_TABLE + " tc " +
				"LEFT JOIN favorite_camera fc ON tc._id = fc.fk_trafficCameraId " +
				"WHERE tc.url LIKE '%" + url + "%' " +
				"ORDER BY tc.title ASC";
		return getReadableDatabase().rawQuery(query, null);
	}

	/**
	 * Inserts a list of traffic cameras into the database
	 * 
	 * @param trafficCameras
	 *            traffic cameras to insert
	 * @return success of the insert operation
	 */
	public boolean insertTrafficCameras(List<Camera> trafficCameras) {
		SQLiteDatabase database = getWritableDatabase();
		boolean result = false;
		try {
			database.beginTransaction();
			for (Camera trafficCamera : trafficCameras) {
				ContentValues values = new ContentValues();
				values.put("title", trafficCamera.getTitle());
				values.put("url", trafficCamera.getUrl());
				values.put("latitude", trafficCamera.getLatitude());
				values.put("longitude", trafficCamera.getLongitude());
				values.put("city", trafficCamera.getCity());
				values.put("zipCode", trafficCamera.getZipCode());
				values.put("updated", trafficCamera.getUpdated());
				values.put("_id", trafficCamera.getId());
				result = database.insert(TC_TABLE, null, values) > 0;
			}
			database.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.w(TAG, "Error inserting traffic cameras");
			Log.w(TAG, e.getMessage());
		} finally {
			database.endTransaction();
		}

		return result;
	}

	/**
	 * Returns a distinct list of cities containing cameras
	 * 
	 * @return list of cities with cameras
	 */
	public List<String> getCities() {
		List<String> cityList = null;
		SQLiteDatabase database = getReadableDatabase();
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT city FROM ")
				.append(TC_TABLE).append(" ORDER BY city ASC");
		Cursor cursor = database.rawQuery(query.toString(), null);
		if (cursor != null) {
			cityList = new ArrayList<String>(cursor.getCount());
			while (cursor.moveToNext()) {
				cityList.add(cursor.getString(cursor.getColumnIndex("city")));
			}
			cursor.close();
		}
		return cityList;
	}

	/**
	 * Returns a distinct list of zip codes containing cameras
	 * 
	 * @return list of zip codes with cameras
	 */
	public List<String> getZipCodes() {
		List<String> cityList = null;
		SQLiteDatabase database = getReadableDatabase();
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT zipCode FROM").append(TC_TABLE);
		Cursor cursor = database.rawQuery(query.toString(), null);
		if (cursor != null) {
			cityList = new ArrayList<String>(cursor.getCount());
			while (cursor.moveToNext()) {
				cityList.add(cursor.getString(cursor
						.getColumnIndex("zipCode")));
			}
			cursor.close();
		}
		return cityList;
	}
	
	/**
	 * Updates, inserts, or removes cameras
	 * @param cameras
	 */
	public void updateCameras(Map<String, Camera> cameras) {
		// get existing cameras
		Cursor cursor = getAllCameras();
		Map<String, Camera> existingCameras = cursorToCameraMap(cursor);
		cursor.close();
		// see which cameras to insert and which to update
		List<Camera> camerasToUpdate = new ArrayList<Camera>();
		List<Camera> camerasToInsert = new ArrayList<Camera>();
		List<Camera> camerasToDelete = new ArrayList<Camera>();
		
		for(Camera camera : cameras.values()) {
			if(existingCameras.containsKey(camera.getId())) {
				camerasToUpdate.add(camera);
			} else {
				camerasToInsert.add(camera);
			}
		}
		// see which cameras to delete
		for(Camera existingCamera : existingCameras.values()) {
			if (!cameras.containsKey(existingCamera.getId())) {
				camerasToDelete.add(existingCamera);
			}
		}
		Log.d(TAG, "Cameras to update: " + camerasToUpdate.size());
		Log.d(TAG, "Cameras to insert: " + camerasToInsert.size());
		Log.d(TAG, "Cameras to delete: " + camerasToDelete.size());
		// insert new cameras
		insertTrafficCameras(camerasToInsert);
		// update existing cameras
		for(Camera camera : camerasToUpdate) {
			updateTrafficCamera(camera);
		}
		// delete cameras
		for(Camera camera : camerasToDelete) {
			deleteTrafficCamera(camera);
		}
		
		transitionFavorites();
	}
	
	/**
	 * Finishes copying favorite cameras to the new schema
	 */
	private void transitionFavorites() {
		
		// are their favorite cameras to transition
		if(mFavoriteCameraUrls != null && mFavoriteCameraUrls.size() > 0) {
			Log.d(TAG, "There are favorite traffic cameras to transition");
			
			for(String url : mFavoriteCameraUrls) {
				// get camera  by url
				Log.d(TAG, "Getting traffic camera by url " + url);
				Cursor cursor = getCamerasByUrl(url);
				
				if(cursor.moveToFirst()) {
					Camera camera = cursorToCamera(cursor);
					Log.d(TAG, "Camera to favorite: " + camera.toString());
					// mark it as a favorite
					addFavoriteCamera(camera);
				}
				cursor.close();
			}
			mFavoriteCameraUrls.clear();
			mFavoriteCameraUrls = null;
		}
	}

	/**
	 * Creates a list of traffic cameras from a database cursor
	 * 
	 * @param cursor
	 *            cursor containing traffic camera records
	 * @return list of traffic cameras
	 */
	public static Camera cursorToCamera(Cursor cursor) {
		Camera trafficCamera = new Camera();

		trafficCamera.setTitle(cursor.getString(cursor
				.getColumnIndex("title")));
		trafficCamera.setUrl(cursor.getString(cursor.getColumnIndex("url")));
		trafficCamera.setId(cursor.getString(cursor.getColumnIndex("_id")));
		trafficCamera.setLatitude(cursor.getDouble(cursor
				.getColumnIndex("latitude")));
		trafficCamera.setLongitude(cursor.getDouble(cursor
				.getColumnIndex("longitude")));
		trafficCamera
				.setCity(cursor.getString(cursor.getColumnIndex("city")));
		trafficCamera.setZipCode(cursor.getString(cursor
				.getColumnIndex("zipCode")));
		trafficCamera.setUpdated(cursor.getLong(cursor
				.getColumnIndex("updated")));
		if(cursor.getInt(cursor.getColumnIndex("favorite")) == 0) {
			trafficCamera.setIsFavorite(false);
		} else {
			trafficCamera.setIsFavorite(true);
		}
		return trafficCamera;
	}

	/**
	 * Creates a list of traffic cameras from a database cursor
	 * 
	 * @param cursor
	 *            cursor containing traffic camera records
	 * @return list of traffic cameras
	 */
	public static ArrayList<Camera> cursorToCameras(Cursor cursor) {
		ArrayList<Camera> trafficCameras = new ArrayList<Camera>(cursor.getCount());
		if (cursor.moveToFirst()) {
			do {
				trafficCameras.add(cursorToCamera(cursor));
			} while (cursor.moveToNext());
		}
		return trafficCameras;
	}
	
	/**
	 * Converts cursor to a camera map
	 * @param cursor
	 * @return
	 */
	public static Map<String, Camera> cursorToCameraMap(Cursor cursor) {
		Map<String, Camera> cameraMap = new HashMap<String, Camera>();
		if (cursor.moveToFirst()) {
			do {
				Camera camera = cursorToCamera(cursor);
				String id = camera.getId();
				cameraMap.put(id, camera);
			} while (cursor.moveToNext());
		}
		
		return cameraMap;
	}
	
	/**
	 * Retrieves cameras within a distance from the user location
	 * @param distance
	 * @param userLocation
	 * @return
	 */
	public List<Camera> getCamerasWithDistance(double distance, Location userLocation) {
		Cursor cursor = getAllCameras();
		List<Camera> cameras = new ArrayList<Camera>();
		
		if(userLocation != null) {
			while (cursor.moveToNext()) {
				// calculate the distance between this camera and the user's location
				float[] results = new float[1];
				Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), 
						cursor.getDouble(cursor.getColumnIndex("latitude")), cursor.getDouble(cursor.getColumnIndex("longitude")),
						results);	
				double calculatedDistance = ConversionUtils.metersToMiles(results[0]);
				
				// is the camera in close proximity
				if(calculatedDistance <= distance) {
					cameras.add(cursorToCamera(cursor));
				}
			}
			cursor.close();
		} else {
			Log.w(TAG, "User location is null");
			EasyTracker.getInstance(mContext).send(MapBuilder
		      .createEvent(Category.Other.name(), "User location is null", "User location is null", null)
		      .build());
		}
		Log.d(TAG,
				cameras.size() + " cameras found within "
						+ String.valueOf(distance)
						+ " miles of the users location");
		
		return cameras;
	}
}
