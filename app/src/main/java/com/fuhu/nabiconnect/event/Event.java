package com.fuhu.nabiconnect.event;

import android.content.Context;

import java.util.ArrayList;

public class Event {

	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private ArrayList<IEventListener> m_EventListeners;
	private boolean m_IsHandled;
	
	/*======================================================================
	 * Constructor
	 *=======================================================================*/
	public Event(Context context)
	{
		this.m_Context = context;
		m_IsHandled = false;
	}
	
	/*======================================================================
	 * Raise event
	 *=======================================================================*/
	public void raise()
	{
		this.raise(null);
	}
	
	public void raise(Object[] objs)
	{
		m_IsHandled = false;
		if(m_EventListeners != null)
			for(IEventListener listener : m_EventListeners)
				listener.onEvent(this, objs);
	}
	
	/*======================================================================
	 * Add event listeners
	 *=======================================================================*/
	public void addEventListener(IEventListener listener)
	{
		if(m_EventListeners == null)
			m_EventListeners = new ArrayList<IEventListener>();
			
		if(!m_EventListeners.contains(listener))
			m_EventListeners.add(listener);
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
