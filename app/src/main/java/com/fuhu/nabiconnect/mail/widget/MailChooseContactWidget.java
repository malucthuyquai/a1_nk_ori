package com.fuhu.nabiconnect.mail.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.data.FriendData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;

import java.util.ArrayList;

public class MailChooseContactWidget extends RelativeLayout{

	public static final String TAG = "MailChooseContactWidget";
	
	private Context m_Context;

	private RelativeLayout m_BackgroundContainer;
	private ImageView m_ContactAvatar;
	private ImageView m_ChooseIndicator;
	private TextView m_ContactName;
	private boolean m_IsChoosed = false;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private FriendData m_FriendData;
	private LoadAvatarBitmapTask m_Task;
	private ApiBaseActivity m_Activity;
	

	public MailChooseContactWidget(Context context, FriendData data) {
		super(context, null);

		this.m_Context = context;
		this.m_FriendData = data;
		if(context instanceof ApiBaseActivity)
		{
			m_Activity = (ApiBaseActivity)context;
		}
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.mail_choose_contact_widget, this);
		
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.mail_choose_widget_container);
		m_ContactAvatar = (ImageView)m_BackgroundContainer.findViewById(R.id.contact_avatar);
		m_ChooseIndicator = (ImageView)m_BackgroundContainer.findViewById(R.id.contact_avatar_choose_indicator);
		m_ContactName = (TextView)m_BackgroundContainer.findViewById(R.id.contact_name);
		//m_Name.setTypeface(Typeface.createFromAsset(m_Context.getAssets(), "fonts/GothamRnd-Bold.otf"));
		
		m_ContactAvatar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchChoosedStatus();

                //tracking
                Tracking.pushTrack(getContext(), "dialog_choose_contact_select_#" + m_ContactName.getText());
			}
		});
		
		m_ContactName.setText(m_FriendData != null ? m_FriendData.userName : "");
		
		
		// load from cache
		if(m_Activity != null)
		{
			Bitmap bitmap = m_Activity.getDatabaseAdapter().getFriendAvatar(
					m_Activity.getCurrentUserData().userKey,
					m_FriendData.userID,
					m_Activity.getResources().getDimensionPixelSize(R.dimen.mail_choose_contact_widget_avatar_size));
			
			if(bitmap != null)
			{
				// update icon
				m_ContactAvatar.setImageBitmap(bitmap);
			}
			else
			{
				LOG.V(TAG, "MailChooseContactWidget() - no avatar in db");
			}
		}
		
		
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
				
				if(m_Activity != null)
				{
					// update database				
					m_Activity.getDatabaseAdapter().saveFriendAvatar(
							m_Activity.getCurrentUserData().userKey, 
							m_FriendData.userID, 
							bitmap);
				}
				
				
				
				// update icon
				m_ContactAvatar.setImageBitmap(bitmap);
			
			}}, 
			m_FriendData.AvatarUrl);

	}
	
	public void cancelLoadingAvater()
	{
		if(m_Task != null)
			m_Task.cancel(true);
	}
	
	public void setInformation(int userId, String userName)
	{
		
	}
	
	private void switchChoosedStatus()
	{
		m_IsChoosed = !m_IsChoosed;
		m_ChooseIndicator.setVisibility(m_IsChoosed ? View.VISIBLE : View.INVISIBLE);
	}
	
	public boolean getIsChoosen()
	{
		return m_IsChoosed;
	}
	
	public FriendData getFriendData()
	{
		return this.m_FriendData;
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
