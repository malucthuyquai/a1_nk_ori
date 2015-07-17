package com.fuhu.nabiconnect.nsa.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.fuhu.data.FriendData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;

public class BlockedFriendConfirmDialog extends Dialog {

	final private String TAG = BlockedFriendConfirmDialog.class.getSimpleName();

	/**
	 * 
	 * @param context
	 * @param unblockCallback
	 *            callback to be invoked when confirm button is pressed
	 */
	public BlockedFriendConfirmDialog(Context context, View.OnClickListener unblockCallback, FriendData data) {
		super(context, R.style.Theme_GeneralCustomDialog);
		setContentView(R.layout.dialog_blocked_friend_confirm);
		setCanceledOnTouchOutside(false);
		findViewById(R.id.ll_root).setOnClickListener(cancel_ocl);
		findViewById(R.id.btn_confirm).setTag(data);
		findViewById(R.id.btn_confirm).setOnClickListener(unblockCallback);
		findViewById(R.id.btn_cancel).setOnClickListener(cancel_ocl);
	}

	private View.OnClickListener cancel_ocl = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			BlockedFriendConfirmDialog.this.dismiss();

            //tracking
            Tracking.pushTrack(v.getContext(), "dialog_blocked_friends_confirm_close");
		}
	};
}