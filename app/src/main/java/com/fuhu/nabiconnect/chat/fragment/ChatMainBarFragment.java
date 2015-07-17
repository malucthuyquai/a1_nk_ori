/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fuhu.nabiconnect.chat.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.IOnMainBarIndexChangedListener;
import com.fuhu.nabiconnect.IOnMainBarItemSelectedListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.chat.ChatActivity;
import com.fuhu.nabiconnect.chat.widget.MainBarButtonWidget;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.tracking.TrackingFragmentActivity;

public class ChatMainBarFragment extends Tracking.TrackingInfoFragment implements IOnMainBarIndexChangedListener{
	
	
	public static final String TAG = "MainBarFragment";
	IOnMainBarItemSelectedListener mCallback;

	public static final int ITEM_CONTACT_ID = 1;
	public static final int ITEM_CHAT_ID = 2;
	public static final int ITEM_SHOP_ID = 3;
	
	private ImageView m_ChatBarIcon;
	private MainBarButtonWidget m_ContactButton;
	private MainBarButtonWidget m_ChatButton;
	private MainBarButtonWidget m_ShopButton;
	
	private int m_CurrentMainbarItem;
	
	private ChatActivity m_Activity;

    public ChatMainBarFragment() {
        super(ChatMainBarFragment.class.getSimpleName());
    }

    @Override
    public String getTrack() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_Activity = (ChatActivity)getActivity();
    }

    
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//return super.onCreateView(inflater, container, savedInstanceState);
    	return inflater.inflate(R.layout.chat_mainbar_view, container, false);
	}



	@Override
	public void onResume() {
		super.onResume();
				
		m_ChatBarIcon = (ImageView)getView().findViewById(R.id.chat_bar_icon);
		m_ContactButton = (MainBarButtonWidget)getView().findViewById(R.id.contact_button);
		m_ChatButton = (MainBarButtonWidget)getView().findViewById(R.id.chat_button);
		m_ShopButton = (MainBarButtonWidget)getView().findViewById(R.id.shop_button);
			
		m_ChatBarIcon.setVisibility(m_Activity.isParentMode() ? View.VISIBLE : View.INVISIBLE);
		m_ContactButton.setInformation(R.drawable.chat_choosebar_hover, R.drawable.chat_btn_book_hover, R.drawable.chat_btn_book);
		m_ChatButton.setInformation(R.drawable.chat_choosebar_hover, R.drawable.chat_btn_text_hover, R.drawable.chat_btn_text);
		m_ShopButton.setInformation(R.drawable.chat_choosebar_hover, R.drawable.chat_btn_shop_hover, R.drawable.chat_btn_shop);
		
		m_ContactButton.addButtonListener(new IButtonClickListener() {
			
			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {

                //tracking
                Tracking.pushTrack(getActivity(),
                        ((TrackingFragmentActivity) getActivity()).getPageName(), "contact_list");

				mCallback.OnMainBarItemSelected(ITEM_CONTACT_ID);
				
			}
		});
		m_ChatButton.addButtonListener(new IButtonClickListener() {
			
			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {

                //tracking
                Tracking.pushTrack(getActivity(),
                        ((TrackingFragmentActivity) getActivity()).getPageName(), "chat_page");

				if(m_Activity.getCurrentMainBarItem() == ChatMainBarFragment.ITEM_CHAT_ID )
				{
					LOG.V(TAG, "already in chat page");
					return;
				}
				
				if(m_Activity.getCurrenctContactWidget() != null)
				{
					m_Activity.getCurrenctContactWidget().performWhenContactClicked();
				}
				else
				{
					LOG.V(TAG, "m_ContactButton clicked, but there is no conversation selected");
				}
				
//				if(m_Activity.getCurrentChatFriend() == null || m_Activity.getCurrenctConversationData() == null)
//				{
//					LOG.V(TAG, "m_ContactButton clicked, but there is no conversation selected");
//					return;
//				}
//				
//				mCallback.OnMainBarItemSelected(ITEM_CHAT_ID);
				
			}
		});
		m_ShopButton.addButtonListener(new IButtonClickListener() {
			
			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {

                //tracking not used
                Tracking.pushTrack(getActivity(),
                        ((TrackingFragmentActivity) getActivity()).getPageName(), "sticker_store");

                mCallback.OnMainBarItemSelected(ITEM_SHOP_ID);
				
			}
		});
		
		m_CurrentMainbarItem = m_Activity.getCurrentMainBarItem();
		updataMainbarUI();
	}

	private void updataMainbarUI()
	{
		switch(m_CurrentMainbarItem)
		{
		case ChatMainBarFragment.ITEM_CONTACT_ID:		
			m_ContactButton.setSelected(true);		
			m_ChatButton.setSelected(false);
			m_ShopButton.setSelected(false);
			break;
		case ChatMainBarFragment.ITEM_CHAT_ID:
			m_ContactButton.setSelected(false);		
			m_ChatButton.setSelected(true);
			m_ShopButton.setSelected(false);
			break;
		case ChatMainBarFragment.ITEM_SHOP_ID:
			m_ContactButton.setSelected(false);		
			m_ChatButton.setSelected(false);
			m_ShopButton.setSelected(true);
			break;
		}
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (IOnMainBarItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IOnMainBarItemSelectedListener");
        }
    }



	@Override
	public void OnMainBarIndexChanged(int position) {
		
		LOG.V(TAG,"OnMainBarIndexChanged() - position is "+position);
		
		m_CurrentMainbarItem = position;
		updataMainbarUI();
	}


}