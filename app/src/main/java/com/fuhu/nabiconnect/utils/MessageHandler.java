package com.fuhu.nabiconnect.utils;

import android.os.Handler;
import android.os.Message;

import com.fuhu.nabiconnect.log.LOG;

public class MessageHandler {

	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "MessageHandler";
	
	public static void sendMessage(Handler handler, int what)
	{
		sendMessage(handler, what, 0, 0, null, 0);
	}
	public static void sendMessage(Handler handler, int what, int delay)
	{
		sendMessage(handler, what, 0, 0, null, delay);
	}
	
	public static void sendMessage(Handler handler, int what, int arg1, int arg2, Object obj)
	{
		sendMessage(handler, what, arg1, arg2, obj, 0);
	}
	
	public static void sendMessage(Handler handler, int what, int arg1, int arg2, Object obj, int delay)
	{
		if(handler == null)
		{
			LOG.W(TAG, "sendMessage - handler is null");
			return;
		}
		Message msg = handler.obtainMessage(what, arg1, arg2, obj);
		handler.sendMessageDelayed(msg, delay);		
	}
}
