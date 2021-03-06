package com.fuhu.nabiconnect;

import android.content.Intent;
import android.os.Bundle;

import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.log.LOG;

public abstract class ParentActivityLauncher extends Tracking.TrackingInfoActivity {

	public static final String TAG = "ParentActivityLauncher";

    public ParentActivityLauncher(String name) {
        super(name);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LOG.V(TAG,"onCreate() - start , targetActivity is "+getTargetActivityIntent());
		
		Intent i = new Intent(getTargetActivityIntent());
		i.putExtra(ApiBaseActivity.KEY_IS_MOMMY_MODE, true);
		startActivity(i);
		
		finish();
		
		LOG.V(TAG,"onCreate() - end ");
	}
	
	abstract public String getTargetActivityIntent();
}
