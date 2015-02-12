package com.fuhu.nabiconnect.notification;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fuhu.account.AccountParser;
import com.fuhu.account.data.AccessToken.OSGToken;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class ApiHelperSocial {

	final private String TAG = ApiHelperSocial.class.getSimpleName();

	private static ApiHelperSocial mHelper;
	private static String mApiHost;
	private static String mApiKey;

	// app01.social-dev.fuhu.org
	// social-qa.fuhu.org
	// m_FriendsPort = ":8883/";
	// m_MailPort = ":8882/";
	// m_PhotoPort = ":8884/";
	// m_ChatPort = ":8881/";
	final private static String HOST_STAGING = "http://social-qa.fuhu.org%s";
	final private static String HOST_PRODUCTION = "http://social.fuhu.com%s";
	final private static String API_KEY_NSA_PRODUCTION = "9030fd4c-ab19-11e3-b490-244d19929226";
	final private static String API_KEY_NSA_STAGING = "68da39e2-75fe-4db3-ab87-f8c5c5f049bc";

	private Context mContext;
	private static Handler mHandler;

	private AQuery aq;

	public long mCurrentKidId = -1;
	public String mCurrentUserKey = "";
	public String mCurrentSessionKey = "";
	public String mCurrentUserName = "";
	public String mCurrentKidAvatarUrl = "";

	public String mParentKey = "";
	public String mParentSessionKey = "";

	final private static String PREF_NAME = "dashboard_pref";
	final private static String KEY_PARENT_KEY = "parent_key";

	final public static int LOGIN = 0;
	final public static int FRIEND_NEW = 101;
	final public static int FRIEND_LIST = 102;
	final public static int FRIEND_DELETE = 103;
	final public static int FRIEND_REQUEST = 104;
	final public static int FRIEND_REQUEST_ERROR = 1044;
	final public static int CHAT_LIST = 201;
	final public static int CHAT_HISTORY = 202;
	final public static int CHAT_DELETE = 203;
	final public static int CHAT_DELETE_ERROR = 2033;
	final public static int MAIL_INBOX = 301;
	final public static int MAIL_OUTBOX = 302;
	final public static int MAIL_GET_IN_MAIL = 303;
	final public static int MAIL_GET_OUT_MAIL = 304;
	final public static int MAIL_DELETE = 305;
	final public static int PHOTO_ALL = 401;
	final public static int PHOTO_HISTORY = 402;
	final public static int PHOTO_DELETE_RECEIVED = 403;
	final public static int PHOTO_DELETE_SHARED = 404;
	final public static int PHOTO_NEW = 403;

	final public static int ERROR = 9999;
	final public static int STATUS_8049 = 8049;

	public static ApiHelperSocial getInstance(Context context, Handler handler) {
		ApiHelperSocial helper = mHelper;
		if (helper == null) {
			synchronized (ApiHelperSocial.class) {
				helper = mHelper;
				if (helper == null) {
					mHelper = helper = new ApiHelperSocial(context, handler);
				} else {
					mHandler = handler;
				}
			}
		} else {
			mHandler = handler;
		}

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
		return helper;
	}

	private ApiHelperSocial(Context context, Handler handler) {
		aq = new AQuery(context);
		mContext = context;
		mHandler = handler;
	}

	public void cancel() {
		aq.ajaxCancel();
	}

	/**
	 * pass 0 for parent login
	 * 
	 * @param kidId
	 */
	public void login(final int requestCode, final long kidId) {
		mCurrentKidId = kidId;
		AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject response, AjaxStatus status) {
				Message m = mHandler.obtainMessage(requestCode);
				if (response != null) {
					try {
						if (kidId == 0) {
							// parent login
							mParentKey = response.getString("userId");
							mParentSessionKey = response.getString("sessionKey");
							mContext.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
									.edit().putString(KEY_PARENT_KEY, mParentKey).commit();
						} else {
							// kid login
							mCurrentUserKey = response.getString("userId");
							mCurrentSessionKey = response.getString("sessionKey");
							mCurrentKidAvatarUrl = response.getString("avatarUrl");
						}
						m.arg1 = LOGIN;
						m.obj = response;
					} catch (JSONException e) {
						e.printStackTrace();
						m.arg1 = ERROR;
					}
				} else {
					m.arg1 = ERROR;
				}
				mHandler.sendMessage(m);
			}
		};
		callback.url(String.format(mApiHost, "/user/loginUser"));
		callback.type(JSONObject.class);
		callback.headers(ApiUtils.getLoginHeader());
		callback.params(ApiUtils.getLoginEntity(mContext, kidId));
		aq.ajax(callback);
	}
	
	private static class ApiUtils {

		final private static String TAG = ApiUtils.class.getSimpleName();

		final public static int KEY_CHAT_HISTORY_POLL_LIMIT = 50;
		final public static int KEY_PHOTO_POLL_LIMIT = 50;

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
			// TODO: this line needs to be updated prior to release
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

	}

}