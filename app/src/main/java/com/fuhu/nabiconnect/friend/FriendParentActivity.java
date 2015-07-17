package com.fuhu.nabiconnect.friend;

import com.fuhu.nabiconnect.ParentActivityLauncher;
import com.fuhu.nabiconnect.event.ApiBaseActivity;

public class FriendParentActivity extends ParentActivityLauncher{

    public FriendParentActivity() {
        super(FriendParentActivity.class.getSimpleName());
    }

    @Override
	public String getTargetActivityIntent() {
		return ApiBaseActivity.INTENT_FRIEND_ACTIVITY;
	}

}
