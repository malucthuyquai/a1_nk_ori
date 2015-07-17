package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.Tracking;

import java.util.ArrayList;

public abstract class Effect implements Tracking.OnTrackListener {

	protected ArrayList<Effect> m_SubItems = null;
	
	abstract public void apply();
	public void cancel()
	{
		// default do nothing
	};
	abstract public int getEffectIconRes();
	abstract public ArrayList<Effect> getSubItems();
}
