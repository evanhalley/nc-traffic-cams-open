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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.emuneee.nctrafficcams.R;

/**
 * Manages HTTP connections for the application
 *
 * @author ehalley
 *
 */
public class HttpUtils {
	private final static String sTag = "HttpUtils";
	private final static int sTimeout = 2000;
	private final static String sUsername = "nc_traffic_cams";
	private final static String sPassword = "tr4ff1c_c4ms!";

	public static void initializeVerifiedHostnames(final Context context) {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				Log.d(sTag, "Hostname: " + hostname);

				if(hostname.contentEquals(context.getString(R.string.hostname))) {
					return true;
				} else if(hostname.contentEquals(context.getString(R.string.dot_hostname))) {
					return true;
				} else if(hostname.contentEquals(context.getString(R.string.maps_hostname))) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	public static SSLContext getSSLContext(Context context) throws CertificateException, IOException, KeyStoreException,
		NoSuchAlgorithmException, KeyManagementException {
		AssetManager assetMgr = context.getAssets();
		// Load CAs from an InputStream
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream caInput = new BufferedInputStream(assetMgr.open("cert.pem"));
		Certificate ca;

		try {
		    ca = cf.generateCertificate(caInput);
		    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
		} finally {
		    caInput.close();
		}

		// Create a KeyStore containing our trusted CAs
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		keyStore.setCertificateEntry("ca", ca);

		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);

		// Create an SSLContext that uses our TrustManager
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);
		return sslContext;
	}

	/**
	 * Returns a URL connection object tailor made for the Google APIs
	 *
	 * @param urlString
	 *            URL to connect to
	 * @return
	 * @throws IOException
	 */
	public static HttpURLConnection getUrlConnection(String urlString)
			throws IOException {
		Log.d(sTag, "New URL connection: " + urlString);
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();
		urlConnection.setConnectTimeout(sTimeout);
		return urlConnection;
	}

	/**
	 * Returns a URL connection object tailor made for the Google APIs
	 *
	 * @param urlString
	 *            URL to connect to
	 * @return
	 * @throws IOException
	 */
	public static HttpsURLConnection getSecureUrlConnection(String urlString, final Context context)
			throws Exception {
		Log.d(sTag, "New secure URL connection: " + urlString);
		SSLContext sslContext = getSSLContext(context);
		URL url = new URL(urlString);
		HttpsURLConnection urlConnection = (HttpsURLConnection) url
				.openConnection();
		urlConnection.setConnectTimeout(sTimeout);
		urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
		return urlConnection;
	}

	public static HttpsURLConnection getAuthUrlConnection(String urlString, Context context) throws Exception {
		HttpsURLConnection urlConnection = getSecureUrlConnection(urlString, context);
		String userpass = sUsername + ":" + sPassword;
		String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes(), Base64.DEFAULT));
		urlConnection.setRequestProperty ("Authorization", basicAuth);
		return urlConnection;
	}

	/**
	 * Downloads a bitmap from the network
	 *
	 * @param url
	 *            url that points to the bitmap to be downloaded
	 * @return bitmap downloaded from the network
	 */
	public static Bitmap getBitmapFromNetwork(String url) {
		Bitmap bitmap = null;
		InputStream input = null;
		HttpURLConnection connection = null;
		try {
			connection = getUrlConnection(url);
			input = connection.getInputStream();
			bitmap = BitmapFactory.decodeStream(input);
		} catch (IOException e) {
			Log.w(sTag, "Error downloading bitmap from network");
			Log.w(sTag, "" + e.getMessage());
		} finally {
			try {
				if (connection != null) {
					connection.disconnect();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
			}
		}
		return bitmap;
	}
}
