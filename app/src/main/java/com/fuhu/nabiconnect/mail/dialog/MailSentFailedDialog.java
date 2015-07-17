package com.fuhu.nabiconnect.mail.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;

import java.util.ArrayList;

public class MailSentFailedDialog extends PopupDialog {

	final public static String TAG = MailSentFailedDialog.class.getSimpleName();

	public static final int CLOSE_BUTTON_ID = 100;
	public static final int OK_BUTTON_ID = 101;
	public static final int X_BUTTON_ID = 102;

	private Context m_Context;
	private Button m_AcceptButton;
	private Button m_DeclineButton;
	private Button m_XButton;
	private ArrayList<IButtonClickListener> m_ButtonListeners;

	private TextView m_DialogTitle;

	public MailSentFailedDialog(Context context) {
		super(context);
		this.m_Context = context;

		setContentView(R.layout.dialog_mail_sent_fail);

		m_AcceptButton = (Button) this.findViewById(R.id.friend_dialog_submit_icon);
		m_DeclineButton = (Button) this.findViewById(R.id.friend_dialog_cancel_icon);
		m_XButton = (Button) this.findViewById(R.id.friend_dialog_x_icon);
		m_DialogTitle = (TextView) this.findViewById(R.id.friend_dialog_title);

		// set font
		Typeface tf = Typeface.createFromAsset(m_Context.getAssets(), "fonts/Roboto-Medium.ttf");
		m_DialogTitle.setTypeface(tf, Typeface.NORMAL);
		m_AcceptButton.setTypeface(tf, Typeface.NORMAL);
		m_DeclineButton.setTypeface(tf, Typeface.NORMAL);

		if (m_AcceptButton != null) {
			m_AcceptButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					notifyButtonListeners(OK_BUTTON_ID, TAG, null);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_mail_sent_failed_try_again");
				}
			});
		}

		if (m_DeclineButton != null) {
			m_DeclineButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					notifyButtonListeners(CLOSE_BUTTON_ID, TAG, null);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_mail_sent_failed_close");
				}
			});
		}

		if (m_XButton != null) {
			m_XButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					notifyButtonListeners(X_BUTTON_ID, TAG, null);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_mail_sent_failed_close_x");
				}
			});
		}
	}

	/*
	 * ======================================================================
	 * Add listners for button
	 * =======================================================================
	 */
	public void notifyButtonListeners(int buttonID, String tag, Object[] args) {
		if (m_ButtonListeners != null) {
			for (IButtonClickListener listener : m_ButtonListeners) {
				listener.onButtonClicked(buttonID, tag, args);
			}
		}
	}

	/*
	 * ======================================================================
	 * Notify button listeners
	 * =======================================================================
	 */
	public void addButtonListener(IButtonClickListener listener) {
		if (m_ButtonListeners == null) {
			m_ButtonListeners = new ArrayList<IButtonClickListener>();
		}
		m_ButtonListeners.add(listener);
	}
}
