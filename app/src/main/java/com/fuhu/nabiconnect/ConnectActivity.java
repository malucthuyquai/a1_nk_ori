package com.fuhu.nabiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.fuhu.data.InboxesData;
import com.fuhu.data.UserData;
import com.fuhu.data.conversationData;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.utils.LibraryUtils;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.nabiconnect.widget.ConnectItemWidget;
import com.fuhu.ndnslibs.FGC_FLA_Handler;
import com.fuhu.ndnslibsoutstructs.chatPollMessage_outObj;
import com.fuhu.ndnslibsoutstructs.getInboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getUnreadPhotoCount_outObj;
import com.fuhu.ndnslibsoutstructs.pendingFriendRequests_outObj;

public class ConnectActivity extends ApiBaseActivity {

	public static final String TAG = "ConnectActivity";

	private ConnectItemWidget m_ChatItem;
	private ConnectItemWidget m_FriendItem;
	private ConnectItemWidget m_PhotoItem;
	private ConnectItemWidget m_MailItem;

	private TextView m_FriendCodeText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_activity_main);

		m_FriendCodeText = (TextView) findViewById(R.id.friend_code);

		m_ChatItem = (ConnectItemWidget) this.findViewById(R.id.connect_chat_item);
		m_FriendItem = (ConnectItemWidget) this.findViewById(R.id.connect_friend_item);
		m_PhotoItem = (ConnectItemWidget) this.findViewById(R.id.connect_photo_item);
		m_MailItem = (ConnectItemWidget) this.findViewById(R.id.connect_mail_item);

		m_ChatItem.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				Intent i = new Intent(INTENT_CHAT_ACTIVITY);
				i.putExtra(KEY_IS_MOMMY_MODE, true);
				if (getCurrentUserData() != null)
					i.putExtra(KEY_LOGON_USER_KEY, getCurrentUserData().userKey);
				startActivity(i);
			}
		});

		m_FriendItem.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				Intent i = new Intent(INTENT_FRIEND_ACTIVITY);
				i.putExtra(KEY_IS_MOMMY_MODE, true);
				if (getCurrentUserData() != null)
					i.putExtra(KEY_LOGON_USER_KEY, getCurrentUserData().userKey);
				startActivity(i);
			}
		});

		m_PhotoItem.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				Intent i = new Intent(INTENT_PHOTO_ACTIVITY);
				i.putExtra(KEY_IS_MOMMY_MODE, true);
				if (getCurrentUserData() != null)
					i.putExtra(KEY_LOGON_USER_KEY, getCurrentUserData().userKey);
				startActivity(i);
			}
		});

		m_MailItem.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				Intent i = new Intent(INTENT_MAIL_ACTIVITY);
				i.putExtra(KEY_IS_MOMMY_MODE, true);
				if (getCurrentUserData() != null)
					i.putExtra(KEY_LOGON_USER_KEY, getCurrentUserData().userKey);
				startActivity(i);
			}
		});

		// set information
		m_ChatItem.setInfomation(R.drawable.connect_imgv_chat, this.getString(R.string.connect_btn_chat),
				R.drawable.connect_icon_chat);
		m_FriendItem.setInfomation(R.drawable.connect_imgv_friend, this.getString(R.string.connect_btn_friend),
				R.drawable.connect_icon_friend);
		m_PhotoItem.setInfomation(R.drawable.connect_imgv_photo, this.getString(R.string.connect_btn_photo),
				R.drawable.connect_icon_photo);
		m_MailItem.setInfomation(R.drawable.connect_imgv_mail, this.getString(R.string.connect_btn_mail),
				R.drawable.connect_icon_mail);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// reset the API key
		if (Utils.isProductionVersion(this)) {
			FGC_FLA_Handler.getInstance().Initialize(this, LibraryUtils.PRODUCTION_NSA_API_KEY,
					LibraryUtils.PRODUCTION_BASE_URL, LibraryUtils.USE_DEFAULT_SPINNER);
		} else {
			FGC_FLA_Handler.getInstance().Initialize(this, LibraryUtils.STAGING_NSA_API_KEY,
					LibraryUtils.STAGING_BASE_URL, LibraryUtils.USE_DEFAULT_SPINNER);
		}

		if (!isFinishing()) {
			loginAccountNoKid();
		}
	}

	@Override
	public void loginUserSuccess(UserData data) {
		super.loginUserSuccess(data);

		m_FriendCodeText.setText(data.friendCode);

		getChatPollMessage(data.userKey, false);
		getInboxes(data.userKey, false);
		getUnreadPhotoCount(data.userKey, false);
		getPendingFriendList(data.userKey, false);
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
		callDialogForAskCreateUserName(true);
	}

	@Override
	public void getChatPollMessageSuccess(chatPollMessage_outObj data) {

		super.getChatPollMessageSuccess(data);

		int unreadChatCount = 0;
		for (conversationData cd : data.getConversations()) {
			unreadChatCount += cd.m_UnreadMessageCount;
		}
		m_ChatItem.setUnreadCount(unreadChatCount);
	}

	@Override
	public void getInboxesSuccess(getInboxes_outObj data) {

		super.getInboxesSuccess(data);

		int unreadMailCount = 0;
		for (InboxesData id : data.getInboxes()) {
			unreadMailCount += id.newReceiveCount;
		}
		m_MailItem.setUnreadCount(unreadMailCount);
	}

	@Override
	public void getUnreadPhotoCountSuccess(getUnreadPhotoCount_outObj data) {
		super.getUnreadPhotoCountSuccess(data);
		m_PhotoItem.setUnreadCount(data.getUnreadCount() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) data
				.getUnreadCount());
	}

	@Override
	public void getPendingFriendListSuccess(pendingFriendRequests_outObj data) {

		super.getPendingFriendListSuccess(data);
		m_FriendItem.setUnreadCount(data.getPendingFriendRequests().size());
	}
}
