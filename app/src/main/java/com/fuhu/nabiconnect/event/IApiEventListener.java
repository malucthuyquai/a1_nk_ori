package com.fuhu.nabiconnect.event;

public interface IApiEventListener {
	public void onEvent(ApiEvent event, boolean isSuccess, Object obj);
}
