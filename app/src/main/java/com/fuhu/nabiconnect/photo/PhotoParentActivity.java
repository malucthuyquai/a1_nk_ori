package com.fuhu.nabiconnect.photo;

import com.fuhu.nabiconnect.ParentActivityLauncher;
import com.fuhu.nabiconnect.event.ApiBaseActivity;

public class PhotoParentActivity extends ParentActivityLauncher{

	@Override
	public String getTargetActivityIntent() {
		return ApiBaseActivity.INTENT_PHOTO_ACTIVITY;
	}

}
