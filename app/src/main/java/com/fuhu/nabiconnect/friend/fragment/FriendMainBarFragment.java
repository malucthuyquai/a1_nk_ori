package com.fuhu.nabiconnect.friend.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;

public class FriendMainBarFragment extends Tracking.TrackingInfoFragment {

    public FriendMainBarFragment() {
        super(FriendMainBarFragment.class.getSimpleName());
    }

    @Override
    public String getTrack() {
        return null;
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.friend_mainbar_view, container, false);
	}
}