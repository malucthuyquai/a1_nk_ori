package com.fuhu.nabiconnect.nsa.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Handler;

import com.fuhu.account.data.Kid;
import com.fuhu.data.FriendData;
import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;

import java.util.ArrayList;

public class FragmentNSA extends Fragment {

	final private String TAG = FragmentNSA.class.getSimpleName();

	/** CHAT HISTORY KEY */
	final public static String KEY_CONVERSATION_ID = "conversationId";
	final public static String KEY_TARGET_NAME = "targetName";
	final public static String KEY_TARGET_ID = "targetId";
	final public static String KEY_TIMESTAMP = "lastTimestamp";

	/** MAILBOX KEY */
	final public static String KEY_MY_USERKEY = "userKey";
	final public static String KEY_MAILBOX_ID = "mailboxId";
	final public static String KEY_AVATAR_URL = "avatarUrl";
	final public static String KEY_UNREAD = "unread";
	final public static String KEY_USER_ID = "userId";
	final public static String KEY_USER_NAME = "userName";
	final public static String KEY_BLOCKED = "blocked";

	// final public static int PHOTO_TARGET_WIDTH = 800;
	final public static int MAIL_TARGET_WIDTH = 400;
	final public static int AVATAR_TARGET_WIDTH = 160;

	final public static int FRAGMENT_FRIEND = 0;
	final public static int FRAGMENT_CHAT = 1;
	final public static int FRAGMENT_MAIL = 2;
	final public static int FRAGMENT_PHOTO = 3;

	/** CONTROL VARIABLE */
	final public static int NETWORK_LIMIT = 25;
	final public static int CHAT_POLL_LIMIT = 500;
	final public static int PHOTO_POLL_LIMIT = 50;
	final public static long REFRESH_INTERVAL = 30000;
	final public static int REFRESH_WHAT = 1;

	public interface NSAEventListener {
		public ArrayList<Kid> getKidList();

		public UserData getUserData();

		public Kid getCurrentKid();

		public String getKidUserKey(String KidId);

		public boolean checkDataOwnership(String receivedKey);

		public void onKidChanged(Kid kid);

		public void sendFriendReq(String friendCode);

		public void deleteFriend(String targetId);

		public void refreshFriendList();

		public void getConversationList();

		public void loadChatHistory(String conversationId, int limit, long since, long until, boolean showDialog);

		public void delChatMessage(String childKey, String messageId, Handler handler);

		public void loadInbox();

		public void loadOutbox();

		public void loadInboxMessage(String boxId);

		public void loadOutboxMessage(String boxId);

		public void removeReceivedPhoto(String photoId, Handler handler);

		public void deleteSharedPhoto(String photoId, Handler handler);

		public void loadAllPhoto(long since, long until, long limit, boolean showDialog);

		public void deleteMailMessage(String boxId, String mailId, boolean isIncoming, Handler handler);

		public void showMailContent(String userKey, String mailId, String url);

		public DatabaseAdapter getDB();

		public void showErrorDialog(boolean shouldExit);

		public void unblockFriend(FriendData fd);

		public boolean isFriendBlocked(String userKey, String friendKey);

		public void setFriendBlockedState(String userKey, String friendKey, boolean blocked);

		public NabiNotificationManager getNotificationManager();
	}

	protected NSAEventListener mCallback;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (NSAEventListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " not implementing OnKidChangedListener");
		}
	}

	/**
	 * 
	 * @param kid
	 * @return index of supplied kid in array list, 0 if not found
	 */
	protected static int getKidIdx(ArrayList<Kid> kids, Kid kid) {
		for (int i = 0; i < kids.size(); i++) {
			if (kids.get(i).getKidId() == kid.getKidId()) {
				return i;
			}
		}
		return 0;
	}
}
