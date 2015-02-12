package com.fuhu.nabiconnect.friend.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.data.FriendData;
import com.fuhu.data.PendingFriendRequestData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.friend.FriendActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.LoadAvatarTask;
import com.fuhu.nabiconnect.utils.LoadDbFriendAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;

import java.util.ArrayList;

public class FriendItemWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "FriendItemWidget";
	private Context m_Context;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private enum DeleteStatus {NORMAL, DELETING};
	private RelativeLayout m_BackgroundContainer;
	private ImageView m_AvatarImageView;
	private RelativeLayout m_FriendRequestContainer;
	private TextView m_FriendRequestName;
	private TextView m_FriendRequestDescription;
	private TextView m_FriendName;
	private Button m_AcceptButton;
	private Button m_RejectButton;
	private RelativeLayout m_DeleteFriendContainer;
	private ImageView m_DeleteFriendIcon;
	private FriendActivity m_FriendActivity;
	
	private FriendData m_FriendData;
	private PendingFriendRequestData m_FriendRequestData;
	
	private DeleteStatus m_DeleteStatus;
	private LoadAvatarBitmapTask m_Task;
	private LoadDbFriendAvatarBitmapTask m_DbTask;
	
	public static final int ACCEPT_BUTTON_ID = 100;
	public static final int REJECT_BUTTON_ID = 101;
	public static final int DELETE_BUTTON_ID = 102;
	
	public FriendItemWidget(Context context, FriendData friendData) {
		this(context, friendData, null);
	}
	
	public FriendItemWidget(Context context, PendingFriendRequestData friendRequest) {
		this(context, null, friendRequest);
	}
	
	public FriendItemWidget(Context context, FriendData friendData, PendingFriendRequestData friendRequest) {
		super(context, null);

		this.m_Context = context;
		this.m_FriendData = friendData;
		this.m_FriendRequestData = friendRequest;
		if(context instanceof FriendActivity)
			m_FriendActivity = (FriendActivity)context;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.friend_item_widget, this);
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.friend_item_background);
		
		m_AvatarImageView = (ImageView)m_BackgroundContainer.findViewById(R.id.friend_avatar);
		m_FriendRequestContainer = (RelativeLayout)m_BackgroundContainer.findViewById(R.id.friend_request_container);
		m_FriendRequestName = (TextView)m_BackgroundContainer.findViewById(R.id.friend_request_name);
		m_FriendName = (TextView)m_BackgroundContainer.findViewById(R.id.friend_name);
		m_FriendRequestDescription = (TextView)m_BackgroundContainer.findViewById(R.id.be_friend_string);
		m_AcceptButton = (Button)m_BackgroundContainer.findViewById(R.id.accept_button);
		m_RejectButton = (Button)m_BackgroundContainer.findViewById(R.id.reject_button);
		m_DeleteFriendContainer = (RelativeLayout)m_BackgroundContainer.findViewById(R.id.delete_friend_container);
		m_DeleteFriendIcon = (ImageView)m_BackgroundContainer.findViewById(R.id.delete_friend_icon);
		
		//m_FriendRequestName.setTypeface(Typeface.createFromAsset(m_Context.getAssets(), "fonts/GothamRnd-Medium.otf"));
		//m_FriendRequestDescription.setTypeface(Typeface.createFromAsset(m_Context.getAssets(), "fonts/GothamRnd-Medium.otf"));
		//m_FriendName.setTypeface(Typeface.createFromAsset(m_Context.getAssets(), "fonts/GothamRnd-Medium.otf"));
		
		
		m_FriendRequestName.setText(friendRequest != null ? friendRequest.userName : "");
		m_FriendName.setText(friendData != null ? friendData.userName : "");
		
		if(m_FriendActivity != null)
		{
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)m_BackgroundContainer.getLayoutParams();
			if(m_FriendActivity.isParentMode())
				params.width = m_Context.getResources().getDimensionPixelSize(R.dimen.friend_item_background_parent_mode_width);
			else
				params.width = m_Context.getResources().getDimensionPixelSize(R.dimen.friend_item_background_width);
			
			m_BackgroundContainer.requestLayout();
		}
		
		LoadAvatarTask task = new LoadAvatarTask();
		if(friendRequest != null)
		{
			m_BackgroundContainer.setBackgroundColor(m_Context.getResources().getColor(R.color.friend_request_list_item_color));
			
			m_FriendName.setVisibility(View.INVISIBLE);
			m_DeleteFriendContainer.setVisibility(View.INVISIBLE);
			Utils.executeAsyncTask(task, m_AvatarImageView, friendRequest.AvatarUrl);
			
		}
		else if(friendData != null)
		{
			m_BackgroundContainer.setBackgroundColor(m_Context.getResources().getColor(R.color.friend_list_item_color));
			
			m_FriendRequestContainer.setVisibility(View.INVISIBLE);
			m_AcceptButton.setVisibility(View.INVISIBLE);
			m_RejectButton.setVisibility(View.INVISIBLE);
			//task.execute(m_AvatarImageView, friendData.AvatarUrl);

			if(friendData.relationship == FriendData.FRIEND)
				m_DeleteFriendContainer.setVisibility(View.VISIBLE);
			else
				m_DeleteFriendContainer.setVisibility(View.INVISIBLE);
			
			
			updateFriendData(friendData);
			
		}
		
		m_AcceptButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				notifyButtonListeners(ACCEPT_BUTTON_ID, TAG, new Object[]{m_FriendRequestData});
			}
		});
		m_RejectButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				notifyButtonListeners(REJECT_BUTTON_ID, TAG, new Object[]{m_FriendRequestData});
			}
		});
		
		m_DeleteFriendContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (m_DeleteStatus) {
				case NORMAL:
					setDeleteStatus(DeleteStatus.DELETING);
					break;
				case DELETING:
					notifyButtonListeners(DELETE_BUTTON_ID, TAG, new Object[]{m_FriendData});
					break;
				}
			}
		});
		
		setDeleteStatus(DeleteStatus.NORMAL);
	}
	
	public void updateFriendData(FriendData updatedData)
	{
		if(updatedData == null)
			return;
		
		m_FriendData = updatedData;
		m_FriendName.setText(updatedData.userName);
		
		if(updatedData.relationship == FriendData.FRIEND)
			m_DeleteFriendContainer.setVisibility(View.VISIBLE);
		else
			m_DeleteFriendContainer.setVisibility(View.INVISIBLE);
		
		
		// load from cache		
		if(m_FriendActivity != null)
		{
			LOG.V(TAG, "updateFriendData() - load avatar from db");
			
			
			Bitmap bitmap = m_FriendActivity.getDatabaseAdapter().getFriendAvatar(
					m_FriendActivity.getCurrentUserData().userKey,
					m_FriendData.userID,
					m_FriendActivity.getResources().getDimensionPixelSize(R.dimen.friend_item_avatar_size));
			
			if(bitmap != null)
			{
				// update icon
				m_AvatarImageView.setImageBitmap(bitmap);
			}
			else
			{
				LOG.V(TAG, "updateFriendData() - no avatar in db");
			}
			
			
			/*
			m_DbTask = new LoadDbFriendAvatarBitmapTask();
			m_DbTask.execute(new LoadDbFriendAvatarBitmapTask.IOnBitmapLoaded(){

				@Override
				public void onBitmapLoaded(Bitmap bitmap) {
					
					if(bitmap == null)
					{
						LOG.E(TAG, "onBitmapLoaded() - bitmap from db is null");
						return;
					}
					
					// update icon
					m_AvatarImageView.setImageBitmap(bitmap);
					
				}},
				m_FriendActivity.getDatabaseAdapter(),
				m_FriendActivity.getCurrentUserData().userKey,
				m_FriendData.userID,
				150
			);
			*/
			
			
			// load from server
			m_Task = new LoadAvatarBitmapTask();
			Utils.executeAsyncTask(m_Task, new LoadAvatarBitmapTask.IOnBitmapLoaded(){

				@Override
				public void onBitmapLoaded(Bitmap bitmap) {
					
					if(bitmap == null)
					{
						LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
						return;
					}
					
					// update database				
					m_FriendActivity.getDatabaseAdapter().saveFriendAvatar(
							m_FriendActivity.getCurrentUserData().userKey, 
							m_FriendData.userID, 
							bitmap);
					
					
					// update icon
					m_AvatarImageView.setImageBitmap(bitmap);
				
				}}, 
				updatedData.AvatarUrl);
			
		}

			

	}
	
	
	public void cancelTask()
	{
		if(m_Task != null)
			m_Task.cancel(true);
	}
	
	public String getUserId()
	{
		if(m_FriendData != null)
			return m_FriendData.userID;
		else if (m_FriendRequestData != null)
			return m_FriendRequestData.userID;
		else
		{
			LOG.E(TAG,"getUserId() - failed to get userID");
			return null;
		}
	}
	
	private void setDeleteStatus(DeleteStatus status)
	{
		if(m_DeleteStatus != status)
		{
			m_DeleteStatus = status;
			onDeleteStatusUpdated();
		}
		
	}
	private void onDeleteStatusUpdated()
	{
		switch (m_DeleteStatus) {
			case NORMAL:
				m_DeleteFriendIcon.setBackgroundResource(R.drawable.friend_x_hover);
				break;
			case DELETING:
				m_AvatarImageView.setAlpha(127);
				m_FriendName.setTextColor(Color.argb(127, 255, 255, 255));
				m_DeleteFriendIcon.setBackgroundResource(R.drawable.friend_x_normal);
				m_DeleteFriendContainer.setBackgroundColor(m_Context.getResources().getColor(R.color.friend_delete_container_blue));
				break;

		}
	}
	/*======================================================================
	 * Add listners for button
	 *=======================================================================*/
	public void notifyButtonListeners(int buttonID, String tag, Object[] args)
	{
		if(m_ButtonListeners != null)
			for(IButtonClickListener listener : m_ButtonListeners)
				listener.onButtonClicked(buttonID, tag, args);
	}
	
	/*======================================================================
	 * Notify button listeners
	 *=======================================================================*/
	public void addButtonListener(IButtonClickListener listener)
	{
		if(m_ButtonListeners == null)
			m_ButtonListeners = new ArrayList<IButtonClickListener>();
			
		if(!m_ButtonListeners.contains(listener))
			m_ButtonListeners.add(listener);
	}
	
}
