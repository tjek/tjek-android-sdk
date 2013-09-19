/**
 * @fileoverview	Utilities.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * @version			0.0.1
 */
package com.eTilbudsavis.etasdk.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.util.Log;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;

public final class Utils {
	
	/** A second in milliseconds */
	public static final long SECOND_IN_MILLIS = 1000;

	/** A minute in milliseconds */
	public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;

	/** A hour in milliseconds */
	public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;

	/** A day in milliseconds */
	public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

	/** A week in milliseconds */
	public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;

	/** A month in milliseconds */
	public static final long MONTH_IN_MILLIS = DAY_IN_MILLIS * 30;

	/** A year in milliseconds */
	public static final long YEAR_IN_MILLIS = WEEK_IN_MILLIS * 52;

	/** The date format as returned from the server */
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";
	
//	public static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	
	/**
	 * Create universally unique identifier.
	 *
	 * @return Universally unique identifier (UUID).
	 */
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * A proxy for Log.d API that silences log messages in release.
	 *
	 * @param tag Used to identify the source of a log message. It usually
	 *			identifies the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	public static void logd(String tag, String msg) {
		if (Eta.DEBUG)
			Log.d(tag, msg);
	}	

	public static void logd(String tag, String name, boolean isCache, int statusCode, Object data, EtaError error) {
		if (!Eta.DEBUG)
			return;
		
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(": ")
		.append("cache: ").append(isCache)
		.append(", status: ").append(statusCode)
		.append(", data: ");
		if (isSuccess(statusCode)) {
			if (data == null) {
				sb.append("null");
			} else {
				if (data instanceof List<?>) {
					sb.append("List Size: ").append(((List<?>) data).size());
				} else {
					sb.append(data.toString());
				}
			}
		} else {
			sb.append(error.toString());
		}
		Log.d(tag, sb.toString());
	}
	
	public static void logdMax(String tag, String sb) {
		if (!Eta.DEBUG)
			return;
		
		if (sb.length() > 4000) {
		    Log.d(tag, "sb.length = " + sb.length());
		    int chunkCount = sb.length() / 4000;     // integer division
		    for (int i = 0; i <= chunkCount; i++) {
		        int max = 4000 * (i + 1);
		        if (max >= sb.length()) {
		            Log.d(tag, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
		        } else {
		            Log.d(tag, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
		        }
		    }
		}
	}
	
	public static void printStackTrace() {
		if (Eta.DEBUG)
			for (StackTraceElement ste : Thread.currentThread().getStackTrace())
				System.out.println(ste);
	}
    /**
     * Generate a SHA256 checksum of a string.
     * 
     * @param string to SHA256
     * @return A SHA256 string
     */
	public static String generateSHA256(String string) {
		
	    MessageDigest digest=null;
	    String hash = "";
	    try {
	        digest = MessageDigest.getInstance("SHA-256");
	        digest.update(string.getBytes());
	        byte[] bytes = digest.digest();
	        
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < bytes.length; i++) {
	            String hex = Integer.toHexString(0xFF & bytes[i]);
	            if (hex.length() == 1) {
	                sb.append('0');
	            }
	            sb.append(hex);
	        }
	        hash = sb.toString();

	    } catch (NoSuchAlgorithmException e1) {
	        e1.printStackTrace();
	    }
	    return hash;
	}
	
	public static String bundleToQueryString( Bundle apiParams) {
		StringBuilder result = new StringBuilder();
		for (String key : apiParams.keySet()) {
			if (apiParams.get(key) instanceof Bundle) {
				logd("Utils", "Nested parameters not allowed.");
			} else {
				if (result.length() > 0)
					result.append("&");
				result.append(key);
				result.append("=");
				result.append(valueIsNull(apiParams.get(key)));
				
			}
		}
		
		return result.toString();
	}

	public static List<NameValuePair> bundleToNameValuePair( Bundle apiParams) {
		List<NameValuePair> list = new ArrayList<NameValuePair>(apiParams.size());
		for (String key : apiParams.keySet()) {
			if (apiParams.get(key) instanceof Bundle) {
				logd("Utils", "Nested parameters not allowed.");
			} else {
				list.add(new BasicNameValuePair(key, valueIsNull(apiParams.get(key))));
			}
		}
		return list;
	}
	
	private static String valueIsNull(Object value) {
		String s = value == null ? "" : value.toString();
		return s;
	}
	
	/**
	 * Builds the block of JavaScript parameters for injecting into a WebView.
	 *
	 * @param data The Map to process
	 * @return A String in JavaScript format
	 */
	@SuppressWarnings("unchecked")
	public static String mapToJavaScript(Map<String, Object> data) {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		boolean firstTime = true;
		for (String s : data.keySet()) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(", ");
			}
			sb.append(s);
			if (data.get(s) instanceof Map<?, ?>) {
				sb.append(": ");
				sb.append( mapToJavaScript( (Map<String, Object>)data.get(s) ) );
			} else {
				sb.append(": '");
				sb.append(data.get(s).toString());
				sb.append("'");
			}
		}
		return sb.append(" }").toString();
	}
	
	/**
	 * Checks if the name is a valid user name for eta.dk <br><br>
	 * 
	 * Requirements: length > 2 chars.
	 * @param name to check
	 * @return boolean, true if name if valid
	 */
	public static boolean isNameValid(String name) {
		return name.length() > 1 ? (name.length() < 81 ? true : false ) : false;
	}

	/**
	 * Checks if a given password fits the requirements of etilbudsavis.dk.<br><br>
	 * 
	 * Requirements: password length from 6 through 39 chars.
	 * @param password
	 * @return
	 */
	public static boolean isPasswordValid(String password) {
		return 5 < password.length() && password.length() < 40;
	}
	
	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * 
	 * Requirements: birth year from 1901 through 2011.
	 * @param birthyear
	 * @return
	 */
	public static boolean isBirthyearValid(Integer birthyear) {
		return birthyear > 1900 ? (birthyear < 2012 ? true : false ) : false ;
	}

	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * 
	 * Requirements: birth year from 1901 through 2011.
	 * @param birthyear
	 * @return
	 */
	public static boolean isGenderValid(String gender) {
		gender = gender.toLowerCase();
		return (gender.equals("male") || gender.equals("female") );
	}

	public static boolean isSuccess(int statusCode) {
		return 200 <= statusCode && statusCode < 300;
	}

	public static boolean isRedirection(int statusCode) {
		return 300 <= statusCode && statusCode < 400;
	}

	public static boolean isClientError(int statusCode) {
		return 400 <= statusCode && statusCode < 500;
	}

	public static boolean isServerError(int statusCode) {
		return 500 <= statusCode && statusCode < 600;
	}
	
	public static Date parseDate(String date) {
		Date d = null;
		try {
			d = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	public static String formatDate(Date date) {
		return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
	}
	
}