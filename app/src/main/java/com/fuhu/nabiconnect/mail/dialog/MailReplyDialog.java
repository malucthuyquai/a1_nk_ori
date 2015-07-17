package com.fuhu.nabiconnect.mail.dialog;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;
import com.fuhu.nabiconnect.mail.MailActivity.ReplyReceiverData;
import com.fuhu.nabiconnect.utils.LoadAvatarTask;
import com.fuhu.nabiconnect.utils.Utils;

public class MailReplyDialog extends PopupDialog {
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "MailReplyDialog";
	public static final int YES_BUTTON_ID = 100;
	public static final int CANCEL_BUTTON_ID = 101;
	
	
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private ImageView m_AvatarImage;
	private Button m_YesButton;
	private Button m_CancelButton;
	private TextView m_ReplyToText;
	private ReplyReceiverData m_ReceiverInfo;
	
	public MailReplyDialog(Context context, ReplyReceiverData info)
	{
		super(context);
		this.m_Context = context;
		this.m_ReceiverInfo = info;
		setContentView(R.layout.mail_reply_dialog);

		m_AvatarImage = (ImageView) this.findViewById(R.id.mail_reply_dialog_avatar);
		m_YesButton = (Button) this.findViewById(R.id.mail_reply_dialog_ok_button);
		m_CancelButton = (Button) this.findViewById(R.id.mail_reply_dialog_cancel_button);
		m_ReplyToText = (TextView) this.findViewById(R.id.mail_reply_dialog_reply_to_text);

		m_YesButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				notifyButtonListeners(YES_BUTTON_ID, TAG, new Object[]{m_ReceiverInfo});

                //tracking
                Tracking.pushTrack(v.getContext(), "dialog_reply_mail_send_message");
			}
		});
		m_CancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				notifyButtonListeners(CANCEL_BUTTON_ID, TAG, null);

                //tracking
                Tracking.pushTrack(v.getContext(), "dialog_reply_mail_close");
			}
		});
		
		m_ReplyToText.setText(String.format(m_Context.getString(R.string.mail_reply_to), m_ReceiverInfo.getInboxData().userName));
		
		Bitmap avatar = m_ReceiverInfo.getAvatarBitmap();
	    
	    if(avatar == null)
		{
			LoadAvatarTask task = new LoadAvatarTask();
			Utils.executeAsyncTask(task, m_AvatarImage, m_ReceiverInfo.getInboxData().avatarURL);
		}
		else
			m_AvatarImage.setImageBitmap(avatar);
	}

}


