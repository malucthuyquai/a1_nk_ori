package com.fuhu.nabiconnect.friend;

import com.fuhu.nabiconnect.ParentActivityLauncher;
import com.fuhu.nabiconnect.event.ApiBaseActivity;

public class FriendParentActivity extends ParentActivityLauncher{

	@Override
	public String getTargetActivityIntent() {
		return ApiBaseActivity.INTENT_FRIEND_ACTIVITY;
	}

}
