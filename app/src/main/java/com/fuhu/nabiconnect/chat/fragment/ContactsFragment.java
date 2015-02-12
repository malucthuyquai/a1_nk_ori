package com.fuhu.nabiconnect.chat.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.data.FriendData;
import com.fuhu.data.conversationData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.chat.ChatActivity;
import com.fuhu.nabiconnect.chat.IOnChatMessageReceivedListener;
import com.fuhu.nabiconnect.chat.widget.ContactWidget;
import com.fuhu.nabiconnect.chat.widget.ContactWidget.IOnContactClickedListener;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.ndnslibs.FGC_FLA_Handler.FriendsListPortions;
import com.fuhu.ndnslibsoutstructs.chatPollMessage_outObj;
import com.fuhu.ndnslibsoutstructs.friends_outObj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class ContactsFragment extends Fragment implements IOnChatMessageReceivedListener, IOnContactClickedListener {

	public static final String TAG = "ContactsFragment";

	public static final int PRELOAD_TIME = 5000;
	public static final String ADD_FRIEND_ID = "ID_ADD_FRIEND";
	private GridView m_ContactGridView;
	private ContactWidgetAdapter m_ContactWidgetAdapter;
	private ArrayList<FriendData> m_ContactList = new ArrayList<FriendData>();
	private ChatActivity m_Activity;
	private SharedPreferences m_LastConversationPreference;
	private DatabaseAdapter m_DatabaseAdapter;
	private Hashtable<String, ContactProperty> m_ContactPropertyTable = new Hashtable<String, ContactProperty>();
	private ArrayList<LoadAvatarBitmapTask> m_LoadAvatarTasks = new ArrayList<LoadAvatarBitmapTask>();

	private ArrayList<FriendData> mTempFriendList = new ArrayList<FriendData>();
	private ArrayList<conversationData> mTempConversationList = new ArrayList<conversationData>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.chat_contact_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		m_Activity = (ChatActivity) this.getActivity();
	}

	@Override
	public void onResume() {
		super.onResume();
		/**
		 * null pointer crash @m_ContactWidgetAdapter if put in
		 * onActivityCreated. leave it for now
		 */
		m_ContactGridView = (GridView) getView().findViewById(R.id.contact_grid_view);
		m_ContactGridView.setOnItemClickListener(oicl);
		m_ContactWidgetAdapter = new ContactWidgetAdapter(m_Activity, 0, m_ContactList);
		m_ContactGridView.setAdapter(m_ContactWidgetAdapter);

		// Add event listener
		addApiEventListener();
		// get database adapter
		m_DatabaseAdapter = m_Activity.getDatabaseAdapter();
		// get friend list from cache
		ArrayList<FriendData> friendCache = m_DatabaseAdapter.getFriendList(m_Activity.getCurrentUserData().userKey);
		mTempFriendList.clear();
		mTempFriendList.addAll(friendCache);
		updateContactTable(friendCache);
		// get conversation data from cache
		ArrayList<conversationData> conversastionCache = m_DatabaseAdapter.getConversationList(m_Activity
				.getCurrentUserData().userKey);
		updateConversationData(conversastionCache);
		if (!m_Activity.isFinishing()) {
			if (m_Activity.getNeedRelogin()) {
				// login is dirty
				if (m_Activity.isParentMode()) {
					m_Activity.loginAccountNoKid();
				} else {
					m_Activity.loginAccount();
				}
			} else {
				m_Activity.getFriendList(m_Activity.getCurrentUserData().userKey, false, FriendsListPortions.CHAT);
			}
		}
	}

	@Override
	public void onPause() {
		removeApiEventListener();
		for (LoadAvatarBitmapTask task : m_LoadAvatarTasks)
			task.cancel(true);
		m_LoadAvatarTasks.clear();
		m_ContactPropertyTable.clear();
		m_ContactWidgetAdapter = null;
		super.onPause();
	}

	private IApiEventListener mOnUserLogin = new IApiEventListener() {
		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				m_Activity.setNeedRelogin(false);
				// get friend list
				m_Activity.getFriendList(m_Activity.getCurrentUserData().userKey, false, FriendsListPortions.CHAT);
			} else {
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private IApiEventListener m_GetFriendEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				friends_outObj data = (friends_outObj) obj;

				if (data == null) {
					LOG.V(TAG, "m_GetFriendEventListener - data is null");
					return;
				}

				// update database
				m_DatabaseAdapter.updateFriendList(data);

				// store in memory but do not update ui yet.
				mTempFriendList.clear();
				mTempFriendList.addAll(data.getFriends());
				/*
				 * ArrayList<FriendData> friendCache = data.getFriends();
				 * Collections.reverse(friendCache);
				 * updateContactTable(friendCache);
				 */

				// check the unread message
				m_Activity.getChatPollMessage(m_Activity.getCurrentUserData().userKey, false);
			} else {
				LOG.V(TAG, "m_GetFriendEventListener - failed to get friend list");
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private IApiEventListener m_GetChatPollEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				chatPollMessage_outObj data = (chatPollMessage_outObj) obj;

				if (data == null) {
					LOG.V(TAG, "m_GetChatPollEventListener - data is null");
					return;
				}

				LOG.V(TAG, "data.getConversations() : " + data.getConversations());

				// update database
				m_DatabaseAdapter.updateConversationList(data);

				// get conversation data from cache
				// ArrayList<conversationData> conversastionCache =
				// m_DatabaseAdapter.getConversationList(m_Activity
				// .getCurrentUserData().userKey);
				// use server returned data
				mTempConversationList.clear();
				mTempConversationList.addAll(data.getConversations());
				sortFriendList(mTempConversationList, mTempFriendList);
				updateContactTable(mTempFriendList);
				updateConversationData(mTempConversationList);
				// update contact table unread message
				// updateConversationData(data.getConversations());

			} else {
				LOG.V(TAG, "m_GetChatPollEventListener - failed to get chat poll");
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private void updateContactTable(ArrayList<FriendData> updatedList) {
		LOG.V(TAG, "updateContactTable() - start");

		if (updatedList == null) {
			LOG.V(TAG, "updateContactTable() - updatedList is null");
			return;
		}

		// add Add Friend icon
		FriendData addFriendData = new FriendData();
		addFriendData.userID = ContactWidget.ADD_FRIEND_ID;
		addFriendData.userName = m_Activity.getString(R.string.chat_add_friend);
		// make it smaller than default -1, so the button will sit at the bottom
		// of the list
		addFriendData.mLastTalkTime = -2;
		updatedList.add(addFriendData);

		// check the last conversation data
		m_LastConversationPreference = m_Activity.getSharedPreferences(ChatActivity.LAST_CONVERSATION_PREF,
				Context.MODE_PRIVATE);
		String lastTalkUserKey = m_LastConversationPreference.getString(m_Activity.getCurrentUserData().userKey, "");
		LOG.V(TAG, "updateContactTable() - lastTalkUserKey is " + lastTalkUserKey);

		for (int i = 0; i < updatedList.size(); i++) {
			final FriendData fData = updatedList.get(i);
			ContactProperty property = m_ContactPropertyTable.get(fData.userID);
			if (property == null)
				property = new ContactProperty(m_Activity);

			// update frienddata
			property.addCallBacks(this);
			property.setFriendData(fData);

			if (i == 0) {
				// set the first contact as the default chat target
				m_Activity.setCurrentChatFriend(fData);
				m_Activity.setCurrenctContactWidget(property);
			}

			if (fData.userID.equals(lastTalkUserKey)) {
				LOG.V(TAG, "updateContactTable() - Set current chat friend according to preference");
				m_Activity.setCurrentChatFriend(fData);
				m_Activity.setCurrenctContactWidget(property);
			}

			// update bitmap table from cache
			Bitmap bitmap = m_Activity.getDatabaseAdapter().getFriendAvatar(m_Activity.getCurrentUserData().userKey,
					fData.userID, m_Activity.getResources().getDimensionPixelSize(R.dimen.friend_item_avatar_size));

			if (bitmap != null) {
				property.setAvatarBitmap(bitmap);
			}

			// load picture from server
			LoadAvatarBitmapTask bitmapTask = new LoadAvatarBitmapTask();
			m_LoadAvatarTasks.add(bitmapTask);
			Utils.executeAsyncTask(bitmapTask, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

				@Override
				public void onBitmapLoaded(Bitmap bitmap) {

					if (bitmap == null) {
						LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
						return;
					}

					// update database
					m_Activity.getDatabaseAdapter().saveAvatarAsync(m_Activity.getCurrentUserData().userKey,
							fData.userID, bitmap);

					// update hash table
					ContactProperty property = m_ContactPropertyTable.get(fData.userID);
					if (property == null)
						property = new ContactProperty(m_Activity);

					property.setAvatarBitmap(bitmap);
					m_ContactPropertyTable.put(fData.userID, property);

					// notify data changed
					if (m_ContactWidgetAdapter != null) {
						// LOG.V(TAG,"onBitmapLoaded() - m_ContactWidgetAdapter.notifyDataSetChanged()");
						m_ContactWidgetAdapter.notifyDataSetChanged();
					}

				}
			}, fData.AvatarUrl);

			// update hashtable
			m_ContactPropertyTable.put(fData.userID, property);
		}

		// refresh gridview
		m_ContactList.clear();
		m_ContactList.addAll(updatedList);
		m_ContactWidgetAdapter.notifyDataSetChanged();
		LOG.V(TAG, "updateContactTable() - end");
	}

	private void updateConversationData(ArrayList<conversationData> conversationData) {
		if (conversationData == null) {
			LOG.W(TAG, "updateConversationData() - conversationData is null.");
			return;
		}

		for (conversationData fData : conversationData) {
			ArrayList<String> userInChatRoom = fData.m_Actors;

			// for (String str : userInChatRoom)
			// LOG.V(TAG, "userInChatRoom : " + str);

			/*
			 * for(ContactWidget widget : m_ContactWidgetList) {
			 * ArrayList<String> list = new ArrayList<String>();
			 * list.add(widget.getUserId());
			 * list.add(m_Activity.getCurrentUserData().userKey); //for(String
			 * str : list) // LOG.V(TAG, "userInWidget : "+str);
			 * if(Utils.compareArrayList(userInChatRoom, list)) { // check
			 * unread message LOG.V(TAG,
			 * "fData.m_UnreadMessageCount is "+fData.m_UnreadMessageCount);
			 * LOG.V(TAG,
			 * "fData.m_LastReadTimestamp is "+fData.m_LastReadTimestamp);
			 * widget.setMessageIndicator(fData.m_UnreadMessageCount > 0);
			 * widget.setConversationData(fData);
			 * //widget.setLastReadTimeStamp(fData.m_LastReadTimestamp);
			 * 
			 * 
			 * // set the default time stamp & conversation data
			 * if(widget.getUserId
			 * ().equals(m_Activity.getCurrentChatFriend().userID)) {
			 * //m_Activity.setCurrentTimeStamp(fData.m_LastReadTimestamp);
			 * m_Activity.setCurrenctConversationData(fData); } } }
			 */

			// traverse the hashtable
			Enumeration<String> keys = m_ContactPropertyTable.keys();
			while (keys.hasMoreElements()) {
				String userId = keys.nextElement();
				ContactProperty property = m_ContactPropertyTable.get(userId);

				ArrayList<String> list = new ArrayList<String>();
				list.add(userId);
				list.add(m_Activity.getCurrentUserData().userKey);
				// for(String str : list)
				// LOG.V(TAG, "userInWidget : "+str);
				if (Utils.compareArrayList(userInChatRoom, list)) {
					// check unread message
					LOG.V(TAG, "fData.m_UnreadMessageCount is " + fData.m_UnreadMessageCount);
					LOG.V(TAG, "fData.m_LastReadTimestamp is " + fData.m_LastReadTimestamp);

					if (property == null)
						property = new ContactProperty(m_Activity);

					property.setConversationData(fData);

					m_ContactPropertyTable.put(userId, property);

					if (m_ContactWidgetAdapter != null)
						m_ContactWidgetAdapter.notifyDataSetChanged();

					// set the default time stamp & conversation data
					if (userId.equals(m_Activity.getCurrentChatFriend().userID)) {
						m_Activity.setCurrenctConversationData(fData);
					}
				}
			}
		}
	}

	@Override
	public void OnChatMessageReceived() {
		// updateContactTable();
	}

	private void addApiEventListener() {
		LOG.V(TAG, "addApiEventListener() - start");
		m_Activity.onLoginAccount.addEventListener(mOnUserLogin);
		m_Activity.onGetFriendList.addEventListener(m_GetFriendEventListener);
		m_Activity.onGetChatPoll.addEventListener(m_GetChatPollEventListener);
		LOG.V(TAG, "addApiEventListener() - end");
	}

	private void removeApiEventListener() {
		LOG.V(TAG, "removeApiEventListener() - start");
		m_Activity.onLoginAccount.removeEventListener(mOnUserLogin);
		m_Activity.onGetFriendList.removeEventListener(m_GetFriendEventListener);
		m_Activity.onGetChatPoll.removeEventListener(m_GetChatPollEventListener);
		LOG.V(TAG, "removeApiEventListener() - end");
	}

	private class ContactWidgetAdapter extends ArrayAdapter<FriendData> {

		private LayoutInflater inflater;

		public ContactWidgetAdapter(Context context, int resource, List<FriendData> objects) {
			super(context, resource, objects);
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.chat_contact_widget, parent, false);
				holder = new ViewHolder();
				holder.rl_container = (RelativeLayout) convertView.findViewById(R.id.contact_background_boarder);
				holder.iv_avatar = (ImageView) convertView.findViewById(R.id.avatar);
				holder.iv_unread = (ImageView) convertView.findViewById(R.id.message_indicator);
				holder.tv_name = (TextView) convertView.findViewById(R.id.name);
				convertView.setTag(R.id.HOLDER, holder);
			} else {
				holder = (ViewHolder) convertView.getTag(R.id.HOLDER);
			}

			// default value
			holder.iv_unread.setVisibility(View.INVISIBLE);
			holder.iv_avatar.setImageResource(R.drawable.chat_avatar_default);

			String keyUserId = getItem(position).userID;

			ContactProperty property = m_ContactPropertyTable.get(keyUserId);
			FriendData friendData = getItem(position);
			conversationData conversationData = null;

			if (property != null) {
				conversationData = property.getConversationData();
				final Bitmap avatarBitmap = property.getAvatarBitmap();
				holder.tv_name.setText(friendData.userName);
				holder.iv_unread
						.setVisibility(conversationData != null && conversationData.m_UnreadMessageCount > 0 ? View.VISIBLE
								: View.INVISIBLE);
				if (friendData.userID.equals(ADD_FRIEND_ID)) {
					holder.iv_avatar.setImageResource(R.drawable.chat_add_friend);
				} else if (avatarBitmap != null) {
					holder.iv_avatar.setImageBitmap(avatarBitmap);
				}
			} else {
				LOG.V(TAG, "getView() - property is null");
			}
			convertView.setTag(R.id.FRIEND_DATA, friendData);
			convertView.setTag(R.id.CONVERSATION_DATA, conversationData);
			convertView.setTag(R.id.PROPERTY, property);
			return convertView;
		}
	}

	private AdapterView.OnItemClickListener oicl = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			FriendData friendData = (FriendData) view.getTag(R.id.FRIEND_DATA);
			conversationData conversationData = (conversationData) view.getTag(R.id.CONVERSATION_DATA);
			m_Activity.OnContactClicked(friendData, conversationData);
		}
	};

	static class ViewHolder {
		public RelativeLayout rl_container;
		public TextView tv_name;
		public ImageView iv_avatar;
		public ImageView iv_unread;
	}

	public static class ContactProperty {

		private IOnContactClickedListener callback;
		private FriendData friendData;
		private conversationData conversationData;
		private Bitmap avatarBitmap;
		private ArrayList<IOnContactClickedListener> m_CallBackList;

		public ContactProperty(Context context) {
			try {
				callback = (IOnContactClickedListener) context;
				addCallBacks(callback);
			} catch (ClassCastException e) {
				throw new ClassCastException(context.toString() + " must implement IOnContactClickedListener");
			}
		}

		public FriendData getFriendData() {
			return friendData;
		}

		public void setFriendData(FriendData friendData) {
			this.friendData = friendData;
		}

		public conversationData getConversationData() {
			return conversationData;
		}

		public void setConversationData(conversationData conversationData) {
			this.conversationData = conversationData;
		}

		public Bitmap getAvatarBitmap() {
			return avatarBitmap;
		}

		public void setAvatarBitmap(Bitmap avatarBitmap) {
			this.avatarBitmap = avatarBitmap;
		}

		public void addCallBacks(IOnContactClickedListener calllback) {
			if (m_CallBackList == null)
				m_CallBackList = new ArrayList<ContactWidget.IOnContactClickedListener>();

			if (!m_CallBackList.contains(calllback))
				m_CallBackList.add(calllback);
		}

		public void notifyCallbacks(FriendData friendData, conversationData conversationData) {
			if (m_CallBackList != null)
				for (IOnContactClickedListener callback : m_CallBackList) {
					callback.OnContactClicked(friendData, conversationData);
				}
		}

		public void performWhenContactClicked() {

			if (friendData == null || conversationData == null) {
				LOG.V(TAG, "performWhenContactClicked() - failed");
				return;
			}
			notifyCallbacks(friendData, conversationData);
		}
	}

	@Override
	public void OnContactClicked(FriendData friendData, conversationData conversationData) {

		for (LoadAvatarBitmapTask task : m_LoadAvatarTasks) {
			task.cancel(true);
		}
		m_LoadAvatarTasks.clear();
	}

	/**
	 * puts friendList in order based on chat history
	 * 
	 * @param conversationList
	 * @param friendList
	 */
	private void sortFriendList(ArrayList<conversationData> conversationList, ArrayList<FriendData> friendList) {
		ArrayList<FriendData> buffer = new ArrayList<FriendData>();
		FriendData parent = null;
		for (FriendData fd : friendList) {
			if (fd.relationship == FriendData.PARENT) {
				parent = fd;
			}
			conversationData cd = findChatByFriendId(fd.userID, conversationList);
			if (cd != null && cd.m_LatestMessageTimeStamp > 0) {
				// NOTE: some conversation has latest message timestamp 0,
				// in that case, leave mLastTalkTime in FriendData as default
				// (-1)
				fd.mLastTalkTime = cd.m_LatestMessageTimeStamp;
			}
			buffer.add(fd);
		}
		friendList.clear();
		friendList.addAll(buffer);
		Collections.sort(friendList, Collections.reverseOrder(mFriendComparator));
		if (!m_Activity.isParentMode() && parent != null) {
			LOG.D(TAG, "placing parent at head");
			// in nabi / kid's mode, place parent at position 0
			friendList.remove(parent);
			friendList.add(0, parent);
		} else {
			LOG.D(TAG, "parent mode or parent null");
		}
	}

	// private Comparator<conversationData> mConversationComparator = new
	// Comparator<conversationData>() {
	// @Override
	// public int compare(conversationData lhs, conversationData rhs) {
	// // to sort in reverse chronological order
	// return (int) (rhs.m_LatestMessageTimeStamp -
	// lhs.m_LatestMessageTimeStamp);
	// }
	// };

	/**
	 * 
	 * @param friendId
	 * @param chatList
	 * @return null if not found
	 */
	private conversationData findChatByFriendId(String friendId, ArrayList<conversationData> chatList) {
		for (conversationData cd : chatList) {
			if (cd.m_Actors.contains(friendId)) {
				return cd;
			}
		}
		return null;
	}

	private Comparator<FriendData> mFriendComparator = new Comparator<FriendData>() {
		@Override
		public int compare(FriendData lhs, FriendData rhs) {
			long diff = lhs.mLastTalkTime - rhs.mLastTalkTime;
			if (diff == 0) {
				if (lhs.mLastTalkTime == -1) {
					// sort using contact user name, in reverse order
					return rhs.userName.compareToIgnoreCase(lhs.userName);
				} else {
					return 0;
				}
			} else if (diff < 0) {
				return -1;
			} else {
				return 1;
			}
		}
	};
}