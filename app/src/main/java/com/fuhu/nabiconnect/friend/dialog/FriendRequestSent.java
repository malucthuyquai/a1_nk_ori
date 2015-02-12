package com.fuhu.nabiconnect.friend.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class FriendRequestSent extends PopupDialog {

	final private static String TAG = FriendRequestSent.class.getSimpleName();

	public static final int OK_BUTTON_ID = 101;
	public static final int X_BUTTON_ID = 102;

	private Button m_RightButton;
	private Button m_XButton;
	private ArrayList<IButtonClickListener> m_ButtonListeners;

	private TextView m_friend_dialog_title;

	public FriendRequestSent(Context context) {
		super(context);
		setContentView(R.layout.dialog_friend_request_sent);

		m_RightButton = (Button) this.findViewById(R.id.friend_dialog_submit_icon);
		m_XButton = (Button) this.findViewById(R.id.friend_dialog_x_icon);
		m_friend_dialog_title = (TextView) this.findViewById(R.id.friend_dialog_title);

		// set font
		Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
		m_friend_dialog_title.setTypeface(tf, Typeface.NORMAL);
		m_RightButton.setTypeface(tf, Typeface.NORMAL);

		if (m_RightButton != null) {
			m_RightButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					notifyButtonListeners(OK_BUTTON_ID, TAG, null);
				}
			});
		}

		if (m_XButton != null) {
			m_XButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					notifyButtonListeners(X_BUTTON_ID, TAG, null);
				}
			});
		}
	}

	/*
	 * ======================================================================
	 * Add listeners for button
	 * =======================================================================
	 */
	public void notifyButtonListeners(int buttonID, String tag, Object[] args) {
		if (m_ButtonListeners != null) {
			for (IButtonClickListener listener : m_ButtonListeners) {
				listener.onButtonClicked(buttonID, tag, args);
			}
		}
	}

	public void addButtonListener(IButtonClickListener listener) {
		if (m_ButtonListeners == null) {
			m_ButtonListeners = new ArrayList<IButtonClickListener>();
		}
		m_ButtonListeners.add(listener);
	}
}
