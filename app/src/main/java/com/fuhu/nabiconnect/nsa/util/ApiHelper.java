package com.fuhu.nabiconnect.nsa.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.fuhu.nabiconnect.log.LOG;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiHelper {

	final private String TAG = ApiHelper.class.getSimpleName();

	private static ApiHelper mHelper;

	private static String mApiHost;

	private static Handler mHandler;
	private static boolean mCancelled;

	private AQuery aq;
	public long mCurrentKidId = -1;
	public String mCurrentUserKey = "";
	public String mCurrentSessionKey = "";
	public String mCurrentUserName = "";

	private static String mParentKey;
	private static String mParentSessionKey;
	private static String mChildKey;
	private static String mChildSessionKey;

	final public static int LOGIN = 0;
	final public static int FRIEND_NEW = 1;
	final public static int FRIEND_LIST = 2;
	final public static int CHAT_POLL = 3;
	final public static int CHAT_DELETE = 31;
	final public static int MAIL_INBOX = 4;
	final public static int PHOTO_NEW = 5;

	final public static int MAIL_IN_DELETE = 301;
	final public static int MAIL_OUT_DELETE = 302;
	// final public static int PHOTO_REMOVE_RECEIVED = 100;
	// final public static int PHOTO_DELETE = 200;
	// ---------------------------------------------------
	final public static int NSA_PHOTO_DELETE = 200;

	public static ApiHelper getInstance(Context context, Handler handler) {
		ApiHelper helper = mHelper;
		if (helper == null) {
			synchronized (ApiHelper.class) {
				helper = mHelper;
				if (helper == null) {
					mHelper = helper = new ApiHelper(context, handler);
				} else {
					mHandler = handler;
				}
			}
		} else {
			mHandler = handler;
		}
		ApiUtils.init(context);
		mApiHost = ApiUtils.getApiHost();
		mCancelled = false;
		return helper;
	}

	private ApiHelper(Context context, Handler handler) {
		aq = new AQuery(context);
		mHandler = handler;
	}

	public void setCredential(String sessionKey, String userKey) {
		this.mCurrentSessionKey = sessionKey;
		this.mCurrentUserKey = userKey;
	}

	public static void setChildCredential(String sessionKey, String userKey) {
		mChildKey = userKey;
		mChildSessionKey = sessionKey;
	}

	public static void setParentCredential(String sessionKey, String userKey) {
		mParentSessionKey = sessionKey;
		mParentKey = userKey;
	}

	public void cancel() {
		aq.ajaxCancel();
		mCancelled = true;
	}

	/**
	 * m.what = CHAT_DELETE<br>
	 * m.arg1 = status code<br>
	 * m.obj = message id
	 * 
	 * @param childKey
	 * @param messageId
	 */
	public void nsaDeleteChatMessage(String childKey, final String messageId) {

		AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject response, AjaxStatus status) {
				Message m = mHandler.obtainMessage();
				m.what = CHAT_DELETE;
				m.arg1 = -1;
				m.obj = messageId;
				try {
					m.arg1 = Integer.parseInt(response.getString("status"));
					LOG.D(TAG, "url: " + url + " response: " + response.toString(5));
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				mHandler.sendMessage(m);
			}
		};
		callback.url(String.format(mApiHost, "/nsamessage/" + messageId + "?actor_id=" + childKey));
		callback.type(JSONObject.class);
		callback.method(AQuery.METHOD_DELETE);
		callback.headers(ApiUtils.getDeleteChatMessageHeader(mCurrentUserKey, mCurrentSessionKey));
		aq.ajax(callback);
	}

	public void nsaDeleteReceivedPhoto(final String photoId, String kidUserKey) {
		AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject response, AjaxStatus status) {
				LOG.D(TAG, "callback: " + url);
				Message m = mHandler.obtainMessage();
				m.what = NSA_PHOTO_DELETE;
				m.obj = photoId;
				m.arg1 = -1;
				if (response != null) {
					try {
						LOG.D(TAG, "response: " + response.toString(5));
						m.arg1 = response.getInt("status");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				mHandler.sendMessage(m);
			}
		};
		// use child userkey here
		callback.url(String.format(mApiHost, "/photouser/" + kidUserKey + "/receivedPhotos/" + photoId + "/nsa"));
		callback.type(JSONObject.class);
		callback.method(AQuery.METHOD_DELETE);
		callback.headers(ApiUtils.getRemoveReceivedPhotoHeader(mCurrentUserKey, mCurrentSessionKey));
		aq.ajax(callback);
	}

	/**
	 * this works
	 * 
	 * @param photoId
	 * @param kidUserKey
	 */
	public void nsaDeleteSharedPhoto(final String photoId, String kidUserKey) {
		AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject response, AjaxStatus status) {
				LOG.D(TAG, "callback: " + url);
				Message m = mHandler.obtainMessage();
				m.what = NSA_PHOTO_DELETE;
				m.obj = photoId;
				m.arg1 = -1;
				if (response != null) {
					try {
						LOG.D(TAG, "response: " + response.toString(5));
						m.arg1 = response.getInt("status");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				mHandler.sendMessage(m);
			}
		};
		// use child userkey here
		callback.url(String.format(mApiHost, "/photouser/" + kidUserKey + "/photo/" + photoId + "/nsa"));
		callback.type(JSONObject.class);
		callback.method(AQuery.METHOD_DELETE);
		callback.headers(ApiUtils.getDeletePhotoHeader(mCurrentUserKey, mCurrentSessionKey));
		aq.ajax(callback);
	}

	/**
	 * @param mailboxId
	 * @param mailId
	 * 
	 * @return sends a message to handler with<br>
	 *         arg1 = MAIL_DELETE<br>
	 *         obj = original mail message object
	 */
	public void deleteInMail(String mailboxId, final String mailId) {
		AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject response, AjaxStatus status) {
				LOG.D(TAG, "url: " + url);
				Message m = mHandler.obtainMessage(MAIL_IN_DELETE);
				m.obj = mailId;
				m.arg1 = -1;
				if (response != null) {
					LOG.D(TAG, response.toString());
					try {
						m.arg1 = response.getInt("status");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					// null response
				}
				mHandler.sendMessage(m);
			}
		};
		callback.url(String.format(mApiHost, "/mailuser/" + mChildKey + "/parent/" + mParentKey + "/inbox/" + mailboxId
				+ "/mail/" + mailId));
		callback.type(JSONObject.class);
		callback.method(AQuery.METHOD_DELETE);
		callback.headers(ApiUtils.getMailApiHeader(mParentKey, mParentSessionKey));
		aq.ajax(callback);
	}

	/**
	 * @param mailboxId
	 * @param mailId
	 * 
	 * @return sends a message to handler with<br>
	 *         arg1 = MAIL_DELETE<br>
	 *         obj = original mail message object
	 */
	public void deleteOutMail(String mailboxId, final String mailId) {
		AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
			@Override
			public void callback(String url, JSONObject response, AjaxStatus status) {
				LOG.D(TAG, "url: " + url);
				Message m = mHandler.obtainMessage(MAIL_OUT_DELETE);
				m.obj = mailId;
				m.arg1 = -1;
				if (response != null) {
					LOG.D(TAG, response.toString());
					try {
						m.arg1 = response.getInt("status");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				mHandler.sendMessage(m);
			}
		};
		callback.url(String.format(mApiHost, "/mailuser/" + mChildKey + "/parent/" + mParentKey + "/outbox/"
				+ mailboxId + "/mail/" + mailId));
		callback.type(JSONObject.class);
		callback.method(AQuery.METHOD_DELETE);
		callback.headers(ApiUtils.getMailApiHeader(mParentKey, mParentSessionKey));
		aq.ajax(callback);
	}
}
