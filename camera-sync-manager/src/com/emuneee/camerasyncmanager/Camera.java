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

import java.util.Date;

/**
 * @author evan
 *
 */
public class Camera {

	private String mId;
	private String mTitle;
	private String mUrl;
	private Double mLatitude;
	private Double mLongitude;
	private String mCity;
	private String mArea;
	private String mZipCode;
	private String mState;
	private String mCountry;
	private Date mCreated;
	private Date mUpdated;

	public Camera() {

	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public Date getCreated() {
		return mCreated;
	}

	public void setCreated(Date created) {
		mCreated = created;
	}

	public Date getUpdated() {
		return mUpdated;
	}

	public void setUpdated(Date updated) {
		mUpdated = updated;
	}

	public void setLatitude(Double latitude) {
		mLatitude = latitude;
	}

	public void setLongitude(Double longitude) {
		mLongitude = longitude;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return mUrl;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		mUrl = url;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		mTitle = title;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	/**
	 * @return the zipCode
	 */
	public String getZipCode() {
		return mZipCode;
	}

	/**
	 * @param zipCode
	 *            the zipCode to set
	 */
	public void setZipCode(String zipCode) {
		mZipCode = zipCode;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return mCity;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		mCity = city;
	}

	public String getState() {
		return mState;
	}

	public void setState(String state) {
		this.mState = state;
	}

	public String getArea() {
		return mArea;
	}

	public void setArea(String area) {
		mArea = area;
	}

	public String getCountry() {
		return mCountry;
	}

	public void setCountry(String country) {
		mCountry = country;
	}

	public boolean containsAllRequiredData() {
		if (mTitle == null || mUrl == null || mLatitude == null || mLongitude == null ||
				mCity == null || mState == null || mZipCode == null || mCountry == null |
				mId == null) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "Camera [mId=" + mId + ", mTitle=" + mTitle + ", mUrl=" + mUrl
				+ ", mLatitude=" + mLatitude + ", mLongitude=" + mLongitude
				+ ", mCity=" + mCity + ", mArea=" + mArea + ", mZipCode="
				+ mZipCode + ", mState=" + mState + ", mCountry=" + mCountry
				+ ", mCreated=" + mCreated + ", mUpdated=" + mUpdated + "]";
	}

	/**
	 * Determines if the non-geo data is identical
	 * @param camera
	 * @return
	 */
	public boolean equalsWithoutGeo(Camera camera) {
		boolean equal = false;

		if(mTitle.contentEquals(camera.getTitle()) && mUrl.contentEquals(camera.getUrl()) &&
				mLatitude == camera.getLatitude() && mLongitude == camera.getLongitude()) {
			equal = true;
		}

		return equal;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;

		Camera camera = (Camera) obj;

		if(mTitle.contentEquals(camera.getTitle()) && mUrl.contentEquals(camera.getUrl()) &&
				mLatitude == camera.getLatitude() && mLongitude == camera.getLongitude() &&
				mCity.contentEquals(camera.getCity()) && mZipCode.contentEquals(camera.getZipCode()) &&
				mState.contentEquals(camera.getState()) && mCountry.contentEquals(camera.getCountry())) {
			equal = true;
		}

		return equal;
	}


}
