package com.fuhu.nabiconnect.photo.widget;

import android.content.Context;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.utils.TwoButtonsDialog;

public class PhotoSentFailedDialog extends TwoButtonsDialog {

	public PhotoSentFailedDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setDialogInfo(
				context.getString(R.string.photo_not_send_dialog_title),
				context.getString(R.string.photo_not_send_dialog_description),
				R.drawable.friend_friend_request_sent_icon,
				context.getString(R.string.photo_not_send_dialog_try_again),
				context.getString(R.string.photo_not_send_dialog_cancel));
		
		setIconMarginTop(context.getResources().getDimensionPixelSize(R.dimen.mail_sent_failed_dialog_icon_margin_top));
	
	}

}
