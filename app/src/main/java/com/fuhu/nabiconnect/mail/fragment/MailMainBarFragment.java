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
package com.fuhu.nabiconnect.mail.fragment;

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
import com.fuhu.nabiconnect.chat.widget.MainBarButtonWidget;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.MailActivity;

public class MailMainBarFragment extends Tracking.TrackingInfoFragment implements IOnMainBarIndexChangedListener {

	public static final String TAG = "MailMainBarFragment";
	IOnMainBarItemSelectedListener mCallback;

	public static final int ITEM_COMPOSE_ID = 1;
	public static final int ITEM_INBOX_ID = 2;
	public static final int ITEM_UNSENT_ID = 3;

	private ImageView m_MailBarIcon;
	private MainBarButtonWidget m_ComposeButton;
	private MainBarButtonWidget m_InboxButton;
	private MainBarButtonWidget m_UnsentButton;

	private int m_CurrentMainbarItem;

	private MailActivity m_Activity;

    public MailMainBarFragment() {
        super(MailMainBarFragment.class.getSimpleName());
    }

    @Override
    public String getTrack() {
        return null;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_Activity = (MailActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.mail_mainbar_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		m_MailBarIcon = (ImageView) getView().findViewById(R.id.mail_bar_icon);
		m_ComposeButton = (MainBarButtonWidget) getView().findViewById(R.id.mail_compose_button);
		m_InboxButton = (MainBarButtonWidget) getView().findViewById(R.id.mail_inbox_button);
		m_UnsentButton = (MainBarButtonWidget) getView().findViewById(R.id.mail_unsent_button);

		m_MailBarIcon.setVisibility(m_Activity.isParentMode() ? View.VISIBLE : View.INVISIBLE);
		m_ComposeButton.setInformation(R.drawable.chat_choosebar_hover, R.drawable.mail_compose,
				R.drawable.mail_compose_pressed);
		m_InboxButton.setInformation(R.drawable.chat_choosebar_hover, R.drawable.mail_inbox,
				R.drawable.mail_inbox_pressed);
		m_UnsentButton.setInformation(R.drawable.chat_choosebar_hover, R.drawable.mail_unsent,
				R.drawable.mail_unsent_pressed);

		m_ComposeButton.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {

				mCallback.OnMainBarItemSelected(ITEM_COMPOSE_ID);

			}
		});
		m_InboxButton.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {

				mCallback.OnMainBarItemSelected(ITEM_INBOX_ID);

			}
		});
		m_UnsentButton.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				mCallback.OnMainBarItemSelected(ITEM_UNSENT_ID);

			}
		});
		m_CurrentMainbarItem = 1;// m_Activity.getCurrentMainBarItem();
		updataMainbarUI();
	}

	private void updataMainbarUI() {

		switch (m_CurrentMainbarItem) {
		case MailMainBarFragment.ITEM_COMPOSE_ID:
			m_ComposeButton.setSelected(true);
			m_InboxButton.setSelected(false);
			m_UnsentButton.setSelected(false);
			break;
		case MailMainBarFragment.ITEM_INBOX_ID:
			m_ComposeButton.setSelected(false);
			m_InboxButton.setSelected(true);
			m_UnsentButton.setSelected(false);
			break;
		case MailMainBarFragment.ITEM_UNSENT_ID:
			m_ComposeButton.setSelected(false);
			m_InboxButton.setSelected(false);
			m_UnsentButton.setSelected(true);
			break;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (IOnMainBarItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnMainBarItemSelectedListener");
		}
	}

	@Override
	public void OnMainBarIndexChanged(int position) {
		LOG.V(TAG, "OnMainBarIndexChanged() - position is " + position);
		m_CurrentMainbarItem = position;
		updataMainbarUI();
	}
}