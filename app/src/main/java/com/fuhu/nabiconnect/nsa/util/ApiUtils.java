package com.fuhu.nabiconnect.nsa.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.androidquery.AQuery;
import com.fuhu.account.AccountParser;
import com.fuhu.account.data.AccessToken.OSGToken;
import com.fuhu.json.JSONException;
import com.fuhu.json.JSONObject;
import com.fuhu.nabiconnect.log.LOG;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class ApiUtils {

	final private static String TAG = ApiUtils.class.getSimpleName();

	private static String mApiHost;
	private static String mApiKey;

	final private static String HOST_STAGING = "http://social-qa.fuhu.org%s";
	final private static String HOST_PRODUCTION = "http://social.fuhu.com%s";
	final private static String API_KEY_NSA_PRODUCTION = "9030fd4c-ab19-11e3-b490-244d19929226";
	final private static String API_KEY_NSA_STAGING = "68da39e2-75fe-4db3-ab87-f8c5c5f049bc";

	public static void init(Context context) {
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
			if (ai.metaData.getString("server").contains("prod")) {
				mApiKey = API_KEY_NSA_PRODUCTION;
				mApiHost = HOST_PRODUCTION;
			} else {
				mApiKey = API_KEY_NSA_STAGING;
				mApiHost = HOST_STAGING;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			mApiKey = API_KEY_NSA_STAGING;
		}
	}

	public static String getApiHost() {
		return mApiHost;
	}

	public static HashMap<String, String> getLoginHeader() {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("apiKey", mApiKey);
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("nabiFirmwareVersion", com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion());
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		return params;
	}

	/**
	 * includes: <br>
	 * content type<br>
	 * api key<br>
	 * device key<br>
	 * device type<br>
	 * android version<br>
	 * device edition<br>
	 * session key<br>
	 * user id<br>
	 * 
	 * @param userKey
	 * @param sessionKey
	 * @return
	 */
	public static HashMap<String, String> getGeneralRequestHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("apiKey", mApiKey);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("sessionKey", sessionKey);
		params.put("userId", userKey);
		return params;
	}

	/**
	 * same as {@link #getGeneralRequestHeader(String, String)}
	 * 
	 * @param userKey
	 * @param sessionKey
	 * @return
	 */
	public static HashMap<String, String> getNewFriendHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("apiKey", mApiKey);
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("sessionKey", sessionKey);
		params.put("userId", userKey);
		return params;
	}

	/**
	 * same as {@link #getGeneralRequestHeader(String, String)}
	 * 
	 * @param userKey
	 * @param sessionKey
	 * @return
	 */
	public static HashMap<String, String> getNewMailHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("apiKey", mApiKey);
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("sessionKey", sessionKey);
		params.put("userId", userKey);
		return params;
	}

	/**
	 * Not the same as general header
	 * 
	 * @param userKey
	 * @param sessionKey
	 * @return
	 */
	public static HashMap<String, String> getNewMsgHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("apiKey", mApiKey);
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("sessionKey", sessionKey);
		params.put("nabiVersion", com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion());
		return params;
	}

	public static HashMap<String, String> getDeleteChatMessageHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("apiKey", mApiKey);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("nabiVersion", com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion());
		params.put("userId", userKey);
		params.put("sessionKey", sessionKey);
		// LOG.E(TAG, params.toString());
		return params;
	}

	/**
	 * not the same as general header
	 * 
	 * @param userKey
	 * @param sessionKey
	 * @return
	 */
	public static HashMap<String, String> getNewPhotoHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("apiKey", mApiKey);
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("sessionKey", sessionKey);
		params.put("nabiFirmwareVersion", com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion());
		return params;
	}

	public static HashMap<String, String> getRemoveReceivedPhotoHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("apiKey", mApiKey);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("nabiFirmwareVersion", com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion());
		params.put("sessionKey", sessionKey);
		for (String s : params.keySet()) {
			LOG.D(TAG, s + ": " + params.get(s));
		}
		return params;
	}

	public static HashMap<String, String> getDeletePhotoHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("apiKey", mApiKey);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("nabiFirmwareVersion", com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion());
		params.put("sessionKey", sessionKey);
		for (String s : params.keySet()) {
			LOG.D(TAG, s + ": " + params.get(s));
		}
		return params;
	}

	/**
	 * Entity methods
	 */
	/**
	 * Will include json field osgKidId if kidId is not 0
	 * 
	 * @param context
	 * @param kidId
	 * @return
	 */
	public static HashMap<String, Object> getLoginEntity(Context context, long kidId) {
		OSGToken token = AccountParser.getAccount(context).getAccessToken().getOSGToken();
		try {
			JSONObject dto = new JSONObject();
			dto.put("osgUserKey", token.getOSGUserKey());
			dto.put("osgSessKey", token.getOSGSessionKey());
			dto.put("osgAuthKey", token.getOSGAuthKey());
			if (kidId != 0) {
				dto.put("osgKidId", kidId);
			}
			StringEntity se = new StringEntity(dto.toString());
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(AQuery.POST_ENTITY, se);
			return params;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static HashMap<String, String> getMailApiHeader(String userKey, String sessionKey) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content-Type", "application/json");
		params.put("apiKey", mApiKey);
		params.put("deviceKey", com.fuhu.nabicontainer.util.Utils.getSerialId().trim());
		params.put("deviceType", com.fuhu.nabicontainer.util.Utils.getMODELID().trim());
		params.put("androidVersion", "Android API Level: " + android.os.Build.VERSION.SDK_INT);
		params.put("deviceEdtion", com.fuhu.nabicontainer.util.NabiFunction.getEdition());
		params.put("sessionKey", sessionKey);
		params.put("userId", userKey);
		LOG.D(TAG, params.toString());
		return params;
	}
}
