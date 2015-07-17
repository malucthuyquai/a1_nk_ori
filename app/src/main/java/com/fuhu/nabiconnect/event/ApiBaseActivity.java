package com.fuhu.nabiconnect.event;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.fuhu.account.AccountParser;
import com.fuhu.account.data.AccessToken.OSGToken;
import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.ConnectActivity;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.chat.ChatActivity;
import com.fuhu.nabiconnect.friend.FriendActivity;
import com.fuhu.nabiconnect.friend.avatar.FriendBean;
import com.fuhu.nabiconnect.friend.dialog.CreateUserNameDialog;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.MailActivity;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.nsa.NSAActivity;
import com.fuhu.nabiconnect.nsa.util.NSAPhotoUtil;
import com.fuhu.nabiconnect.photo.PhotoActivity;
import com.fuhu.nabiconnect.photo.widget.PhotoSendingAnimationDialog;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.nabiconnect.utils.IntentList;
import com.fuhu.nabiconnect.utils.KidAccountManager;
import com.fuhu.nabiconnect.utils.LibraryUtils;
import com.fuhu.nabiconnect.utils.NetworkManager;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.ndnslibs.FGC_FLA_Handler;
import com.fuhu.ndnslibs.FGC_FLA_Handler.FriendsListPortions;
import com.fuhu.ndnslibsinstructs.acceptFriend_inObj;
import com.fuhu.ndnslibsinstructs.chatDeleteMessage_inObj;
import com.fuhu.ndnslibsinstructs.chatHistory_inObj;
import com.fuhu.ndnslibsinstructs.chatPollMessageUpdateTimestamp_inObj;
import com.fuhu.ndnslibsinstructs.chatSendMessage_inObj;
import com.fuhu.ndnslibsinstructs.deleteIncomingMail_inObj;
import com.fuhu.ndnslibsinstructs.deleteOutgoingMail_inObj;
import com.fuhu.ndnslibsinstructs.deletePhoto_inObj;
import com.fuhu.ndnslibsinstructs.denyFriend_inObj;
import com.fuhu.ndnslibsinstructs.friendsCreateUser_inObj;
import com.fuhu.ndnslibsinstructs.friendsLoginUser_inObj;
import com.fuhu.ndnslibsinstructs.getAllPhotos_inObj;
import com.fuhu.ndnslibsinstructs.getMailFromInbox_inObj;
import com.fuhu.ndnslibsinstructs.getMailFromOutbox_inObj;
import com.fuhu.ndnslibsinstructs.getReceivedPhotos_inObj;
import com.fuhu.ndnslibsinstructs.getSharedPhotos_inObj;
import com.fuhu.ndnslibsinstructs.makeFriend_inObj;
import com.fuhu.ndnslibsinstructs.removeFriend_inObj;
import com.fuhu.ndnslibsinstructs.removePhotoSharing_inObj;
import com.fuhu.ndnslibsinstructs.sendMailWithThumbnail_inObj;
import com.fuhu.ndnslibsinstructs.sendMail_inObj;
import com.fuhu.ndnslibsinstructs.sharePhotoWithThumb_inObj;
import com.fuhu.ndnslibsinstructs.sharePhoto_inObj;
import com.fuhu.ndnslibsinstructs.updateUserInfoWithAvatar_inObj;
import com.fuhu.ndnslibsinstructs.updateUserInfo_inObj;
import com.fuhu.ndnslibsinstructs.uploadUserAvatarImage_inObj;
import com.fuhu.ndnslibsoutstructs.chatHistory_outObj;
import com.fuhu.ndnslibsoutstructs.chatPollMessage_outObj;
import com.fuhu.ndnslibsoutstructs.friends_outObj;
import com.fuhu.ndnslibsoutstructs.getInboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getMail_outObj;
import com.fuhu.ndnslibsoutstructs.getOutboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getOutgoingMail_outObj;
import com.fuhu.ndnslibsoutstructs.getReceivedPhotos_outObj;
import com.fuhu.ndnslibsoutstructs.getSharedAndReceivedPhotos_outObj;
import com.fuhu.ndnslibsoutstructs.getSharedPhotos_outObj;
import com.fuhu.ndnslibsoutstructs.getUnreadPhotoCount_outObj;
import com.fuhu.ndnslibsoutstructs.idReturn_outObj;
import com.fuhu.ndnslibsoutstructs.pendingFriendRequests_outObj;
import com.fuhu.ndnslibsoutstructs.uploadUserAvatarImage_outObj;
import com.fuhu.util.NabiFunction;

import java.util.ArrayList;

public class ApiBaseActivity extends Tracking.TrackingInfoActivity {

	public static final String TAG = "ApiBaseActivity";
	public static final String KEY_IS_MOMMY_MODE = "mommyMode";
	public static final String KEY_LOGON_USER_KEY = "logonUserKey";
	public static final int ASK_MOM_REQUEST_CODE = 700;

	public static final String INTENT_CHAT_ACTIVITY = "com.fuhu.nabiconnect.chat.NABICHAT";
	public static final String INTENT_FRIEND_ACTIVITY = "com.fuhu.nabiconnect.friend.NABIFRIEND";
	public static final String INTENT_MAIL_ACTIVITY = "com.fuhu.nabiconnect.mail.NABIMAIL";
	public static final String INTENT_PHOTO_ACTIVITY = "com.fuhu.nabiconnect.photo.NABIPHOTO";

	// private AccountManager m_AccountManager;
	public static Activity m_Activity;
	public KidAccountManager m_KidAccountManager;
	public NetworkManager m_NetworkManager;
	private OSGToken m_OsgToken;
	private UserData m_CurrentUserData;
	private UserData mNSAUserData;
	private NabiNotificationManager m_NabiNotificationManager;

	// events
	public ApiEvent onLoginAccount;
	public ApiEvent onCreateAccount;
	public ApiEvent onUploadAvatar;
	public ApiEvent onUpdateUserInfo;
	public ApiEvent onUpdateUserInfoWithAvatar;
	public ApiEvent onGetFriendList;
	public ApiEvent onGetFriendRequestList;
	public ApiEvent onAcceptFriendRequest;
	public ApiEvent onDenyFriendRequest;
	public ApiEvent onMakeFriend;
	public ApiEvent onRemoveFriend;
	public ApiEvent onGetChatPoll;
	public ApiEvent onChatPollUpdated;
	public ApiEvent onGetConversation;
	public ApiEvent onGetChatHistory;
	public ApiEvent onSendChatMessage;
	public ApiEvent onGetMailInboxes;
	public ApiEvent onGetMailContent;
	public ApiEvent onGetMailOutboxes;
	public ApiEvent onGetSentMailContent;
	public ApiEvent onSendMail;

	// =============add ApiEvent by ricky
	public ApiEvent onDeletePhoto;
	public ApiEvent onCallUrlFromPaginatedResponseForSharePhoto;
	public ApiEvent onCallUrlFromPaginatedResponseForReceivedPhoto;
	public ApiEvent onDeleteChatMessage;
	public ApiEvent onDeleteInMailMessage;
	public ApiEvent onDeleteOutMailMessage;
	public ApiEvent onGetReceivedPhoto;
	public ApiEvent onGetSharedPhoto;
	public ApiEvent onGetAllPhoto;
	public ApiEvent onSharePhotoWithThumb;

	// =============add ApiEvent by Sid
	public ApiEvent onGetUnreadPhotoCount;

	private DatabaseAdapter m_DatabaseAdapter;
	private CreateUserNameDialog m_CreateUserNameDialog;
	private PhotoSendingAnimationDialog m_PhotoSendingAnimationDialog;
	boolean flagstrat = false;

	// for FriendCode -> (UserKey:KidId) cache
	static final public String PREF_NAME_OF_MESSAGE_CENTER_CACHE = "preference_for_message_center";
	private SharedPreferences m_Preference;

    public ApiBaseActivity(String name) {
        super(name);
    }

//    private void setTrackInfo(Intent intent) {
//        String from = null;
//        Bundle bundle = null;
//
//        do {
//            if(intent == null) break;
//
//            bundle = intent.getExtras();
//            if(bundle == null) break;
//
//            from = bundle.getString(TrackingInfo.TAG_INTNET_FROM);
//            TrackingInfo.INTENT_FROM = from;
//
//            //jack@150703
//            TrackingInfo.trackNextSync(from);
//            TrackingInfo.trackNextSync(getPageName());
//
//            Log.e(TAG, "intent from: " + from);
//
//        } while(false);
//
//        TrackingInfo.NOW_ACTIVITY = this;
//    }



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_Activity = this;

