package com.fuhu.nabiconnect.event;

import android.content.Context;

import java.util.ArrayList;

public class ApiEvent {

	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private ArrayList<IApiEventListener> m_EventListeners;
	private boolean m_IsHandled;
	
	/*======================================================================
	 * Constructor
	 *=======================================================================*/
	public ApiEvent(Context context)
	{
		this.m_Context = context;
		m_IsHandled = false;
	}
	
	/*======================================================================
	 * Raise event
	 *=======================================================================*/
	public void raise(boolean isSuccess)
	{
		this.raise(isSuccess, null);
	}
	
	public void raise(boolean isSuccess, Object obj)
	{
		
		m_IsHandled = false;
		if(m_EventListeners != null)
			for(IApiEventListener listener : m_EventListeners)
				listener.onEvent(this, isSuccess, obj);
				
	}
	
	/*======================================================================
	 * Add event listeners
	 *=======================================================================*/
	public void addEventListener(IApiEventListener listener)
	{
		if(m_EventListeners == null)
			m_EventListeners = new ArrayList<IApiEventListener>();
			
		if(!m_EventListeners.contains(listener))
			m_EventListeners.add(listener);
	}
	
	/*======================================================================
	 * Remove event listeners
	 *=======================================================================*/
	public void removeEventListener(IApiEventListener listener)
	{
		if(m_EventListeners != null)
		{		
			m_EventListeners.remove(listener);
		}
	}
	
	
	public void setHandled()
	{
		m_IsHandled = true;
	}
	
	public boolean getHandled()
	{
		return this.m_IsHandled;
	}
}
