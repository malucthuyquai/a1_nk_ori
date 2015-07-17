package com.fuhu.nabiconnect.chat;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;

import com.fuhu.data.FriendData;
import com.fuhu.data.UserData;
import com.fuhu.data.conversationData;
import com.fuhu.data.messageData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.IOnMainBarItemSelectedListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.chat.fragment.ChatFragment;
import com.fuhu.nabiconnect.chat.fragment.ChatMainBarFragment;
import com.fuhu.nabiconnect.chat.fragment.ContactsFragment;
import com.fuhu.nabiconnect.chat.fragment.ContactsFragment.ContactProperty;
import com.fuhu.nabiconnect.chat.fragment.ShopFragment;
import com.fuhu.nabiconnect.chat.widget.ContactWidget;
import com.fuhu.nabiconnect.chat.widget.ContactWidget.IOnContactClickedListener;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.friend.dialog.AddFriendDialog;
import com.fuhu.nabiconnect.friend.dialog.BlockedDialog;
import com.fuhu.nabiconnect.friend.dialog.FriendRequestSent;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.MailActivity.ReplyReceiverData;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.ndnslibsoutstructs.chatHistory_outObj;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMSenderEventCallback;
import com.fuhu.tracking.TrackingFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatActivity extends ApiBaseActivity implements IOnMainBarItemSelectedListener, IOnContactClickedListener {

	public static final String TAG = "nabiChatMainActivity";

	public static final String SHOP_ITEM_PREF = "SHOP_ITEM_PURCHASED_RECORD";
	public static final String LAST_CONVERSATION_PREF = "LAST_CONVERSATION_RECORD";
	public static final String CHAT_TIME_STAMP_FAKE_MESSAGE = "!+@_forTimeStamp%^(*";
	public static final int CHAT_HISTORY_PRELOAD_COUNT = 20;
	public static final int CHAT_HISTORY_COUNT_LIMIT = 500;

	private ChatMainBarFragment m_MainBarFrag;
	private IOnChatMessageReceivedListener m_CurrentChatListener;
	private ContactsFragment m_ContactFrag;
	private ChatFragment m_ChatFrag;
	private ShopFragment m_ShopFrag;
	private int m_CurrentMainBarItem;
	private boolean m_IsMommyMode;
	private AddFriendDialog m_AddFriendDialog;
	private FriendRequestSent m_FriendRequestSentDialog;
	private conversationData m_CurrentConversationData;
	private ContactProperty m_CurrenctContactWidget;
	private String m_InputFriendCode;
	private String m_LogonUserKey;

	private ArrayList<ChatMessageData> m_ChatHistoryMessages = new ArrayList<ChatMessageData>();
	private ArrayList<ChatMessageData> m_ChatTotalMessages = new ArrayList<ChatMessageData>();

	private boolean mNeedRelogin = false;

	public static class ChatMessageData {
		public messageData messageData;
		public boolean sendFailed = false;

		public ChatMessageData(messageData data) {
			this.messageData = data;
		}

		public messageData getMessageData() {
			return messageData;
		}

		public void setMessageData(messageData messageData) {
			this.messageData = messageData;
		}

		public boolean isSendFailed() {
			return sendFailed;
		}

		public void setSendFailed(boolean sendFailed) {
			this.sendFailed = sendFailed;
		}

	}

	// private MessageDatabaseManager m_DatabaseManager;
	// private String m_CurrentContactId = null;
	// private ArrayList<String> m_CurrentChatActors;
	private FriendData m_CurrentChatFriend;
	// private long m_CurrentLastTimeStamp;

	private SharedPreferences m_ShopPreference;

    public ChatActivity() {
        super("nabiChat");
    }


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_activity_main);

		// get intent extra
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			m_IsMommyMode = extras.getBoolean(KEY_IS_MOMMY_MODE, false);
			m_LogonUserKey = extras.getString(KEY_LOGON_USER_KEY);
		}

		// create fragments
		m_MainBarFrag = new ChatMainBarFragment();
		m_ContactFrag = new ContactsFragment();
		m_ChatFrag = new ChatFragment();
		m_ShopFrag = new ShopFragment();

		// check login function
		if (m_IsMommyMode) {
			if (m_LogonUserKey != null) {
				LOG.V(TAG, "onCreate() - there is logon userkey. Get data from cache.");
				UserData logonUser = getDatabaseAdapter().getUserData(m_LogonUserKey);

				if (logonUser != null) {
					LOG.V(TAG, "onCreate() - use the cache as the logon data.");
					loginUserSuccess(logonUser);
				} else {
					LOG.V(TAG, "onCreate() - there is no cache in database.");
					loginAccountNoKid();
				}
			} else {
				LOG.V(TAG, "onCreate() - there is no logon userkey in extra.");
				loginAccountNoKid();
			}
		} else {
			loginAccount();
		}

		m_CurrentChatListener = (IOnChatMessageReceivedListener) m_ContactFrag;

		// create shop item preference
		m_ShopPreference = this.getSharedPreferences("SHOP_ITEM_PREF", 0);
		SharedPreferences.Editor editor = m_ShopPreference.edit();
		editor.putBoolean("G", true);
		editor.putBoolean("C", true);
		editor.putBoolean("E", true);
		editor.apply();

		m_CurrentMainBarItem = ChatMainBarFragment.ITEM_CONTACT_ID;
	}

	@Override
	protected void onResume() {
		LOG.V(TAG, "onResume() - start");
		super.onResume();
		// m_DatabaseManager = new MessageDatabaseManager(this);
		// m_CurrentContactId = getCurrentContaciId();
		LOG.V(TAG, "onResume() - end");
	}

	@Override
	public void OnMainBarItemSelected(int item) {
		OnMainBarItemSelected(item, null);
	}

	@Override
	public void OnMainBarItemSelected(int item, ReplyReceiverData data) {
		LOG.V(TAG, "OnMainBarItemSelected() - item is " + item);

		if (m_CurrentMainBarItem == item) {
			LOG.V(TAG, "OnMainBarItemSelected() - switch to same item");
			return;
		}

		m_CurrentMainBarItem = item;

		// notify listener
		m_MainBarFrag.OnMainBarIndexChanged(item);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		TrackingFragment newFragment = null;

		switch (item) {
		case ChatMainBarFragment.ITEM_CONTACT_ID:
			newFragment = m_ContactFrag;
			break;
		case ChatMainBarFragment.ITEM_CHAT_ID:
			newFragment = m_ChatFrag;
			break;
		case ChatMainBarFragment.ITEM_SHOP_ID:
			newFragment = m_ShopFrag;
			break;
		}

		m_CurrentChatListener = (IOnChatMessageReceivedListener) newFragment;

		// Replace whatever is in the fragment_container view with this
		// fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.right_fragment_container, newFragment);
		// transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();

	}

	public int getCurrentMainBarItem() {
		return this.m_CurrentMainBarItem;
	}

	@Override
	protected void onPause() {

		LOG.V(TAG, "onPause() - start");

		super.onPause();

		// dismiss dialog
		closeDialog(m_AddFriendDialog);

		// if(m_DatabaseManager != null)
		// m_DatabaseManager.disconnect();

		LOG.V(TAG, "onPause() - end");
		mNeedRelogin = true;
	}

	public boolean getNeedRelogin() {
		return mNeedRelogin;
	}

	public void setNeedRelogin(boolean needsRelogin) {
		mNeedRelogin = needsRelogin;
	}

	public void onChatMessageReceived(Message msg) {
		/*
		 * String sender = XmppManager.getFriendCodeFromMessage(msg); //long
		 * kidId = m_KidAccountManager.getCurrentKid().getKidId(); //String
		 * receiver = String.valueOf(kidId); String receiver = getFriendCode();
		 * String content = msg.getBody(); // Write into database
		 * m_DatabaseManager.addMessage(sender, receiver, content,
		 * MessageBean.UNREAD, receiver+"/"+sender);
		 * 
		 * //m_DatabaseManager.printKidInformation(getFriendCode());
		 */
		// notify current fragment
		m_CurrentChatListener.OnChatMessageReceived();
	}

	@Override
	protected void onDestroy() {

		LOG.V(TAG, "onDestroy() - start");
		super.onDestroy();

		LOG.V(TAG, "onDestroy() - end");
	}

	/*
	 * public MessageDatabaseManager getDatabaseManager() { return
	 * this.m_DatabaseManager; }
	 */
	public SharedPreferences getShopPreference() {
		return this.m_ShopPreference;
	}

	public FriendData getCurrentChatFriend() {
		return this.m_CurrentChatFriend;
	}

	public void setCurrentChatFriend(FriendData friend) {
		this.m_CurrentChatFriend = friend;
	}

	// public long getCurrentTimeStamp()
	// {
	// return this.m_CurrentLastTimeStamp;
	// }
	//
	// public void setCurrentTimeStamp(long timeStamp)
	// {
	// this.m_CurrentLastTimeStamp = timeStamp;
	// }

	public boolean isParentMode() {
		return this.m_IsMommyMode;
	}

	/*
	 * @Override public boolean handleMessage(android.os.Message msg) {
	 * super.handleMessage(msg);
	 * LOG.V(TAG,"nabiChatMainActivity receive msg : "+msg.what); Object[] objs;
	 * switch (msg.what) {
	 * 
	 * case MSG_MESSAGE_RECEIVED: objs = (Object[])msg.obj; Message chatMsg =
	 * (Message) objs[0]; onChatMessageReceived(chatMsg); break; }
	 * 
	 * 
	 * return false; }
	 */

	@Override
	public void OnContactClicked(FriendData friendData, conversationData conversationData) {
		if (friendData.userID.equals(ContactWidget.ADD_FRIEND_ID)) {
			LOG.V(TAG, "OnContactClicked() -  Add friend widget");
			if (m_AddFriendDialog != null && m_AddFriendDialog.isShowing()) {
				LOG.V(TAG, "m_AddFriendDialog is already shown");
				return;
			}
			m_AddFriendDialog = new AddFriendDialog(this);
			m_AddFriendDialog.addButtonListener(new IButtonClickListener() {
				@Override
				public void onButtonClicked(int buttonId, String viewName, Object[] args) {
					switch (buttonId) {
					case AddFriendDialog.YES_BUTTON_ID:
						String inputFriendCode = (String) args[0];
						m_InputFriendCode = inputFriendCode;
						if (m_AddFriendDialog.isForAccept()) {
						} else {
							makeFriend(getCurrentUserData().userKey, inputFriendCode);
						}
						break;
					case AddFriendDialog.CANCEL_BUTTON_ID:
						closeDialog(m_AddFriendDialog);
						break;
					}
				}
			});
			m_AddFriendDialog.show();
			return;
		}

		// check WIFI & show dialog
		if (!m_NetworkManager.checkWifiProcess()) {
			return;
		}

		m_CurrentConversationData = conversationData;
		m_ChatHistoryMessages.clear();
		m_ChatTotalMessages.clear();

		m_CurrentChatFriend = friendData;
		m_ChatFrag.setChatFriend(friendData);
		if (m_CurrentConversationData != null) {
			getChatHistory(getCurrentUserData().userKey, m_CurrentConversationData.m_ConversationId,
					CHAT_HISTORY_COUNT_LIMIT, 0, m_CurrentConversationData.m_LastReadTimestamp + 1);
		} else {
			ArrayList<String> actorList = new ArrayList<String>();
			actorList.add(m_CurrentChatFriend.userID);
			actorList.add(getCurrentUserData().userKey);
			createGetConversation(actorList);
		}
	}

	private void closeDialog(PopupDialog dialog) {

		if (dialog == null) {
			LOG.W(TAG, "closeDialog() - dialog is null.");
			return;
		}

		if (!dialog.isShowing()) {
			LOG.V(TAG, "dialog is hidden.");
			return;
		} else
			dialog.dismiss();
	}

	@Override
	public void onBackPressed() {
		if (m_ChatFrag != null && m_ChatFrag.isVisible()) {
			if (m_ChatFrag.onBackPressed()) {
				// handled by fragment
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	public void loginUserSuccess(UserData data) {
		super.loginUserSuccess(data);
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (!m_MainBarFrag.isVisible()) {
                ft.replace(R.id.left_fragment_container, m_MainBarFrag, "leftfragment");
            }
            if (!m_ContactFrag.isVisible()) {
                if (!m_ChatFrag.isVisible()) {
                    // if currently in chat room, stay
                    // otherwise, show contact fragment
                    ft.replace(R.id.right_fragment_container, m_ContactFrag, "rightfragment");
                    // ft.addToBackStack(null);
                }
            }
            //ft.commit();
            ft.commitAllowingStateLoss();
        }catch (Exception e){
            e.printStackTrace();
            this.showGeneralWarningDialog();
            finish();
        }
	}

	@Override
	public void loginUserFailure(String data) {
		super.loginUserFailure(data);

		this.showGeneralWarningDialog();
		finish();

	}

	@Override
	public void accountNeedsCreate(String data) {
		super.accountNeedsCreate(data);
		callDialogForAskCreateUserName(m_IsMommyMode);
	}

	@Override
	public void makeFriendSuccess(String data) {
		super.makeFriendSuccess(data);

		// dismiss dialog
		closeDialog(m_AddFriendDialog);

		// notify GCM server
		NabiNotificationManager notificationManager = getNabiNotificationManager();
		notificationManager.notifyServerByFriendCode(m_InputFriendCode, getCurrentUserData().userName,
				getString(R.string.notification_friend_description), NabiNotificationManager.APPLICATION_NAME_FRIEND,
				new GCMSenderEventCallback() {

					@Override
					public void onSendMessageSuccess() {
					}

					@Override
					public void onMessgaeSendingError(int errorCode) {
					}
				});

		m_FriendRequestSentDialog = new FriendRequestSent(this);
		m_FriendRequestSentDialog.setCancelable(false);
		m_FriendRequestSentDialog.addButtonListener(new IButtonClickListener() {
			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				m_FriendRequestSentDialog.dismiss();
			}
		});
		m_FriendRequestSentDialog.show();
	}

	@Override
	public void makeFriendFailure(String data) {
		super.makeFriendFailure(data);
		try {
			JSONObject jobj = new JSONObject(data);
			if (jobj.getString("status").equals("8085")) {
				// on block list
				closeDialog(m_AddFriendDialog);
				new BlockedDialog(ChatActivity.this).show();
			} else if (jobj.getString("status").equals("8043")) {
				// already sent
				m_AddFriendDialog.setRequestAlreadySent(true);
			} else {
				LOG.V(TAG, "m_AcceptFriendRequestEventListener - failed to add friend");
				m_AddFriendDialog.setIsInvalid(true);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			LOG.V(TAG, "makeFriendFailure - failed to add friend");
			m_AddFriendDialog.setIsInvalid(true);
		}
	}

	@Override
	public void createGetConversationSuccess(String data) {
		super.createGetConversationSuccess(data);

		m_CurrentConversationData = new conversationData();
		m_CurrentConversationData.m_ConversationId = data;

		OnMainBarItemSelected(ChatMainBarFragment.ITEM_CHAT_ID);
	}

	@Override
	public void createGetConversationFailure(String data) {
		showGeneralWarningDialog();
	}

	@Override
	public void getChatHistorySuccess(chatHistory_outObj data) {
		super.getChatHistorySuccess(data);

		LOG.V(TAG, "getChatHistorySuccess() - start");

		ArrayList<messageData> messageData = data.getMessages();
		for (int i = 0; i < messageData.size(); i++) {
			m_ChatHistoryMessages.add(new ChatMessageData(messageData.get(i)));
			// LOG.V(TAG,
			// "messageData["+i+"] : "+messageData.get(i).m_MessageTime);
			/*
			 * 
			 * [0] : newest ex. 1000 [1] : 997 [2] : 888 ...
			 */

		}
		if (messageData.size() > 0) {
			// add time stamp data
			messageData timeData = new messageData();
			timeData.m_MessageContent = CHAT_TIME_STAMP_FAKE_MESSAGE;
			timeData.m_MessageTime = messageData.get(0).m_MessageTime;
			m_ChatHistoryMessages.add(0, new ChatMessageData(timeData));
		}

		// preload 20 history into total message list
		if (m_ChatHistoryMessages.size() >= CHAT_HISTORY_PRELOAD_COUNT) {
			for (int i = 0; i < CHAT_HISTORY_PRELOAD_COUNT; i++) {
				m_ChatTotalMessages.add(0, m_ChatHistoryMessages.get(i));
			}

			// remove loaded messages in history
			m_ChatHistoryMessages.subList(0, CHAT_HISTORY_PRELOAD_COUNT).clear();
		} else {
			for (int i = 0; i < m_ChatHistoryMessages.size(); i++) {
				m_ChatTotalMessages.add(0, m_ChatHistoryMessages.get(i));
				// LOG.V(TAG,
				// "messageData["+i+"] : "+messageData.get(i).m_MessageTime);
				/*
				 * 
				 * [0] : newest ex. 1000 [1] : 997 [2] : 888 ...
				 */

			}
			m_ChatHistoryMessages.clear();
		}
		OnMainBarItemSelected(ChatMainBarFragment.ITEM_CHAT_ID);
	}

	@Override
	public void getChatHistoryFailure(String data) {
		super.getChatHistoryFailure(data);
		showGeneralWarningDialog();
	}

	public boolean loadAllCHatHistory() {
		if (m_ChatHistoryMessages == null || m_ChatHistoryMessages.size() == 0) {
			return false;
		}

		for (int i = 0; i < m_ChatHistoryMessages.size(); i++) {
			m_ChatTotalMessages.add(0, m_ChatHistoryMessages.get(i));
		}
		m_ChatHistoryMessages.clear();

		return true;
	}

	public ArrayList<ChatMessageData> getChatTotalMessage() {
		return this.m_ChatTotalMessages;
	}

	public ArrayList<ChatMessageData> getChatHistoryMessage() {
		return this.m_ChatHistoryMessages;
	}

	public conversationData getCurrenctConversationData() {
		return this.m_CurrentConversationData;
	}

	public void setCurrenctConversationData(conversationData data) {
		this.m_CurrentConversationData = data;
	}

	public ContactProperty getCurrenctContactWidget() {
		return this.m_CurrenctContactWidget;
	}

	public void setCurrenctContactWidget(ContactProperty widget) {
		this.m_CurrenctContactWidget = widget;
	}
}