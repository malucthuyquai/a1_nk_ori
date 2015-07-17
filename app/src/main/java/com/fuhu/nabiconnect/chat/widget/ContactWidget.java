package com.fuhu.nabiconnect.chat.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.data.FriendData;
import com.fuhu.data.conversationData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.chat.ChatActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;

import java.util.ArrayList;

/**
 * This class is deprecated but still have some class refer to static member and inner interface.
 */
public class ContactWidget extends RelativeLayout /*
												 * implements
												 * IFriendBeanUpdateListner
												 */{

	public static final String TAG = "ContactWidget";

	public static final String ADD_FRIEND_ID = "ID_ADD_FRIEND";

	public interface IOnContactClickedListener {
		public void OnContactClicked(FriendData friendData, conversationData conversationData);
	}

	private Context m_Context;
	private TextView m_Name;
	private ImageView m_Avatar;
	private ImageView m_MessageIndicator;
	// private RelativeLayout m_BackgroundContainer;
	private IOnContactClickedListener m_Callback;
	private FriendData m_FriendData;
	// private long m_LastReadTimeStamp;
	private conversationData m_ConversationData;
	private LoadAvatarBitmapTask m_Task;
	private ArrayList<IOnContactClickedListener> m_CallBackList;
	private ChatActivity m_ChatActivity;

	public ContactWidget(Context context, FriendData data) {
		super(context, null);

		this.m_Context = context;
		this.m_FriendData = data;
		if (context instanceof ChatActivity)
			m_ChatActivity = (ChatActivity) context;

		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.chat_contact_widget, this);

		m_Name = (TextView) findViewById(R.id.name);
		m_Avatar = (ImageView) findViewById(R.id.avatar);
		m_MessageIndicator = (ImageView) findViewById(R.id.message_indicator);

		try {
			m_Callback = (IOnContactClickedListener) m_Context;
			addCallBacks(m_Callback);
		} catch (ClassCastException e) {
			throw new ClassCastException(m_Context.toString() + " must implement IOnContactClickedListener");
		}

		this.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View view, MotionEvent mv) {
				if (mv.getX() < 0 || mv.getX() > view.getWidth() || mv.getY() < 0 || mv.getY() > view.getHeight()) {
					// m_Background.setBackgroundResource(m_BackgroundId);
					return false;
				}
				switch (mv.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// m_Background.setBackgroundResource(m_BackgroundPressedId);
					return true;
				case MotionEvent.ACTION_UP:
					// m_Background.setBackgroundResource(m_BackgroundId);
					// notifyButtonListeners(BUTTON_ID, TAG);
					notifyCallbacks(m_FriendData, m_ConversationData);
					// m_Callback.OnContactClicked(m_FriendData,
					// m_ConversationData);


					return true;
				}
				return false;
			}
		});

		m_Name.setText(m_FriendData != null ? m_FriendData.userName : "");

		if (data.userID.equals(ADD_FRIEND_ID)) {
			m_Avatar.setImageResource(R.drawable.chat_add_friend);
		} else {
			if (m_ChatActivity != null) {

				// load from cache
				Bitmap bitmap = m_ChatActivity.getDatabaseAdapter().getFriendAvatar(
						m_ChatActivity.getCurrentUserData().userKey, m_FriendData.userID,
						m_ChatActivity.getResources().getDimensionPixelSize(R.dimen.contact_widget_size));

				if (bitmap != null) {
					// update icon
					m_Avatar.setImageBitmap(bitmap);
				} else {
					LOG.V(TAG, "ContactWidget() - no avatar in db");
				}

				// load from server
				m_Task = new LoadAvatarBitmapTask();
				Utils.executeAsyncTask(m_Task, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

					@Override
					public void onBitmapLoaded(Bitmap bitmap) {

						if (bitmap == null) {
							LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
							return;
						}

						// update database
						m_ChatActivity.getDatabaseAdapter().saveFriendAvatar(
								m_ChatActivity.getCurrentUserData().userKey, m_FriendData.userID, bitmap);

						// update icon
						m_Avatar.setImageBitmap(bitmap);

					}
				}, m_FriendData.AvatarUrl);
			}

		}

	}

	public String getUserId() {
		if (m_FriendData != null)
			return m_FriendData.userID;
		{
			LOG.E(TAG, "getUserId() - failed to get userID");
			return null;
		}
	}

	public void cancelAvatarLoadingTask() {

		if (m_Task != null) {
			LOG.V(TAG, "cancelAvatarLoadingTask() -start");
			m_Task.cancel(true);
			LOG.V(TAG, "cancelAvatarLoadingTask() -end");
		}
	}

	public void setMessageIndicator(boolean hasMessage) {
		m_MessageIndicator.setVisibility(hasMessage ? View.VISIBLE : View.INVISIBLE);
	}

	/*
	 * public void setLastReadTimeStamp(long timeStamp) { m_LastReadTimeStamp =
	 * timeStamp; } public long getLastReadTimeStamp() { return
	 * m_LastReadTimeStamp; }
	 */
	public conversationData getConversationData() {
		return m_ConversationData;
	}

	public void setConversationData(conversationData conversationData) {
		this.m_ConversationData = conversationData;
	}

	public void addCallBacks(IOnContactClickedListener calllback) {
		if (m_CallBackList == null)
			m_CallBackList = new ArrayList<ContactWidget.IOnContactClickedListener>();

		if (!m_CallBackList.contains(calllback))
			m_CallBackList.add(calllback);
	}

	private void notifyCallbacks(FriendData friendData, conversationData conversationData) {
		if (m_CallBackList != null)
			for (IOnContactClickedListener callback : m_CallBackList)
				callback.OnContactClicked(friendData, conversationData);
	}

	public void performWhenContactClicked() {

		if (m_FriendData == null || m_ConversationData == null) {
			LOG.V(TAG, "performWhenContactClicked() - failed");
			return;
		}

		notifyCallbacks(m_FriendData, m_ConversationData);
	}
}
