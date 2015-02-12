package com.fuhu.nabiconnect.friend.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.fuhu.nabiconnect.R;

public class BlockedDialog extends Dialog {

	public BlockedDialog(Context context) {
		super(context, R.style.Theme_GeneralCustomDialog);
		setContentView(R.layout.dialog_unable_to_add_friend);
		findViewById(R.id.btn_close).setOnClickListener(ocl);
		findViewById(R.id.btn_cross).setOnClickListener(ocl);
	}

	private View.OnClickListener ocl = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
}
