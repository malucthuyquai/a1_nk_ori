package com.fuhu.nabiconnect.mail;

import com.fuhu.nabiconnect.ParentActivityLauncher;
import com.fuhu.nabiconnect.event.ApiBaseActivity;

public class MailParentActivity extends ParentActivityLauncher{

    public MailParentActivity() {
        super(MailParentActivity.class.getSimpleName());
    }

    @Override
	public String getTargetActivityIntent() {
		return ApiBaseActivity.INTENT_MAIL_ACTIVITY;
	}

}