		LOG.V(TAG, "onCreate() - start");

		// get intent extra
		boolean isMommyMode = false;
		if (this instanceof NSAActivity || this instanceof ConnectActivity)
			isMommyMode = true;
		else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				isMommyMode = extras.getBoolean(KEY_IS_MOMMY_MODE, false);
			}
		}

		// library initialize
		setLibraryApiKey();

		FGC_FLA_Handler.getInstance().setUseFakeData(LibraryUtils.USE_FAKE_DATA);
		FGC_FLA_Handler.getInstance().SetDeviceData(com.fuhu.nabicontainer.util.Utils.getSerialId().trim(), // device
																											// key
				com.fuhu.nabicontainer.util.Utils.getMODELID().trim(), // device
																		// type
				com.fuhu.nabicontainer.util.NabiFunction.getEdition(), // device
																		// edition
				com.fuhu.nabicontainer.util.NabiFunction.getNabiVersion() // nabi
																			// firmware
				);

		// check wi-fi connection
		m_NetworkManager = new NetworkManager(this);
		if (!m_NetworkManager.isWifiEnabled()) {
			LOG.V(TAG, "onCreate() - wifi is not available");
			Intent intent = new Intent(NetworkManager.INTENT_CONNECTION_DIALOG);
			this.startActivity(intent);
			finish();
			return;
		}

		// check account
		if (AccountParser.getAccount(this) == null) {
			LOG.W(TAG, "onCreate() - there is no account.");
			Intent intent = new Intent(IntentList.INTENT_OUTSIDE_SIGN_IN);
			this.startActivity(intent);
			finish();
			return;
		}

		// check COPPA
		// only US version needs to check COPPA
		String countryCode = "";
		try {
			if (NabiFunction.getDefaultCountryCode() != null)
				countryCode = NabiFunction.getDefaultCountryCode();
		} catch (Throwable th) {
		}
		boolean isUsUkIeVersion = countryCode.equals("US") || countryCode.equals("UK") || countryCode.equals("IE");
		LOG.V(TAG, "onCreate() - countryCode is " + countryCode);

		if (isUsUkIeVersion) {
			try {
				if (!AccountParser.getAccount(this).getPassCOPPA()) {

					if (isMommyMode) {
						LOG.V(TAG, "onCreate() - COPPA is not passed from nabi library");

						performCoppaProcess();
						/*
						 * Intent intent = new
						 * Intent(IntentList.INTENT_CHECK_PRIVACY_POLICY);
						 * intent.putExtra("style", "SignInDialog");
						 * this.startActivity(intent); finish();
						 */
						return;
					} else {
						LOG.V(TAG, "onCreate() - COPPA is not passed from nabi library, in nabi Mode");

						// pop ask mom dialog
						Intent intent = new Intent(IntentList.INTENT_ASK_MOM);
						// intent.putExtra("dialogContent",
						// getResources().getString(R.string.friend_ask_mom_edit_name));
						intent.putExtra("style", "MomLoginActivity");
						intent.putExtra("fullscreen", true);
						startActivityForResult(intent, ASK_MOM_REQUEST_CODE);
						return;
					}

				} else {
					// COPPA is alreay passed, continue
					LOG.V(TAG, "onCreate() - COPPA is already passed from nabi mode");
				}
			} catch (Exception e) {
				LOG.E(TAG, "onCreate() - Failed to check COPPA.", e);
			}
		}

		// initialize database
		m_DatabaseAdapter = DatabaseAdapter.getInstance(this);

		// if (m_AccountManager == null)
		// m_AccountManager = AccountManager.getInstance(this,
		// com.fuhu.nabiconnect.utils.Utils.getAPIKEY(this));
		m_KidAccountManager = new KidAccountManager(this);// ,
															// m_AccountManager);

		try {
			m_OsgToken = AccountParser.getAccount(this).getAccessToken().getOSGToken();
		} catch (Throwable tr) {
			LOG.E(TAG, "Failed to get osg token", tr);
			finish();
			return;
		}

		// api events
		onLoginAccount = new ApiEvent(this);
		onCreateAccount = new ApiEvent(this);
		onUploadAvatar = new ApiEvent(this);
		onUpdateUserInfoWithAvatar = new ApiEvent(this);
		onGetFriendList = new ApiEvent(this);
		onGetFriendRequestList = new ApiEvent(this);
		onAcceptFriendRequest = new ApiEvent(this);
		onDenyFriendRequest = new ApiEvent(this);
		onMakeFriend = new ApiEvent(this);
		onRemoveFriend = new ApiEvent(this);
		onGetChatPoll = new ApiEvent(this);
		onChatPollUpdated = new ApiEvent(this);
		onGetConversation = new ApiEvent(this);
		onGetChatHistory = new ApiEvent(this);
		onSendChatMessage = new ApiEvent(this);
		onGetMailInboxes = new ApiEvent(this);
		onGetMailContent = new ApiEvent(this);
		onGetMailOutboxes = new ApiEvent(this);
		onGetSentMailContent = new ApiEvent(this);
		onSendMail = new ApiEvent(this);
		onDeleteInMailMessage = new ApiEvent(this);
		onDeleteOutMailMessage = new ApiEvent(this);
		onUpdateUserInfo = new ApiEvent(this);
		onDeletePhoto = new ApiEvent(this);
		onDeleteChatMessage = new ApiEvent(this);
		onGetReceivedPhoto = new ApiEvent(this);
		onGetSharedPhoto = new ApiEvent(this);
		onSharePhotoWithThumb = new ApiEvent(this);
		onGetAllPhoto = new ApiEvent(this);
		onGetUnreadPhotoCount = new ApiEvent(this);
		onCallUrlFromPaginatedResponseForSharePhoto = new ApiEvent(this);
		onCallUrlFromPaginatedResponseForReceivedPhoto = new ApiEvent(this);

		m_NabiNotificationManager = new NabiNotificationManager(this);

		m_Preference = this.getSharedPreferences(PREF_NAME_OF_MESSAGE_CENTER_CACHE, Context.MODE_PRIVATE);

		LOG.V(TAG, "onCreate() - end");
	}

	@Override
	protected void onResume() {
		super.onResume();
		setLibraryApiKey();
		// check wi-fi connection
		if (!m_NetworkManager.isWifiEnabled()) {
			LOG.V(TAG, "onResume() - wifi is not available");
			Intent intent = new Intent(NetworkManager.INTENT_CONNECTION_DIALOG);
			this.startActivity(intent);
			finish();
		}
	}

	private void setLibraryApiKey() {
		if (Utils.isProductionVersion(this)) {

			LOG.V(TAG, "setLibraryApiKey() - production version");

			String apiKey = LibraryUtils.PRODUCTION_NSA_API_KEY;
			if (this instanceof FriendActivity) {
				LOG.V(TAG, "setLibraryApiKey() - friend activity");
				apiKey = LibraryUtils.PRODUCTION_FRIEND_API_KEY;
			} else if (this instanceof ChatActivity) {
				LOG.V(TAG, "setLibraryApiKey() - chat activity");
				apiKey = LibraryUtils.PRODUCTION_CHAT_API_KEY;
			} else if (this instanceof MailActivity) {
				LOG.V(TAG, "setLibraryApiKey() - mail activity");
				apiKey = LibraryUtils.PRODUCTION_MAIL_API_KEY;
			} else if (this instanceof PhotoActivity) {
				LOG.V(TAG, "setLibraryApiKey() - photo activity");
				apiKey = LibraryUtils.PRODUCTION_PHOTO_API_KEY;
			} else if (this instanceof NSAActivity || this instanceof ConnectActivity) {
				LOG.V(TAG, "setLibraryApiKey() - nsa activity or connect activity");
				apiKey = LibraryUtils.PRODUCTION_NSA_API_KEY;
			} else {
				LOG.W(TAG, "setLibraryApiKey() - undefined activity");
			}

			FGC_FLA_Handler.getInstance().Initialize(this, apiKey, LibraryUtils.PRODUCTION_BASE_URL,
					LibraryUtils.USE_DEFAULT_SPINNER);
		} else {

			LOG.V(TAG, "setLibraryApiKey() - staging version");

			String apiKey = LibraryUtils.STAGING_NSA_API_KEY;
			if (this instanceof FriendActivity) {
				LOG.V(TAG, "setLibraryApiKey() - friend activity");
				apiKey = LibraryUtils.STAGING_FRIEND_API_KEY;
			} else if (this instanceof ChatActivity) {
				LOG.V(TAG, "setLibraryApiKey() - chat activity");
				apiKey = LibraryUtils.STAGING_CHAT_API_KEY;
			} else if (this instanceof MailActivity) {
				LOG.V(TAG, "setLibraryApiKey() - mail activity");
				apiKey = LibraryUtils.STAGING_MAIL_API_KEY;
			} else if (this instanceof PhotoActivity) {
				LOG.V(TAG, "setLibraryApiKey() - photo activity");
				apiKey = LibraryUtils.STAGING_PHOTO_API_KEY;
			} else if (this instanceof NSAActivity || this instanceof ConnectActivity) {
				LOG.V(TAG, "setLibraryApiKey() - nsa activity or connect activity");
				apiKey = LibraryUtils.STAGING_NSA_API_KEY;
			} else {
				LOG.W(TAG, "setLibraryApiKey() - undefined activity");
			}

			FGC_FLA_Handler.getInstance().Initialize(this, apiKey, LibraryUtils.STAGING_BASE_URL,
					LibraryUtils.USE_DEFAULT_SPINNER);

			FGC_FLA_Handler.getInstance().setLogLevel(2);
		}
	}

	public NabiNotificationManager getNabiNotificationManager() {
		return this.m_NabiNotificationManager;
	}

	public NetworkManager getNetworkManager() {
		return this.m_NetworkManager;
	}

	public DatabaseAdapter getDatabaseAdapter() {
		return this.m_DatabaseAdapter;
	}

	public UserData getCurrentUserData() {
		return this.m_CurrentUserData;
	}

	public UserData getNSACurrentUserData() {
		return mNSAUserData;
	}

	// Library calls
	public void loginAccount() {
		LOG.V(TAG, "loginAccount - start");

		try {

			long kidId = m_KidAccountManager.getNabiCurrentKid().getKidId();

			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().LoginAccount(
					this,
					ApiBaseActivity.class.getMethod("loginUserSuccess", UserData.class),
					ApiBaseActivity.class.getMethod("loginUserFailure", String.class),
					ApiBaseActivity.class.getMethod("accountNeedsCreate", String.class),
					new friendsLoginUser_inObj(m_OsgToken.getOSGUserKey(), m_OsgToken.getOSGSessionKey(), m_OsgToken
							.getOSGAuthKey(), String.valueOf(kidId)
					// fakeId
					));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		} catch (Throwable tr) {
			LOG.E(TAG, "loginAccount - failed to login", tr);
		}

		LOG.V(TAG, "loginAccount - end");
	}

	public void loginUserSuccess(UserData data) {
		LOG.V(TAG, "loginUserSuccess - " + data);
		m_CurrentUserData = data;

		if (data != null) {
			onLoginAccount.raise(true);
			LOG.V(TAG, "loginUserSuccess - getData : " + data.getData());

			// update database
			getDatabaseAdapter().updateUserData(data);

			// store cache data
			String cacheString = data.userKey + ":" + data.kidID;
			LOG.V(TAG, "loginUserSuccess - cacheString is " + cacheString);
			m_Preference.edit().putString(data.friendCode, cacheString).commit();

			// register GCM server
			m_NabiNotificationManager.register(data.friendCode, new NabiNotificationManager.IRegisterCallback() {

				@Override
				public void onRegistered() {
					LOG.V(TAG, "onCreate() -  NabiNotificationManager has been registered.");
				}
			});

			// notify message center to remove
			String applicationName = "";
			if (this instanceof FriendActivity) {
				applicationName = NabiNotificationManager.APPLICATION_NAME_FRIEND;
			} else if (this instanceof ChatActivity) {
				applicationName = NabiNotificationManager.APPLICATION_NAME_CHAT;
			} else if (this instanceof MailActivity) {
				applicationName = NabiNotificationManager.APPLICATION_NAME_MAIL;
			} else if (this instanceof PhotoActivity) {
				applicationName = NabiNotificationManager.APPLICATION_NAME_PHOTO;
			}
			if (!applicationName.isEmpty()) {
				NabiNotificationManager.notifyNabiMessageCenterRemove(this, applicationName, m_CurrentUserData.kidID,
						m_OsgToken.getOSGUserKey());
			}
		}
	}

	public void loginUserFailure(String data) {
		LOG.V(TAG, "loginUserFailure - " + data);
		m_CurrentUserData = null;
		LOG.V(TAG, "loginUserFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onLoginAccount.raise(false);
	}

	public void loginAccountNoKid() {
		LOG.V(TAG, "loginAccountNoKid - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().LoginAccount(
					this,
					ApiBaseActivity.class.getMethod("loginUserSuccess", UserData.class),
					ApiBaseActivity.class.getMethod("loginUserFailure", String.class),
					ApiBaseActivity.class.getMethod("accountNeedsCreate", String.class),
					new friendsLoginUser_inObj(m_OsgToken.getOSGUserKey(), m_OsgToken.getOSGSessionKey(), m_OsgToken
							.getOSGAuthKey()));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		} catch (Throwable tr) {
			LOG.E(TAG, "loginAccountNoKid - failed to login", tr);
		}

		LOG.V(TAG, "loginAccountNoKid - end");
	}

	public void accountNeedsCreate(String data) {
		LOG.V(TAG, "accountNeedsCreate - " + data);
		m_CurrentUserData = null;
		LOG.V(TAG, "accountNeedsCreate - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onLoginAccount.raise(false);
	}

	public void createAccount(String inputUserName, FriendBean bean) {
		LOG.V(TAG, "createAccount - start");

		// friendsCreateUser_inObj obj = new
		// friendsCreateUser_inObj("firstName", "lastName",
		// "UserName",token.getOSGUserKey(), String.valueOf(KID_ID), 1, 1, 1, 1,
		// null, "deviceType");
		try {

			long kidId = m_KidAccountManager.getNabiCurrentKid().getKidId();

			ArrayList<Long> accessories = FriendBean.getAccssoriesList(bean);
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance()
					.CreateAccount(
							this,
							ApiBaseActivity.class.getMethod("createAccountSuccess", UserData.class),
							ApiBaseActivity.class.getMethod("createAccountFailure", String.class),
							new friendsCreateUser_inObj("firstName", "lastName", inputUserName, m_OsgToken
									.getOSGUserKey(), m_OsgToken.getOSGSessionKey(), m_OsgToken.getOSGAuthKey(), String
									.valueOf(kidId),
							// fakeId,
									bean.getCharacterTypeIndex(), bean.getCharacterColorIndex(), bean
											.getCharacterClothingIndex(), bean.getCharacterBackgroundColorIndex(),
									accessories));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		} catch (Throwable tr) {
			LOG.E(TAG, "createAccount - failed to createAccount", tr);
		}

		LOG.V(TAG, "createAccount - end");
	}

	public void createAccountSuccess(UserData data) {
		LOG.V(TAG, "createAccountSuccess - " + data);
		m_CurrentUserData = data;
		onCreateAccount.raise(true, data);
	}

	public void createAccountFailure(String data) {
		LOG.V(TAG, "createAccountFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onCreateAccount.raise(false, data);
	}

	public void createAccountNoKid(String inputUserName, FriendBean bean) {
		LOG.V(TAG, "createAccountNoKid - start");

		// friendsCreateUser_inObj obj = new
		// friendsCreateUser_inObj("firstName", "lastName",
		// "UserName",token.getOSGUserKey(), String.valueOf(KID_ID), 1, 1, 1, 1,
		// null, "deviceType");
		try {

			long kidId = m_KidAccountManager.getNabiCurrentKid().getKidId();

			ArrayList<Long> accessories = FriendBean.getAccssoriesList(bean);
			setApiProcessingDialog(true);
			FGC_FLA_Handler
					.getInstance()
					.CreateAccount(
							this,
							ApiBaseActivity.class.getMethod("createAccountSuccess", UserData.class),
							ApiBaseActivity.class.getMethod("createAccountFailure", String.class),
							new friendsCreateUser_inObj("firstName", "lastName", inputUserName, m_OsgToken
									.getOSGUserKey(), bean.getCharacterTypeIndex(), bean.getCharacterColorIndex(), bean
									.getCharacterClothingIndex(), bean.getCharacterBackgroundColorIndex(), accessories));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		} catch (Throwable tr) {
			LOG.E(TAG, "createAccountNoKid - failed to createAccount", tr);
		}

		LOG.V(TAG, "createAccountNoKid - end");
	}

	public void updateUserInfoWithAvatar(String userId, FriendBean bean, String filePath) {
		LOG.V(TAG, "updateUserInfoWithAvatar - start");

		try {

			ArrayList<Long> accessories = FriendBean.getAccssoriesList(bean);
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().UpdateUserInfoWithAvatar(
					this,
					ApiBaseActivity.class.getMethod("updateUserInfoWithAvatarSuccess",
							uploadUserAvatarImage_outObj.class),
					ApiBaseActivity.class.getMethod("updateUserInfoWithAvatarFailure", String.class),
					new updateUserInfoWithAvatar_inObj(userId, bean.getName(), bean.getCharacterTypeIndex(), bean
							.getCharacterColorIndex(), bean.getCharacterClothingIndex(), bean
							.getCharacterBackgroundColorIndex(), accessories, filePath));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		} catch (Throwable tr) {
			LOG.E(TAG, "updateUserInfoWithAvatar - failed to updateUserInfoWithAvatar", tr);
		}

		LOG.V(TAG, "updateUserInfoWithAvatar - end");
	}

	public void updateUserInfoWithAvatarSuccess(uploadUserAvatarImage_outObj data) {
		LOG.V(TAG, "updateUserInfoWithAvatarSuccess - " + data);
		onUpdateUserInfoWithAvatar.raise(true, data);
	}

	public void updateUserInfoWithAvatarFailure(String data) {
		LOG.V(TAG,
				"updateUserInfoWithAvatarFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onUpdateUserInfoWithAvatar.raise(false, data);
	}

	public void updateUserInfo(String userId, FriendBean bean) {
		LOG.V(TAG, "updateUserInfo - start");

		try {

			ArrayList<Long> accessories = FriendBean.getAccssoriesList(bean);
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().UpdateUserInfo(
					this,
					ApiBaseActivity.class.getMethod("updateUserInfoSuccess", String.class),
					ApiBaseActivity.class.getMethod("updateUserInfoFailure", String.class),
					new updateUserInfo_inObj(userId, bean.getName(), bean.getCharacterTypeIndex(), bean
							.getCharacterColorIndex(), bean.getCharacterClothingIndex(), bean
							.getCharacterBackgroundColorIndex(), accessories));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		} catch (Throwable tr) {
			LOG.E(TAG, "updateUserInfo - failed to uploadUserInfo", tr);
		}

		LOG.V(TAG, "updateUserInfo - end");
	}

	public void updateUserInfoSuccess(String data) {
		LOG.V(TAG, "updateUserInfoSuccess - " + data);
		onUpdateUserInfo.raise(true, data);
	}

	public void updateUserInfoFailure(String data) {
		LOG.V(TAG, "updateUserInfoFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onUpdateUserInfo.raise(false, data);
	}

	public void uploadUserAvatar(String userId, String filePath) {
		LOG.V(TAG, "uploadUserAvatar - start");

		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().UploadUserAvatarImage(this,
					ApiBaseActivity.class.getMethod("uploadUserAvatarSuccess", uploadUserAvatarImage_outObj.class),
					ApiBaseActivity.class.getMethod("uploadUserAvatarFailure", String.class),
					new uploadUserAvatarImage_inObj(userId, filePath));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		} catch (Throwable tr) {
			LOG.E(TAG, "uploadUserAvatar - failed to uploadUserAvatar", tr);
		}

		LOG.V(TAG, "uploadUserAvatar - end");
	}

	public void uploadUserAvatarSuccess(uploadUserAvatarImage_outObj data) {
		LOG.V(TAG, "uploadUserAvatarSuccess - " + data);
		onUploadAvatar.raise(true, data);

		if (data != null) {
			LOG.V(TAG, "m_avatarImageUrl : " + data.getAvatarImageUrl());
		}
	}

	public void uploadUserAvatarFailure(String data) {
		LOG.V(TAG, "uploadUserAvatarFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onUploadAvatar.raise(false, data);
	}

	public void getFriendList(String userKey) {
		getFriendList(userKey, true, null);
	}

	public void getFriendList(String userKey, FriendsListPortions portions) {
		getFriendList(userKey, true, portions);
	}

	public void getFriendList(String userKey, boolean showDialog) {
		getFriendList(userKey, showDialog, null);
	}

	public void getFriendList(String userKey, boolean showDialog, FriendsListPortions portions) {
		LOG.V(TAG, "getFriendList - start");

		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetFriendsList(this,
					ApiBaseActivity.class.getMethod("getFriendListSuccess", friends_outObj.class),
					ApiBaseActivity.class.getMethod("getFriendListFailure", String.class), userKey, portions);
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "getFriendList - end");
	}

	public void getFriendListSuccess(friends_outObj data) {
		LOG.V(TAG, "getFriendListSuccess - " + data);
		onGetFriendList.raise(true, data);

		/*
		 * if (data != null) { for (FriendData fData : data.getFriends()) {
		 * fData.dumpData(); // LOG.V(TAG, "getFriendListSuccess() - "+fData.);
		 * } }
		 */
	}

	public void getFriendListFailure(String data) {
		LOG.V(TAG, "getFriendListFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetFriendList.raise(false, data);
	}

	public void getPendingFriendList(String userId) {
		getPendingFriendList(userId, true);
	}

	public void getPendingFriendList(String userId, boolean showDialog) {
		LOG.V(TAG, "getPendingFriendList - start");

		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetPendingFriendRequests(this,
					ApiBaseActivity.class.getMethod("getPendingFriendListSuccess", pendingFriendRequests_outObj.class),
					ApiBaseActivity.class.getMethod("getPendingFriendListFailure", String.class), userId);
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "getPendingFriendList - end");
	}

	public void getPendingFriendListSuccess(pendingFriendRequests_outObj data) {
		LOG.V(TAG, "getPendingFriendListSuccess - " + data);
		onGetFriendRequestList.raise(true, data);
		/*
		 * if (data != null) { for (PendingFriendRequestData fData :
		 * data.getPendingFriendRequests()) { fData.dumpData(); // LOG.V(TAG,
		 * "getFriendListSuccess() - "+fData.); } }
		 */

	}

	public void getPendingFriendListFailure(String data) {
		LOG.V(TAG, "getPendingFriendListFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetFriendRequestList.raise(false, data);
	}

	public void acceptFriend(String userId, String targetId, String targetFriendCode) {
		LOG.V(TAG, "acceptFriend - start");

		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().AcceptFriendRequest(this,
					ApiBaseActivity.class.getMethod("acceptFriendSuccess", String.class),
					ApiBaseActivity.class.getMethod("acceptFriendFailure", String.class),
					new acceptFriend_inObj(userId, targetId, targetFriendCode));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "acceptFriend - end");
	}

	public void acceptFriendSuccess(String data) {
		LOG.V(TAG, "acceptFriendSuccess - " + data);
		onAcceptFriendRequest.raise(true, data);

	}

	public void acceptFriendFailure(String data) {
		LOG.V(TAG, "acceptFriendFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onAcceptFriendRequest.raise(false, data);
	}

	public void denyFriend(String userId, String targetId) {
		LOG.V(TAG, "denyFriend - start");

		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().DenyFriendRequest(this,
					ApiBaseActivity.class.getMethod("denyFriendSuccess", String.class),
					ApiBaseActivity.class.getMethod("denyFriendFailure", String.class),
					new denyFriend_inObj(userId, targetId));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "denyFriend - end");
	}

	public void denyFriendSuccess(String data) {
		LOG.V(TAG, "denyFriendSuccess - " + data);
		onDenyFriendRequest.raise(true, data);

	}

	public void denyFriendFailure(String data) {
		LOG.V(TAG, "denyFriendFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onDenyFriendRequest.raise(false, data);
	}

	public void makeFriend(String userId, String targetFriendcode) {
		LOG.V(TAG, "makeFriend - start");

		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().MakeFriendRequest(this,
					ApiBaseActivity.class.getMethod("makeFriendSuccess", String.class),
					ApiBaseActivity.class.getMethod("makeFriendFailure", String.class),
					new makeFriend_inObj(userId, targetFriendcode));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "makeFriend - end");
	}

	public void makeFriendSuccess(String data) {
		LOG.V(TAG, "makeFriendSuccess - " + data);
		onMakeFriend.raise(true, data);

	}

	public void makeFriendFailure(String data) {
		LOG.V(TAG, "makeFriendFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onMakeFriend.raise(false, data);
	}

	public void removeFriend(String userId, String targetId) {
		LOG.V(TAG, "removeFriend - start");

		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().RemoveFriend(this,
					ApiBaseActivity.class.getMethod("removeFriendSuccess", String.class),
					ApiBaseActivity.class.getMethod("removeFriendFailure", String.class),
					new removeFriend_inObj(userId, targetId));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "removeFriend - end");
	}

	public void removeFriendSuccess(String data) {
		LOG.V(TAG, "removeFriendSuccess - " + data);
		onRemoveFriend.raise(true, data);

	}

	public void removeFriendFailure(String data) {
		LOG.V(TAG, "removeFriendFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onRemoveFriend.raise(false, data);
	}

	/**
	 * This API checks for UNREAD message. If you want to see the detail of
	 * message, you should use the conversation id to check cahtPollUpdate
	 * 
	 * @param userId
	 */
	public void getChatPollMessage(String userId) {
		getChatPollMessage(userId, true);
	}

	public void getChatPollMessage(String userId, boolean showDialog) {

		LOG.V(TAG, "getChatPollMessage - start");

		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetChatPollMessage(this,
					ApiBaseActivity.class.getMethod("getChatPollMessageSuccess", chatPollMessage_outObj.class),
					ApiBaseActivity.class.getMethod("getChatPollMessageFailure", String.class), userId);
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "getChatPollMessage - end");
	}

	public void getChatPollMessageSuccess(chatPollMessage_outObj data) {
		LOG.V(TAG, "getChatPollMessageSuccess - " + data);
		onGetChatPoll.raise(true, data);
		if (data != null) {

			// data.DumpData();
			/*
			 * LOG.V(TAG, "getChatHistory - dump conversations");
			 * for(conversationData fData : data.m_Conversations) {
			 * fData.DumpData(); //LOG.V(TAG,
			 * "getFriendListSuccess() - "+fData.); }
			 */
		}

	}

	public void getChatPollMessageFailure(String data) {
		LOG.V(TAG, "getChatPollMessageFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetChatPoll.raise(false, data);
	}

	public void getChatPollMessageUpdate(String userId, ArrayList<String> coversationList) {
		LOG.V(TAG, "getChatPollMessageUpdate - start");

		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().GetChatPollMessageUpdateTimestamp(
					this,
					ApiBaseActivity.class.getMethod("getChatPollMessageUpdateSuccess", chatPollMessage_outObj.class),
					ApiBaseActivity.class.getMethod("getChatPollMessageUpdateFailure", String.class),
					new chatPollMessageUpdateTimestamp_inObj(userId, coversationList,
							LibraryUtils.CAHT_POLL_MESSAGE_LIMIT));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "getChatPollMessageUpdate - end");
	}

	public void getChatPollMessageUpdateSuccess(chatPollMessage_outObj data) {
		LOG.V(TAG, "getChatPollMessageUpdateSuccess - " + data);
		onChatPollUpdated.raise(true, data);
		/*
		 * if (data != null) { data.DumpData();
		 * 
		 * LOG.V(TAG, "getChatPollMessageUpdateSuccess - dump conversations");
		 * for (conversationData fData : data.getConversations()) {
		 * fData.DumpData(); // LOG.V(TAG, "getFriendListSuccess() - "+fData.);
		 * } }
		 */
	}

	public void getChatPollMessageUpdateFailure(String data) {
		LOG.V(TAG,
				"getChatPollMessageUpdateFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onChatPollUpdated.raise(false, data);
	}

	public void getChatHistory(String conversationId, int limit, long since, long until, boolean showDialog) {
		getChatHistory(getCurrentUserData() == null ? null : getCurrentUserData().userKey, conversationId, limit,
				since, until, showDialog);
	}

	private String mLastLoadChatId = "";
	private int mLastLoadLimit = 0;
	private long mLastLoadSince = 0;
	private long mLastLoadUntil = 0;

	public void getChatHistory(String userId, String conversationId, int limit, long since, long until) {
		getChatHistory(userId, conversationId, limit, since, until, true);
	}

	/**
	 * sink method
	 */
	public void getChatHistory(String userId, String conversationId, int limit, long since, long until,
			boolean showDialog) {
		LOG.V(TAG, "getChatHistory - start");
		/**
		 * serves as a temporary solution for double-click on chat contact
		 * causing chat history to be loaded twice
		 */
		if (mLastLoadChatId == conversationId && mLastLoadLimit == limit && mLastLoadSince == since
				&& mLastLoadUntil == until) {
			// exact same request, skip
			return;
		} else {
			mLastLoadChatId = conversationId;
			mLastLoadLimit = limit;
			mLastLoadSince = since;
			mLastLoadUntil = until;
			try {
				setApiProcessingDialog(showDialog);
				FGC_FLA_Handler.getInstance().GetChatHistory(ApiBaseActivity.this,

				ApiBaseActivity.class.getMethod("getChatHistorySuccess", chatHistory_outObj.class),
						ApiBaseActivity.class.getMethod("getChatHistoryFailure", String.class),
						new chatHistory_inObj(userId, conversationId, limit, since, until));
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		LOG.V(TAG, "getChatHistory - end");
	}

	public void getChatHistorySuccess(chatHistory_outObj data) {
		mLastLoadChatId = "";
		LOG.V(TAG, "getChatHistorySuccess - " + data);
		onGetChatHistory.raise(true, data);
	}

	public void getChatHistoryFailure(String data) {
		mLastLoadChatId = "";
		LOG.V(TAG, "getChatHistoryFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetChatHistory.raise(false, data);
	}

	public void createGetConversation(ArrayList<String> userIdList) {
		LOG.V(TAG, "createGetConversation - start");

		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().CreateGetConversation(this,
					ApiBaseActivity.class.getMethod("createGetConversationSuccess", String.class),
					ApiBaseActivity.class.getMethod("createGetConversationFailure", String.class), userIdList);
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "createGetConversation - end");
	}

	public void createGetConversationSuccess(String data) {
		LOG.V(TAG, "createGetConversationSuccess - " + data);
		onGetConversation.raise(true, data);
	}

	public void createGetConversationFailure(String data) {
		LOG.V(TAG, "createGetConversationFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetConversation.raise(false, data);
	}

	public void sendMessage(String conversationId, String messageBody) {
		LOG.V(TAG, "sendMessage - start");
		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().SendMessage(this,
					ApiBaseActivity.class.getMethod("sendMessageSuccess", String.class),
					ApiBaseActivity.class.getMethod("sendMessageFailure", String.class),
					new chatSendMessage_inObj(conversationId, messageBody));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "sendMessage - end");
	}

	/**
	 * use this to pass in a String token. The same token will be delivered to
	 * callback methods
	 * 
	 * @param conversationId
	 * @param messageBody
	 * @param messageToken
	 */
	public void sendMessage(String conversationId, String messageBody, String messageToken) {
		LOG.V(TAG, "sendMessage - start, token is " + messageToken);
		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().SendMessage(this,
					ApiBaseActivity.class.getMethod("sendMessageSuccess", String.class),
					ApiBaseActivity.class.getMethod("sendMessageFailure", String.class),
					new chatSendMessage_inObj(conversationId, messageBody, messageToken));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "sendMessage - end");
	}

	/**
	 * 
	 * @param data
	 *            String token originally sent with api call if supplied. An
	 *            empty String will be passed in if nothing was supplied.
	 * 
	 */
	public void sendMessageSuccess(String data) {
		LOG.V(TAG, "sendMessageSuccess - " + data);
		onSendChatMessage.raise(true, data);

	}

	/**
	 * 
	 * @param data
	 *            String token originally sent with api call if supplied. An
	 *            empty String will be passed in if nothing was supplied.
	 * 
	 */
	public void sendMessageFailure(String data) {
		LOG.V(TAG, "sendMessageFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onSendChatMessage.raise(false, data);
	}

	public void deleteChatMessage(String parentKey, String messageId) {
		LOG.V(TAG, "deleteChatMessage - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().DeleteMessage(this,
					ApiBaseActivity.class.getMethod("deleteChatMessageSuccess", String.class),
					ApiBaseActivity.class.getMethod("deleteChatMessageFailure", String.class),
					new chatDeleteMessage_inObj(parentKey, messageId));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "deleteChatMessage - end");
	}

	public void deleteChatMessageSuccess(String data) {
		LOG.V(TAG, "deleteChatMessageSuccess - " + data);
		onDeleteChatMessage.raise(true, data);
	}

	public void deleteChatMessageFailure(String data) {
		LOG.V(TAG, "deleteChatMessageFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onDeleteChatMessage.raise(false, data);
	}

	public void getInboxes(String userId) {
		getInboxes(userId, true);
	}

	public void getInboxes(String userId, boolean showDialog) {
		LOG.V(TAG, "getInboxes - start");

		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetInboxes(this,
					ApiBaseActivity.class.getMethod("getInboxesSuccess", getInboxes_outObj.class),
					ApiBaseActivity.class.getMethod("getInboxesFailure", String.class), userId);
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "getInboxes - end");
	}

	public void getInboxesSuccess(getInboxes_outObj data) {
		LOG.V(TAG, "getInboxesSuccess - " + data);
		onGetMailInboxes.raise(true, data);
	}

	public void getInboxesFailure(String data) {
		LOG.V(TAG, "getInboxesFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetMailInboxes.raise(false, data);
	}

	public void getOutboxes(String userId) {
		getOutboxes(userId, true);
	}

	public void getOutboxes(String userId, boolean showDialog) {
		LOG.V(TAG, "getOutboxes - start");
		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetOutboxes(this,
					ApiBaseActivity.class.getMethod("getOutboxesSuccess", getOutboxes_outObj.class),
					ApiBaseActivity.class.getMethod("getOutboxesFailure", String.class), userId);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "getOutboxes - end");
	}

	public void getOutboxesSuccess(getOutboxes_outObj data) {
		LOG.V(TAG, "getOutboxesSuccess - " + data);
		onGetMailOutboxes.raise(true, data);
	}

	public void getOutboxesFailure(String data) {
		LOG.V(TAG, "getOutboxesFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetMailOutboxes.raise(false, data);
	}

	public void getMail(String userId, String inboxId) {
		getMail(userId, inboxId, true);

	}

	public void getMail(String userId, String inboxId, boolean showDialog) {
		LOG.V(TAG, "getMail - start");

		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetMailFromInbox(this,
					ApiBaseActivity.class.getMethod("getMailSuccess", getMail_outObj.class),
					ApiBaseActivity.class.getMethod("getMailFailure", String.class),
					new getMailFromInbox_inObj(userId, inboxId));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "getMail - end");
	}

	public void getMailSuccess(getMail_outObj data) {
		LOG.V(TAG, "getMailSuccess - " + data);
		onGetMailContent.raise(true, data);
		/*
		 * if (data != null) { data.dumpData();
		 * 
		 * LOG.V(TAG, "dump each data");
		 * 
		 * for (MailData fData : data.getMail()) { fData.dumpData(); //
		 * LOG.V(TAG, "getFriendListSuccess() - "+fData.); } }
		 */
	}

	public void getMailFailure(String data) {
		LOG.V(TAG, "getMailFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetMailContent.raise(false, data);
	}

	public void getSentMail(String userId, String outboxId) {
		getSentMail(userId, outboxId, true);
	}

	public void getSentMail(String userId, String outboxId, boolean showDialog) {
		LOG.V(TAG, "getSentMail - start");
		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetMailFromOutbox(this,
					ApiBaseActivity.class.getMethod("getSentMailSuccess", getOutgoingMail_outObj.class),
					ApiBaseActivity.class.getMethod("getSentMailFailure", String.class),
					new getMailFromOutbox_inObj(userId, outboxId));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "getMail - end");
	}

	public void getSentMailSuccess(getOutgoingMail_outObj data) {
		LOG.V(TAG, "getSentMailSuccess - " + data);
		onGetSentMailContent.raise(true, data);
	}

	public void getSentMailFailure(String data) {
		LOG.V(TAG, "getSentMailFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetSentMailContent.raise(false, data);
	}

	public void sendMail(String userId, ArrayList<String> targetIds, String filePath, String mailName) {
		LOG.V(TAG, "sendMail - start");

		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().SendMail(this,
					ApiBaseActivity.class.getMethod("sendMailSuccess", idReturn_outObj.class),
					ApiBaseActivity.class.getMethod("sendMailFailure", String.class),
					new sendMail_inObj(userId, targetIds, filePath, mailName));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "sendMail - end");
	}

	public void sendMailWithThumbnail(String userId, ArrayList<String> targetIds, String filePath, String mailName,
			String mailSize, String thumbnailFilePath, String thumbnailSize) {
		LOG.V(TAG, "sendMail - start");

		// the size string will be like "1024x768"
		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().SendMailWithThumbNail(
					this,
					ApiBaseActivity.class.getMethod("sendMailSuccess", idReturn_outObj.class),
					ApiBaseActivity.class.getMethod("sendMailFailure", String.class),
					new sendMailWithThumbnail_inObj(userId, targetIds, filePath, mailName, mailSize, thumbnailFilePath,
							thumbnailSize));
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "sendMail - end");
	}

	public void sendMailSuccess(idReturn_outObj data) {
		LOG.V(TAG, "sendMailSuccess - " + data);
		onSendMail.raise(true, data);
	}

	public void sendMailFailure(String data) {
		LOG.V(TAG, "sendMailFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onSendMail.raise(false, data);
	}

	public void deleteInMailMessage(String boxId, String mailId) {
		LOG.V(TAG, "deleteInMailMessage - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().DeleteIncomingMail(
					this,
					ApiBaseActivity.class.getMethod("deleteInMailMessageSuccess", String.class),
					ApiBaseActivity.class.getMethod("deleteInMailMessageFailure", String.class),
					new deleteIncomingMail_inObj(getCurrentUserData() == null ? null : getCurrentUserData().userKey,
							boxId, mailId));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "deleteInMailMessage - end");
	}

	public void deleteInMailMessageSuccess(String data) {
		LOG.V(TAG, "deleteInMailMessageSuccess - " + data);
		onDeleteInMailMessage.raise(true, data);
	}

	public void deleteInMailMessageFailure(String data) {
		LOG.V(TAG, "deleteInMailMessageFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onDeleteInMailMessage.raise(false, data);
	}

	public void deleteOutMailMessage(String boxId, String mailId) {
		LOG.V(TAG, "deleteOutMailMessage - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().DeleteOutgoingMail(
					this,
					ApiBaseActivity.class.getMethod("deleteOutMailMessageSuccess", String.class),
					ApiBaseActivity.class.getMethod("deleteOutMailMessageFailure", String.class),
					new deleteOutgoingMail_inObj(getCurrentUserData() == null ? null : getCurrentUserData().userKey,
							boxId, mailId));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "deleteOutMailMessage - end");
	}

	public void deleteOutMailMessageSuccess(String data) {
		LOG.V(TAG, "deleteOutMailMessageSuccess - " + data);
		onDeleteOutMailMessage.raise(true, data);
	}

	public void deleteOutMailMessageFailure(String data) {
		LOG.V(TAG, "deleteOutMailMessageFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onDeleteOutMailMessage.raise(false, data);
	}

	// ================= add photo api function by Ricky==============
	public void SharePhoto(String UserId, ArrayList<String> targetUserId, String filePath, String photoName,
			String thumbnailfilePath) {
		LOG.V(TAG, "SharePhoto - start");

		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().SharePhoto(this,
					ApiBaseActivity.class.getMethod("SharePhotoSuccess", idReturn_outObj.class),
					ApiBaseActivity.class.getMethod("SharePhotoFailure", String.class),
					new sharePhoto_inObj(UserId, targetUserId, filePath, photoName, thumbnailfilePath));

		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "SharePhoto - end");
	}

	public void SharePhotoSuccess(idReturn_outObj data) {
		LOG.V(TAG, "SharePhotoSuccess - id = " + data.getId());

		// onGetMailContent.raise(true, data);

	}

	public void SharePhotoFailure(String data) {
		LOG.V(TAG, "SharePhotoFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		// onGetMailContent.raise(false, data);
	}

	public void SharePhotoWithThumb(String UserId, ArrayList<String> targetUserId, String filePath, String photoName,
			String thumbnailfilePath, String thumbDimens, String fileDimens) {
		LOG.V(TAG, "SharePhotoWithThumb - start");
		setApiProcessingDialog(false);
		try {
			FGC_FLA_Handler.getInstance().SharePhotoWithThumb(
					this,
					ApiBaseActivity.class.getMethod("SharePhotoWithThumbSuccess", idReturn_outObj.class),
					ApiBaseActivity.class.getMethod("SharePhotoWithThumbFailure", String.class),
					new sharePhotoWithThumb_inObj(UserId, targetUserId, filePath, photoName, thumbnailfilePath,
							thumbDimens, fileDimens));

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOG.V(TAG, "SharePhotoWithThumb - end");
	}

	public void SharePhotoWithThumbSuccess(idReturn_outObj data) {
		LOG.V(TAG, "SharePhotoWithThumbSuccess - id = " + data.getId());
		onSharePhotoWithThumb.raise(true, data);
	}

	public void SharePhotoWithThumbFailure(String data) {
		LOG.V(TAG, "SharePhotoWithThumbFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onSharePhotoWithThumb.raise(false, data);
	}

	public void CallUrlFromPaginatedResponseForSharePhoto(String url) {

		LOG.V(TAG, "CallUrlFromPaginatedResponseForSharePhoto - start");
		setApiProcessingDialog(true);
		try {
			FGC_FLA_Handler.getInstance().CallUrlFromPaginatedResponse(
					this,
					ApiBaseActivity.class.getMethod("CallUrlFromPaginatedResponseForSharePhotoSuccess",
							getSharedPhotos_outObj.class),
					ApiBaseActivity.class.getMethod("CallUrlFromPaginatedResponseForSharePhotoFailure", String.class),
					url);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOG.V(TAG, "CallUrlFromPaginatedResponseForSharePhoto - end");

	}

	public void CallUrlFromPaginatedResponseForSharePhotoSuccess(getSharedPhotos_outObj data) {
		LOG.V(TAG, "CallUrlFromPaginatedResponseForSharePhotoSuccess");

		onCallUrlFromPaginatedResponseForSharePhoto.raise(true, data);

	}

	public void CallUrlFromPaginatedResponseForSharePhotoFailure(String data) {
		LOG.V(TAG, "CallUrlFromPaginatedResponseForSharePhotoFailure "
				+ FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onCallUrlFromPaginatedResponseForSharePhoto.raise(false, data);
	}

	public void GetReceivedPhoto(String UserId, ArrayList<String> fields, long since, long until, long limit) {
		LOG.V(TAG, "GetReceivedPhoto - start");
		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().GetReceivedPhotos(this,
					ApiBaseActivity.class.getMethod("GetReceivedPhotoSuccess", getReceivedPhotos_outObj.class),
					ApiBaseActivity.class.getMethod("GetReceivedPhotoFailure", String.class),
					new getReceivedPhotos_inObj(UserId, fields, since, until, limit));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "GetReceivedPhoto - end");
	}

	public void GetReceivedPhotoSuccess(getReceivedPhotos_outObj data) {
		LOG.V(TAG, "GetReceivedPhotoSuccess  " + data);
		onGetReceivedPhoto.raise(true, data);
	}

	public void GetReceivedPhotoFailure(String data) {
		LOG.V(TAG, "GetReceivedPhotoFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetReceivedPhoto.raise(false, data);
	}

	public void CallUrlFromPaginatedResponseForReceivedPhoto(String url) {

		LOG.V(TAG, "CallUrlFromPaginatedResponseForReceivedPhoto - start");
		setApiProcessingDialog(true);
		try {
			FGC_FLA_Handler.getInstance().CallUrlFromPaginatedResponse(
					this,
					ApiBaseActivity.class.getMethod("CallUrlFromPaginatedResponseForReceivedPhotoSuccess",
							getReceivedPhotos_outObj.class),
					ApiBaseActivity.class
							.getMethod("CallUrlFromPaginatedResponseForReceivedPhotoFailure", String.class), url);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOG.V(TAG, "CallUrlFromPaginatedResponseForReceivedPhoto - end");
	}

	public void CallUrlFromPaginatedResponseForReceivedPhotoSuccess(getReceivedPhotos_outObj data) {
		LOG.V(TAG, "CallUrlFromPaginatedResponseForReceivedPhotoSuccess");
		onCallUrlFromPaginatedResponseForReceivedPhoto.raise(true, data);

	}

	public void CallUrlFromPaginatedResponseForReceivedPhotoFailure(String data) {
		LOG.V(TAG, "CallUrlFromPaginatedResponseForReceivedPhotoFailure "
				+ FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onCallUrlFromPaginatedResponseForReceivedPhoto.raise(false, data);
	}

	public void GetSharedPhoto(String UserId, ArrayList<String> fields, long since, long until, long limit) {
		LOG.V(TAG, "GetSharedPhoto - start");
		try {
			setApiProcessingDialog(false);
			FGC_FLA_Handler.getInstance().GetSharedPhotos(this,
					ApiBaseActivity.class.getMethod("GetSharedPhotoSuccess", getSharedPhotos_outObj.class),
					ApiBaseActivity.class.getMethod("GetSharedPhotoFailure", String.class),
					new getSharedPhotos_inObj(UserId, fields, since, until, limit));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "GetSharedPhoto - end");
	}

	public void GetSharedPhotoSuccess(getSharedPhotos_outObj data) {
		LOG.V(TAG, "GetSharedPhotoSuccess  " + data);
		onGetSharedPhoto.raise(true, data);
	}

	public void GetSharedPhotoFailure(String data) {
		LOG.V(TAG, "GetSharedPhotoFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetSharedPhoto.raise(false, data);
	}

	// public void getAllPhoto(long since, long until, long limit) {
	// getAllPhoto(since, until, limit, true);
	// }

	/** for NSA use only */
	public void getAllPhoto(String userKey, long since, long until, long limit, boolean showDialog) {
		LOG.V(TAG, "getAllPhoto - start");
		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetSharedAndReceivedPhotos(this,
					ApiBaseActivity.class.getMethod("getAllPhotoSuccess", getSharedAndReceivedPhotos_outObj.class),
					ApiBaseActivity.class.getMethod("getAllPhotoFailure", String.class),
					new getAllPhotos_inObj(userKey, NSAPhotoUtil.getAllPhotoQueryField(), since, until, limit));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "getAllPhoto - end");
	}

	public void getAllPhotoSuccess(getSharedAndReceivedPhotos_outObj data) {
		LOG.V(TAG, "getAllPhotoSuccess  " + data);
		onGetAllPhoto.raise(true, data);
	}

	public void getAllPhotoFailure(String data) {
		LOG.V(TAG, "getAllPhotoFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetAllPhoto.raise(false, data);
	}

	// ================= add photo api function by Sid==============
	/**
	 * get unread photo count.
	 * 
	 * @param userId
	 * 
	 * */
	public void getUnreadPhotoCount(String userId) {
		getUnreadPhotoCount(userId, true);
	}

	public void getUnreadPhotoCount(String userId, boolean showDialog) {
		LOG.V(TAG, "GetUnreadPhotoCount - start");

		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().GetUnreadPhotoCount(this,
					ApiBaseActivity.class.getMethod("getUnreadPhotoCountSuccess", getUnreadPhotoCount_outObj.class),
					ApiBaseActivity.class.getMethod("getUnreadPhotoCountFailure", String.class), userId);
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}

		LOG.V(TAG, "GetUnreadPhotoCount - end");
	}

	public void getUnreadPhotoCountSuccess(getUnreadPhotoCount_outObj data) {
		LOG.V(TAG, "GetUnreadPhotoCountSuccess - " + data.getUnreadCount());
		onGetUnreadPhotoCount.raise(true, data);
	}

	public void getUnreadPhotoCountFailure(String data) {
		LOG.V(TAG, "GetUnreadPhotoCountFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onGetUnreadPhotoCount.raise(false, data);
	}

	/**
	 * This is used to unshare a photo
	 * 
	 * @param UserId
	 * @param targetphotoId
	 */
	public void DeletePhoto(String UserId, String targetphotoId) {
		LOG.V(TAG, "RemovePhotoSharing - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().RemovePhotoSharing(this,
					ApiBaseActivity.class.getMethod("DeletePhotoSuccess", String.class),
					ApiBaseActivity.class.getMethod("DeletePhotoFailure", String.class),
					new removePhotoSharing_inObj(UserId, targetphotoId));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "RemovePhotoSharing - end");
	}

	/**
	 * new nsa delete photo api
	 * 
	 * @param userKey
	 * @param photoId
	 */
	public void NSADeletePhoto(String userKey, String photoId) {
		LOG.V(TAG, "NSADeletePhoto - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().NSADeletePhoto(this,
					ApiBaseActivity.class.getMethod("DeletePhotoSuccess", String.class),
					ApiBaseActivity.class.getMethod("DeletePhotoFailure", String.class), userKey, photoId);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "NSADeletePhoto - end");
	}

	/**
	 * new nsa delete received photo api
	 * 
	 * @param userKey
	 * @param photoId
	 */
	public void NSADeleteReceivedPhoto(String userKey, String photoId) {
		LOG.V(TAG, "NSADeleteReceivedPhoto - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().NSADeleteReceivedPhoto(this,
					ApiBaseActivity.class.getMethod("DeletePhotoSuccess", String.class),
					ApiBaseActivity.class.getMethod("DeletePhotoFailure", String.class), userKey, photoId);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "NSADeleteReceivedPhoto - end");
	}

	public void DeletePhotoSuccess(String data) {
		LOG.V(TAG, "RemovePhotoSharingSuccess  " + data);
		onDeletePhoto.raise(true, data);
		// onGetMailContent.raise(true, data);

	}

	public void DeletePhotoFailure(String data) {
		LOG.V(TAG, "RemovePhotoSharingFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		// onGetMailContent.raise(false, data);
		onDeletePhoto.raise(false, data);
	}

	/**
	 * This is used to delete a photo we sent out, currently only used in NSA
	 */
	public void DeleteOwnPhoto(String photoId) {
		LOG.V(TAG, "DeleteOwnPhoto - start");
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().DeletePhoto(this,
					ApiBaseActivity.class.getMethod("DeleteOwnPhotoSuccess", String.class),
					ApiBaseActivity.class.getMethod("DeleteOwnPhotoFailed", String.class),
					new deletePhoto_inObj(photoId));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		LOG.V(TAG, "DeleteOwnPhoto - end");
	}

	public void DeleteOwnPhotoSuccess(String data) {
		LOG.V(TAG, "DeleteOwnPhotoSuccess  " + data);
		onDeletePhoto.raise(true, data);
	}

	public void DeleteOwnPhotoFailed(String data) {
		LOG.V(TAG, "DeleteOwnPhotoFailed - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
		onDeletePhoto.raise(false, data);
	}

	public void callDialogForAskCreateUserName(final boolean isMommyMode) {
		// add dialog here if user choose 'create' do follow step as below if
		// not, do finish().
		m_CreateUserNameDialog = new CreateUserNameDialog(this);
		// block back button when dialog pop up
		m_CreateUserNameDialog.setCancelable(false);
		m_CreateUserNameDialog.addButtonListener(new IButtonClickListener() {
			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				// ChooseContactDialog @ erase's folder
				if (buttonId == CreateUserNameDialog.CREATE_BUTTON_ID) {
					// go to nabi friend
					Intent i = new Intent("com.fuhu.nabiconnect.friend.NABIFRIEND");
					i.putExtra(KEY_IS_MOMMY_MODE, isMommyMode);
					startActivity(i);
				} else if (buttonId == CreateUserNameDialog.CLOSE_BUTTON_ID) {
				}
				m_CreateUserNameDialog.dismiss();
				finish();
			}
		});
		m_CreateUserNameDialog.show();
	}

	public void sendingAnimationDialog() {
		m_PhotoSendingAnimationDialog = new PhotoSendingAnimationDialog(this);
		m_PhotoSendingAnimationDialog.setCancelable(false);
		m_PhotoSendingAnimationDialog.show();
	}

	private void setApiProcessingDialog(boolean enable) {
		FGC_FLA_Handler.getInstance().SetDefaultSpinnerActive(enable);
	}

	public void showGeneralWarningDialog() {
		Intent intent = new Intent(IntentList.INTENT_NABI_GENERAL_ALERT);
		intent.putExtra("style", "GeneralAttentionDialog");
		intent.putExtra("fullscreen", true);
		intent.putExtra("dialog_title", getResources().getString(R.string.general_string_attention));
		intent.putExtra("dialog_content", getResources().getString(R.string.general_string_warning));
		startActivity(intent);
	}

	public void performCoppaProcess() {
		LOG.V(TAG, "performCoppaProcess() - start");
		Intent intent = new Intent(IntentList.INTENT_CHECK_PRIVACY_POLICY);
		intent.putExtra("style", "SignInDialog");
		this.startActivity(intent);
		finish();
		LOG.V(TAG, "performCoppaProcess() - end");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == NetworkManager.WIFI_DIALOG_REQUEST_CODE) {
			LOG.V(TAG, "onActivityResult() - WIFI_DIALOG_REQUEST_CODE");
			switch (resultCode) {
			case RESULT_OK:
				LOG.V(TAG, "Result OK");
				break;
			case RESULT_CANCELED:
				LOG.V(TAG, "Result cancel");
				finish();
				break;
			}
		} else if (requestCode == ASK_MOM_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				performCoppaProcess();
			} else {
				LOG.V(TAG, "onActivityResult() - result is not OK from ask mom dialog");
				finish();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// always enable the notification dialog funciton when leaving
		if (m_NabiNotificationManager != null)
			m_NabiNotificationManager.setNotificaionDialogStatus(true);
	}

	/** new flow for parent login in NSA */
	public void parentLogin() {
		try {
			setApiProcessingDialog(true);
			FGC_FLA_Handler.getInstance().LoginAccount(
					this,
					ApiBaseActivity.class.getMethod("onParentLogin", UserData.class),
					ApiBaseActivity.class.getMethod("onParentLoginFail", String.class),
					ApiBaseActivity.class.getMethod("onParentAccountNotFound", String.class),
					new friendsLoginUser_inObj(m_OsgToken.getOSGUserKey(), m_OsgToken.getOSGSessionKey(), m_OsgToken
							.getOSGAuthKey()));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Throwable tr) {
			tr.printStackTrace();
		}
	}

	public void onParentLogin(final UserData data) {
		// mNSAUserData = data;
		if (data != null) {
			// update database
			getDatabaseAdapter().updateUserData(data);
			// store cache data
			String cacheString = data.userKey + ":" + data.kidID;
			m_Preference.edit().putString(data.friendCode, cacheString).commit();

			m_NabiNotificationManager.register(data.friendCode, new NabiNotificationManager.IRegisterCallback() {

				@Override
				public void onRegistered() {
					LOG.V(TAG, "notification manager registered for: " + data.userName);
				}
			});
		}
	}

	public void onParentLoginFail(String data) {
		LOG.V(TAG, "loginUserFailure - " + data);
		// mNSAUserData = null;
		LOG.V(TAG, "loginUserFailure - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
	}

	public void onParentAccountNotFound(String data) {
		LOG.V(TAG, "accountNeedsCreate - " + data);
		// mNSAUserData = null;
		LOG.V(TAG, "accountNeedsCreate - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
	}

	/** new flow for kid login in NSA */
	public void childLogin(long kidId) {
		childLogin(kidId, true);
	}

	public void childLogin(long kidId, boolean showDialog) {
		try {
			setApiProcessingDialog(showDialog);
			FGC_FLA_Handler.getInstance().LoginAccount(
					this,
					ApiBaseActivity.class.getMethod("onChildLogin", UserData.class),
					ApiBaseActivity.class.getMethod("onChildLoginFail", String.class),
					ApiBaseActivity.class.getMethod("onChildAccountNotFound", String.class),
					new friendsLoginUser_inObj(m_OsgToken.getOSGUserKey(), m_OsgToken.getOSGSessionKey(), m_OsgToken
							.getOSGAuthKey(), String.valueOf(kidId)));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Throwable tr) {
			tr.printStackTrace();
		}
		LOG.V(TAG, "loginAccount - end");
	}

	public void onChildLogin(UserData data) {
		mNSAUserData = data;
		if (data != null) {
			// update database
			getDatabaseAdapter().updateUserData(data);
			// store cache data
			String cacheString = data.userKey + ":" + data.kidID;
			m_Preference.edit().putString(data.friendCode, cacheString).commit();
		}
	}

	public void onChildLoginFail(String data) {
		LOG.V(TAG, "onChildLoginFail - " + data);
		mNSAUserData = null;
		LOG.V(TAG, "onChildLoginFail - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
	}

	public void onChildAccountNotFound(String data) {
		LOG.V(TAG, "onChildAccountNotFound - " + data);
		mNSAUserData = null;
		LOG.V(TAG, "onChildAccountNotFound - " + FGC_FLA_Handler.getInstance().GetErrorCodeFormattedForLog(data));
	}
}
