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
package com.emuneee.nctrafficcams.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POJO for a traffic camera
 *
 * @author Evan
 *
 */
public class Camera implements Parcelable {
	private String mUrl;
	private String mTitle;
	private long mUpdated;
	private double mLatitude;
	private double mLongitude;
	private String mCity;
	private String mZipCode;
	private boolean mIsFavorite;
	private String mId;

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public Camera() {

	}

	public Camera(Parcel in) {
		mId = in.readString();
		mUrl = in.readString();
		mTitle = in.readString();
		mUpdated = in.readLong();
		mLatitude = in.readDouble();
		mLongitude = in.readDouble();
		mCity = in.readString();
		mZipCode = in.readString();
		mIsFavorite = in.readInt() == 1 ? true : false;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mUrl);
		dest.writeString(mTitle);
		dest.writeLong(mUpdated);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		dest.writeString(mCity);
		dest.writeString(mZipCode);
		dest.writeInt(mIsFavorite ? 1 : 0);
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
	 * @return the updated
	 */
	public long getUpdated() {
		return mUpdated;
	}

	/**
	 * @param updated
	 *            the updated to set
	 */
	public void setUpdated(long updated) {
		mUpdated = updated;
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

	/**
	 * @return the isFavorite
	 */
	public boolean isFavorite() {
		return mIsFavorite;
	}

	/**
	 * @param isFavorite
	 *            the isFavorite to set
	 */
	public void setIsFavorite(boolean isFavorite) {
		mIsFavorite = isFavorite;
	}

	/**
	 * @param isFavorite
	 *            the isFavorite to set
	 */
	public void setIsFavorite(int isFavorite) {
		mIsFavorite = isFavorite == 1 ? true : false;
	}

	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * It will be required during un-marshaling data stored in a Parcel
	 *
	 * @author ehalley
	 */
	public static final Parcelable.Creator<Camera> CREATOR = new Parcelable.Creator<Camera>() {
		public Camera createFromParcel(Parcel source) {
			return new Camera(source);
		}

		public Camera[] newArray(int size) {
			return new Camera[size];
		}
	};

	@Override
	public String toString() {
		return "Camera [mUrl=" + mUrl + ", mTitle=" + mTitle + ", mUpdated="
				+ mUpdated + ", mLatitude=" + mLatitude + ", mLongitude="
				+ mLongitude + ", mCity=" + mCity + ", mZipCode=" + mZipCode
				+ ", mIsFavorite=" + mIsFavorite + ", mId=" + mId + "]";
	}
}
