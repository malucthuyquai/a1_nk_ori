package com.fuhu.nabiconnect.mail;

import com.fuhu.nabiconnect.ParentActivityLauncher;
import com.fuhu.nabiconnect.event.ApiBaseActivity;

public class MailParentActivity extends ParentActivityLauncher{

	@Override
	public String getTargetActivityIntent() {
		return ApiBaseActivity.INTENT_MAIL_ACTIVITY;
	}

}
