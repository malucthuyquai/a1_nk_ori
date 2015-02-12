package com.fuhu.nabiconnect.notification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.fuhu.account.AccountParser;
import com.fuhu.account.data.AccessToken.OSGToken;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.notification.bean.NotificationBean;
import com.fuhu.nabiconnect.notification.service.NotificationDialogService;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMParameters;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMSenderEventCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NabiNotificationManager {

	public final static String TAG = "NabiNotificationManager";
	private final static String GCM_API_KEY = "NTU2NmNtcy1jbGllbnQtcHVzaA==";
	public final static String PREF_NAME = "nabiNotificationPref";
	private final static String PREF_KEY_VERSION_CODE = "versionCode";
	private final static String ACTION_NABI_MESSAGE_CENTER_SEND = "nabi.intent.action.GCM_NOTIFICATION_SEND";
	private final static String ACTION_NABI_MESSAGE_CENTER_REMOVE = "nabi.intent.action.GCM_NOTIFICATION_REMOVE";

	public final static String APPLICATION_NAME_CHAT = "nabi Chat";
	public final static String APPLICATION_NAME_PHOTO = "nabi Photo";
	public final static String APPLICATION_NAME_MAIL = "nabi Mail";
	public final static String APPLICATION_NAME_FRIEND = "nabi Friends";

	public final static String CLASS_NAME_CHAT = "com.fuhu.nabiconnect.chat.ChatActivity";
	public final static String CLASS_NAME_PHOTO = "com.fuhu.nabiconnect.photo.PhotoActivity";
	public final static String CLASS_NAME_MAIL = "com.fuhu.nabiconnect.mail.MailActivity";
	public final static String CLASS_NAME_FRIEND = "com.fuhu.nabiconnect.friend.FriendActivity";

	// for notification center
	public final static String CLASS_NAME_PARENT_CHAT = "com.fuhu.nabiconnect.chat.ChatParentActivity";
	public final static String CLASS_NAME_PARENT_PHOTO = "com.fuhu.nabiconnect.photo.PhotoParentActivity";
	public final static String CLASS_NAME_PARENT_MAIL = "com.fuhu.nabiconnect.mail.MailParentActivity";
	public final static String CLASS_NAME_PARENT_FRIEND = "com.fuhu.nabiconnect.friend.FriendParentActivity";

	public final static boolean isGcmEnabled = true;

	private Context m_Context;

	private String mApiKey = "";

	/** device info */
	private String mDeviceKey = "";
	private String mDeviceType = "";
	private String mDeviceEdition = "";
	private String mAndroidVersion = "";
	private String mFirmwareVersion = "";
	private String mMacAddress = "";

	/** user setting */
	private String mLang = "";
	private String mCountryCode = "";

	/** need login to retrieve */
	private OSGToken mToken;
	private String mAuthKey = "";
	private String mSessKey = "";
	private String mUserKey = "";

	private ClientCloudMessageReceiver mClientCloudMessageReceiver;

	private boolean m_IsInitialized;

	public interface IRegisterCallback {
		public void onRegistered();
	}

	public NabiNotificationManager(Context context) {
		this.m_Context = context;

		try {

			// NOTE:
			mApiKey = GCM_API_KEY;
			/** device info */
			mDeviceKey = com.fuhu.nabicontainer.util.Utils.getSerialId().trim();
			mDeviceType = com.fuhu.nabicontainer.util.Utils.getMODELID().trim();
			mDeviceEdition = com.fuhu.nabicontainer.util.NabiFunction.getEdition();
			mFirmwareVersion = com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion();
			mAndroidVersion = "Android API Level: " + android.os.Build.VERSION.SDK_INT;
			mMacAddress = NotificationUtils.getMacAddress();
			/** user setting */
			mLang = NotificationUtils.getDisplayLanguage();
			mCountryCode = NotificationUtils.getCountryCode(m_Context);

			mToken = AccountParser.getAccount(m_Context).getAccessToken().getOSGToken();
			mAuthKey = mToken.getOSGAuthKey();
			mSessKey = mToken.getOSGSessionKey();
			mUserKey = mToken.getOSGUserKey();

			m_IsInitialized = true;

		} catch (Throwable tr) {
			LOG.E(TAG, "NotificationManager() - failed to initialize.", tr);
			m_IsInitialized = false;
		}

	}

	public void register(String friendCode, final IRegisterCallback callBack) {
		if (!isGcmEnabled)
			return;

		if (!m_IsInitialized) {
			LOG.W(TAG, "register() - NabiNotificationManager is not initiailzed.");
			return;
		}

		/*
		 * // check the version code in preference, if the version code is
		 * different, than register again SharedPreferences perference =
		 * m_Context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); int
		 * storedVersionCode = perference.getInt(PREF_KEY_VERSION_CODE, -1); int
		 * currentVersionCode = NotificationUtils.getAppVersion(m_Context);
		 * LOG.V(TAG, "register() - storedVersionCode is "+storedVersionCode+
		 * " , currentVersionCode is "+currentVersionCode); if(storedVersionCode
		 * == currentVersionCode) { LOG.V(TAG,
		 * "register() - registed already."); return; } else {
		 * perference.edit().putInt(PREF_KEY_VERSION_CODE,
		 * currentVersionCode).apply(); }
		 */

		// registration process
		String url = NotificationConfig.APP_PUSH_SERVER_URL;// +
															// "/cms/register/user/";
		String Project = NotificationConfig.GOOGLE_PROJECT_ID;
		final String inputFriendCode = friendCode;

		GCMParameters gcmParams = new GCMParameters();
		gcmParams.setServerURL(url);
		gcmParams.setGoogle_Project_ID(Project);
		gcmParams.setUserKey(mUserKey);
		gcmParams.setAPIKey(mApiKey);
		gcmParams.setDeviceKey(mDeviceKey);
		gcmParams.setDeviceType(mDeviceType);
		gcmParams.setDeviceEdition(mDeviceEdition);
		gcmParams.setFirmwareVersion(mFirmwareVersion);
		gcmParams.setMacAddress(mMacAddress);
		gcmParams.setAuthKey(mAuthKey);
		gcmParams.setSessKey(mSessKey);

		mClientCloudMessageReceiver = new ClientCloudMessageReceiver(false, m_Context, gcmParams) {
			@Override
			protected void onWaitReceiveingMessage() {
				if (inputFriendCode != null && !inputFriendCode.isEmpty()) {
					this.addFriendCode(inputFriendCode);
				}
				callBack.onRegistered();
			}

			@Override
			public void onNotifyMessage(final String responsedMessage) {

			}

			@Override
			public void onMessageSendError(final String returnedMessage) {

			}

			@Override
			public void onMessageDelete(final String returnedMessage) {
			}
		};
	}

	public void notifyServerByFriendCode(String targetFriendCode, String title, String content, String applicationName,
			GCMSenderEventCallback callback) {
		LOG.D(TAG, "notify by friend code");
		LOG.D(TAG, "friend code: " + targetFriendCode);
		LOG.D(TAG, "title: " + title);
		LOG.D(TAG, "content:" + content);
		LOG.D(TAG, "applicationName: " + applicationName);
		notifyServer("", 0, targetFriendCode, title, content, applicationName, callback);
	}

	public void notifyServerByUserKey(String targetUserkey, long targetKidId, String title, String content,
			String applicationName, GCMSenderEventCallback callback) {
		notifyServer(targetUserkey, targetKidId, "", title, content, applicationName, callback);
	}

	private void notifyServer(String targetUserkey, long targetKidId, String targetFriendCode, String title,
			String content, String applicationName, GCMSenderEventCallback callback) {
		if (!isGcmEnabled)
			return;

		if (!m_IsInitialized) {
			LOG.W(TAG, "notifyServer() - NabiNotificationManager is not initiailzed.");
			return;
		}

		String url = NotificationConfig.APP_PUSH_SERVER_URL;

		GCMParameters gcmParams = new GCMParameters();
		gcmParams.setServerURL(url);
		gcmParams.setUserKey(targetUserkey);
		gcmParams.setAPIKey(mApiKey);
		gcmParams.setDeviceKey(mDeviceKey);
		gcmParams.setDeviceType(mDeviceType);
		gcmParams.setDeviceEdition(mDeviceEdition);
		gcmParams.setFirmwareVersion(mFirmwareVersion);
		gcmParams.setMacAddress(mMacAddress);
		gcmParams.setAuthKey(mAuthKey);
		gcmParams.setSessKey(mSessKey);

		JSONObject mJSONObject;
		try {

			// generate json string of gcm
			// JSONEncoder jsonEncoder = new JSONEncoder();
			// jsonEncoder.put(NotificationBean.KEY_PACKAGE_NAME,
			// m_Context.getPackageName());
			// jsonEncoder.put(NotificationBean.KEY_USER_KEY, targetUserkey);
			// jsonEncoder.put(NotificationBean.KEY_KID_ID,
			// String.valueOf(targetKidId));
			// jsonEncoder.put(NotificationBean.KEY_TITLE, title);
			// jsonEncoder.put(NotificationBean.KEY_FRIEND_CODE,
			// targetFriendCode);
			// jsonEncoder.put(NotificationBean.KEY_CONTENT, content);
			// jsonEncoder.put(NotificationBean.KEY_APPLICATION_NAME,
			// applicationName);

			JSONObject data = new JSONObject();
			data.put(NotificationBean.KEY_PACKAGE_NAME, m_Context.getPackageName());
			data.put(NotificationBean.KEY_USER_KEY, targetUserkey);
			data.put(NotificationBean.KEY_KID_ID, String.valueOf(targetKidId));
			data.put(NotificationBean.KEY_TITLE, title);
			data.put(NotificationBean.KEY_FRIEND_CODE, targetFriendCode);
			data.put(NotificationBean.KEY_CONTENT, content);
			data.put(NotificationBean.KEY_APPLICATION_NAME, applicationName);

			ArrayList<String> packageNames = new ArrayList<String>();
			packageNames.add("com.fuhu.nabigator");
			packageNames.add(m_Context.getPackageName());

			// TODO: don't use string comparing
			boolean isSticker = content.equals(m_Context.getString(R.string.notification_chat_sticker_description));

			mJSONObject = genMessageJSONObject(data.toString(), packageNames, applicationName, title, isSticker);
			LOG.V(TAG, "notifyServer() - mJSONObject is " + mJSONObject);

			if (targetFriendCode.isEmpty()) {
				LOG.V(TAG, "notifyServer() - pushMessageFromServer");
				ClientCloudMessageReceiver.pushMessageFromServer(callback, gcmParams, mJSONObject);
			} else {
				LOG.V(TAG, "notifyServer() - pushMessageToFriend");
				ClientCloudMessageReceiver.pushMessageToFriend(callback, targetFriendCode, gcmParams, mJSONObject);
			}
		} catch (Throwable tr) {
			LOG.E(TAG, "notifyServer() - failed to notify server.", tr);
		}
	}

	private JSONObject genMessageJSONObject(final String sendMessage, ArrayList<String> packageNames,
			String applicationName, String senderUserName, boolean isSticker) throws JSONException {

		// prepare aps data
		String lockeyString = "";
		int apsDataCategory = 2;
		int apsDataFunction = 0;
		if (NabiNotificationManager.APPLICATION_NAME_CHAT.equals(applicationName)) {
			lockeyString = isSticker ? "KonnectSticker" : "KonnectChat";
			apsDataFunction = 1;
		} else if (NabiNotificationManager.APPLICATION_NAME_FRIEND.equals(applicationName)) {
			lockeyString = "KonnectFriend";
			apsDataFunction = 4;
		} else if (NabiNotificationManager.APPLICATION_NAME_MAIL.equals(applicationName)) {
			lockeyString = "KonnectMail";
			apsDataFunction = 2;
		} else if (NabiNotificationManager.APPLICATION_NAME_PHOTO.equals(applicationName)) {
			lockeyString = "KonnectPhoto";
			apsDataFunction = 3;
		}

		// create GCM data
		JSONObject mJSONObject = new JSONObject();

		// data
		JSONObject subJSONObj = new JSONObject();
		subJSONObj.put("message", sendMessage);
		mJSONObject.put(new String("data"), subJSONObj);

		// aps
		subJSONObj = new JSONObject();
		// aps -> alert
		JSONObject subsubJSONObject = new JSONObject();
		subsubJSONObject.put(new String("body"), "123");
		subsubJSONObject.put(new String("action-loc-key"), "123");
		subsubJSONObject.put(new String("loc-key"), lockeyString);
		JSONArray subJSONList = new JSONArray();
		subJSONList.put(senderUserName);
		subsubJSONObject.put(new String("loc-args"), subJSONList);
		subJSONObj.put(new String("alert"), subsubJSONObject);
		// aps -> badge
		subJSONObj.put(new String("badge"), 1);
		// aps -> sound
		subJSONObj.put(new String("sound"), "default");
		// aps -> content-available
		subJSONObj.put(new String("content-available"), 1);
		mJSONObject.put(new String("aps"), subJSONObj);
		// apsData
		subJSONObj = new JSONObject();
		subJSONObj.put(new String("c"), apsDataCategory);
		subJSONObj.put(new String("f"), apsDataFunction);
		mJSONObject.put(new String("apsData"), subJSONObj);
		// delayWhileIdle
		mJSONObject.put(new String("delayWhileIdle"), true);
		// package names
		JSONArray subPKGJSONList = new JSONArray();
		if (packageNames != null && packageNames.size() > 0) {
			for (String tmp : packageNames) {
				subPKGJSONList.put(tmp);
			}
		} else {
			subPKGJSONList.put(m_Context.getPackageName());
		}
		mJSONObject.put(new String("packageNames"), subPKGJSONList);
		// timeToLive
		mJSONObject.put(new String("timeToLive"), 600);

		return mJSONObject;
	}

	public void setNotificaionDialogStatus(boolean isEnabled) {
		if (!isGcmEnabled)
			return;

		SharedPreferences perference = m_Context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		perference.edit().putBoolean(NotificationDialogService.PREF_KEY_ENABLED, isEnabled).apply();
	}

	public static void notifyNabiMessageCenter(Context context, NotificationBean bean) {
		LOG.V(TAG, "notifyNabiMessageCenter() - start");

		if (context == null) {
			LOG.W(TAG, "notifyNabiMessageCenter() - context is null");
			return;
		}

		if (bean == null) {
			LOG.W(TAG, "notifyNabiMessageCenter() - bean is null");
			return;
		}

		long beanKidIdValue = -1;
		try {
			beanKidIdValue = Long.parseLong(bean.getKidId());
		} catch (Throwable th) {
			LOG.W(TAG, "notifyNabiMessageCenter() - failed to parse kid Id.");
		}

		Intent it = new Intent();
		it.setAction(ACTION_NABI_MESSAGE_CENTER_SEND);
		it.putExtra("title", bean.getTitle());
		it.putExtra("content", bean.getContent());
		it.putExtra("package", bean.getPackageName());
		it.putExtra("kid_id", beanKidIdValue > 0 ? bean.getKidId() : "");
		it.putExtra("user_id", bean.getUserKey());
		it.putExtra("application", bean.getApplicationName());

		int iconResId = 0;
		String className = "";
		if (NabiNotificationManager.APPLICATION_NAME_CHAT.equals(bean.getApplicationName())) {
			iconResId = R.drawable.notification_chat_icon;
			className = beanKidIdValue > 0 ? NabiNotificationManager.CLASS_NAME_CHAT
					: NabiNotificationManager.CLASS_NAME_PARENT_CHAT;
		} else if (NabiNotificationManager.APPLICATION_NAME_FRIEND.equals(bean.getApplicationName())) {
			iconResId = R.drawable.notification_friend_icon;
			className = beanKidIdValue > 0 ? NabiNotificationManager.CLASS_NAME_FRIEND
					: NabiNotificationManager.CLASS_NAME_PARENT_FRIEND;
		} else if (NabiNotificationManager.APPLICATION_NAME_MAIL.equals(bean.getApplicationName())) {
			iconResId = R.drawable.notification_mail_icon;
			className = beanKidIdValue > 0 ? NabiNotificationManager.CLASS_NAME_MAIL
					: NabiNotificationManager.CLASS_NAME_PARENT_MAIL;
		} else if (NabiNotificationManager.APPLICATION_NAME_PHOTO.equals(bean.getApplicationName())) {
			iconResId = R.drawable.notification_photo_icon;
			className = beanKidIdValue > 0 ? NabiNotificationManager.CLASS_NAME_PHOTO
					: NabiNotificationManager.CLASS_NAME_PARENT_PHOTO;
		} else {
			LOG.E(TAG, "notifyNabiMessageCenter() - bean.getApplicationName() is " + bean.getApplicationName()
					+ " , which is un-defined.");
			iconResId = R.drawable.ic_launcher;
		}

		// put class name
		it.putExtra("class", className);

		Drawable iconDrawable = context.getResources().getDrawable(iconResId);
		Bitmap icon = NotificationUtils.convertDrawableToBitmap(context, iconDrawable);

		// Bitmap icon =
		// NotificationUtils.getAppIconBitmapByPackageName(context,
		// bean.getPackageName());
		if (icon != null)
			it.putExtra("icon", icon);
		else
			LOG.W(TAG, "notifyNabiMessageCenter() - icon is null");
		context.sendBroadcast(it);

		LOG.V(TAG, "notifyNabiMessageCenter() - end");
	}

	public static void notifyNabiMessageCenterRemove(Context context, String applicationName, String kidId,
			String userKey) {
		LOG.V(TAG, "notifyNabiMessageCenterRemove() - start");

		LOG.V(TAG, "notifyNabiMessageCenterRemove() - applicationName : " + applicationName);
		LOG.V(TAG, "notifyNabiMessageCenterRemove() - kidId : " + kidId);
		LOG.V(TAG, "notifyNabiMessageCenterRemove() - userKey : " + userKey);

		long kidIdValue = -1;
		try {
			kidIdValue = Long.parseLong(kidId);
		} catch (Throwable th) {
			LOG.W(TAG, "notifyNabiMessageCenterRemove() - failed to parse kid Id.");
		}

		Intent it = new Intent();
		it.setAction(ACTION_NABI_MESSAGE_CENTER_REMOVE);
		it.putExtra("application", applicationName);
		it.putExtra("kid_id", kidIdValue > 0 ? kidId : "");
		it.putExtra("user_id", userKey);

		context.sendBroadcast(it);

		LOG.V(TAG, "notifyNabiMessageCenterRemove() - end");
	}
}
